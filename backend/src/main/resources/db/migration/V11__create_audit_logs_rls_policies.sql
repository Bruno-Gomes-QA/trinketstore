-- Drop policies if they exist
DROP POLICY IF EXISTS "audit_logs: admin read" ON public.audit_logs;
DROP POLICY IF EXISTS "audit_logs: no client insert" ON public.audit_logs;

-- Create policies
CREATE POLICY "audit_logs: admin read"
ON public.audit_logs FOR SELECT
TO authenticated
USING (public.is_admin(auth.uid()));

CREATE POLICY "audit_logs: no client insert"
ON public.audit_logs FOR INSERT
TO authenticated
WITH CHECK (false);
