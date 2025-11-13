-- Update RLS policies to allow admins to do everything on all tables

-- ============================================
-- AUDIT LOGS: Allow admins to manage everything
-- ============================================
DROP POLICY IF EXISTS "audit_logs: admin read" ON public.audit_logs;
DROP POLICY IF EXISTS "audit_logs: no client insert" ON public.audit_logs;
DROP POLICY IF EXISTS "audit_logs: admin all" ON public.audit_logs;

-- Admin can do everything on audit_logs
CREATE POLICY "audit_logs: admin all"
ON public.audit_logs FOR ALL
TO authenticated
USING (public.is_admin(auth.uid()))
WITH CHECK (public.is_admin(auth.uid()));

-- Regular users cannot access audit_logs
CREATE POLICY "audit_logs: no regular user access"
ON public.audit_logs FOR ALL
TO authenticated
USING (false)
WITH CHECK (false);

-- ============================================
-- WEBHOOK EVENTS: Allow admins to manage everything
-- ============================================
DROP POLICY IF EXISTS "webhook_events: admin read" ON public.webhook_events;
DROP POLICY IF EXISTS "webhook_events: no client write" ON public.webhook_events;
DROP POLICY IF EXISTS "webhook_events: admin all" ON public.webhook_events;

-- Admin can do everything on webhook_events
CREATE POLICY "webhook_events: admin all"
ON public.webhook_events FOR ALL
TO authenticated
USING (public.is_admin(auth.uid()))
WITH CHECK (public.is_admin(auth.uid()));

-- Regular users cannot access webhook_events
CREATE POLICY "webhook_events: no regular user access"
ON public.webhook_events FOR ALL
TO authenticated
USING (false)
WITH CHECK (false);

-- ============================================
-- USERS: Ensure admins can manage all users
-- ============================================
-- Drop existing policies to recreate them properly
DROP POLICY IF EXISTS "users: select self" ON public.users;
DROP POLICY IF EXISTS "users: update self" ON public.users;
DROP POLICY IF EXISTS "users: admin all" ON public.users;

-- Admins can do everything on all users (highest priority)
CREATE POLICY "users: admin all"
ON public.users FOR ALL
TO authenticated
USING (public.is_admin(auth.uid()))
WITH CHECK (public.is_admin(auth.uid()));

-- Regular users can only read themselves
CREATE POLICY "users: select self"
ON public.users FOR SELECT
TO authenticated
USING (auth.uid() = auth_id);

-- Regular users can only update themselves
CREATE POLICY "users: update self"
ON public.users FOR UPDATE
TO authenticated
USING (auth.uid() = auth_id)
WITH CHECK (auth.uid() = auth_id);

