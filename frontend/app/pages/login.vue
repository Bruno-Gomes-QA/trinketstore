<template>
  <div class="relative flex min-h-screen items-center justify-center overflow-hidden py-12">
    <img
      src="/login-one-background.png"
      alt="Background"
      class="absolute inset-0 z-0 h-full w-full object-cover"
    />

    <div class="relative z-10 w-full max-w-5xl px-4">
      <div class="group relative mx-auto overflow-hidden rounded-[26px] border border-white/10 bg-white/5 shadow-2xl backdrop-blur-xl">
        <div class="relative grid md:grid-cols-2">
          <!-- Seção Esquerda - Hero -->
          <section class="flex flex-col justify-between gap-8 bg-slate-900/95 backdrop-blur-sm p-8 md:p-12">
            <div class="flex flex-col gap-10">
              <img
                v-if="!showLogoFallback"
                src="/logo_full.png"
                alt="Trinket Store"
                class="h-auto w-[440px]"
                @error="showLogoFallback = true"
              />
              <!-- Fallback quando logo não existe -->
              <div v-if="showLogoFallback" class="flex items-center gap-3">
                <div class="flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-[#a4f380] to-[#80d3e4] shadow-lg">
                  <svg xmlns="http://www.w3.org/2000/svg" class="w-9 h-9 text-stone-900" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"/>
                    <path d="M3 6h18"/>
                    <path d="M16 10a4 4 0 0 1-8 0"/>
                  </svg>
                </div>
                <span class="text-4xl font-bold bg-gradient-to-r from-[#a4f380] via-[#80d3e4] to-[#f6d95c] bg-clip-text text-transparent">
                  Trinket Store
                </span>
              </div>
              <div class="space-y-6">
                <p class="text-base text-gray-200 md:text-lg leading-relaxed">
                  Plataforma completa para gerenciar seu e-commerce de forma profissional e eficiente.
                </p>
                
                <div class="space-y-3">
                  <div class="flex items-start gap-3">
                    <div class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-[#a4f380] to-[#80d3e4] mt-0.5">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5 text-stone-900" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                        <polyline points="20 6 9 17 4 12"/>
                      </svg>
                    </div>
                    <div>
                      <p class="font-medium text-white">Catálogo de Produtos</p>
                      <p class="text-sm text-gray-400">Organize produtos com imagens, preços e categorias</p>
                    </div>
                  </div>

                  <div class="flex items-start gap-3">
                    <div class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-[#a4f380] to-[#80d3e4] mt-0.5">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5 text-stone-900" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                        <polyline points="20 6 9 17 4 12"/>
                      </svg>
                    </div>
                    <div>
                      <p class="font-medium text-white">Pagamentos Integrados</p>
                      <p class="text-sm text-gray-400">Receba pagamentos via Stripe com segurança</p>
                    </div>
                  </div>

                  <div class="flex items-start gap-3">
                    <div class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-[#a4f380] to-[#80d3e4] mt-0.5">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5 text-stone-900" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                        <polyline points="20 6 9 17 4 12"/>
                      </svg>
                    </div>
                    <div>
                      <p class="font-medium text-white">QR Code para Retirada</p>
                      <p class="text-sm text-gray-400">Geração automática e validação única de códigos</p>
                    </div>
                  </div>

                  <div class="flex items-start gap-3">
                    <div class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-[#a4f380] to-[#80d3e4] mt-0.5">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5 text-stone-900" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                        <polyline points="20 6 9 17 4 12"/>
                      </svg>
                    </div>
                    <div>
                      <p class="font-medium text-white">Dashboard & Métricas</p>
                      <p class="text-sm text-gray-400">Acompanhe vendas, ticket médio e mais em tempo real</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- Seção Direita - Formulário -->
          <section class="relative flex flex-col justify-center bg-white backdrop-blur-sm p-8 md:p-12">
            <div class="pointer-events-none absolute inset-0 bg-gradient-to-br from-[#a4f380]/10 via-[#80d3e4]/5 to-transparent opacity-80" />
            <div class="relative z-10 space-y-8">
              <header class="space-y-4">
                <div class="flex justify-end">
                  <span class="rounded-full bg-gradient-to-r from-[#a4f380]/20 to-[#80d3e4]/20 px-3 py-1 text-xs font-medium text-slate-900 shadow-sm backdrop-blur-sm border border-slate-200">
                    Painel Administrativo
                  </span>
                </div>
                <div class="space-y-1">
                  <h2 class="text-2xl font-semibold text-slate-900">
                    Acessar Painel
                  </h2>
                  <p class="text-sm text-slate-600">
                    Entre com suas credenciais para gerenciar a loja.
                  </p>
                </div>
              </header>

              <form class="space-y-5" @submit.prevent="handleSubmit">
                <div class="space-y-2">
                  <label class="text-sm font-medium text-slate-900" for="email">
                    E-mail
                  </label>
                  <input
                    id="email"
                    v-model="formData.email"
                    type="email"
                    placeholder="user@example.com"
                    autocomplete="email"
                    :disabled="isLoading"
                    class="flex h-10 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#a4f380] focus-visible:border-transparent disabled:cursor-not-allowed disabled:opacity-50 transition-all"
                  />
                </div>
                <div class="space-y-2">
                  <label class="text-sm font-medium text-slate-900" for="password">
                    Senha
                  </label>
                  <input
                    id="password"
                    v-model="formData.password"
                    type="password"
                    placeholder="••••••••"
                    autocomplete="current-password"
                    :disabled="isLoading"
                    class="flex h-10 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#a4f380] focus-visible:border-transparent disabled:cursor-not-allowed disabled:opacity-50 transition-all"
                  />
                </div>
                <p 
                  v-if="error" 
                  class="text-sm font-medium text-red-600" 
                  role="alert"
                >
                  {{ error }}
                </p>
                <button
                  type="submit"
                  :disabled="isLoading"
                  class="w-full h-10 rounded-md bg-gradient-to-r from-[#a4f380] to-[#80d3e4] text-stone-900 font-semibold shadow-lg shadow-[#a4f380]/50 transition-all hover:shadow-[#a4f380]/70 hover:from-[#8aef5d] hover:to-[#5dc5db] disabled:pointer-events-none disabled:opacity-50"
                >
                  {{ isLoading ? 'Entrando...' : 'Entrar' }}
                </button>
              </form>

              <footer class="text-xs text-slate-500 space-y-1">
                <p>Acesso restrito a administradores do Trinket Store.</p>
                <p>
                  Precisa de ajuda? <a href="https://wa.me/5511967790534" target="_blank" rel="noopener noreferrer" class="text-slate-700 hover:text-slate-900 hover:underline transition-colors">Contate o suporte</a>.
                </p>
              </footer>
            </div>
          </section>
        </div>
      </div>
    </div>

    <!-- Modal para Clientes -->
    <div
      v-if="showCustomerModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
      @click="redirectToStore"
    >
      <div
        class="relative mx-4 max-w-md rounded-2xl border border-white/10 bg-white p-8 shadow-2xl"
        @click.stop
      >
        <div class="flex flex-col items-center gap-4 text-center">
          <div class="flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-[#f6d95c] to-[#a4f380]">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-stone-900" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"/>
              <path d="M3 6h18"/>
              <path d="M16 10a4 4 0 0 1-8 0"/>
            </svg>
          </div>
          
          <div class="space-y-2">
            <h3 class="text-2xl font-bold text-stone-900">
              Ops! Você é um cliente! 🛍️
            </h3>
            <p class="text-sm text-stone-600">
              Este é o painel administrativo. Clientes devem acessar nossa loja online.
            </p>
          </div>

          <button
            @click="redirectToStore"
            class="w-full rounded-lg bg-gradient-to-r from-[#a4f380] to-[#80d3e4] px-6 py-3 font-semibold text-stone-900 shadow-lg transition-all hover:shadow-xl hover:from-[#8aef5d] hover:to-[#5dc5db]"
          >
            Ir para a Loja
          </button>
          
          <p class="text-xs text-stone-500">
            Você será redirecionado em instantes...
          </p>
        </div>
      </div>
    </div>

    <!-- Server Status Badge - Canto inferior direito -->
    <div class="fixed bottom-6 right-6 z-50">
      <div
        :class="[
          'flex items-center gap-2 px-4 py-2 text-xs font-medium shadow-lg backdrop-blur-sm transition-all duration-300 rounded-lg border',
          serverStatus === 'up' 
            ? 'border-emerald-500/30 bg-emerald-500/10 text-emerald-300 hover:bg-emerald-500/20' 
            : serverStatus === 'down'
            ? 'border-red-500/30 bg-red-500/10 text-red-300 hover:bg-red-500/20'
            : 'border-amber-500/30 bg-amber-500/10 text-amber-300 hover:bg-amber-500/20'
        ]"
      >
        <span class="relative flex h-2 w-2">
          <span 
            v-if="serverStatus === 'up'"
            class="absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75 animate-ping"
          ></span>
          <span 
            :class="[
              'relative inline-flex h-2 w-2 rounded-full',
              serverStatus === 'up' 
                ? 'bg-emerald-400' 
                : serverStatus === 'down'
                ? 'bg-red-400'
                : 'bg-amber-400'
            ]"
          ></span>
        </span>
        <span class="font-medium">
          {{ serverStatus === 'up' 
            ? 'Servidor Online' 
            : serverStatus === 'down'
            ? 'Servidor Offline'
            : 'Verificando...'
          }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuth } from '~/composables/api/useAuth'

const { login, logout, isAuthenticated } = useAuth()
const router = useRouter()

onMounted(() => {
  checkServerHealth()
  healthCheckInterval = setInterval(checkServerHealth, 30000)

  if (isAuthenticated.value) {
    router.push('/dashboard')
  }
})

const formData = ref({
  email: '',
  password: ''
})

const error = ref<string | null>(null)
const isLoading = ref(false)
const serverStatus = ref<'up' | 'down' | 'checking'>('checking')
const showCustomerModal = ref(false)
const showLogoFallback = ref(false)

let healthCheckInterval: ReturnType<typeof setInterval> | null = null

const checkServerHealth = async () => {
  try {
    const config = useRuntimeConfig()
    const { data } = await useFetch(`${config.public.backendUrl}/actuator/health`, {
      method: 'GET',
    })
    
    const status = (data.value as any)?.status
    
    if (status?.toString().trim().toUpperCase() === 'UP') {
      serverStatus.value = 'up'
    } else {
      serverStatus.value = 'down'
    }
  } catch (err) {
    console.error('Error checking server health:', err)
    serverStatus.value = 'down'
  }
}

onUnmounted(() => {
  if (healthCheckInterval) {
    clearInterval(healthCheckInterval)
  }
})

const handleSubmit = async () => {
  const email = formData.value.email.trim()
  const password = formData.value.password

  if (!email || !password) {
    error.value = 'Todos os campos são obrigatórios'
    return
  }

  isLoading.value = true
  error.value = null

  try {
    await login({
      email,
      password,
    })
    
    await router.push('/dashboard')
  } catch (err: any) {
    console.error('Login error:', err)
    
    // Verificar se é erro de acesso de cliente
    if (err.message === 'CUSTOMER_ACCESS') {
      showCustomerModal.value = true
      // Aguarda um pouco antes de fazer logout
      setTimeout(async () => {
        await logout()
      }, 100)
    } else {
      error.value = err.message || 'Erro ao realizar login. Tente novamente.'
    }
  } finally {
    isLoading.value = false
  }
}

const redirectToStore = () => {
  window.location.href = 'https://trinket-store.reducess.com'
}
</script>

<style scoped>
</style>
