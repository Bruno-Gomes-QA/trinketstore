-- Drop policies if they exist
DROP POLICY IF EXISTS "webhook_events: admin read" ON public.webhook_events;
DROP POLICY IF EXISTS "webhook_events: no client write" ON public.webhook_events;

-- Create policies
CREATE POLICY "webhook_events: admin read"
ON public.webhook_events FOR SELECT
TO authenticated
USING (public.is_admin(auth.uid()));

CREATE POLICY "webhook_events: no client write"
ON public.webhook_events FOR ALL
TO authenticated
USING (false)
WITH CHECK (false);
