<template>
  <header class="sticky top-0 z-50 border-b border-border/60 bg-white/90 backdrop-blur-md">
    <div class="container mx-auto px-4">
      <div class="flex items-center justify-between gap-4 py-3">
        <!-- Logo + tagline -->
        <NuxtLink to="/" class="flex items-center gap-3">
          <img
            v-if="logoFullExists"
            src="/logo_full.png"
            alt="Trinket Store"
            class="h-10 w-auto"
          />
          <div v-else class="flex items-center gap-3">
            <div class="h-11 w-11 rounded-xl bg-brand-cyan/15 text-brand-cyan flex items-center justify-center text-xl font-bold">
              T
            </div>
            <div class="hidden sm:flex flex-col leading-tight">
              <span class="text-lg font-semibold text-foreground">Trinket Store</span>
              <span class="text-xs text-muted-foreground">Patos e tartarugas em resina</span>
            </div>
          </div>
        </NuxtLink>

        <!-- Info copy - Desktop only, centered -->
        <div class="hidden lg:flex flex-1 justify-center space-y-1 text-sm">
          <div class="flex items-center gap-3 text-muted-foreground">
            <span class="inline-flex items-center gap-1.5">
              <MapPin class="h-4 w-4 text-brand-cyan" />
              Retirada no evento · 3° Andar - Lab 7
            </span>
            <span class="text-xs">Pagamentos via PIX ou cartão</span>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex items-center gap-2">
          <button
            @click="emit('show-onboarding')"
            class="inline-flex items-center gap-2 rounded-xl border border-border px-4 py-2 text-sm font-medium text-foreground transition hover:border-brand-cyan hover:text-brand-cyan"
          >
            <Info class="h-4 w-4" />
            <span class="hidden md:inline">Como funciona?</span>
          </button>

          <button
            @click="scrollToProducts"
            class="hidden md:inline-flex items-center gap-2 rounded-xl border border-border px-4 py-2 text-sm font-medium text-foreground transition hover:border-brand-cyan hover:text-brand-cyan"
          >
            <ShoppingBag class="h-4 w-4" />
            <span>Ver produtos</span>
          </button>

          <NuxtLink
            to="/carrinho"
            class="relative inline-flex h-11 w-11 items-center justify-center rounded-xl border border-border text-foreground transition hover:border-brand-cyan"
            aria-label="Abrir carrinho"
          >
            <ShoppingCart class="h-5 w-5" />
            <span
              v-if="cartCount > 0"
              class="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-brand-cyan text-[11px] font-bold text-white"
            >
              {{ cartCount }}
            </span>
          </NuxtLink>

          <button
            v-if="!isAuthenticated"
            type="button"
            class="inline-flex h-11 w-11 items-center justify-center rounded-xl border border-brand-cyan/60 text-brand-cyan transition hover:border-brand-cyan hover:bg-brand-cyan/10"
            :disabled="authLoading"
            :aria-label="authLoading ? 'Conectando ao Google' : 'Entrar com Google'"
            @click="handleGoogleLogin"
          >
            <Icon
              name="logos:google-icon"
              :class="['h-5 w-5 transition', authLoading ? 'animate-pulse opacity-70' : '']"
            />
          </button>

          <Popover v-else v-model:open="profilePopoverOpen">
            <PopoverTrigger as-child>
              <button
                type="button"
                class="inline-flex h-11 w-11 items-center justify-center rounded-full border border-border bg-brand-cyan/5 text-sm font-semibold text-brand-cyan transition hover:border-brand-cyan"
                :aria-label="`Perfil de ${userEmail}`"
              >
                <span>{{ userInitials }}</span>
              </button>
            </PopoverTrigger>
            <PopoverContent align="end" class="w-56 space-y-4">
              <div>
                <p class="text-xs uppercase text-muted-foreground">Conectado como</p>
                <p class="text-sm font-semibold text-foreground break-all">{{ userEmail }}</p>
              </div>
              <button
                type="button"
                class="flex w-full items-center justify-center gap-2 rounded-xl border border-border px-4 py-2 text-sm font-semibold text-destructive transition hover:border-destructive"
                :disabled="authLoading"
                @click="handleLogout"
              >
                <LogOut class="h-4 w-4" />
                <span>{{ authLoading ? 'Saindo...' : 'Sair' }}</span>
              </button>
            </PopoverContent>
          </Popover>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ShoppingCart, Info, ShoppingBag, MapPin, LogOut } from 'lucide-vue-next'
import { Popover, PopoverContent, PopoverTrigger } from '~/components/ui/popover'

const { user, loading: authLoading, isAuthenticated, signInWithGoogle, signOut } = useSupabaseAuth()
const { totalItems } = useStorefrontCart()

const emit = defineEmits(['show-onboarding'])
const logoFullExists = ref(false)
const profilePopoverOpen = ref(false)
const cartCount = computed(() => totalItems.value)

const userEmail = computed(() => user.value?.email ?? 'usuário')
const userInitials = computed(() => {
  const name = user.value?.user_metadata?.name as string | undefined
  if (name) {
    return name
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0])
      .join('')
      .toUpperCase()
  }
  return user.value?.email?.charAt(0).toUpperCase() ?? 'U'
})

const scrollToProducts = () => {
  const section = document.getElementById('colecao-glow')
  section?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const handleGoogleLogin = async () => {
  await signInWithGoogle()
}

const handleLogout = async () => {
  await signOut()
  profilePopoverOpen.value = false
}

onMounted(() => {
  const img = new Image()
  img.onload = () => { logoFullExists.value = true }
  img.onerror = () => { logoFullExists.value = false }
  img.src = '/logo_full.png'
})
</script>
