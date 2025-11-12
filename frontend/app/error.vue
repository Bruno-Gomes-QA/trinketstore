<script setup lang="ts">
import { AlertCircle, Home, ArrowLeft } from 'lucide-vue-next'

const props = defineProps({
  error: Object
})

const router = useRouter()

const showLogoFallback = ref(false)

const goBack = () => {
  router.back()
}

const goHome = () => {
  navigateTo('/dashboard')
}

const is404 = computed(() => props.error?.statusCode === 404)
const errorTitle = computed(() => {
  if (is404.value) return 'Página não encontrada'
  return 'Algo deu errado'
})

const errorMessage = computed(() => {
  if (is404.value) {
    return 'Desculpe, não conseguimos encontrar a página que você está procurando. Ela pode ter sido movida ou não existe mais.'
  }
  return props.error?.message || 'Ocorreu um erro inesperado. Por favor, tente novamente.'
})
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100 dark:from-slate-950 dark:to-slate-900 p-4">
    <div class="w-full max-w-2xl">
      <div class="relative overflow-hidden rounded-2xl border border-border bg-card shadow-2xl backdrop-blur-sm">
        <div class="absolute inset-0 bg-gradient-to-br from-[#a4f380]/5 via-transparent to-[#80d3e4]/5" />
        
        <div class="relative p-8 md:p-12 text-center space-y-6">
          <div class="flex justify-center">
            <div class="relative">
              <div class="absolute inset-0 rounded-full bg-red-500/20 blur-xl" />
              <div class="relative rounded-full bg-gradient-to-br from-red-500/10 to-red-600/10 p-6 border border-red-500/20">
                <AlertCircle class="h-16 w-16 text-red-500" />
              </div>
            </div>
          </div>
          
          <div class="space-y-2">
            <h1 class="text-6xl font-bold text-foreground">
              {{ error?.statusCode || '500' }}
            </h1>
            <h2 class="text-2xl font-semibold text-foreground">
              {{ errorTitle }}
            </h2>
          </div>
          
          <p class="text-muted-foreground max-w-md mx-auto">
            {{ errorMessage }}
          </p>
          
          <div class="flex flex-col sm:flex-row gap-3 justify-center pt-4">
            <button
              @click="goBack"
              class="inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-lg text-sm font-medium transition-all h-11 px-6 border border-input bg-background hover:bg-accent hover:text-accent-foreground shadow-sm"
            >
              <ArrowLeft class="h-4 w-4" />
              Voltar
            </button>
            
            <button
              @click="goHome"
              class="inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-lg text-sm font-medium transition-all h-11 px-6 bg-gradient-to-r from-[#a4f380] to-[#80d3e4] text-stone-900 font-semibold hover:from-[#8aef5d] hover:to-[#5dc5db] shadow-lg shadow-[#a4f380]/25"
            >
              <Home class="h-4 w-4" />
              Ir para Dashboard
            </button>
          </div>
          
          <div class="pt-6 border-t border-border">
            <p class="text-xs text-muted-foreground">
              Se o problema persistir, entre em contato com o 
              <a 
                href="https://wa.me/5511967790534" 
                target="_blank" 
                rel="noopener noreferrer"
                class="text-[#a4f380] hover:text-[#8aef5d] hover:underline font-medium"
              >
                suporte
              </a>
            </p>
          </div>
        </div>
      </div>
      
      <div class="mt-6 text-center">
        <img
          v-if="!showLogoFallback"
          src="/logo_square.png" 
          alt="Trinket Store" 
          class="h-12 mx-auto opacity-50"
          @error="showLogoFallback = true"
        />
        <!-- Fallback quando logo não existe -->
        <div v-if="showLogoFallback" class="flex justify-center">
          <div class="flex items-center justify-center h-12 w-12 rounded-lg bg-gradient-to-br from-[#a4f380] to-[#80d3e4] opacity-50">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-7 h-7 text-stone-900" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"/>
              <path d="M3 6h18"/>
              <path d="M16 10a4 4 0 0 1-8 0"/>
            </svg>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
