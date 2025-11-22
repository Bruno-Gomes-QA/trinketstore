<template>
  <div class="rounded-3xl border border-brand-cyan/30 bg-white/95 p-4 shadow-[0_15px_45px_rgba(14,165,233,0.12)] sm:p-5">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-[0.3em] text-brand-cyan">Pedidos</p>
        <h2 class="text-xl font-bold text-foreground">Suas compras</h2>
        <p class="mt-1 text-sm text-muted-foreground">
          Toque em um pedido para ver detalhes
        </p>
      </div>
      <span class="w-fit rounded-full bg-brand-cyan/10 px-3 py-1 text-xs font-semibold text-brand-cyan">
        {{ ordersCount }} pedido{{ ordersCount !== 1 ? 's' : '' }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  ordersCount: number
}

defineProps<Props>()
</script>
