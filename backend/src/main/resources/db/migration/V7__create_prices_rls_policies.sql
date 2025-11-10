CREATE POLICY "prices: public read"
ON public.prices FOR SELECT
TO anon, authenticated
USING (true);

CREATE POLICY "prices: admin write"
ON public.prices FOR ALL
TO authenticated
USING (public.is_admin(auth.uid()))
WITH CHECK (public.is_admin(auth.uid()));

