-- Script de verificação das políticas RLS
-- Execute este script para verificar se as políticas de admin estão corretas

-- ============================================
-- VERIFICAR POLÍTICAS POR TABELA
-- ============================================

-- Verificar políticas da tabela users
SELECT
    schemaname,
    tablename,
    policyname,
    cmd AS command,
    roles,
    CASE
        WHEN policyname LIKE '%admin%' THEN '✓ ADMIN POLICY'
        ELSE '  Regular Policy'
    END AS policy_type
FROM pg_policies
WHERE tablename = 'users'
ORDER BY policyname;

-- Verificar políticas da tabela audit_logs
SELECT
    schemaname,
    tablename,
    policyname,
    cmd AS command,
    roles,
    CASE
        WHEN policyname LIKE '%admin%' THEN '✓ ADMIN POLICY'
        ELSE '  Regular Policy'
    END AS policy_type
FROM pg_policies
WHERE tablename = 'audit_logs'
ORDER BY policyname;

-- Verificar políticas da tabela webhook_events
SELECT
    schemaname,
    tablename,
    policyname,
    cmd AS command,
    roles,
    CASE
        WHEN policyname LIKE '%admin%' THEN '✓ ADMIN POLICY'
        ELSE '  Regular Policy'
    END AS policy_type
FROM pg_policies
WHERE tablename = 'webhook_events'
ORDER BY policyname;

-- ============================================
-- VERIFICAR TODAS AS POLÍTICAS DE ADMIN
-- ============================================

SELECT
    tablename,
    policyname,
    cmd AS command
FROM pg_policies
WHERE policyname LIKE '%admin%'
ORDER BY tablename, policyname;

-- ============================================
-- RESUMO DE POLÍTICAS POR TABELA
-- ============================================

SELECT
    tablename,
    COUNT(*) AS total_policies,
    COUNT(CASE WHEN policyname LIKE '%admin%' THEN 1 END) AS admin_policies,
    COUNT(CASE WHEN cmd = 'ALL' AND policyname LIKE '%admin%' THEN 1 END) AS admin_all_policies
FROM pg_policies
WHERE schemaname = 'public'
GROUP BY tablename
ORDER BY tablename;

-- ============================================
-- TESTAR FUNÇÃO is_admin
-- ============================================

-- Substitua 'seu-uuid-aqui' pelo auth_id de um usuário admin
-- SELECT public.is_admin('seu-uuid-aqui'::uuid);

-- Listar todos os admins
SELECT
    id_user,
    nome_user,
    email,
    role,
    auth_id
FROM public.users
WHERE role = 'admin';

-- ============================================
-- VERIFICAR RLS HABILITADO
-- ============================================

SELECT
    schemaname,
    tablename,
    rowsecurity AS rls_enabled
FROM pg_tables
WHERE schemaname = 'public'
    AND tablename IN (
        'users', 'products', 'prices', 'inventory',
        'orders', 'order_items', 'audit_logs', 'webhook_events'
    )
ORDER BY tablename;

