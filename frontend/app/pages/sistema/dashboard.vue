<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Clock3, CreditCard, Package, Truck } from 'lucide-vue-next'
import type { InventoryEntity } from '~/types/inventory'
import type { OrderEntity, OrderStatus } from '~/types/orders'
import type { ProductEntity } from '~/types/products'
import { Button } from '~/components/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '~/components/ui/table'
import { Skeleton } from '~/components/ui/skeleton'
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyTitle,
} from '~/components/ui/empty'

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Dashboard',
})

const LOW_STOCK_THRESHOLD = 10
const MAX_LATEST_ORDERS = 5

const { formatDateTime, formatCurrencyFromCents, formatOrderStatus } = useFormatters()

const summaryLoading = ref(true)
const lowStockLoading = ref(true)
const ordersLoading = ref(true)

const metrics = reactive({
  activeProducts: 0,
  pendingOrders: 0,
  paidOrders: 0,
  readyOrders: 0,
})

const lowStockItems = ref<InventoryEntity[]>([])
const latestOrders = ref<OrderEntity[]>([])

const summaryCards = computed(() => [
  {
    key: 'activeProducts',
    label: 'Produtos ativos',
    description: 'Disponíveis no catálogo',
    value: metrics.activeProducts,
    icon: Package,
    gradient: 'from-[#a4f380]/30 to-[#80d3e4]/30',
  },
  {
    key: 'pendingOrders',
    label: 'Pedidos pendentes',
    description: 'Aguardando pagamento',
    value: metrics.pendingOrders,
    icon: Clock3,
    gradient: 'from-[#f6d95c]/30 to-[#a4f380]/30',
  },
  {
    key: 'paidOrders',
    label: 'Pedidos pagos',
    description: 'Últimas 24h',
    value: metrics.paidOrders,
    icon: CreditCard,
    gradient: 'from-[#80d3e4]/30 to-[#a4f380]/30',
  },
  {
    key: 'readyOrders',
    label: 'Prontos para retirada',
    description: 'Fulfilled + picked',
    value: metrics.readyOrders,
    icon: Truck,
    gradient: 'from-[#a4f380]/30 to-[#f6d95c]/30',
  },
])

const statusColorMap: Record<OrderStatus, string> = {
  pending: 'bg-amber-100 text-amber-900 border border-amber-200',
  paid: 'bg-emerald-100 text-emerald-900 border border-emerald-200',
  canceled: 'bg-rose-100 text-rose-900 border border-rose-200',
  fulfilled: 'bg-blue-100 text-blue-900 border border-blue-200',
  picked_up: 'bg-purple-100 text-purple-900 border border-purple-200',
}

const statusBadgeClass = (status: OrderStatus) => {
  return statusColorMap[status] || 'bg-muted text-foreground border border-border'
}

const fetchSummary = async () => {
  summaryLoading.value = true
  try {
    const [active, pending, paid, fulfilled, pickedUp] = await Promise.all([
      useBackendFetchDirect<ProductEntity[]>('/products/active'),
      useBackendFetchDirect<OrderEntity[]>('/orders/status/pending'),
      useBackendFetchDirect<OrderEntity[]>('/orders/status/paid'),
      useBackendFetchDirect<OrderEntity[]>('/orders/status/fulfilled'),
      useBackendFetchDirect<OrderEntity[]>('/orders/status/picked_up'),
    ])

    metrics.activeProducts = active.length
    metrics.pendingOrders = pending.length
    metrics.paidOrders = paid.length
    metrics.readyOrders = fulfilled.length + pickedUp.length
  } catch {
    // handled globally by useBackendFetchDirect
  } finally {
    summaryLoading.value = false
  }
}

const fetchLowStock = async () => {
  lowStockLoading.value = true
  try {
    const data = await useBackendFetchDirect<InventoryEntity[]>(`/inventory/low-stock/${LOW_STOCK_THRESHOLD}`)
    lowStockItems.value = data
  } catch {
    lowStockItems.value = []
  } finally {
    lowStockLoading.value = false
  }
}

const fetchLatestOrders = async () => {
  ordersLoading.value = true
  try {
    const orders = await useBackendFetchDirect<OrderEntity[]>('/orders')
    latestOrders.value = orders
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      .slice(0, MAX_LATEST_ORDERS)
  } catch {
    latestOrders.value = []
  } finally {
    ordersLoading.value = false
  }
}

const refreshDashboard = () => {
  fetchSummary()
  fetchLowStock()
  fetchLatestOrders()
}

onMounted(refreshDashboard)
</script>

<template>
  <div class="flex flex-1 flex-col gap-4">
    <div class="grid gap-4 md:grid-cols-4">
      <div
        v-for="card in summaryCards"
        :key="card.key"
        class="bg-card border rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow"
      >
        <div class="flex items-center justify-between">
          <p class="text-sm text-muted-foreground">{{ card.label }}</p>
          <div :class="['p-2 rounded-lg', card.gradient, 'bg-gradient-to-br']">
            <component :is="card.icon" class="h-4 w-4 text-foreground/80" />
          </div>
        </div>
        <p class="text-2xl font-bold mt-2">
          <Skeleton v-if="summaryLoading" class="h-7 w-16" />
          <span v-else>{{ card.value }}</span>
        </p>
        <p class="text-xs text-muted-foreground mt-1">{{ card.description }}</p>
      </div>
    </div>

    <div class="grid gap-4 lg:grid-cols-2">
      <div class="bg-card border rounded-2xl p-6 shadow-sm">
        <div class="mb-4 flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold">Estoque baixo</h3>
            <p class="text-sm text-muted-foreground">
              Produtos abaixo de {{ LOW_STOCK_THRESHOLD }} unidades
            </p>
          </div>
          <Button variant="ghost" size="sm" as-child>
            <NuxtLink to="/sistema/inventario">Ver inventário</NuxtLink>
          </Button>
        </div>

        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Produto</TableHead>
              <TableHead>Quantidade</TableHead>
              <TableHead class="text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <template v-if="lowStockLoading">
              <TableRow v-for="index in 3" :key="index">
                <TableCell>
                  <Skeleton class="h-5 w-44" />
                  <Skeleton class="h-4 w-24 mt-2" />
                </TableCell>
                <TableCell>
                  <Skeleton class="h-5 w-12" />
                </TableCell>
                <TableCell class="text-right">
                  <Skeleton class="h-8 w-24 ml-auto" />
                </TableCell>
              </TableRow>
            </template>
            <template v-else-if="lowStockItems.length">
              <TableRow
                v-for="item in lowStockItems"
                :key="item.idInventory"
              >
                <TableCell>
                  <div class="font-semibold">
                    {{ item.nomeProduct || `Produto #${item.productId}` }}
                  </div>
                  <p class="text-xs text-muted-foreground">
                    {{ item.categoriaProduct || 'Sem categoria' }}
                  </p>
                </TableCell>
                <TableCell class="font-semibold">
                  {{ item.qtyOnHand }} un
                </TableCell>
                <TableCell class="text-right">
                  <Button 
                    variant="outline" 
                    size="sm" 
                    class="text-xs"
                    as-child
                  >
                    <NuxtLink :to="`/sistema/produtos/${item.productId}/estoque`">
                      Ajustar estoque
                    </NuxtLink>
                  </Button>
                </TableCell>
              </TableRow>
            </template>
            <TableEmpty v-else :colspan="3">
              <Empty>
                <EmptyHeader>
                  <EmptyTitle>Tudo certo por aqui</EmptyTitle>
                  <EmptyDescription>
                    Nenhum produto abaixo de {{ LOW_STOCK_THRESHOLD }} unidades.
                  </EmptyDescription>
                </EmptyHeader>
              </Empty>
            </TableEmpty>
          </TableBody>
        </Table>
      </div>

      <div class="bg-card border rounded-2xl p-6 shadow-sm">
        <div class="mb-4 flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold">Últimos pedidos</h3>
            <p class="text-sm text-muted-foreground">
              Atualizado em tempo real
            </p>
          </div>
          <Button variant="ghost" size="sm" as-child>
            <NuxtLink to="/sistema/pedidos">Ver todos</NuxtLink>
          </Button>
        </div>

        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Cliente</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Total</TableHead>
              <TableHead>Data</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <template v-if="ordersLoading">
              <TableRow v-for="index in 4" :key="index">
                <TableCell><Skeleton class="h-5 w-20" /></TableCell>
                <TableCell><Skeleton class="h-5 w-32" /></TableCell>
                <TableCell><Skeleton class="h-6 w-24" /></TableCell>
                <TableCell><Skeleton class="h-5 w-20" /></TableCell>
                <TableCell><Skeleton class="h-5 w-32" /></TableCell>
              </TableRow>
            </template>
            <template v-else-if="latestOrders.length">
              <TableRow
                v-for="order in latestOrders"
                :key="order.idOrder"
              >
                <TableCell class="font-semibold">
                  #{{ order.idOrder }}
                </TableCell>
                <TableCell>
                  Cliente #{{ order.userId }}
                </TableCell>
                <TableCell>
                  <span
                    class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold"
                    :class="statusBadgeClass(order.statusOrder)"
                  >
                    {{ formatOrderStatus(order.statusOrder) }}
                  </span>
                </TableCell>
                <TableCell class="font-semibold">
                  {{ formatCurrencyFromCents(order.totalOrders) }}
                </TableCell>
                <TableCell class="text-sm text-muted-foreground">
                  {{ formatDateTime(order.createdAt) }}
                </TableCell>
              </TableRow>
            </template>
            <TableEmpty v-else :colspan="5">
              <Empty>
                <EmptyHeader>
                  <EmptyTitle>Nenhum pedido encontrado</EmptyTitle>
                  <EmptyDescription>Que tal criar um novo pedido manual?</EmptyDescription>
                </EmptyHeader>
              </Empty>
            </TableEmpty>
          </TableBody>
        </Table>
      </div>
    </div>
  </div>
</template>
