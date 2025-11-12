<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Separator } from '~/components/ui/separator'
import { SidebarTrigger } from '~/components/ui/sidebar'

const route = useRoute()

const pageTitle = computed(() => {
  const metaTitle = route.meta?.pageTitle as string | undefined
  if (metaTitle) return metaTitle
  
  if (route.path === '/dashboard') {
    return 'Dashboard'
  }
  
  return 'Aplicação'
})
</script>

<template>
  <header class="flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-[[data-collapsible=icon]]/sidebar-wrapper:h-12">
    <div class="flex items-center gap-3 px-4">
      <SidebarTrigger class="-ml-1" />
      <Separator orientation="vertical" class="mr-2 h-4" />
      <div class="flex flex-col">
        <span class="text-xs uppercase tracking-wide text-muted-foreground">
          Página atual
        </span>
        <h1 class="text-lg font-semibold leading-tight text-foreground">
          {{ pageTitle }}
        </h1>
      </div>
    </div>
  </header>
</template>
