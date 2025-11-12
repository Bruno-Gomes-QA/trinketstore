-- Drop policies if they exist
DROP POLICY IF EXISTS "products: public read" ON public.products;
DROP POLICY IF EXISTS "products: admin write" ON public.products;

-- Create policies
CREATE POLICY "products: public read"
ON public.products FOR SELECT
TO anon, authenticated
USING (true);

CREATE POLICY "products: admin write"
ON public.products FOR ALL
TO authenticated
USING (public.is_admin(auth.uid()))
WITH CHECK (public.is_admin(auth.uid()));
