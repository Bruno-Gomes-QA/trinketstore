<script setup lang="ts">
import type { DialogTitleProps } from 'reka-ui'
import type { HTMLAttributes } from 'vue'
import { DialogTitle as DialogPrimitiveTitle } from 'reka-ui'
import { cn } from '@/lib/utils'

const props = defineProps<DialogTitleProps & { class?: HTMLAttributes['class'] }>()
</script>

<template>
  <DialogPrimitiveTitle
    data-slot="dialog-title"
    v-bind="props"
    :class="cn('text-2xl font-semibold leading-tight text-foreground tracking-tight', props.class)"
  >
    <slot />
  </DialogPrimitiveTitle>
</template>
