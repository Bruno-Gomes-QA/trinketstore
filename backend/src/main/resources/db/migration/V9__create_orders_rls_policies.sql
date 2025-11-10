CREATE POLICY "orders: select own"
ON public.orders FOR SELECT
TO authenticated
USING (EXISTS (
  SELECT 1 FROM public.users u
  WHERE u.id_user = orders.user_id
    AND u.auth_id = auth.uid()
));

CREATE POLICY "orders: insert own"
ON public.orders FOR INSERT
TO authenticated
WITH CHECK (EXISTS (
  SELECT 1 FROM public.users u
  WHERE u.id_user = public.orders.user_id
    AND u.auth_id = auth.uid()
));

CREATE POLICY "orders: update own"
ON public.orders FOR UPDATE
TO authenticated
USING (EXISTS (
  SELECT 1 FROM public.users u
  WHERE u.id_user = public.orders.user_id
    AND u.auth_id = auth.uid()
))
WITH CHECK (EXISTS (
  SELECT 1 FROM public.users u
  WHERE u.id_user = public.orders.user_id
    AND u.auth_id = auth.uid()
));

CREATE POLICY "orders: delete own"
ON public.orders FOR DELETE
TO authenticated
USING (EXISTS (
  SELECT 1 FROM public.users u
  WHERE u.id_user = public.orders.user_id
    AND u.auth_id = auth.uid()
));

CREATE POLICY "orders: admin all"
ON public.orders FOR ALL
TO authenticated
USING (public.is_admin(auth.uid()))
WITH CHECK (public.is_admin(auth.uid()));

