<script lang="ts" setup>
import type { ComputedRef } from 'vue'
import type { DrawerRootEmits, DrawerRootProps } from "vaul-vue"
import { useForwardPropsEmits } from "reka-ui"
import { DrawerRoot } from "vaul-vue"

const props = withDefaults(defineProps<DrawerRootProps>(), {
  shouldScaleBackground: true,
})

const emits = defineEmits<DrawerRootEmits>()

const forwarded = useForwardPropsEmits(props, emits) as ComputedRef<Record<string, any>>

const resolveForwardedProps = () => forwarded.value
</script>

<template>
  <DrawerRoot
    data-slot="drawer"
    v-bind="resolveForwardedProps()"
  >
    <slot />
  </DrawerRoot>
</template>
