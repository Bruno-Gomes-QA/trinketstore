<template>
  <div class="rounded-2xl border border-border/60 bg-white/90 p-4">
    <p class="text-sm font-semibold text-foreground">Itens do pedido</p>
    <div class="mt-3 grid gap-3">
      <div
        v-for="item in items"
        :key="item.idItems"
        class="grid grid-cols-[auto_1fr_auto] items-center gap-3 rounded-xl border border-border/60 bg-white/70 p-3"
      >
        <div class="h-14 w-14 overflow-hidden rounded-lg border border-border/60 bg-muted">
          <img v-if="item.imagem" :src="item.imagem" :alt="item.nome" class="h-full w-full object-cover">
          <div v-else class="flex h-full w-full items-center justify-center text-[10px] text-muted-foreground">Sem foto</div>
        </div>
        <div>
          <p class="text-sm font-semibold text-foreground">{{ item.nome }}</p>
          <p class="text-xs text-muted-foreground">#{{ item.productId }}</p>
        </div>
        <div class="text-right">
          <p class="text-sm font-semibold text-foreground">{{ item.qtyItems }} un</p>
          <p class="text-xs text-muted-foreground">{{ formatCurrency(item.subtotalAmount ?? 0) }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { OrderItemEntity } from '~/types/order-items'

interface ItemWithDetails extends OrderItemEntity {
  nome: string
  imagem: string | null
}

interface Props {
  items: ItemWithDetails[]
}

defineProps<Props>()

const { formatCurrencyFromCents } = useFormatters()
const formatCurrency = (amount: number) => formatCurrencyFromCents(amount ?? 0)
</script>
