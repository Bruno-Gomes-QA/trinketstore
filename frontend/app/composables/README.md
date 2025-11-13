# Composables

Estrutura organizada dos composables do projeto.

## 📁 Estrutura

```
composables/
├── api/              # Composables que fazem chamadas à API
│   └── useAuth.ts    # Autenticação e gestão de usuário
│
├── core/             # Funcionalidades base do sistema
│   └── useBackendFetch.ts  # Cliente HTTP para API
│
└── helpers/          # Utilitários e funções auxiliares
    ├── useFormatters.ts   # Formatação de dados (datas, CPF, CNPJ, moeda, telefone)
    ├── useNormalizers.ts  # Sanitização e normalização de inputs (slug, moeda, dígitos)
    └── useValidators.ts   # Validações reutilizáveis (nome, slug, url, email)
```

## 📦 Categorias

### API (`/api`)
Composables que interagem diretamente com endpoints do backend:
- **useAuth**: Login, logout e gestão de sessão

### Core (`/core`)
Funcionalidades fundamentais do sistema:
- **useBackendFetch**: Wrapper do $fetch com autenticação automática

### Helpers (`/helpers`)
Utilitários e funções auxiliares:
- **useFormatters**: Formatação de dados (datas, CPF/CNPJ, moeda, telefone, status)
- **useNormalizers**: Sanitização e normalização de entradas (slug, dígitos, centavos)
- **useValidators**: Regras de validação reutilizáveis (nome, slug, URL, email, inteiros)

## 🔄 Auto-import

Todos os composables são auto-importados pelo Nuxt. Use-os diretamente:

```vue
<script setup>
const { user, login } = useAuth()
const { formatCpf, formatDate, formatCurrency } = useFormatters()
</script>
```

## 📝 Convenções

- **api/**: Deve retornar dados do backend
- **core/**: Funcionalidades base reutilizáveis
- **helpers/**: Funções puras sem side-effects quando possível
