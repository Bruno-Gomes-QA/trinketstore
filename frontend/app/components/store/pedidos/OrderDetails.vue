<template>
  <section class="rounded-3xl border border-border/60 bg-white/95 p-4 shadow-lg shadow-sky-100/80 sm:p-5">
    <div class="flex items-start justify-between gap-3 border-b border-border/50 pb-4">
      <div>
        <h2 class="text-lg font-bold text-foreground">
          Pedido #{{ order.idOrder }}
        </h2>
        <p class="text-xs text-muted-foreground">
          {{ formatDate(order.createdAt) }}
        </p>
      </div>
      <button
        v-if="order.statusOrder === 'pending'"
        type="button"
        class="inline-flex items-center justify-center gap-1 rounded-xl border border-destructive/40 px-3 py-1.5 text-xs font-semibold text-destructive transition hover:bg-destructive/10 disabled:opacity-60"
        :disabled="canceling"
        @click="$emit('cancel')"
      >
        <AlertTriangle class="h-3.5 w-3.5" />
        <span class="hidden sm:inline">Cancelar</span>
      </button>
    </div>

    <div class="mt-4 space-y-4">
      <div class="space-y-3">
        <div class="rounded-2xl border border-brand-cyan/30 bg-gradient-to-r from-sky-50 via-white to-emerald-50 p-4">
          <div class="space-y-3">
            <div>
              <p class="text-sm font-semibold text-foreground">{{ statusMeta.label }}</p>
              <p class="mt-1 text-xs text-muted-foreground">
                {{ statusMeta.description }}
              </p>
            </div>
            <div class="flex items-center justify-between border-t border-brand-cyan/20 pt-3">
              <span class="text-xs font-medium text-muted-foreground">Valor total</span>
              <span class="text-xl font-bold text-foreground">{{ formatCurrency(order.totalOrders) }}</span>
            </div>
          </div>
        </div>

        <OrderPaymentSection
          v-if="order.statusOrder === 'pending'"
          :pix-code="order.pickupQrToken ?? ''"
          :pix-qr-url="pixQrUrl"
        />

        <OrderItemsList :items="itemsWithDetails" />
      </div>

      <div class="space-y-3">
        <OrderQrCodeSection
          v-if="order.statusOrder === 'pending'"
          :qr-url="pixQrUrl"
        />

        <NuxtLink
          to="/carrinho"
          class="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-emerald-500 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-emerald-500/30 transition hover:bg-emerald-600 hover:shadow-emerald-500/40"
        >
          <ShoppingCart class="h-4 w-4" />
          Fazer nova compra
        </NuxtLink>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { AlertTriangle, ShoppingCart } from 'lucide-vue-next'
import type { OrderResponse } from '~/types/orders'
import type { ProductResponse } from '~/types/products'
import OrderPaymentSection from './OrderPaymentSection.vue'
import OrderItemsList from './OrderItemsList.vue'
import OrderQrCodeSection from './OrderQrCodeSection.vue'

interface Props {
  order: OrderResponse
  productCache: Record<number, ProductResponse>
  canceling?: boolean
}

const props = defineProps<Props>()
defineEmits<{
  cancel: []
}>()

const { formatCurrencyFromCents } = useFormatters()
const LOCAL_RETRIEVAL = 'Sala 4, à direita após a passarela'

const formatCurrency = (amount: number) => formatCurrencyFromCents(amount ?? 0)
const formatDate = (iso?: string | null) => {
  if (!iso) return '—'
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return '—'
  return date.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

const statusMeta = computed(() => {
  const normalized = (props.order.statusOrder || '').toLowerCase()
  if (normalized === 'paid') {
    return {
      label: 'Pagamento aprovado',
      description: `Seu PIX foi confirmado. Retire seu pedido na ${LOCAL_RETRIEVAL}.`,
      badge: 'bg-emerald-100 text-emerald-800 border border-emerald-200',
    }
  }
  if (normalized === 'picked_up') {
    return {
      label: 'Pedido retirado',
      description: 'Agradecemos a compra! Até a próxima.',
      badge: 'bg-emerald-100 text-emerald-800 border border-emerald-200',
    }
  }
  if (normalized === 'canceled') {
    return {
      label: 'Pagamento cancelado',
      description: '',
      badge: 'bg-destructive/10 text-destructive border border-destructive/20',
    }
  }
  return {
    label: 'Aguardando pagamento',
    description: 'Use o QR Code ou o código copia e cola para pagar.',
    badge: 'bg-amber-100 text-amber-800 border border-amber-200',
  }
})

const pixQrUrl = computed(() => {
  if (props.order.statusOrder !== 'pending') {
    return ''
  }
  if (props.order.pixQrCodeBase64) {
    return `data:image/png;base64,${props.order.pixQrCodeBase64}`
  }
  if (props.order.pickupQrToken) {
    return `https://api.qrserver.com/v1/create-qr-code/?size=280x280&data=${encodeURIComponent(props.order.pickupQrToken)}`
  }
  return ''
})

const itemsWithDetails = computed(() => {
  if (!props.order.items) return []
  return props.order.items.map((item) => {
    const product = props.productCache[item.productId]
    return {
      ...item,
      nome: product?.nomeProduct ?? `Produto #${item.productId}`,
      imagem: product?.imagemurlProduct ?? null,
    }
  })
})
</script>
