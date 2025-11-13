<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { Eye, Search } from 'lucide-vue-next'
import { Button } from '~/components/ui/button'
import { Input } from '~/components/ui/input'
import { Label } from '~/components/ui/label'
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
  pageTitle: 'Pedidos',
})

const { formatCurrencyFromCents, formatDateTime, formatOrderStatus } = useFormatters()
const { filteredOrders, loading, fetchOrders } = useOrdersList()

const statusFilter = ref<'all' | import('~/types/orders').OrderStatus>('all')
const paymentFilter = ref('')
const checkoutFilter = ref('')
const userFilter = ref('')

const filteredList = computed(() => filteredOrders.value)

watch([statusFilter, paymentFilter, checkoutFilter, userFilter], ([status, payment, checkout, user]) => {
  fetchOrders({
    status: status === 'all' ? undefined : status,
    paymentIntent: payment || undefined,
    checkoutId: checkout || undefined,
    userId: user ? Number(user) : undefined,
  })
})

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="flex flex-1 flex-col gap-6">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold">Pedidos</h1>
        <p class="text-sm text-muted-foreground">Acompanhe vendas recentes.</p>
      </div>
      <Button as-child>
        <NuxtLink to="/sistema/pedidos/criar">Novo pedido</NuxtLink>
      </Button>
    </div>

    <div class="rounded-2xl border bg-card p-4 shadow-sm space-y-4">
      <div class="grid gap-4 md:grid-cols-4">
        <div>
          <Label class="text-xs text-muted-foreground uppercase">Status</Label>
          <select v-model="statusFilter" class="mt-1 h-10 w-full rounded-lg border px-3 text-sm">
            <option value="all">Todos</option>
            <option value="pending">Pendente</option>
            <option value="paid">Pago</option>
            <option value="canceled">Cancelado</option>
            <option value="fulfilled">Pronto</option>
            <option value="picked_up">Retirado</option>
          </select>
        </div>
        <div>
          <Label class="text-xs text-muted-foreground uppercase">Payment Intent</Label>
          <Input v-model="paymentFilter" placeholder="pi_123" class="mt-1" />
        </div>
        <div>
          <Label class="text-xs text-muted-foreground uppercase">Checkout ID</Label>
          <Input v-model="checkoutFilter" placeholder="chk_123" class="mt-1" />
        </div>
        <div>
          <Label class="text-xs text-muted-foreground uppercase">ID Usuário</Label>
          <Input v-model="userFilter" placeholder="1" type="number" min="1" class="mt-1" />
        </div>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>ID</TableHead>
            <TableHead>Cliente</TableHead>
            <TableHead>Status</TableHead>
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
              <TableCell><Skeleton class="h-5 w-20" /></TableCell>
              <TableCell><Skeleton class="h-5 w-20" /></TableCell>
              <TableCell><Skeleton class="h-5 w-32" /></TableCell>
              <TableCell class="text-right"><Skeleton class="h-8 w-20 ml-auto" /></TableCell>
            </TableRow>
          </template>
          <template v-else-if="filteredList.length">
            <TableRow
              v-for="order in filteredList"
              :key="order.idOrder"
            >
              <TableCell class="font-semibold">#{{ order.idOrder }}</TableCell>
              <TableCell>Cliente #{{ order.userId }}</TableCell>
              <TableCell>
                <span class="inline-flex rounded-full bg-muted px-2 py-0.5 text-xs font-semibold">
                  {{ formatOrderStatus(order.statusOrder) }}
                </span>
              </TableCell>
              <TableCell>{{ formatCurrencyFromCents(order.totalOrders) }}</TableCell>
              <TableCell>{{ formatDateTime(order.createdAt) }}</TableCell>
              <TableCell class="text-right">
                <Button variant="ghost" size="icon-sm" as-child>
                  <NuxtLink :to="`/sistema/pedidos/${order.idOrder}`">
                    <Eye class="h-4 w-4" />
                  </NuxtLink>
                </Button>
              </TableCell>
            </TableRow>
          </template>
          <TableEmpty v-else :colspan="6">
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
