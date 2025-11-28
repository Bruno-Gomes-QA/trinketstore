# Visão Técnica · Trinket Store

## Panorama
Trinket Store é um e-commerce full-stack que combina vitrine Nuxt 3 com backend Spring Boot 3. O frontend orquestra a experiência do cliente, autentica via Supabase/Google e consome APIs REST expostas em `/api`. O backend centraliza regras de negócio, integra Mercado Pago para PIX dinâmico, aplica validações e sincroniza estoque/pedidos em PostgreSQL.

## Stack por camada
### Frontend
- **Nuxt 3 + Vue 3** com pages em `frontend/app/pages`, layouts, middleware de auth e componentes reativos específicos da loja.
- **Tailwind + Shadcn** (`@nuxtjs/tailwindcss`, `shadcn-nuxt`) para sistema de design, ícones Lucide e animações `tw-animate-css`.
- **Supabase Auth** (via `useSupabaseAuth`) usa OAuth Google para identificar cliente e persistir perfil.
- **Composables** (`frontend/app/composables/**`) encapsulam chamadas HTTP (`useBackendFetchDirect`, `useOrdersManager`), formatações e stores reativos.
- **Server routes** em `frontend/server/api` permitem proxies e operações sensíveis (ex.: upload para storage) sem expor chaves no browser.

### Backend
- **Spring Boot 3 / Java 21** com módulos tradicionais (`controller`, `service`, `repository`, `entity`, `dto`, `exception`).
- **Banco**: PostgreSQL com versionamento via Flyway (`backend/src/main/resources/db/migration`).
- **Segurança**: JWT assinado (`security/JwtTokenProvider`, `JwtAuthenticationFilter`) em conjunto com Supabase para federar identidades.
- **Integrações**: Mercado Pago SDK (`CheckoutController`, `WebhookController`) usando OkHttp para chamadas, Supabase Admin API (`SupabaseConfig`), OpenAPI (springdoc) publicado automaticamente.
- **Observabilidade**: Actuator, validação (Bean Validation) e perfis `.env` carregados por `spring-dotenv`.

### Infraestrutura e DevX
- **Gradle** (`backend/build.gradle`) e **pnpm** (`frontend/package.json`) padronizam builds.
- **Docker**: cada lado tem `Dockerfile` e Compose simplifica subir banco + serviços.
- **Scripts**: `./gradlew test` e `pnpm dev`/`pnpm build` descritos no README.

## Arquitetura em camadas
1. **Experiência do cliente**: páginas Nuxt (`/`, `/carrinho`, `/pedidos`) renderizam dados do catálogo e guiam o checkout multi-step.
2. **APIs REST**: controllers Spring expõem recursos como produtos, preços, estoque, pedidos e autenticação.
3. **Domínio**: services consolidam validações (estoque mínimo, status do pedido, cancelamentos) e acionam repositórios JPA.
4. **Persistência**: Postgres mantém catálogos, inventário, pedidos, além de tabelas auxiliares (logs, webhook events).
5. **Integrações externas**: Mercado Pago gera/consulta pagamentos PIX; Supabase autentica usuários e fornece perfis; notificações chegam via webhook público (`/api/webhooks/mercadopago`).
6. **Clientes internos**: o painel administrativo (`/sistema/**`) consome as mesmas APIs com permissões elevadas para catalogar produtos, ajustar preços e revisar métricas.

## Estrutura de pastas
```
trinketstore/
├── backend/
│   ├── src/main/java/com/reducess/trinketstore/
│   │   ├── config/           # SecurityConfig, MercadoPagoConfiguration, OpenAPI, Supabase
│   │   ├── controller/       # Auth, Checkout PIX, Inventory, Orders, Prices, Webhooks
│   │   ├── dto/              # Request/response records compartilhados entre camadas
│   │   ├── entity/           # Mapeamentos JPA (Product, Price, Inventory, Order)
│   │   ├── repository/       # Interfaces Spring Data
│   │   └── service/          # Regras de negócio e integrações externas
│   └── src/main/resources/
│       ├── application.properties
│       └── db/migration/     # Scripts Flyway V1..V20
├── frontend/
│   ├── app/
│   │   ├── layouts/          # Layout padrão e layout "sistema" do painel
│   │   ├── pages/            # Vitrine (`/`), carrinho, pedidos e área interna `/sistema`
│   │   ├── components/       # UI (Shadcn) e blocos de negócio (`components/store/*`)
│   │   ├── composables/      # Hooks de API, helpers e stores reativos
│   │   ├── lib/              # Utilitários de formatação, máscaras, timers
│   │   └── types/            # Tipagens TS alinhadas com o backend
│   ├── public/               # Assets estáticos (ícones, manifestos)
│   ├── server/api/           # Endpoints Nitro usados como BFF
│   └── nuxt.config.ts        # Configuração de módulos, runtime env e aliases
└── README.md                 # Guia rápido + instruções PIX
```

## Fluxos críticos
### Checkout PIX
1. Carrinho (`frontend/app/pages/carrinho.vue`) controla esteiras `review → identify → pay`. Sincroniza estoque em tempo real e exige login Google.
2. Ao confirmar itens, o front chama `POST /api/checkout/pix` com usuário, itens e total.
3. `CheckoutService` cria o pagamento via Mercado Pago, persiste pedido `pending` e retorna QR Code (base64 + copia-e-cola) e metadados.
4. Front mostra cronômetro de expiração, inicia polling (`useOrdersPolling`) e permite cancelar enquanto o status não é definitivo.
5. Mercado Pago notifica `WebhookController`, que consulta o pagamento e atualiza o pedido para `paid`, `canceled`, `fulfilled` ou `picked_up`.

### Gestão de pedidos e estoque
- `OrderController` e `InventoryController` expõem endpoints protegidos. O painel (`/sistema/dashboard`, `/sistema/inventario`) lista pedidos recentes, alerta estoque baixo e permite ações (aprovar, cancelar, mover status).
- `OrdersManager` no front mantém cache de produtos para apresentar detalhes sem recarregar toda a vitrine.
- Flyway scripts V18/V19 adicionam constraints e papéis administrativos para garantir consistência.

### Autenticação e autorização
- Supabase fornece OAuth Google e tokens. O backend recebe JWT assinado via `Authorization: Bearer` e protege endpoints com `SecurityConfig`.
- Perfis de clientes (idUser, email) são carregados por composables (`useStorefrontCustomer`), permitindo filtrar pedidos e itens por usuário.

## Configuração e ambientes
- Variáveis críticas (`MERCADO_PAGO_ACCESS_TOKEN`, `MERCADO_PAGO_BASE_URL`, `MERCADO_PAGO_NOTIFICATION_URL`) devem estar presentes antes de subir o backend.
- Credenciais Supabase (URL, anon/public keys, service role) são consumidas tanto pelo front (runtime config) quanto pelo backend (`SupabaseConfig`).
- Para desenvolvimento: `pnpm dev` sobe Nuxt no :3000 e proxy aponta para Spring (:8080). `./gradlew bootRun` liga APIs em `/api`.
- Para produção: builde as imagens Docker ou use Compose para orquestrar `backend`, `frontend` (SSR) e PostgreSQL.

## Qualidade e testes
- Backend usa `spring-boot-starter-test` (JUnit 5) e pode ser executado com `./gradlew test`.
- Frontend não possui suite formal ainda, mas o setup inclui `@nuxt/test-utils`, ESLint 9 e TypeScript estrito para prevenir regressões.
- Actuator + logs estruturados ajudam a monitorar filas de webhook, erros de integração e métricas customizadas.
