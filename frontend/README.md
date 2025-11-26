# 🛍️ Trinket Store - Painel Administrativo

Sistema de gerenciamento de e-commerce para stands e lojas físicas com integração de pagamentos via Mercado Pago e controle de retiradas por QR Code.

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
- Integração com Mercado Pago Checkout
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

Certifique-se de instalar as dependências:

```bash
# bun
bun install
```

## 🔧 Servidor de Desenvolvimento

Inicie o servidor de desenvolvimento em `http://localhost:3000`:

```bash
# bun
bun run dev
```

## 🏗️ Produção

Build da aplicação para produção:

```bash
# bun
bun run build
```

Pré-visualizar a build de produção localmente:

```bash
# bun
bun run preview
```

## 🛠️ Stack Tecnológica

- **Framework**: Nuxt 3
- **UI Components**: Shadcn-vue
- **Estilização**: TailwindCSS v4
- **Linguagem**: TypeScript
- **Ícones**: Lucide Icons
- **Pagamentos**: Mercado Pago

## 📝 Variáveis de Ambiente

```bash
# Backend API URL
NUXT_PUBLIC_BACKEND_URL=http://localhost:8080/api
NUXT_BACKEND_URL=http://localhost:8080/api
```

## 📚 Documentação Adicional

- [Nuxt 3 Docs](https://nuxt.com/docs)
- [Shadcn-vue Docs](https://www.shadcn-vue.com)
- [TailwindCSS Docs](https://tailwindcss.com)

---

**Trinket Store** - Sistema de gerenciamento de e-commerce © 2025
