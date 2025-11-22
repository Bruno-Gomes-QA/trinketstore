<template>
  <div class="rounded-2xl border border-amber-200 bg-amber-50/80 p-4">
    <p class="text-sm font-semibold text-foreground">Código PIX</p>
    <div class="mt-2 max-h-32 overflow-x-auto overflow-y-auto rounded-xl border border-amber-200 bg-white/90 p-3">
      <p class="font-mono text-[10px] text-foreground break-all sm:text-xs">{{ pixCode }}</p>
    </div>
    <button
      type="button"
      class="mt-3 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-emerald-500 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-emerald-500/30 transition hover:bg-emerald-600 disabled:opacity-60"
      :disabled="!pixCode"
      @click="handleCopy"
    >
      <Copy class="h-4 w-4" />
      {{ copied ? 'Copiado!' : 'Copiar código' }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { Copy } from 'lucide-vue-next'
import { useClipboard } from '@vueuse/core'

interface Props {
  pixCode: string
  pixQrUrl: string
}

const props = defineProps<Props>()

const { copy, copied } = useClipboard()

const handleCopy = async () => {
  if (!props.pixCode) return
  await copy(props.pixCode)
}
</script>
