-- Disable RLS and drop all related policies so access control is managed by the API
DO $$
DECLARE
    table_name text;
BEGIN
    FOR table_name IN SELECT unnest(ARRAY[
        'users','products','prices','inventory','orders','order_items','audit_logs','webhook_events'
    ]) LOOP
        EXECUTE format('ALTER TABLE public.%I DISABLE ROW LEVEL SECURITY', table_name);
        EXECUTE format('ALTER TABLE public.%I NO FORCE ROW LEVEL SECURITY', table_name);
    END LOOP;
END $$;

-- Users policies
DROP POLICY IF EXISTS "users: select self" ON public.users;
DROP POLICY IF EXISTS "users: update self" ON public.users;
DROP POLICY IF EXISTS "users: admin all" ON public.users;

-- Products policies
DROP POLICY IF EXISTS "products: public read" ON public.products;
DROP POLICY IF EXISTS "products: admin write" ON public.products;

-- Prices policies
DROP POLICY IF EXISTS "prices: public read" ON public.prices;
DROP POLICY IF EXISTS "prices: admin write" ON public.prices;

-- Inventory policies
DROP POLICY IF EXISTS "inventory: public read" ON public.inventory;
DROP POLICY IF EXISTS "inventory: admin write" ON public.inventory;

-- Orders policies
DROP POLICY IF EXISTS "orders: select own" ON public.orders;
DROP POLICY IF EXISTS "orders: insert own" ON public.orders;
DROP POLICY IF EXISTS "orders: update own" ON public.orders;
DROP POLICY IF EXISTS "orders: delete own" ON public.orders;
DROP POLICY IF EXISTS "orders: admin all" ON public.orders;

-- Order items policies
DROP POLICY IF EXISTS "order_items: select own" ON public.order_items;
DROP POLICY IF EXISTS "order_items: write own" ON public.order_items;
DROP POLICY IF EXISTS "order_items: update own" ON public.order_items;
DROP POLICY IF EXISTS "order_items: delete own" ON public.order_items;
DROP POLICY IF EXISTS "order_items: admin all" ON public.order_items;

-- Audit logs policies
DROP POLICY IF EXISTS "audit_logs: admin read" ON public.audit_logs;
DROP POLICY IF EXISTS "audit_logs: no client insert" ON public.audit_logs;
DROP POLICY IF EXISTS "audit_logs: admin all" ON public.audit_logs;
DROP POLICY IF EXISTS "audit_logs: no regular user access" ON public.audit_logs;

-- Webhook events policies
DROP POLICY IF EXISTS "webhook_events: admin read" ON public.webhook_events;
DROP POLICY IF EXISTS "webhook_events: no client write" ON public.webhook_events;
DROP POLICY IF EXISTS "webhook_events: admin all" ON public.webhook_events;
DROP POLICY IF EXISTS "webhook_events: no regular user access" ON public.webhook_events;
