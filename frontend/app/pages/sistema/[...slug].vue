<script setup lang="ts">
import { computed } from 'vue'
import { ArrowLeft, Compass, RefreshCw } from 'lucide-vue-next'
import { Button } from '~/components/ui/button'

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Área não encontrada',
})

const route = useRoute()
const router = useRouter()

const requestedPath = computed(() => {
  const slug = route.params.slug
  if (!slug) return ''
  return Array.isArray(slug) ? slug.join('/') : String(slug)
})

const goHome = () => {
  router.push('/sistema/dashboard')
}

const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="flex flex-1 items-center justify-center py-10">
    <div class="w-full max-w-2xl space-y-8 rounded-3xl border bg-card/90 p-8 text-center shadow-2xl backdrop-blur">
      <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-[#a4f380] via-[#80d3e4] to-[#5dc5db] text-stone-900 shadow-inner shadow-[#5dc5db]/40">
        <Compass class="h-8 w-8" />
      </div>
      <div class="space-y-3">
        <p class="text-xs uppercase tracking-[0.4em] text-muted-foreground">Filtro sem rota</p>
        <h1 class="text-3xl font-semibold text-foreground">
          Não encontramos “/sistema/{{ requestedPath || '...' }}”
        </h1>
        <p class="text-sm text-muted-foreground">
          O atalho digitado não corresponde a nenhuma página de gestão disponível. Revise o termo ou volte para uma das telas principais.
        </p>
      </div>
      <div class="rounded-2xl border border-dashed bg-muted/40 p-4 text-left">
        <p class="text-[11px] font-semibold uppercase tracking-[0.3em] text-muted-foreground">Atalho solicitado</p>
        <p class="mt-1 font-mono text-lg text-foreground">
          {{ requestedPath ? `/sistema/${requestedPath}` : '/sistema' }}
        </p>
      </div>
      <div class="flex flex-col gap-3 sm:flex-row sm:justify-center">
        <Button variant="outline" class="gap-2" @click="goBack">
          <ArrowLeft class="h-4 w-4" />
          Voltar
        </Button>
        <Button class="gap-2" @click="goHome">
          <RefreshCw class="h-4 w-4" />
          Ir para o dashboard
        </Button>
      </div>
    </div>
  </div>
</template>
