CREATE OR REPLACE FUNCTION public.is_admin(uid uuid)
RETURNS BOOLEAN LANGUAGE SQL STABLE AS
$$
  SELECT EXISTS (
    SELECT 1 FROM public.users u
    WHERE u.auth_id = uid AND u.role = 'admin'
  );
$$;

GRANT EXECUTE ON FUNCTION public.is_admin(uuid) TO anon, authenticated;

