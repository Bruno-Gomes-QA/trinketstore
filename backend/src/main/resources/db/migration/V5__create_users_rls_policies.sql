CREATE POLICY "users: select self"
ON public.users FOR SELECT
TO authenticated
USING (auth.uid() = auth_id);

CREATE POLICY "users: update self"
ON public.users FOR UPDATE
TO authenticated
USING (auth.uid() = auth_id)
WITH CHECK (auth.uid() = auth_id);

CREATE POLICY "users: admin all"
ON public.users FOR ALL
TO authenticated
USING (public.is_admin(auth.uid()))
WITH CHECK (public.is_admin(auth.uid()));

CREATE OR REPLACE FUNCTION public.prevent_role_escalation()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  IF TG_OP = 'UPDATE' AND new.role <> old.role AND NOT public.is_admin(auth.uid()) THEN
    RAISE EXCEPTION 'only admins can change role';
  END IF;
  RETURN new;
END $$;

DROP TRIGGER IF EXISTS trg_prevent_role_escalation ON public.users;
CREATE TRIGGER trg_prevent_role_escalation
BEFORE UPDATE ON public.users
FOR EACH ROW EXECUTE FUNCTION public.prevent_role_escalation();

