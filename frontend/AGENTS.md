# AGENTS – Nuxt Ecommerce Frontend

Estas diretrizes orientam qualquer agente LLM que contribua com o painel administrativo do ecommerce Trinket Store. O frontend vive em `frontend/app/` e usa Nuxt 3 + shadcn-vue. Tudo aqui foi ajustado para o contexto de produtos, pedidos, estoque, pagamentos (Stripe) e retirada com QR Code.

---

## 1. Visão Geral
- **Stack**: Nuxt 3 (Vue 3 + `<script setup lang="ts">`), TypeScript estrito, Tailwind CSS 4, shadcn-vue, lucide icons.
- **Objetivo**: construir telas administrativas que controlam catálogo, estoque, preços, pedidos e usuários, além de acompanhar QR Codes e integrações com o backend Spring Boot.
- **Aliases**: `~/` aponta para `app/`. Todos os imports internos devem usar `~/...` para manter paths estáveis.
- **Estado/Fetch**: use `useState`, `useFetch`, `useBackendFetch` e `useBackendFetchDirect`. Jamais invoque `$fetch` cru em páginas; reencapsule em composables para garantir headers, token e tratamento de erro padronizado.
- **Autenticação**: `useAuth()` mantém token/usuário via cookies + `useState`. Só admins acessam `pages/sistema/*`, portanto derive `isAdmin`/`isAuthenticated` dali e redirecione usando middleware `auth`.
- **UI**: priorize componentes prontos em `app/components/ui/*` (forms, tables, dialogs) e os blocos institucionais em `app/components/store/*` quando trabalhar páginas públicas.

---

## 2. Estrutura de Pastas
```
app/
├─ app.vue, layouts/, middleware/
├─ pages/
│  ├─ index.vue              # Landing pública
│  └─ sistema/               # Painel admin (login, dashboard, produtos, pedidos, etc.)
├─ components/
│  ├─ ui/                    # shadcn-vue adaptado
│  ├─ store/                 # Seções reutilizáveis da vitrine
│  └─ crm-like domains       # Ex.: sistema/pedidos/*
├─ composables/
│  ├─ api/                   # Hooks por entidade (products, orders, inventory…)
│  ├─ core/                  # Autenticação e infraestrutura de fetch
│  └─ helpers/               # formatters, normalizers, currency helpers
├─ types/                    # Tipos por domínio (`types/orders`, `types/products`, etc.)
├─ lib/                      # Helpers globais (ex.: `cn`)
├─ assets/, public/
└─ plugins/                  # Registradores globais
```
- Novos componentes/composables precisam ser exportados por um barrel (`index.ts`) para manter importações centralizadas.
- Componentes complexos devem ter README explicando props/events (ex.: `components/sistema/pedidos/README.md`).

---

## 3. Convenções de Código
1. **Nomeação**: camelCase para variáveis/funções/composables (`useOrdersList`), PascalCase para componentes. Siglas de status seguem backend (`pending`, `paid`, `picked_up`, `canceled`).
2. **Script Setup**: sempre `<script setup lang="ts">`, com `defineProps/defineEmits` tipados. Prefira `const props = defineProps<...>()`.
3. **Estado compartilhado**: dados persistentes entre páginas usam `useState('key')`. Tokens/cookies passam por `useAuth()`.
4. **Fetch seguro**: `useBackendFetch*` centraliza baseURL + headers (token, tenant). Antes de enviar dados, normalize currency (centavos), números e datas.
5. **Composables de API**:
   - Um arquivo por operação relevante (`useProductsGet`, `useOrdersCreate` etc.).
   - Exponha `loading`, `error`, `data` via `readonly(useState(...))`.
   - Os composables devem encapsular sanitização (ex.: remover máscara de CPF) e mapear respostas para os tipos de `~/types`.
6. **Componentização**: telas longas devem ser quebradas (ex.: `components/sistema/pedidos/table`, `components/sistema/pedidos/details`). Evite lógica de negócios dentro de templates: mova para composables ou helpers.
7. **Tratamento de erro + UX**: logue via `console.error` com contexto (`console.error('[orders] fetch failed', err)`) e sempre mostre feedback visível (toast, banner, badge). Loading states devem usar `components/ui/skeleton` ou `Button` com `:loading`.
8. **Formulários sensíveis**:
   - Campos monetários usam helpers de currency (`inputmode="decimal"`, `maxlength`, `step="0.01"`). No envio, converta para centavos ou `Number`.
   - Documentos (CPF/CNPJ) aceitam somente dígitos (`inputmode="numeric"`) e, se necessário, máscaras visuais via helper.
   - Para datas de retirada/expiração, use `components/ui/calendar` + popover e limite range válido.
9. **Permissões/Admin Guard**:
   - Hoje apenas `role === 'admin'` acessa o painel; mantenha essa checagem antes de renderizar ações críticas (ex.: botões de deletar produto).
   - Qualquer rota sob `/sistema` precisa de `definePageMeta({ middleware: 'auth' })`.
10. **Docs/README**: sempre atualize README do domínio quando criar componentes novos ou fluxos não triviais.

---

## 4. Fluxo Padronizado para Novas Funcionalidades Integradas
Siga esta sequência para qualquer endpoint/feature nova do ecommerce.

### Passo 1 – Tipagens
1. Investigue o contrato no backend (DTOs em `Backend/src/main/java/.../dto` ou Swagger).
2. Crie/atualize `app/types/<dominio>/index.d.ts` com interfaces para request/response e enums (ex.: `OrderStatus = 'pending' | ...`).
3. Padronize helpers de transformação no mesmo namespace (ex.: `OrderSummary`, `OrderListFilters`).

### Passo 2 – Composables de API
1. Crie `app/composables/api/<dominio>/useXxx.ts`.
2. Encapsule fetch com `useBackendFetch` (reativo) ou `useBackendFetchDirect` (ação pontual).
3. Armazene `data`, `error`, `pending` em `useState`. Exponha métodos utilitários (`refresh`, `mutate`, `nextPage`, `create`).
4. Sanitize payloads: números, datas ISO, status, slug. Gere logs úteis em caso de erro.

### Passo 3 – Helpers e Regras de Negócio
1. Coloque formatadores/normalizadores em `app/composables/helpers`. Exemplos: `useCurrencyFormatter`, `useOrderStatusBadge`.
2. Helper novo deve exportar funções individuais + um hook agregador (`useFormatters`).

### Passo 4 – Componentes por Domínio
1. Crie pasta em `app/components/sistema/<dominio>/<feature>/`.
2. Separe responsabilidades (cards KPI, tabela, dialog, form). Reutilize componentes de `app/components/ui`.
3. Exporte via `index.ts` e documente no README local.

### Passo 5 – Página em `app/pages`
1. Nomeie rotas seguindo o módulo (`pages/sistema/produtos/index.vue`, `pages/sistema/pedidos/[id].vue`).
2. Dentro do `<script setup>`:
   - Consuma composables criados e `useAuth`.
   - Controle estados de modal/drawer (via `ref`).
   - Monte guards (`if (!auth.isAdmin.value) return navigateTo('/sistema/login')`).
3. No `<template>`:
   - Estruture com layout base (header, filtros, conteúdo).
   - Conecte eventos dos componentes (`@created`, `@updated`, `@refresh`).
   - Respeite tokens e loaders.
4. Configure `definePageMeta` para layout/middleware.

### Passo 6 – Pós-criação
1. Atualize README/domínio e `app/components/.../index.ts`.
2. Verifique se `types`, `composables` e páginas não possuem imports quebrados.
3. Registre TODO/testes pendentes se não conseguir cobrir no momento.

---

## 5. Regras Específicas do Ecommerce
- **Produtos & Estoque**: mantenha `ativo`/`inativo` em sincronia. Cards e listas devem destacar estoque baixo (< limite configurado). Slugs são únicos.
- **Pedidos & QR Code**: status válidos `pending`, `paid`, `picked_up`, `canceled`. Atualizações precisam refletir em badges e permitir disparo de ações (ex.: gerar QR, confirmar retirada).
- **Pagamentos**: Stripe Checkout roda via backend; no frontend apenas exibimos valores formatados (`useCurrencyFormatter`) e indicamos se o pedido está conciliado. Evite exibir dados sensíveis.
- **Retirada e auditoria**: ao confirmar retirada, mostre resumo (quem confirmou, horário). Logs são exibidos em tabelas dedicadas — mantenha padrões de cor (verde para sucesso, amarelo para pendência, vermelho para erro).
- **Responsividade**: todas as telas do painel precisam funcionar bem em largura mínima de 1024px, mas listas/badges devem colapsar em cards abaixo disso. Use utilitários do Tailwind e componentes de tabela responsiva (`components/ui/table`).
- **Internacionalização de moeda**: use sempre BRL (R$) ou currency enviada pelo backend. Nunca concatene strings manualmente; utilize helper.
- **Acessibilidade**: botões/icones devem ter `aria-label` quando não possuírem texto. Inputs com máscaras precisam de descrição e placeholders úteis.

---

## 6. Checklist Rápido
- [ ] Tipos atualizados em `app/types/<dominio>`.
- [ ] Composables retornam estados readonly, sanitizam dados e lidam com erros.
- [ ] UI usa blocos de `app/components/ui` ou `app/components/store` mantendo identidade visual (verde/ciano/amarelo).
- [ ] Rota protegida com middleware/guards corretos.
- [ ] README + barrel exports atualizados.
- [ ] Campos sensíveis (doc, moeda, datas, QR) possuem validação, máscaras e helpers adequados.

Seguindo este AGENTS.md qualquer agente conseguirá evoluir o painel administrativo do ecommerce sem perder consistência visual ou comprometer regras de negócio críticas. Bons commits! 🎯
