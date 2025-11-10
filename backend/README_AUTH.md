# API de Autenticação - Trinket Store

## Configuração

1. Copie o arquivo `.env.example` para `.env` e preencha com suas credenciais do Supabase
2. Execute o projeto com `./gradlew bootRun`

## Endpoints de Autenticação

### 1. Criar Conta (Sign Up)
```
POST /api/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "senha123",
  "name": "Nome do Usuário"
}
```

**Resposta (200 OK):**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 3600,
  "user": {
    "idUser": 1,
    "authId": "uuid-here",
    "nomeUser": "Nome do Usuário",
    "role": "customer",
    "createdAt": "2025-11-10T10:00:00Z",
    "updatedAt": "2025-11-10T10:00:00Z"
  }
}
```

### 2. Login (Sign In)
```
POST /api/auth/signin
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "senha123"
}
```

**Resposta:** Mesma estrutura do signup

## Endpoints de Usuários

### 3. Obter Perfil Atual
```
GET /api/users/me
Authorization: Bearer {accessToken}
```

**Resposta (200 OK):**
```json
{
  "idUser": 1,
  "authId": "uuid-here",
  "nomeUser": "Nome do Usuário",
  "role": "customer",
  "createdAt": "2025-11-10T10:00:00Z",
  "updatedAt": "2025-11-10T10:00:00Z"
}
```

### 4. Atualizar Perfil
```
PUT /api/users/me
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "Novo Nome"
}
```

**Resposta (200 OK):** Objeto user atualizado

### 5. Deletar Própria Conta
```
DELETE /api/users/me
Authorization: Bearer {accessToken}
```

**Resposta (204 No Content)**

### 6. Obter Usuário por ID (Admin apenas)
```
GET /api/users/{userId}
Authorization: Bearer {accessToken}
```

**Resposta (200 OK):** Objeto user

### 7. Deletar Usuário (Admin apenas)
```
DELETE /api/users/{userId}
Authorization: Bearer {accessToken}
```

**Resposta (204 No Content)**

## Segurança

- **JWT Token**: Todos os endpoints exceto `/auth/**` requerem token JWT válido
- **Role-Based Access**: Endpoints administrativos requerem `role = 'admin'`
- **RLS (Row Level Security)**: Políticas do Supabase garantem que usuários só acessem seus próprios dados
- **Service Role**: O backend usa `service_role_key` para operações administrativas que bypassam RLS
- **Password Hashing**: Senhas são gerenciadas pelo Supabase Auth (bcrypt)
- **Token Validation**: Tokens JWT são validados usando o `JWT_SECRET` do Supabase

## Fluxo de Autenticação

1. **Signup**: Cria usuário no Supabase Auth → Cria registro na tabela `users` → Retorna tokens
2. **Signin**: Autentica no Supabase Auth → Busca dados na tabela `users` → Retorna tokens
3. **Requisições Autenticadas**: Frontend envia token → Backend valida JWT → Extrai `auth_id` → Busca user → Autoriza ação

## Validações

- Email: formato válido
- Senha: mínimo 6 caracteres
- Nome: entre 2 e 100 caracteres
- Role: apenas 'admin' ou 'customer'
- Role escalation: Apenas admins podem mudar o role de usuários (protegido por trigger no banco)

