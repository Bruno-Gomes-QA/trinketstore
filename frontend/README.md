````mdc
# 🛍️ Trinket Store - Painel Administrativo

Sistema de gerenciamento de e-commerce para stands e lojas físicas com integração de pagamentos via Stripe e controle de retiradas por QR Code.

## 🎨 Identidade Visual

**Cores da Marca:**
- Verde: `#a4f380` - Cor primária
- Ciano: `#80d3e4` - Cor secundária
- Amarelo: `#f6d95c` - Cor de destaque

## 🚀 Funcionalidades (MVP)

### Autenticação
- Login e cadastro de usuários
- Recuperação de senha
- Permissões (usuário comum e admin)

### Gestão de Produtos
- CRUD completo de produtos
- Upload e gerenciamento de imagens
- Categorização
- Status ativo/inativo
- Controle de estoque

### Pedidos e Pagamentos
- Criação de pedidos com cálculo automático
- Status: `pending`, `paid`, `picked_up`, `canceled`
- Integração com Stripe Checkout
- Controle de preços local

### QR Code
- Geração automática após pagamento
- Validação única para retirada
- Sistema de expiração
- Baixa de estoque na retirada confirmada

### Painel Admin
- Dashboard com métricas em tempo real
- Vendas do dia
- Ticket médio
- Produtos mais vendidos
- Controle de retiradas

### Auditoria
- Logs de ações
- Histórico de alterações
- Registro de retiradas

## 📦 Setup

Make sure to install dependencies:

```bash
# npm
npm install

# pnpm
pnpm install

# yarn
yarn install

# bun
bun install
```

## 🔧 Development Server

Start the development server on `http://localhost:3000`:

```bash
# npm
npm run dev

# pnpm
pnpm dev

# yarn
yarn dev

# bun
bun run dev
```

## 🏗️ Production

Build the application for production:

```bash
# npm
npm run build

# pnpm
pnpm build

# yarn
yarn build

# bun
bun run build
```

Locally preview production build:

```bash
# npm
npm run preview

# pnpm
pnpm preview

# yarn
yarn preview

# bun
bun run preview
```

## 🛠️ Stack Tecnológica

- **Framework**: Nuxt 3
- **UI Components**: Shadcn-vue
- **Estilização**: TailwindCSS v4
- **Linguagem**: TypeScript
- **Ícones**: Lucide Icons
- **Pagamentos**: Stripe

## 📝 Variáveis de Ambiente

```bash
# Backend API URL
NUXT_PUBLIC_BACKEND_URL=http://localhost:8080/api
NUXT_BACKEND_URL=http://localhost:8080/api

# Stripe (futuro)
NUXT_PUBLIC_STRIPE_PUBLISHABLE_KEY=pk_test_...
```

## 📚 Documentação Adicional

- [Nuxt 3 Docs](https://nuxt.com/docs)
- [Shadcn-vue Docs](https://www.shadcn-vue.com)
- [TailwindCSS Docs](https://tailwindcss.com)
- [Stripe Docs](https://stripe.com/docs)

---

**Trinket Store** - Sistema de gerenciamento de e-commerce © 2025

````
