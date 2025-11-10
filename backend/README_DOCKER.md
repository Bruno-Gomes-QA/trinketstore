# Docker Configuration

Este guia explica como executar a aplicação Trinket Store Backend usando Docker.

## Pré-requisitos

- Docker instalado (versão 20.10 ou superior)
- Docker Compose instalado (versão 2.0 ou superior)
- Banco de dados PostgreSQL externo (local ou remoto)

## Configuração

### 1. Configurar variáveis de ambiente

Certifique-se de que o arquivo `.env` existe e contém as variáveis necessárias:

```bash
cp .env.example .env
```

Edite o arquivo `.env` e preencha com suas credenciais:

```env
# Database Configuration (conectar ao seu banco externo)
DB_URL=jdbc:postgresql://<host>:<porta>/<database>
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha

# Supabase Configuration
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your_anon_key_here
SUPABASE_SERVICE_ROLE_KEY=your_service_role_key_here
SUPABASE_JWT_SECRET=your_jwt_secret_here
```

### 2. Configurar conexão com banco de dados

**Opção A: Banco local na sua máquina**
```env
DB_URL=jdbc:postgresql://localhost:5432/trinketstore
```

**Opção B: Banco remoto (Supabase, AWS RDS, etc)**
```env
DB_URL=jdbc:postgresql://db.example.supabase.co:5432/postgres
```

**Importante**: Como o container usa `network_mode: host`, ele acessa diretamente a rede do host, então `localhost` se refere ao seu computador.

## Executar a aplicação

### Iniciar o serviço

```bash
docker compose up -d
```

Este comando irá:
- Construir a imagem do backend da aplicação
- Criar e iniciar o container
- Conectar ao seu banco de dados PostgreSQL externo

### Ver logs dos serviços

```bash
# Ver logs em tempo real
docker compose logs -f backend

# Ver últimas 100 linhas
docker compose logs --tail=100 backend
```

### Parar o serviço

```bash
docker compose down
```

## Acessar a aplicação

- **API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/v3/api-docs
- **Health Check**: http://localhost:8080/api/actuator/health

## Rebuild da aplicação

Se você fizer alterações no código, reconstrua a imagem:

```bash
docker compose up -d --build
```

## Troubleshooting

### Erro de conexão com banco de dados

Se o container não conseguir conectar ao banco:

1. Verifique se o banco PostgreSQL está rodando
2. Verifique as credenciais no arquivo `.env`
3. Se o banco está em `localhost`, certifique-se de que aceita conexões
4. Verifique os logs: `docker compose logs backend`

### Verificar status do container

```bash
docker compose ps
```

### Acessar o shell do container

```bash
docker compose exec backend sh
```

### Testar conexão com banco

```bash
# Do seu computador (host)
psql -h localhost -U seu_usuario -d trinketstore

# Ou usando Docker com cliente PostgreSQL
docker run --rm -it --network host postgres:latest psql -h localhost -U seu_usuario -d trinketstore
```

### Limpar e recomeçar

```bash
docker compose down
docker compose build --no-cache
docker compose up -d
```

## Desenvolvimento

Para desenvolvimento local sem Docker, consulte os arquivos:
- `README_DATABASE.md` - Configuração do banco de dados
- `README_AUTH.md` - Configuração de autenticação

## Notas

- O container não inclui o PostgreSQL, você deve ter seu próprio banco de dados
- As migrações do Flyway serão executadas automaticamente na inicialização
- O container usa `network_mode: host` para facilitar o acesso a serviços locais
