# Trinket Store

Aplicação full-stack (Nuxt + Spring Boot) que gerencia catálogo, estoque e pedidos da Trinket Store.

## Backend

- **Tecnologia:** Spring Boot 3 / Java 21 / PostgreSQL
- **Servidor:** exposto em `/api`
- **Autenticação:** JWT emitido a partir do login com Supabase
- **Migrações:** Flyway em `backend/src/main/resources/db/migration`

### Configuração do PIX (Mercado Pago)

O checkout transparente foi integrado ao Mercado Pago usando PIX dinâmico. Configure as variáveis abaixo antes de subir o backend:

| Variável | Função |
| --- | --- |
| `MERCADO_PAGO_ACCESS_TOKEN` | Access token (test ou production) do app no Mercado Pago |
| `MERCADO_PAGO_BASE_URL` | (opcional) URL base da API. Padrão `https://api.mercadopago.com` |
| `MERCADO_PAGO_NOTIFICATION_URL` | (opcional) URL pública do webhook. Padrão `http://localhost:8080/api/webhooks/mercadopago` |

#### Fluxo

1. Front chama `POST /api/checkout/pix` informando `userId`, itens e total em centavos.
2. Backend cria o pagamento PIX via `/v1/payments` do Mercado Pago e registra o pedido (status `pending`).
3. Resposta retorna:
   - Pedido completo (`order`)
   - Dados do PIX (`pix.qrCode`, `pix.qrCodeBase64`, `pix.expiresAt`, `pix.paymentId`)
4. Front exibe o QR Code base64 e copia-e-cola real. Um polling consulta `/orders/{id}` para refletir o status.
5. O Mercado Pago envia notificações para `POST /api/webhooks/mercadopago`. O backend consulta o pagamento e atualiza o pedido (`paid`, `canceled`, etc.).

> **Importante:** exponha o endpoint de webhook publicamente (por exemplo com ngrok) ao testar com credenciais reais.

## Frontend

- **Tecnologia:** Nuxt 3 + Tailwind + Shadcn
- **Autenticação:** Supabase (Google OAuth)
- **Comandos:** `cd frontend && pnpm dev`

### Checkout na vitrine

1. Usuário revisa itens e estoque
2. Faz login com Google
3. Ao clicar em “Gerar pagamento PIX”, o front consome `/checkout/pix` e recebe QR Code + texto copia-e-cola
4. Um cronômetro acompanha a expiração do PIX enviado pelo banco
5. O status muda automaticamente para “Pagamento aprovado” quando o webhook confirmar a transação

## Scripts úteis

```bash
# Backend
cd backend
./gradlew test

# Frontend
cd frontend
pnpm dev
```
