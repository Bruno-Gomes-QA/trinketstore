# Trinket Store

Aplicação full-stack para catálogo, estoque e pedidos, composta por um frontend Nuxt (Vercel) e um backend Spring Boot em container com Nginx na AWS EC2. O banco é PostgreSQL gerenciado no Supabase.

## Tecnologias principais

- Frontend: Nuxt 3 + Tailwind + Shadcn UI (deploy contínuo na Vercel)
- Backend: Spring Boot 3 (Java 21) + Flyway + Docker + Nginx reverse proxy
- Banco de dados: PostgreSQL (Supabase) e migrações versionadas em `backend/src/main/resources/db/migration`
- Autenticação/autorização: Supabase Auth com JWT para back e front
- Pagamentos: PIX dinâmico via Mercado Pago

## Arquitetura em produção

- **Frontend**: hospedado na Vercel, acessa o backend via `/api` e utiliza apenas assets estáticos.
- **Backend**: container Docker publicado em uma instância EC2. O Nginx expõe `https://trinketstore/api` e faz proxy para o container na porta 8080.
- **Banco**: cluster Postgres do Supabase; todas as instâncias (local e prod) apontam para a URL gerada pelo serviço.
- **Observabilidade**: logs ficam nos containers Docker/CloudWatch; alarmes e métricas são tratados no nível da instância EC2.
- **Documentação de negócios e técnica**: disponível em `docs/visao-negocio.md` e `docs/visao-tecnica.md`. Leia também os READMEs específicos dentro de cada módulo (por exemplo `frontend/README.md`).

## Variáveis de ambiente essenciais

> Guarde tudo no gerenciador secreto da Vercel (frontend) e no arquivo `.env` usado pelo Docker Compose do backend. Nunca faça commit das chaves reais.

**Frontend (Vercel e local)**

- `NUXT_PUBLIC_SUPABASE_URL`
- `NUXT_PUBLIC_SUPABASE_ANON_KEY`
- `NUXT_PUBLIC_API_BASE_URL` (ex.: `https://trinketstore.com/api` ou `http://localhost:8080/api`)
- `NUXT_SUPABASE_SERVICE_ROLE_KEY` (necessário apenas para rotinas server-side)

**Backend (EC2 + Docker Compose)**

- `SPRING_DATASOURCE_URL` / `DB_URL` (string do Supabase: `jdbc:postgresql://...`)
- `SPRING_DATASOURCE_USERNAME` / `DB_USERNAME`
- `SPRING_DATASOURCE_PASSWORD` / `DB_PASSWORD`
- `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY`, `SUPABASE_JWT_SECRET`
- `MERCADO_PAGO_ACCESS_TOKEN`
- `MERCADO_PAGO_BASE_URL` *(opcional, padrão `https://api.mercadopago.com`)*
- `MERCADO_PAGO_NOTIFICATION_URL` *(opcional; configure a URL pública do webhook exposto pelo Nginx)*

## Rodando localmente

Pré-requisitos: Node 20 + pnpm, Java 21 + Gradle wrapper, Docker (opcional para subir serviços).

### Backend

```bash
cd backend
cp .env.example .env               # preencha com as chaves do Supabase e Mercado Pago
./gradlew bootRun                  # sobe em http://localhost:8080/api
```

> O Flyway executa as migrações automaticamente no startup e usa o Postgres do Supabase informado nas variáveis acima.

### Frontend

```bash
cd frontend
cp .env.example .env               # configure a URL do Supabase e do backend local
pnpm install
pnpm dev                           # abre em http://localhost:3000
```

## Deploy simplificado

### Frontend (Vercel)

1. `cd frontend && pnpm install`.
2. Valide se o build está ok com `pnpm lint && pnpm build`.
3. Garanta que todas as variáveis estão configuradas na Vercel (`vercel env ls`).
4. Faça push para o branch conectado ou rode `vercel --prod` para disparar o deploy.

### Backend (Docker + Nginx na EC2)

1. Empacote a aplicação: `cd backend && ./gradlew bootJar`.
2. Gere a imagem: `docker build -t <registry>/trinketstore-backend:<tag> .` e faça push para o registry usado no servidor (ECR/Docker Hub).
3. Na instância EC2, atualize o `.env` com as credenciais mais recentes e execute:
   ```bash
   docker pull <registry>/trinketstore-backend:<tag>
   docker compose --env-file backend/.env -f backend/compose.yaml up -d --force-recreate
   ```
4. O Nginx faz proxy para `localhost:8080`; recarregue-o caso altere certificados: `sudo nginx -s reload`.
5. Verifique `curl -I http://127.0.0.1:8080/actuator/health` para garantir que a API subiu antes de liberar o tráfego.

### Banco de dados (Supabase Postgres)

- Administre os dados diretamente pelo painel do Supabase.
- Exporte snapshots antes de cada deploy crítico.
- As migrações Flyway sobem junto com o backend; valide o histórico em `flyway_schema_history`.

---

Qualquer mudança de arquitetura ou stack deve ser registrada nos documentos de visão técnica e de negócios em `docs/` e, quando fizer sentido, detalhada nos READMEs específicos dos módulos.
