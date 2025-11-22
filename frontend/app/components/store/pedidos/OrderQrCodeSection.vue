<template>
  <div class="rounded-2xl border border-brand-cyan/30 bg-gradient-to-b from-white to-sky-50 p-4 text-center">
    <p class="text-sm font-semibold text-foreground">QR Code PIX</p>
    <div class="mt-3 flex justify-center">
      <img
        v-if="qrUrl"
        :src="qrUrl"
        alt="QR Code do PIX"
        class="w-full max-w-[240px] rounded-2xl border border-border/50 bg-white p-2 shadow-inner sm:max-w-xs sm:p-3"
      >
      <div v-else class="flex h-48 w-full max-w-[240px] items-center justify-center rounded-2xl border border-dashed border-border/60 bg-muted/60 text-sm text-muted-foreground sm:h-60 sm:max-w-xs">
        Sem QR code
      </div>
    </div>
    <p class="mt-3 text-xs text-muted-foreground">
      Escaneie para pagar
    </p>
  </div>
</template>

<script setup lang="ts">
interface Props {
  qrUrl: string
}

defineProps<Props>()
</script>
