-- Drop policies if they exist
DROP POLICY IF EXISTS "order_items: select own" ON public.order_items;
DROP POLICY IF EXISTS "order_items: write own" ON public.order_items;
DROP POLICY IF EXISTS "order_items: update own" ON public.order_items;
DROP POLICY IF EXISTS "order_items: delete own" ON public.order_items;
DROP POLICY IF EXISTS "order_items: admin all" ON public.order_items;

-- Create policies
CREATE POLICY "order_items: select own"
ON public.order_items FOR SELECT
TO authenticated
USING (EXISTS (
  SELECT 1
  FROM public.orders o
  JOIN public.users u ON u.id_user = o.user_id
  WHERE o.id_order = public.order_items.order_id
    AND u.auth_id = auth.uid()
));

CREATE POLICY "order_items: write own"
ON public.order_items FOR INSERT
TO authenticated
WITH CHECK (EXISTS (
  SELECT 1
  FROM public.orders o
  JOIN public.users u ON u.id_user = o.user_id
  WHERE o.id_order = public.order_items.order_id
    AND u.auth_id = auth.uid()
));

CREATE POLICY "order_items: update own"
ON public.order_items FOR UPDATE
TO authenticated
USING (EXISTS (
  SELECT 1 FROM public.orders o
  JOIN public.users u ON u.id_user = o.user_id
  WHERE o.id_order = public.order_items.order_id
    AND u.auth_id = auth.uid()
))
WITH CHECK (EXISTS (
  SELECT 1 FROM public.orders o
  JOIN public.users u ON u.id_user = o.user_id
  WHERE o.id_order = public.order_items.order_id
    AND u.auth_id = auth.uid()
));

CREATE POLICY "order_items: delete own"
ON public.order_items FOR DELETE
TO authenticated
USING (EXISTS (
  SELECT 1 FROM public.orders o
  JOIN public.users u ON u.id_user = o.user_id
  WHERE o.id_order = public.order_items.order_id
    AND u.auth_id = auth.uid()
));

CREATE POLICY "order_items: admin all"
ON public.order_items FOR ALL
TO authenticated
USING (public.is_admin(auth.uid()))
WITH CHECK (public.is_admin(auth.uid()));
