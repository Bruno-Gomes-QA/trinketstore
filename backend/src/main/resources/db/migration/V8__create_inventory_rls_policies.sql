-- Drop policies if they exist
DROP POLICY IF EXISTS "inventory: public read" ON public.inventory;
DROP POLICY IF EXISTS "inventory: admin write" ON public.inventory;

-- Create policies
CREATE POLICY "inventory: public read"
ON public.inventory FOR SELECT
TO anon, authenticated
USING (true);

CREATE POLICY "inventory: admin write"
ON public.inventory FOR ALL
TO authenticated
USING (public.is_admin(auth.uid()))
WITH CHECK (public.is_admin(auth.uid()));
