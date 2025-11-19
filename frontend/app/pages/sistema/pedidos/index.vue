<script setup lang="ts">
import { computed, onMounted, ref, watch, type Component } from 'vue'
import {
  CheckCircle2,
  Clock3,
  CreditCard,
  Eye,
  Hash,
  MoreHorizontal,
  Package,
  QrCode,
  Search,
  SlidersHorizontal,
  Trash2,
  XCircle,
} from 'lucide-vue-next'
import { Button } from '~/components/ui/button'
import {
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from '~/components/ui/input-group'
import { Label } from '~/components/ui/label'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '~/components/ui/popover'
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
import { useToast } from '~/components/ui/toast/use-toast'
import type { OrderStatus } from '~/types/orders'

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Pedidos',
})

const { formatCurrencyFromCents, formatDateTime, formatOrderStatus } = useFormatters()
const { filteredOrders, loading, fetchOrders, setFilters } = useOrdersList()
const ordersApi = useOrderMutations()
const { toast } = useToast()

const searchTerm = ref('')
const statusFilter = ref<'all' | OrderStatus>('all')
const paymentFilter = ref('')
const checkoutFilter = ref('')
const userFilter = ref('')
const statusPopoverOpen = ref<number | null>(null)
const deletePopoverOpen = ref<number | null>(null)
const updatingOrderId = ref<number | null>(null)
const deletingOrderId = ref<number | null>(null)

const filteredList = computed(() => filteredOrders.value)

const statusMeta: Record<OrderStatus, { label: string; classes: string; icon: Component }> = {
  pending: {
    label: 'Pendente',
    classes: 'border-amber-200 bg-amber-50 text-amber-700',
    icon: Clock3,
  },
  paid: {
    label: 'Pago',
    classes: 'border-emerald-200 bg-emerald-50 text-emerald-700',
    icon: CreditCard,
  },
  fulfilled: {
    label: 'Pronto',
    classes: 'border-sky-200 bg-sky-50 text-sky-700',
    icon: Package,
  },
  picked_up: {
    label: 'Retirado',
    classes: 'border-blue-200 bg-blue-50 text-blue-700',
    icon: QrCode,
  },
  canceled: {
    label: 'Cancelado',
    classes: 'border-rose-200 bg-rose-50 text-rose-700',
    icon: XCircle,
  },
}

const statusOptions = Object.entries(statusMeta).map(([value, meta]) => ({
  value: value as OrderStatus,
  label: meta.label,
}))

const updateFilterState = () => {
  setFilters({
    status: statusFilter.value === 'all' ? undefined : statusFilter.value,
    paymentIntent: paymentFilter.value || undefined,
    checkoutId: checkoutFilter.value || undefined,
    userId: userFilter.value ? Number(userFilter.value) : undefined,
    search: searchTerm.value || undefined,
  })
}

watch([statusFilter, paymentFilter, checkoutFilter, userFilter, searchTerm], () => {
  updateFilterState()
})

const handleStatusUpdate = async (orderId: number, status: OrderStatus) => {
  updatingOrderId.value = orderId
  const updated = await ordersApi.updateOrderStatus(orderId, status)
  updatingOrderId.value = null
  if (!updated) {
    toast({
      title: 'Não foi possível atualizar',
      description: 'Tente novamente em instantes.',
      variant: 'destructive',
    })
    return
  }

  toast({
    title: 'Status atualizado',
    description: `Pedido #${updated.idOrder} agora está como ${formatOrderStatus(updated.statusOrder)}.`,
  })
  statusPopoverOpen.value = null
  await fetchOrders()
}

const handleDeleteOrder = async (orderId: number) => {
  deletingOrderId.value = orderId
  const deleted = await ordersApi.deleteOrder(orderId)
  deletingOrderId.value = null
  if (deleted === null) {
    toast({
      title: 'Não foi possível excluir',
      description: 'Verifique a conexão e tente novamente.',
      variant: 'destructive',
    })
    return
  }

  toast({
    title: 'Pedido removido',
    description: `Pedido #${orderId} foi excluído permanentemente.`,
  })
  deletePopoverOpen.value = null
  await fetchOrders()
}

onMounted(() => {
  fetchOrders()
  updateFilterState()
})
</script>

<template>
  <div class="flex flex-1 flex-col gap-6">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold">Pedidos</h1>
        <p class="text-sm text-muted-foreground">Monitore vendas, status e conciliações em tempo real.</p>
      </div>
      <Button as-child>
        <NuxtLink to="/sistema/pedidos/criar">Novo pedido</NuxtLink>
      </Button>
    </div>

    <div class="rounded-2xl border bg-card p-4 shadow-sm space-y-4">
      <div class="grid gap-3 md:grid-cols-2 lg:grid-cols-4">
        <div class="space-y-1.5 lg:col-span-2">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground">Busca rápida</p>
          <InputGroup class="h-12 bg-background">
            <InputGroupAddon class="gap-2 text-muted-foreground">
              <Search class="h-4 w-4" />
            </InputGroupAddon>
            <InputGroupInput
              v-model="searchTerm"
              placeholder="ID, cliente, checkout ou payment"
              class="h-12"
            />
          </InputGroup>
        </div>
        <div class="space-y-1.5">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground">Status</p>
          <InputGroup class="h-12 bg-background">
            <InputGroupAddon class="text-muted-foreground">
              <SlidersHorizontal class="h-4 w-4" />
            </InputGroupAddon>
            <select
              v-model="statusFilter"
              class="h-full flex-1 appearance-none bg-transparent pl-2 pr-8 text-sm font-medium text-foreground outline-none"
              aria-label="Filtrar por status"
            >
              <option value="all">Todos os status</option>
              <option value="pending">Pendente</option>
              <option value="paid">Pago</option>
              <option value="fulfilled">Pronto</option>
              <option value="picked_up">Retirado</option>
              <option value="canceled">Cancelado</option>
            </select>
          </InputGroup>
        </div>
        <div class="space-y-1.5">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground">Payment Intent</p>
          <InputGroup class="h-12 bg-background">
            <InputGroupAddon class="text-muted-foreground">
              <CreditCard class="h-4 w-4" />
            </InputGroupAddon>
            <InputGroupInput v-model="paymentFilter" placeholder="pi_123" class="h-12" />
          </InputGroup>
        </div>
        <div class="space-y-1.5">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground">Checkout</p>
          <InputGroup class="h-12 bg-background">
            <InputGroupAddon class="text-muted-foreground font-semibold">chk_</InputGroupAddon>
            <InputGroupInput v-model="checkoutFilter" placeholder="123" class="h-12" />
          </InputGroup>
        </div>
        <div class="space-y-1.5">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground">ID usuário</p>
          <InputGroup class="h-12 bg-background">
            <InputGroupAddon class="text-muted-foreground">
              <Hash class="h-4 w-4" />
            </InputGroupAddon>
            <InputGroupInput v-model="userFilter" type="number" min="1" placeholder="123" class="h-12" />
          </InputGroup>
        </div>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Pedido</TableHead>
            <TableHead>Cliente</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Pagamento</TableHead>
            <TableHead>Total</TableHead>
            <TableHead>Data</TableHead>
            <TableHead class="text-right">Ações</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <template v-if="loading">
            <TableRow v-for="index in 5" :key="index">
              <TableCell><Skeleton class="h-5 w-16" /></TableCell>
              <TableCell><Skeleton class="h-5 w-32" /></TableCell>
              <TableCell><Skeleton class="h-5 w-16" /></TableCell>
              <TableCell><Skeleton class="h-5 w-32" /></TableCell>
              <TableCell><Skeleton class="h-6 w-24" /></TableCell>
              <TableCell><Skeleton class="h-5 w-32" /></TableCell>
              <TableCell><Skeleton class="h-5 w-20" /></TableCell>
              <TableCell><Skeleton class="h-5 w-28" /></TableCell>
              <TableCell class="text-right"><Skeleton class="h-8 w-28 ml-auto" /></TableCell>
            </TableRow>
          </template>
          <template v-else-if="filteredList.length">
            <TableRow
              v-for="order in filteredList"
              :key="order.idOrder"
            >
              <TableCell>
                <div class="font-semibold">Pedido #{{ order.idOrder }}</div>
                <p class="text-xs text-muted-foreground">{{ order.checkoutId }}</p>
              </TableCell>
              <TableCell>
                <div class="font-semibold">{{ order.userName || `Cliente #${order.userId}` }}</div>
                <p class="text-xs text-muted-foreground">ID {{ order.userId }}</p>
              </TableCell>
              <TableCell>
                <span
                  class="inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-xs font-semibold"
                  :class="statusMeta[order.statusOrder].classes"
                >
                  <component :is="statusMeta[order.statusOrder].icon" class="h-3.5 w-3.5" />
                  {{ formatOrderStatus(order.statusOrder) }}
                </span>
              </TableCell>
              <TableCell class="text-sm">
                <div class="flex flex-col">
                  <span class="font-medium">{{ order.paymentIntent }}</span>
                  <span class="text-xs text-muted-foreground">{{ order.currencyOrder }}</span>
                </div>
              </TableCell>
              <TableCell class="font-semibold">
                {{ formatCurrencyFromCents(order.totalOrders) }}
              </TableCell>
              <TableCell class="text-sm text-muted-foreground">
                {{ formatDateTime(order.createdAt) }}
              </TableCell>
              <TableCell class="text-right">
                <div class="flex items-center justify-end gap-2">
                  <Button variant="ghost" size="icon-sm" as-child>
                    <NuxtLink :to="`/sistema/pedidos/${order.idOrder}`">
                      <Eye class="h-4 w-4" />
                    </NuxtLink>
                  </Button>
                  <Popover
                    :open="statusPopoverOpen === order.idOrder"
                    @update:open="(value) => { statusPopoverOpen = value ? order.idOrder : null }"
                  >
                    <PopoverTrigger as-child>
                      <Button variant="ghost" size="icon-sm">
                        <MoreHorizontal class="h-4 w-4" />
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent class="w-60 space-y-2">
                      <p class="text-sm font-semibold">Atualizar status</p>
                      <div class="space-y-2">
                        <button
                          v-for="option in statusOptions"
                          :key="option.value"
                          type="button"
                          class="flex w-full items-center justify-between rounded-lg border px-3 py-2 text-left text-sm transition"
                          :class="option.value === order.statusOrder
                            ? 'border-primary bg-primary/5'
                            : 'border-border hover:bg-muted/60'"
                          :disabled="updatingOrderId === order.idOrder && option.value === order.statusOrder"
                          @click="handleStatusUpdate(order.idOrder, option.value)"
                        >
                          <span>{{ option.label }}</span>
                          <CheckCircle2
                            v-if="option.value === order.statusOrder"
                            class="h-4 w-4 text-primary"
                          />
                        </button>
                      </div>
                    </PopoverContent>
                  </Popover>
                  <Popover
                    :open="deletePopoverOpen === order.idOrder"
                    @update:open="(value) => { deletePopoverOpen = value ? order.idOrder : null }"
                  >
                    <PopoverTrigger as-child>
                      <Button variant="ghost" size="icon-sm" class="text-destructive">
                        <Trash2 class="h-4 w-4" />
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent class="w-64 space-y-3">
                      <div class="text-sm font-semibold text-destructive">
                        Remover pedido #{{ order.idOrder }}?
                      </div>
                      <p class="text-xs text-muted-foreground">
                        Esta ação também exclui os itens vinculados. Não é possível desfazer.
                      </p>
                      <div class="flex gap-2">
                        <Button
                          type="button"
                          variant="ghost"
                          class="flex-1"
                          @click="deletePopoverOpen = null"
                        >
                          Cancelar
                        </Button>
                        <Button
                          type="button"
                          variant="destructive"
                          class="flex-1"
                          :disabled="deletingOrderId === order.idOrder"
                          @click="handleDeleteOrder(order.idOrder)"
                        >
                          {{ deletingOrderId === order.idOrder ? 'Removendo...' : 'Excluir' }}
                        </Button>
                      </div>
                    </PopoverContent>
                  </Popover>
                </div>
              </TableCell>
            </TableRow>
          </template>
          <TableEmpty v-else :colspan="7">
            <Empty>
              <EmptyHeader>
                <EmptyTitle>Nenhum pedido encontrado</EmptyTitle>
                <EmptyDescription>Tente ajustar os filtros.</EmptyDescription>
              </EmptyHeader>
            </Empty>
          </TableEmpty>
        </TableBody>
      </Table>
    </div>
  </div>
</template>
