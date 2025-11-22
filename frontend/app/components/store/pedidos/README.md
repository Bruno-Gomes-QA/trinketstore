# Componentes de Pedidos

Esta pasta contém os componentes reutilizáveis para a página de pedidos da vitrine pública do Trinket Store.

## Estrutura

### `OrdersList.vue`
Lista de pedidos do usuário com badges de status.

**Props:**
- `orders: OrderResponse[]` - Array de pedidos
- `selectedOrderId?: number | null` - ID do pedido selecionado

**Events:**
- `select: (orderId: number)` - Emitido quando um pedido é clicado

**Uso:**
```vue
<OrdersList 
  :orders="orders" 
  :selected-order-id="highlightedOrderId"
  @select="handleSelectOrder"
/>
```

---

### `OrderDetails.vue`
Exibe detalhes completos de um pedido selecionado, incluindo status, itens, pagamento e QR Code.

**Props:**
- `order: OrderResponse` - Pedido a ser exibido
- `productCache: Record<number, ProductResponse>` - Cache de produtos para exibir detalhes dos itens
- `canceling?: boolean` - Indica se está cancelando o pedido

**Events:**
- `cancel: ()` - Emitido quando o botão cancelar é clicado

**Uso:**
```vue
<OrderDetails
  v-if="selectedOrder"
  :order="selectedOrder"
  :product-cache="productCache"
  :canceling="canceling"
  @cancel="showCancelDialog = true"
/>
```

---

### `OrdersHeader.vue`
Cabeçalho da seção de pedidos com contagem e botão de atualizar.

**Props:**
- `ordersCount: number` - Número total de pedidos
- `lastUpdate: string` - Horário da última atualização
- `loading?: boolean` - Indica se está carregando

**Events:**
- `refresh: ()` - Emitido quando o botão atualizar é clicado

---

### `OrderPaymentSection.vue`
Seção de pagamento PIX com código copia-e-cola.

**Props:**
- `pixCode: string` - Código PIX para copiar
- `pixQrUrl: string` - URL do QR Code

---

### `OrderItemsList.vue`
Lista de itens do pedido com imagens e valores.

**Props:**
- `items: ItemWithDetails[]` - Array de itens com detalhes do produto

---

### `OrderQrCodeSection.vue`
Exibe o QR Code do PIX para pagamento.

**Props:**
- `qrUrl: string` - URL da imagem do QR Code

---

### `OrderCancelDialog.vue`
Dialog de confirmação para cancelamento de pedido.

**Props:**
- `open: boolean` - Controla visibilidade do dialog
- `canceling?: boolean` - Indica se está cancelando

**Events:**
- `update:open: (value: boolean)` - Atualiza estado do dialog
- `confirm: ()` - Confirma o cancelamento

---

## Composables

### `useOrdersManager`
Gerencia estado e operações de pedidos.

**Retorna:**
- `orders` - Lista de pedidos
- `loadingOrders` - Estado de loading
- `ordersError` - Mensagem de erro
- `highlightedOrderId` - ID do pedido selecionado
- `productCache` - Cache de produtos
- `selectedOrder` - Pedido selecionado (computed)
- `fetchOrders(userId)` - Busca pedidos do usuário
- `selectOrder(orderId)` - Seleciona um pedido
- `updateOrderInList(order)` - Atualiza pedido na lista
- `cancelOrder(orderId)` - Cancela um pedido
- `preloadItems(orderId)` - Pré-carrega detalhes dos produtos

### `useOrdersPolling`
Gerencia polling automático para atualização de status.

**Retorna:**
- `lastStatusUpdate` - Timestamp da última atualização
- `startOrderPolling(orderId, status, onUpdate)` - Inicia polling
- `stopOrderPolling()` - Para polling
- `pollOrderStatus(orderId, onUpdate)` - Atualiza status manualmente

---

## Fluxo de Uso

1. Use `useOrdersManager()` para gerenciar lista de pedidos
2. Use `useOrdersPolling()` para atualização automática
3. Renderize `OrdersList` com os pedidos
4. Ao selecionar, exiba `OrderDetails` com o pedido completo
5. Use `OrderCancelDialog` para confirmação de cancelamento

## Regras

- Apenas pedidos com status `pending` mostram seção de pagamento PIX
- Polling automático só roda para pedidos `pending`
- Cache de produtos evita refetch desnecessário
- Todos os componentes seguem design system do shadcn-vue
