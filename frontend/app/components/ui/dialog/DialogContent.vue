<script setup lang="ts">
import type { DialogContentEmits, DialogContentProps } from 'reka-ui'
import type { HTMLAttributes } from 'vue'
import { reactiveOmit } from '@vueuse/core'
import { X } from 'lucide-vue-next'
import {
  DialogClose as DialogPrimitiveClose,
  DialogContent as DialogPrimitiveContent,
  DialogOverlay,
  DialogPortal,
  useForwardPropsEmits,
} from 'reka-ui'
import { cn } from '@/lib/utils'

interface DialogContentExtended extends DialogContentProps {
  class?: HTMLAttributes['class']
  overlayClass?: HTMLAttributes['class']
}

defineOptions({
  inheritAttrs: false,
})

const props = defineProps<DialogContentExtended>()
const emits = defineEmits<DialogContentEmits>()

const delegatedProps = reactiveOmit(props, 'class', 'overlayClass')
const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
  <DialogPortal>
    <DialogOverlay
      data-slot="dialog-overlay"
      :class="cn('fixed inset-0 z-50 bg-black/50 backdrop-blur-sm data-[state=open]:animate-in data-[state=open]:fade-in data-[state=closed]:animate-out data-[state=closed]:fade-out', props.overlayClass)"
    />
    <DialogPrimitiveContent
      data-slot="dialog-content"
      :class="cn('fixed left-1/2 top-1/2 z-50 grid w-[calc(100%-2rem)] max-w-xl -translate-x-1/2 -translate-y-1/2 gap-4 rounded-2xl border border-border/70 bg-white p-6 shadow-2xl outline-none focus-visible:ring-2 focus-visible:ring-brand-cyan/40 sm:p-8', props.class)"
      v-bind="{ ...forwarded, ...$attrs }"
    >
      <slot />

      <DialogPrimitiveClose
        class="absolute right-4 top-4 rounded-full border border-transparent p-2 text-muted-foreground transition hover:text-foreground focus-visible:ring-2 focus-visible:ring-brand-cyan/60 focus-visible:outline-none"
      >
        <X class="h-4 w-4" />
        <span class="sr-only">Fechar</span>
      </DialogPrimitiveClose>
    </DialogPrimitiveContent>
  </DialogPortal>
</template>
