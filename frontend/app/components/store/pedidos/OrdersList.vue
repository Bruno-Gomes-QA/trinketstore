<template>
  <div class="space-y-3">
    <button
      v-for="order in orders"
      :key="order.idOrder"
      type="button"
      class="w-full rounded-2xl border border-border/60 bg-white p-4 text-left shadow-sm transition hover:border-brand-cyan hover:shadow-md"
      :class="selectedOrderId === order.idOrder ? 'border-brand-cyan ring-2 ring-brand-cyan/20' : ''"
      @click="$emit('select', order.idOrder)"
    >
      <div class="flex items-start justify-between gap-3">
        <div class="flex-1 space-y-2">
          <div class="flex items-center gap-2">
            <span class="text-sm font-bold text-foreground">Pedido #{{ order.idOrder }}</span>
            <span
              class="inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide"
              :class="getStatusBadge(order.statusOrder)"
            >
              {{ getStatusLabel(order.statusOrder) }}
            </span>
          </div>
          <div class="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-muted-foreground">
            <span>{{ formatDate(order.createdAt) }}</span>
            <span class="font-semibold text-foreground">{{ formatCurrency(order.totalOrders) }}</span>
          </div>
        </div>
        <div class="flex-shrink-0">
          <component
            :is="getStatusIcon(order.statusOrder)"
            class="h-5 w-5"
            :class="getStatusIconClass(order.statusOrder)"
          />
        </div>
      </div>
    </button>
  </div>
</template>

<script setup lang="ts">
import { CheckCircle2, Clock3, AlertTriangle } from 'lucide-vue-next'
import type { OrderResponse } from '~/types/orders'

interface Props {
  orders: OrderResponse[]
  selectedOrderId?: number | null
}

defineProps<Props>()
defineEmits<{
  select: [orderId: number]
}>()

const { formatCurrencyFromCents } = useFormatters()

const formatCurrency = (amount: number) => formatCurrencyFromCents(amount ?? 0)
const formatDate = (iso?: string | null) => {
  if (!iso) return '—'
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return '—'
  return date.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

const getStatusLabel = (status?: string) => {
  const normalized = (status || '').toLowerCase()
  if (normalized === 'paid') return 'Pagamento aprovado'
  if (normalized === 'picked_up') return 'Pedido retirado'
  if (normalized === 'canceled') return 'Pagamento cancelado'
  return 'Aguardando pagamento'
}

const getStatusBadge = (status?: string) => {
  const normalized = (status || '').toLowerCase()
  if (normalized === 'paid' || normalized === 'picked_up') {
    return 'bg-emerald-100 text-emerald-800 border border-emerald-200'
  }
  if (normalized === 'canceled') {
    return 'bg-destructive/10 text-destructive border border-destructive/20'
  }
  return 'bg-amber-100 text-amber-800 border border-amber-200'
}

const getStatusIcon = (status?: string) => {
  const normalized = (status || '').toLowerCase()
  if (normalized === 'paid' || normalized === 'picked_up') return CheckCircle2
  if (normalized === 'canceled') return AlertTriangle
  return Clock3
}

const getStatusIconClass = (status?: string) => {
  const normalized = (status || '').toLowerCase()
  if (normalized === 'paid' || normalized === 'picked_up') return 'text-emerald-500'
  if (normalized === 'canceled') return 'text-destructive'
  return 'text-amber-500'
}
</script>
