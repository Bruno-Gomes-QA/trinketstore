<script setup lang="ts">
import type { DialogCloseProps } from 'reka-ui'
import type { HTMLAttributes } from 'vue'
import { DialogClose as DialogPrimitiveClose } from 'reka-ui'
import { cn } from '@/lib/utils'

const props = defineProps<DialogCloseProps & { class?: HTMLAttributes['class'] }>()
</script>

<template>
  <DialogPrimitiveClose
    data-slot="dialog-close"
    v-bind="props"
    :class="cn('inline-flex items-center justify-center rounded-md border border-border bg-background px-3 py-1.5 text-sm font-medium text-foreground transition hover:border-brand-cyan hover:text-brand-cyan focus-visible:ring-2 focus-visible:ring-brand-cyan/50 focus-visible:outline-none', props.class)"
  >
    <slot />
  </DialogPrimitiveClose>
</template>
