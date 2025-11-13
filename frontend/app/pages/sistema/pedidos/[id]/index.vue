<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Button } from '~/components/ui/button'
import { Label } from '~/components/ui/label'
import { Skeleton } from '~/components/ui/skeleton'
import { useToast } from '~/components/ui/toast/use-toast'

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Detalhe do pedido',
})

const route = useRoute()
const orderId = Number(route.params.id)

const { toast } = useToast()
const { formatCurrencyFromCents, formatDateTime, formatOrderStatus } = useFormatters()
const orderDetails = useOrderDetails()
const orderMutations = useOrderMutations()

const statusOptions: import('~/types/orders').OrderStatus[] = ['pending', 'paid', 'canceled', 'fulfilled', 'picked_up']
const selectedStatus = ref<import('~/types/orders').OrderStatus>('pending')

const totalItems = computed(() =>
  orderDetails.items.value.reduce((sum, item) => sum + item.qtyItems, 0),
)

const loadOrder = async () => {
  await orderDetails.fetchOrder(orderId)
  if (orderDetails.order.value) {
    selectedStatus.value = orderDetails.order.value.statusOrder
  }
}

const handleStatusUpdate = async () => {
  const updated = await orderMutations.updateOrderStatus(orderId, selectedStatus.value)
  if (updated) {
    toast({ title: 'Status atualizado', description: 'O pedido foi atualizado.' })
    await loadOrder()
  }
}

onMounted(loadOrder)
</script>

<template>
  <div class="flex flex-1 flex-col gap-6">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold">Pedido #{{ orderDetails.order.value?.idOrder }}</h1>
        <p class="text-sm text-muted-foreground">
          Última atualização em {{ formatDateTime(orderDetails.order.value?.createdAt) }}
        </p>
      </div>
      <Button variant="outline" as-child>
        <NuxtLink to="/sistema/pedidos">Voltar à lista</NuxtLink>
      </Button>
    </div>

    <div v-if="orderDetails.loading.value" class="space-y-4">
      <Skeleton class="h-10 w-full" />
      <Skeleton class="h-32 w-full" />
    </div>

    <div v-else class="grid gap-6 lg:grid-cols-[2fr,1fr]">
      <div class="rounded-2xl border bg-card p-6 shadow-sm space-y-4">
        <div class="grid gap-4 sm:grid-cols-2">
          <div>
            <p class="text-xs uppercase text-muted-foreground">Cliente</p>
            <p class="font-semibold">#{{ orderDetails.order.value?.userId }}</p>
          </div>
          <div>
            <p class="text-xs uppercase text-muted-foreground">Total</p>
            <p class="font-semibold">{{ formatCurrencyFromCents(orderDetails.order.value?.totalOrders) }}</p>
          </div>
          <div>
            <p class="text-xs uppercase text-muted-foreground">Checkout ID</p>
            <p class="font-mono text-xs break-all">{{ orderDetails.order.value?.checkoutId }}</p>
          </div>
          <div>
            <p class="text-xs uppercase text-muted-foreground">Payment Intent</p>
            <p class="font-mono text-xs break-all">{{ orderDetails.order.value?.paymentIntent }}</p>
          </div>
        </div>

        <div class="rounded-xl border bg-muted/40 p-4">
          <div class="flex flex-wrap items-center gap-4">
            <div>
              <Label class="text-xs uppercase text-muted-foreground">Status atual</Label>
              <p class="text-lg font-semibold">{{ formatOrderStatus(orderDetails.order.value?.statusOrder) }}</p>
            </div>
            <div class="flex flex-1 items-center gap-2">
              <select v-model="selectedStatus" class="h-10 w-full rounded-lg border px-3 text-sm">
                <option v-for="status in statusOptions" :key="status" :value="status">
                  {{ formatOrderStatus(status) }}
                </option>
              </select>
              <Button :disabled="orderMutations.loading.value" @click="handleStatusUpdate">
                {{ orderMutations.loading.value ? 'Atualizando...' : 'Atualizar' }}
              </Button>
            </div>
          </div>
        </div>

        <div class="space-y-3">
          <h3 class="text-lg font-semibold">Itens do pedido</h3>
          <div class="rounded-xl border">
            <table class="w-full text-sm">
              <thead class="text-left text-xs text-muted-foreground">
                <tr>
                  <th class="px-4 py-3">Produto</th>
                  <th class="px-4 py-3">Qtd</th>
                  <th class="px-4 py-3">Valor unit.</th>
                  <th class="px-4 py-3 text-right">Subtotal</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="item in orderDetails.items.value"
                  :key="item.idItems"
                  class="border-t"
                >
                  <td class="px-4 py-3">#{{ item.productId }}</td>
                  <td class="px-4 py-3">{{ item.qtyItems }}</td>
                  <td class="px-4 py-3">{{ formatCurrencyFromCents(item.unitAmount) }}</td>
                  <td class="px-4 py-3 text-right">{{ formatCurrencyFromCents(item.subtotalAmount) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="rounded-2xl border bg-card p-6 shadow-sm space-y-4">
        <div>
          <p class="text-xs uppercase text-muted-foreground">Itens</p>
          <p class="text-3xl font-bold">{{ totalItems }}</p>
        </div>
        <div>
          <p class="text-xs uppercase text-muted-foreground">QR Token</p>
          <p class="font-mono text-xs break-all">{{ orderDetails.order.value?.pickupQrToken || '—' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
