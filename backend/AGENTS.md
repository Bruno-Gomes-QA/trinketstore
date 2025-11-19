# AGENTS – Spring Ecommerce Backend

Este documento guia qualquer agente LLM que contribua com o backend do Trinket Store. A API é um serviço Spring Boot 3.5.x (Java 21) focado no domínio de ecommerce (produtos, preços, estoque, pedidos, itens de pedido, usuários e autenticação). Toda a pasta vive em `backend/`.

---

## 1. Visão Geral
- **Stack**: Spring Boot 3, Java 21, Gradle, Spring Data JPA, Spring Security, Spring Validation, Springdoc OpenAPI, Flyway e PostgreSQL.
- **Objetivo**: expor APIs REST `/api/**` consumidas pelo painel Nuxt e pelo fluxo público (Stripe Checkout, QR Code de retirada, auditoria de ações).
- **Autenticação**: Supabase Auth para cadastro/login, tokens JWT para proteger endpoints. Clientes `customer` acessam apenas leituras públicas; admins gerenciam catálogo e pedidos.
- **Observabilidade**: Actuator habilitado (`/actuator/health`), logging em `DEBUG` para `com.reducess.trinketstore`.
- **Execução local**: `./gradlew bootRun` (porta `8080`, contexto `/api`). Compose (`backend/compose.yaml`) sobe o serviço com Postgres configurado via variáveis.

---

## 2. Estrutura de Pastas
```
backend/
├─ build.gradle, settings.gradle, gradlew*
├─ src/
│  ├─ main/java/com/reducess/trinketstore/
│  │  ├─ config/        # SecurityConfig, OpenAPIConfig, SupabaseConfig
│  │  ├─ controller/    # Endpoints REST (Products, Orders, Inventory, Prices, Users, Auth…)
│  │  ├─ dto/           # Requests/Responses; 1 arquivo por contrato
│  │  ├─ entity/        # Entidades JPA (User, Product, Price, Inventory, Order, OrderItem)
│  │  ├─ exception/     # Exceptions + GlobalExceptionHandler
│  │  ├─ repository/    # Interfaces Spring Data (`extends JpaRepository`)
│  │  ├─ security/      # JWT filter/provider + UserPrincipal
│  │  └─ service/       # Regras de negócio via @Service + @Transactional
│  └─ main/resources/
│     ├─ application.properties
│     └─ db/migration/  # Scripts Flyway (V1__... .sql)
├─ compose.yaml         # docker compose para rodar a API
└─ verify_rls_policies.sql  # script auxiliar para validar RLS
```
- Cada módulo novo deve manter a mesma hierarquia (`entity` → `repository` → `service` → `controller` + DTOs).
- Nunca deixe código fora do namespace `com.reducess.trinketstore`.

---

## 3. Convenções de Código
1. **Nomeação**: entidades e requests em singular (`Product`, `CreateProductRequest`); endpoints plurais (`/products`). Métodos REST seguem verbos (`createX`, `updateX`, `deleteX`).
2. **DTOs**: ficaram em `dto/` e contam com validações `jakarta.validation.*`. Use `@Valid` em controllers.
3. **Controllers**:
   - `@RestController` + `@RequestMapping`.
   - Documente com `@Tag` e `@Operation`. Exponha o requisito de segurança com `@SecurityRequirement(name = "bearer-jwt")` quando necessário.
   - Retorne `ResponseEntity`, defina status apropriado (`CREATED`, `NO_CONTENT`, etc.).
4. **Services**:
   - `@Service` + `@RequiredArgsConstructor`.
   - Anote métodos com `@Transactional` (readOnly quando for consulta) e concentre neles as validações de negócio (slugs únicos, estoque mínimo, transições de status).
5. **Repositories**: `extends JpaRepository<Entidade, ID>`; crie métodos derivados (`findBySlugProduct`, `existsBySlugProduct`) ao invés de SQL manual quando possível.
6. **Exceções**: Crie classes dedicadas no pacote `exception` e deixe `GlobalExceptionHandler` fazer a tradução para HTTP. Nunca exponha mensagens brutas do banco/Supabase.
7. **Logging**: use `Logger`/`Slf4j` com prefixo do domínio (`log.info("[orders] ...")`). Logs sensíveis (tokens, chaves) não devem aparecer.
8. **Configurações**: qualquer parâmetro externo entra em `application.properties` com placeholder `${VAR}` e documentação no README/AGENTS.
9. **Documentação**: endpoints novos precisam ser descritivos no Swagger (resumo + descrição + `@Parameters` para filtros relevantes).

---

## 4. Segurança e Integrações
- **JWT**: `JwtAuthenticationFilter` injeta o usuário autenticado. Use `@PreAuthorize("hasRole('ADMIN')")` em mutações de catálogo/pedidos e `@PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")` quando clientes puderem ler dados próprios.
- **Supabase Auth**: `AuthService` conversa com Supabase via OkHttp; qualquer ajuste precisa manter headers `apikey` e tratar mensagens legíveis (vide `parseSupabaseError`). Dependências: `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY`, `SUPABASE_JWT_SECRET`.
- **Perfis de acesso**: `role` é armazenado em `users.role`. Nunca confie em dados do request para determinar permissões; use `UserPrincipal`.
- **CORS**: `SecurityConfig` libera `http://localhost:3000`. Se precisar liberar outro host, adicione em `corsConfigurationSource`.
- **Rotas públicas**: GET de produtos, preços e estoque são públicos; qualquer inclusão de rota pública precisa ser revista na mesma configuração para evitar vazamentos.
- **Stripe/Webhooks**: quando criar endpoints para conciliação, valide assinaturas server-side e mantenha logs/auditoria (ver migrações de `webhook_events`).

---

## 5. Banco de Dados & Migrações
- **Flyway** gerencia o schema. Sempre crie scripts incrementais `V{n}__descricao.sql` em `src/main/resources/db/migration`.
- **Row-Level Security (RLS)**: quase todas as tabelas estão protegidas; scripts `V3+` habilitam policies. Quando alterar tabelas, garanta que as policies continuem válidas;
- **Padronização**:
  - Campos `*_at` usam `TIMESTAMPTZ`.
  - Status de pedido: `pending`, `paid`, `picked_up`, `canceled`.
  - Monetários: armazenados como NUMERIC (centavos) e tratados em `prices`.
- **Migrações + entidades**: após criar/alterar script, atualize entidade JPA correspondente e mantenha `spring.jpa.hibernate.ddl-auto=validate` (não mude para `update`).
- **Seeds**: não há seeds automáticos; se precisar criar dados default (ex.: admin), use uma migração idempotente.

---

## 6. Fluxo para Novas Funcionalidades
1. **Descobrir contrato**: desenhe o domínio (ex.: `discounts`, `pickup windows`). Planeje quais DTOs e entidades serão necessárias.
2. **Banco**: escreva a migração Flyway (tabela, índices, FKs, RLS). Salve como `VXX__<descricao>.sql`.
3. **Entidade + Repositório**: crie a classe em `entity/` com `@Entity` + colunas corretas, e um `Repository` com métodos derivados apropriados.
4. **DTOs**: adicione `CreateFooRequest`, `UpdateFooRequest`, `FooResponse` em `dto/` com validações `@NotNull`, `@Positive`, etc.
5. **Serviço**: implemente regras com `@Transactional`, validando transições (ex.: pedido só pode ir para `picked_up` se `paid`).
6. **Controller**: exponha endpoints REST (GET/POST/PUT/PATCH/DELETE). Documente com Swagger e proteja com `@PreAuthorize`.
7. **Mapeamentos**: mantenha métodos `mapToResponse` privados nos serviços ou introduza mapper dedicado se ficar complexo.
8. **Testes**: crie testes unitários/integrados em `src/test/java/...` sempre que possível (ex.: testar estado do estoque ou transições de status). Use `@DataJpaTest`/`@SpringBootTest` conforme necessário.
9. **Execução**: rode `./gradlew flywayMigrate` (ou `bootRun` que aplica migrations automaticamente) e `./gradlew test`.
10. **Documentação**: atualize `backend/README.md` (quando existir) e este AGENTS caso crie padrões novos.

---

## 7. Execução, Variáveis e Testes
- **Variáveis obrigatórias**: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY`, `SUPABASE_JWT_SECRET`.
- **Rodar localmente**:
  ```bash
  cd backend
  ./gradlew bootRun               # inicia API em http://localhost:8080/api
  ./gradlew test                  # roda testes JUnit
  docker compose -f compose.yaml up --build   # sobe backend (usa .env)
  ```
- **Swagger**: disponível em `http://localhost:8080/api/swagger-ui.html`.
- **Actuator**: `http://localhost:8080/api/actuator/health`.
- **Build**: use `./gradlew clean build` antes de gerar imagens Docker.

---

## 8. Checklist Final
- [ ] Migração Flyway criada/atualizada e aplicada.
- [ ] Entidades, DTOs, repositórios e serviços sincronizados com o schema.
- [ ] Endpoints documentados com Swagger e protegidos conforme a regra de negócio.
- [ ] Validações (`@Valid`, `@NotNull`, etc.) e mensagens amigáveis configuradas.
- [ ] Logs úteis adicionados; nenhuma credencial exposta.
- [ ] Testes relevantes rodados (`./gradlew test`).
- [ ] README/AGENTS ou docs afetadas atualizadas.

Seguindo este guia, qualquer agente conseguirá evoluir a API do Trinket Store mantendo segurança, consistência e previsibilidade para o frontend Nuxt. Bons commits! 🚀
