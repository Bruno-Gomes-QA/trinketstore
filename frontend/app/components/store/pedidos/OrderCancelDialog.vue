<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="sm:max-w-md">
      <DialogHeader>
        <DialogTitle>Cancelar pedido</DialogTitle>
        <DialogDescription>
          Tem certeza que deseja cancelar seu pedido?
        </DialogDescription>
      </DialogHeader>
      <div class="space-y-4 py-4">
        <div class="rounded-2xl border border-amber-200 bg-amber-50 p-4">
          <div class="flex items-start gap-3">
            <AlertTriangle class="h-5 w-5 flex-shrink-0 text-amber-600" />
            <div class="flex-1">
              <p class="text-sm font-semibold text-foreground">Atenção</p>
              <p class="mt-1 text-xs text-muted-foreground">
                Esta ação não poderá ser desfeita. Você precisará criar um novo pedido caso deseje continuar com a compra.
              </p>
            </div>
          </div>
        </div>
      </div>
      <div class="flex flex-col gap-3 sm:flex-row sm:justify-end">
        <button
          type="button"
          class="inline-flex items-center justify-center rounded-xl border border-border px-4 py-2 text-sm font-semibold text-foreground transition hover:bg-muted"
          @click="$emit('update:open', false)"
        >
          Voltar
        </button>
        <button
          type="button"
          class="inline-flex items-center justify-center gap-2 rounded-xl bg-destructive px-4 py-2 text-sm font-semibold text-white shadow-md transition hover:bg-destructive/90 disabled:opacity-60"
          :disabled="canceling"
          @click="$emit('confirm')"
        >
          <AlertTriangle class="h-4 w-4" />
          Confirmar cancelamento
        </button>
      </div>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { AlertTriangle } from 'lucide-vue-next'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '~/components/ui/dialog'

interface Props {
  open: boolean
  canceling?: boolean
}

defineProps<Props>()
defineEmits<{
  'update:open': [value: boolean]
  confirm: []
}>()
</script>
