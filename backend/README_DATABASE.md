# Configuração do Banco de Dados

## Pré-requisitos

1. **PostgreSQL instalado e rodando** na sua máquina ou servidor
2. **Criar o banco de dados** `trinketstore` (ou o nome que preferir)
3. **Criar o schema** `auth` se for usar autenticação externa (como Supabase)

## Como Configurar

### Opção 1: Usando Variáveis de Ambiente (Recomendado)

1. Copie o arquivo `.env.example` para `.env`:
   ```bash
   cp .env.example .env
   ```

2. Edite o arquivo `.env` com suas credenciais:
   ```
   DB_URL=jdbc:postgresql://seu-host:5432/trinketstore
   DB_USERNAME=seu_usuario
   DB_PASSWORD=sua_senha
   ```

3. Execute a aplicação (as variáveis serão carregadas automaticamente)

### Opção 2: Editando application.properties diretamente

Edite o arquivo `src/main/resources/application.properties` e substitua os valores padrão:

```properties
spring.datasource.url=jdbc:postgresql://seu-host:5432/trinketstore
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

⚠️ **ATENÇÃO:** Não commite senhas no Git!

### Opção 3: Passando como argumentos na execução

```bash
./gradlew bootRun --args='--spring.datasource.url=jdbc:postgresql://localhost:5432/trinketstore --spring.datasource.username=usuario --spring.datasource.password=senha'
```

## Preparar o Banco de Dados

Execute os seguintes comandos SQL no seu PostgreSQL:

```sql
-- Criar o banco de dados
CREATE DATABASE trinketstore;

-- Conectar ao banco
\c trinketstore

-- Criar o schema auth (necessário para a migração de users)
CREATE SCHEMA IF NOT EXISTS auth;

-- Criar a tabela auth.users (se você usar autenticação externa como Supabase, essa tabela já existirá)
-- Se não usar, você pode criar uma versão simplificada:
CREATE TABLE IF NOT EXISTS auth.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

## Migrações Flyway

As migrações serão executadas automaticamente quando a aplicação iniciar.

Atualmente temos:
- `V1__create_users_table.sql` - Cria a tabela de usuários

## Testar a Conexão

1. Inicie a aplicação:
   ```bash
   ./gradlew bootRun
   ```

2. Verifique os logs. Você deve ver:
   - ✅ Conexão com PostgreSQL estabelecida
   - ✅ Flyway executando as migrações
   - ✅ Aplicação iniciada com sucesso

3. Acesse o health check:
   ```bash
   curl http://localhost:8080/api/actuator/health
   ```

## Troubleshooting

### Erro: "role does not exist"
- Certifique-se de que o usuário existe no PostgreSQL

### Erro: "database does not exist"
- Crie o banco de dados manualmente: `CREATE DATABASE trinketstore;`

### Erro: "schema auth does not exist"
- Crie o schema: `CREATE SCHEMA auth;`
- Crie a tabela auth.users conforme mostrado acima

### Erro de conexão
- Verifique se o PostgreSQL está rodando
- Verifique host, porta e credenciais
- Verifique firewall e permissões de rede
# Database Configuration
# Copie este arquivo para .env e preencha com suas credenciais reais

# URL de conexão com o PostgreSQL
# Formato: jdbc:postgresql://HOST:PORT/DATABASE_NAME
DB_URL=jdbc:postgresql://localhost:5432/trinketstore

# Usuário do banco de dados
DB_USERNAME=seu_usuario_aqui

# Senha do banco de dados
DB_PASSWORD=sua_senha_aqui

