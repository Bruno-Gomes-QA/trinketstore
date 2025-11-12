# 🔒 Estrutura de Rotas - Sistema Trinket Store

## Estrutura de Pastas

```
app/pages/
├── index.vue                    # Redireciona para /sistema/login ou /sistema/dashboard
└── sistema/                     # Todas as rotas do sistema administrativo
    ├── login.vue               # /sistema/login - Página de login (sem proteção)
    └── dashboard.vue           # /sistema/dashboard - Dashboard (protegido)
```

## Rotas Disponíveis

### 🔓 Rotas Públicas
- `/` - Redireciona automaticamente
- `/sistema/login` - Página de login

### 🔐 Rotas Protegidas (Requer autenticação + role admin)
- `/sistema/dashboard` - Dashboard principal
- `/sistema/produtos` - Gestão de produtos (a ser criado)
- `/sistema/pedidos` - Gestão de pedidos (a ser criado)
- `/sistema/qrcodes` - Gestão de QR Codes (a ser criado)
- `/sistema/pagamentos` - Gestão de pagamentos (a ser criado)
- `/sistema/usuarios` - Gestão de usuários (a ser criado)
- `/sistema/relatorios` - Relatórios e métricas (a ser criado)

## Proteção de Rotas

### Middleware de Autenticação (`auth.ts`)
Todas as rotas dentro de `/sistema/` (exceto `/sistema/login`) são protegidas pelo middleware `auth`:

```typescript
// Verifica se o usuário está autenticado
if (!token.value || !user.value) {
  return navigateTo('/sistema/login')
}

// Verifica se o usuário é admin
if (user.value.role !== 'admin') {
  return navigateTo('/sistema/login')
}
```

### Como Adicionar uma Nova Página Protegida

1. Crie o arquivo em `app/pages/sistema/[nome-da-pagina].vue`
2. Adicione o middleware na página:

```vue
<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
  pageTitle: 'Nome da Página',
})
</script>
```

3. Adicione o item no menu lateral em `app/components/AppSidebar.vue`:

```typescript
{
  title: 'Nome da Página',
  url: '/sistema/nome-da-pagina',
  icon: IconName, // Importe de lucide-vue-next
}
```

## Fluxo de Autenticação

```
1. Usuário acessa qualquer rota
   ↓
2. Se não autenticado → /sistema/login
   ↓
3. Faz login
   ↓
4. Sistema verifica role
   ↓
5a. Se role = 'admin' → /sistema/dashboard
5b. Se role = 'customer' → Modal + redirect para loja
   ↓
6. Middleware protege todas as rotas /sistema/*
```

## Redirecionamentos

| De | Para | Condição |
|---|---|---|
| `/` | `/sistema/dashboard` | Usuário autenticado como admin |
| `/` | `/sistema/login` | Usuário não autenticado |
| Qualquer rota protegida | `/sistema/login` | Sem autenticação ou não admin |
| Após login | `/sistema/dashboard` | Login bem-sucedido |
| Após logout | `/sistema/login` | Sempre |

## Sessão e Cookies

- `auth_token` - Token JWT de autenticação (7 dias)
- `user_data` - Dados do usuário codificados (7 dias)

Ambos são limpos no logout.
