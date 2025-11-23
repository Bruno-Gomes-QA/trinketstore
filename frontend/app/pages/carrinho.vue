<template>
  <div class="min-h-screen bg-[#f3fbff]">
    <div class="flex min-h-screen flex-col">
      <StoreOnboarding ref="onboardingRef" />
      <StoreHeader @show-onboarding="showOnboarding" />

      <main class="flex-1 py-10">
        <div class="container mx-auto px-3 sm:px-4">
          <NuxtLink
            to="/"
            class="mb-6 inline-flex items-center gap-2 text-sm font-medium text-muted-foreground transition hover:text-brand-cyan"
          >
            <ArrowLeft class="h-4 w-4" />
            Voltar para a loja
          </NuxtLink>

          <div class="rounded-3xl border border-border/50 bg-white/95 shadow-sm shadow-sky-100/50">
            <div class="border-b border-border/40 px-4 py-4 sm:px-6 lg:px-10">
              <div class="flex flex-col gap-4">
                <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between mb-5">
                  <div>
                    <p class="text-xl font-semibold uppercase tracking-[0.25em] text-brand-cyan">Checkout</p>
                  </div>
                </div>
                
                <div class="flex items-start gap-3 sm:gap-4">
                  <template v-for="(step, index) in checkoutSteps" :key="step.id">
                    <div class="flex flex-col items-center gap-1.5 sm:gap-2">
                      <div
                        class="flex flex-shrink-0 items-center justify-center rounded-full transition-all duration-300"
                        :class="[
                          index <= currentStepIndex
                            ? 'bg-emerald-500 text-white shadow-lg shadow-emerald-500/30'
                            : 'bg-muted text-muted-foreground',
                          'h-8 w-8 sm:h-12 sm:w-12'
                        ]"
                      >
                        <component :is="step.icon" :class="['h-3.5 w-3.5 sm:h-5 sm:w-5']" />
                      </div>
                      <div class="hidden text-center sm:block">
                        <p class="text-xs font-semibold text-foreground">{{ step.label }}</p>
                        <p class="text-[10px] text-muted-foreground">{{ step.description }}</p>
                      </div>
                    </div>
                    <div
                      v-if="index < checkoutSteps.length - 1"
                      class="relative mt-4 h-0.5 flex-1 overflow-hidden rounded-full bg-muted sm:mt-6 sm:h-1"
                    >
                      <div
                        class="h-full rounded-full bg-emerald-500 shadow-sm transition-all duration-500 ease-out"
                        :style="{
                          width: index < currentStepIndex ? '100%' : index === currentStepIndex ? '50%' : '0%'
                        }"
                      ></div>
                    </div>
                  </template>
                </div>
              </div>
            </div>

            <div class="space-y-6 px-3 py-6 sm:px-6 lg:px-10">

              <div
                v-if="!hasItems"
                class="rounded-2xl border border-dashed border-border bg-white/80 px-6 py-12 text-center shadow-inner"
              >
                <p class="text-lg font-semibold text-foreground">Seu carrinho está vazio</p>
                <p class="mt-2 text-sm text-muted-foreground">Explore nossa vitrine e escolha algo especial.</p>
                <NuxtLink
                  to="/"
                  class="mt-4 inline-flex items-center justify-center rounded-xl border border-brand-cyan px-5 py-2 text-sm font-semibold text-brand-cyan transition hover:bg-brand-cyan/10"
                >
                  Descobrir produtos
                </NuxtLink>
              </div>

              <div v-else>
                <div v-if="checkoutStep === 'review'" class="grid gap-5 lg:gap-6 lg:grid-cols-[2fr_1fr]">
                  <div class="space-y-4">
                    <article
                      v-for="item in items"
                      :key="item.productId"
                      class="rounded-2xl border border-border/60 bg-white/90 px-4 py-4 shadow-sm md:px-6"
                    >
                      <div class="flex flex-col gap-4 md:flex-row md:items-start">
                        <div class="flex items-start gap-4">
                          <div class="h-20 w-20 overflow-hidden rounded-xl border border-border/70 bg-muted">
                            <img
                              v-if="item.image"
                              :src="item.image"
                              :alt="item.name"
                              class="h-full w-full object-cover"
                            >
                            <div v-else class="flex h-full w-full items-center justify-center text-muted-foreground">
                              <ShoppingCart class="h-5 w-5" />
                            </div>
                          </div>
                          <div>
                            <h3 class="text-lg font-semibold text-foreground">{{ item.name }}</h3>
                            <p class="text-sm text-muted-foreground">
                              {{ formatPrice(item.priceInCents) }}
                              <span class="text-xs uppercase tracking-[0.25em] text-muted-foreground"> · unitário</span>
                            </p>
                            <p class="text-xs text-muted-foreground">
                              Estoque disponível:
                              <span class="font-semibold text-foreground">{{ item.availableStock }}</span>
                              <span v-if="item.availableStock === 0" class="text-destructive"> (sem estoque)</span>
                            </p>
                          </div>
                        </div>

                        <div class="flex flex-1 flex-col items-end gap-3">
                          <div class="w-full max-w-[180px]">
                            <NumberField
                              :model-value="Math.max(item.quantity, 0)"
                              :min="1"
                              :max="Math.max(item.availableStock, 1)"
                              :disabled="item.availableStock === 0"
                              @update:model-value="(value) => handleQuantityUpdate(item.productId, value, item.availableStock)"
                            >
                              <NumberFieldContent>
                                <NumberFieldDecrement />
                                <NumberFieldInput />
                                <NumberFieldIncrement />
                              </NumberFieldContent>
                            </NumberField>
                          </div>
                          <button
                            type="button"
                            class="inline-flex items-center gap-2 text-xs font-semibold text-muted-foreground transition hover:text-destructive"
                            @click="removeItem(item.productId)"
                          >
                            <Trash2 class="h-4 w-4" />
                            Remover
                          </button>
                        </div>
                      </div>
                    </article>
                  </div>

                  <aside class="space-y-3">
                    <div class="rounded-2xl border border-brand-cyan/30 bg-white p-5 shadow-sm">
                      <div class="flex items-center justify-between text-sm">
                        <span class="text-muted-foreground">Total</span>
                        <span class="text-2xl font-bold text-foreground">{{ formatCurrency(totalAmountInCents) }}</span>
                      </div>
                      <div
                        v-if="cartBlockedMessage"
                        class="mt-3 rounded-xl border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-800"
                      >
                        {{ cartBlockedMessage }}
                      </div>
                      <div class="mt-4 flex flex-col gap-3">
                        <button
                          type="button"
                          class="inline-flex items-center justify-center rounded-xl bg-emerald-500 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-emerald-500/30 transition hover:bg-emerald-600 hover:shadow-xl hover:shadow-emerald-500/40 disabled:opacity-60 disabled:shadow-none"
                          :disabled="Boolean(cartBlockedMessage)"
                          @click="handleProceedToIdentify"
                        >
                          Confirmar itens
                        </button>
                      </div>
                      <p v-if="stockSyncError" class="mt-3 text-xs text-destructive">{{ stockSyncError }}</p>
                    </div>
                  </aside>
                </div>

                <div v-if="checkoutStep === 'identify'" class="grid gap-5 lg:gap-6 lg:grid-cols-[1.5fr_1fr]">
                  <section class="rounded-2xl border border-brand-cyan/30 bg-white p-5 shadow-sm">
                    <p class="text-xs font-semibold uppercase tracking-[0.25em] text-brand-cyan">Passo 2</p>
                    <h2 class="text-xl font-bold text-foreground">Entrar com Google</h2>
                    <p class="text-sm text-muted-foreground">Use sua conta para ligar o pedido a você.</p>

                    <div v-if="!isAuthenticated" class="mt-4 flex flex-col gap-3 rounded-2xl border border-dashed border-brand-cyan/40 bg-gradient-to-r from-sky-50 via-white to-emerald-50 p-4 sm:flex-row sm:items-center sm:justify-between">
                      <div>
                        <p class="text-sm font-semibold text-foreground">Faça login para continuar</p>
                        <p class="text-xs text-muted-foreground">
                          Conecte sua conta Google para prosseguir
                        </p>
                      </div>
                      <button
                        type="button"
                        class="inline-flex h-11 items-center justify-center gap-2 rounded-xl border border-brand-cyan/60 bg-white px-4 text-sm font-semibold text-brand-cyan shadow-sm transition hover:border-brand-cyan hover:bg-brand-cyan/10 disabled:opacity-60"
                        :disabled="authLoading"
                        :aria-label="authLoading ? 'Conectando ao Google' : 'Entrar com Google'"
                        @click="handleGoogleLogin"
                      >
                        <Icon
                          name="logos:google-icon"
                          :class="['h-5 w-5 transition', authLoading ? 'animate-pulse opacity-70' : '']"
                        />
                        Entrar com Google
                      </button>
                    </div>

                    <div v-if="isAuthenticated" class="mt-4">
                      <div class="mb-3 flex items-center gap-2 text-sm">
                        <CheckCircle2 class="h-5 w-5 text-emerald-500" />
                        <span class="font-semibold text-foreground">Identificação confirmada</span>
                      </div>
                      <div class="grid gap-3 sm:grid-cols-2">
                        <div class="rounded-2xl border border-border/60 bg-muted/30 p-3">
                          <p class="text-xs uppercase tracking-[0.25em] text-muted-foreground">Nome</p>
                          <p class="text-base font-semibold text-foreground">{{ customerName }}</p>
                        </div>
                        <div class="rounded-2xl border border-border/60 bg-muted/30 p-3">
                          <p class="text-xs uppercase tracking-[0.25em] text-muted-foreground">E-mail</p>
                          <p class="text-sm font-semibold text-foreground break-all">{{ customerEmail }}</p>
                        </div>
                      </div>
                    </div>

                    <div
                      v-if="checkoutError"
                      class="mt-4 rounded-2xl border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive"
                    >
                      {{ normalizedCheckoutError }}
                    </div>

                    <div class="mt-5 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-end">
                      <button
                        type="button"
                        class="inline-flex items-center justify-center rounded-xl border border-border px-4 py-2 text-sm font-semibold text-muted-foreground transition hover:text-foreground"
                        @click="checkoutStep = 'review'"
                      >
                        Voltar
                      </button>
                      <button
                        type="button"
                        class="inline-flex items-center justify-center gap-2 rounded-xl bg-emerald-400 px-4 py-2 text-sm font-semibold text-emerald-950 shadow-sm transition hover:bg-emerald-300 disabled:opacity-60"
                        :disabled="!canGeneratePayment"
                        @click="handleGeneratePayment"
                      >
                        <CheckCircle2 class="h-4 w-4" />
                        Gerar PIX
                      </button>
                      <p v-if="paymentFlowError" class="text-xs text-destructive sm:ml-4 sm:text-right">
                        {{ paymentFlowError }}
                      </p>
                    </div>
                  </section>

                  <Dialog :open="generatingPayment">
                    <DialogContent class="sm:max-w-md" :show-close="false">
                      <DialogHeader>
                        <DialogTitle class="text-center text-xl font-bold">
                          Processando seu pedido
                        </DialogTitle>
                      </DialogHeader>
                      <div class="space-y-6 py-4">
                        <div v-if="loadingMessages[currentMessageIndex]" class="flex flex-col items-center justify-center gap-4">
                          <div class="relative">
                            <component
                              :is="loadingMessages[currentMessageIndex]!.icon"
                              class="h-16 w-16 text-brand-cyan animate-pulse"
                            />
                          </div>
                          <div class="text-center">
                            <p class="text-lg font-semibold text-foreground">
                              {{ loadingMessages[currentMessageIndex]!.title }}
                            </p>
                            <p class="text-sm text-muted-foreground">
                              {{ loadingMessages[currentMessageIndex]!.description }}
                            </p>
                          </div>
                        </div>
                        <div class="flex items-center justify-center gap-2">
                          <div
                            v-for="(_, index) in loadingMessages"
                            :key="index"
                            class="h-2 w-2 rounded-full transition-all duration-300"
                            :class="[
                              index === currentMessageIndex
                                ? 'bg-brand-cyan w-6'
                                : 'bg-muted'
                            ]"
                          />
                        </div>
                      </div>
                    </DialogContent>
                  </Dialog>

                  <aside class="space-y-3">
                    <div class="rounded-2xl border border-border/60 bg-white p-5 shadow-sm">
                      <p class="text-sm font-semibold text-foreground">Resumo</p>
                      <p class="text-2xl font-bold text-foreground">{{ formatCurrency(totalAmountInCents) }}</p>
                      <ul class="mt-3 space-y-2 text-xs text-muted-foreground">
                        <li v-for="item in items" :key="`identify-${item.productId}`">
                          {{ item.name }} · {{ Math.max(item.quantity, 0) }} un
                        </li>
                      </ul>
                    </div>
                  </aside>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      <StoreFooter />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { Component } from 'vue'
import { useClipboard } from '@vueuse/core'
import { ArrowLeft, Trash2, RefreshCcw, Clock3, Copy, AlertTriangle, ShoppingCart, CheckCircle2, Sparkles, UserCheck, QrCode, Package, CreditCard, Loader2 } from 'lucide-vue-next'
import type { OrderResponse } from '~/types/orders'
import { useBackendFetchDirect } from '~/composables/core/useBackendFetch'

const loadingMessages = [
  {
    icon: Package,
    title: 'Registrando seu pedido',
    description: 'Estamos reservando os itens do seu carrinho...',
  },
  {
    icon: CreditCard,
    title: 'Gerando código PIX',
    description: 'Criando o pagamento seguro para você...',
  },
  {
    icon: Sparkles,
    title: 'Quase lá!',
    description: 'Em breve você poderá retirar seus produtos...',
  },
  {
    icon: CheckCircle2,
    title: 'Finalizando',
    description: 'Só mais um momento, estamos preparando tudo...',
  },
]

const onboardingRef = ref<any>(null)
const showOnboarding = () => onboardingRef.value?.show()

useSeoMeta({
  title: 'Carrinho de compras · Trinket Store',
  description: 'Revise seus itens e finalize o pedido com pagamento via PIX.',
})

const router = useRouter()
const route = useRoute()
const checkoutSteps: Array<{ id: CheckoutStepId; label: string; description: string; icon: Component }> = [
  { id: 'review', label: 'Carrinho', description: 'Revise os itens e estoque', icon: ShoppingCart },
  { id: 'identify', label: 'Identificação', description: 'Conecte-se com Google', icon: UserCheck },
  { id: 'payment', label: 'Pagamento', description: 'PIX válido por 5 minutos', icon: QrCode },
]

type CheckoutStepId = 'review' | 'identify' | 'payment'

const DEFAULT_PAYMENT_WINDOW_SECONDS = 60 * 5

const {
  items,
  hasItems,
  totalItems,
  totalAmountInCents,
  updateItemQuantity,
  removeItem,
  clearCart,
  setCartOwner,
  setItemStock,
} = useStorefrontCart()
const { fetchInventoryForProduct } = useStorefrontInventory()
const { formatCurrencyFromCents } = useFormatters()
const { user: supabaseUser, isAuthenticated, loading: authLoading, signInWithGoogle } = useSupabaseAuth()
const { profile, fetchProfile, loading: profileLoading, clearProfile } = useStorefrontCustomer()
const { createOrderFromCart, creating: creatingOrder, error: checkoutError } = useStorefrontCheckout()
const { copy, copied } = useClipboard()
const fallbackErrorMessage = 'Não foi possível processar o pedido agora. Tente novamente em instantes.'

const checkoutStep = ref<CheckoutStepId>('review')
const currentStepIndex = computed(() => checkoutSteps.findIndex((step) => step.id === checkoutStep.value))
const stepProgress = computed(() => {
  if (checkoutSteps.length <= 1) return 0
  return (currentStepIndex.value / (checkoutSteps.length - 1)) * 100
})

const stockRefreshing = ref(false)
const stockSyncError = ref<string | null>(null)
const pixCode = ref('')
const pixQrImage = ref('')
const pixExpiresAt = ref<string | null>(null)
const activeOrder = ref<OrderResponse | null>(null)
const paymentWindowSeconds = ref(DEFAULT_PAYMENT_WINDOW_SECONDS)
const paymentCountdown = ref(DEFAULT_PAYMENT_WINDOW_SECONDS)
const paymentInterval = ref<ReturnType<typeof setInterval> | null>(null)
const paymentExpireTimeout = ref<ReturnType<typeof setTimeout> | null>(null)
const paymentExpired = ref(false)
const orderPollingInterval = ref<ReturnType<typeof setInterval> | null>(null)
const generatingPayment = ref(false)
const currentMessageIndex = ref(0)
const messageInterval = ref<ReturnType<typeof setInterval> | null>(null)
const paymentFlowError = ref<string | null>(null)
const normalizedCheckoutError = computed(() => checkoutError.value?.message || fallbackErrorMessage)

const cartBlockedMessage = computed(() => {
  if (!hasItems.value) return 'Adicione itens antes de continuar.'
  const unavailable = items.value.find((item) => item.availableStock === 0)
  if (unavailable) {
    return `${unavailable.name} está sem estoque no momento.`
  }
  const over = items.value.find((item) => item.availableStock > 0 && item.quantity > item.availableStock)
  if (over) {
    return `Ajuste a quantidade de ${over.name} para ${over.availableStock}.`
  }
  return ''
})

const customerName = computed(() => profile.value?.nomeUser ?? (supabaseUser.value?.user_metadata?.name as string | undefined) ?? 'Cliente Trinket')
const customerEmail = computed(() => profile.value?.email ?? supabaseUser.value?.email ?? 'email@exemplo.com')
const canGeneratePayment = computed(() => isAuthenticated.value && Boolean(profile.value) && !creatingOrder.value && !cartBlockedMessage.value)

const countdownLabel = computed(() => {
  const minutes = Math.floor(paymentCountdown.value / 60)
  const seconds = paymentCountdown.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

const progressPercent = computed(() => {
  const window = paymentWindowSeconds.value || DEFAULT_PAYMENT_WINDOW_SECONDS
  if (window <= 0) return 0
  return (paymentCountdown.value / window) * 100
})
const pixQrUrl = computed(() => {
  if (pixQrImage.value) {
    return `data:image/png;base64,${pixQrImage.value}`
  }
  if (pixCode.value) {
    return `https://api.qrserver.com/v1/create-qr-code/?size=280x280&data=${encodeURIComponent(pixCode.value)}`
  }
  return ''
})

const paymentStatusState = computed(() => {
  const status = activeOrder.value?.statusOrder
  if (!status) return null

  if (status === 'paid') {
    return {
      label: 'Pagamento aprovado',
      description: 'Recebemos seu PIX. Procure a equipe para retirar seu pedido.',
      badgeClass: 'bg-emerald-100 text-emerald-800',
      iconClass: 'text-emerald-500',
      containerClass: 'border-emerald-200 bg-emerald-50/80',
      icon: CheckCircle2,
    }
  }

  if (status === 'canceled') {
    return {
      label: 'Pagamento cancelado',
      description: 'O PIX foi cancelado ou expirou. Gere um novo QR Code para tentar novamente.',
      badgeClass: 'bg-destructive/10 text-destructive',
      iconClass: 'text-destructive',
      containerClass: 'border-destructive/40 bg-destructive/5',
      icon: AlertTriangle,
    }
  }

  return {
    label: 'Aguardando pagamento',
    description: 'Assim que o PIX for reconhecido liberamos automaticamente seu pedido.',
    badgeClass: 'bg-amber-100 text-amber-800',
    iconClass: 'text-amber-500',
    containerClass: 'border-amber-100 bg-amber-50/80',
    icon: Clock3,
  }
})

const formatCurrency = (amount: number) => formatCurrencyFromCents(amount)
const formatPrice = (amount: number | null) => {
  if (amount === null || amount === undefined) return 'Sob consulta'
  return formatCurrency(amount)
}
const formatLineTotal = (priceInCents: number | null, quantity: number) => {
  const safePrice = priceInCents ?? 0
  return formatCurrency(safePrice * Math.max(quantity, 0))
}

const refreshStocks = async () => {
  if (!items.value.length) return
  stockRefreshing.value = true
  stockSyncError.value = null

  try {
    await Promise.all(
      items.value.map(async (item) => {
        const snapshot = await fetchInventoryForProduct(item.productId, { force: true })
        setItemStock(item.productId, snapshot.qtyOnHand)
      }),
    )
  } catch (error) {
    console.error('[cart] inventory refresh failed', error)
    stockSyncError.value = 'Não conseguimos atualizar o estoque agora. Tente novamente em instantes.'
  } finally {
    stockRefreshing.value = false
  }
}

const handleQuantityUpdate = (productId: number, value?: number | string, stockLimit?: number) => {
  const parsed = typeof value === 'number' ? value : Number(value)
  if (!Number.isFinite(parsed)) return
  updateItemQuantity(productId, parsed, stockLimit)
}

const handleProceedToIdentify = () => {
  if (cartBlockedMessage.value) return
  paymentFlowError.value = null
  checkoutStep.value = 'identify'
  if (isAuthenticated.value && !profile.value) {
    fetchProfile().catch((error) => console.error('[cart] profile fetch failed', error))
  }
}

const handleGoogleLogin = async () => {
  if (isAuthenticated.value) return
  await signInWithGoogle('/carrinho?step=identify')
}

const computeSecondsUntil = (raw?: string | null) => {
  if (!raw) {
    return DEFAULT_PAYMENT_WINDOW_SECONDS
  }
  const target = Date.parse(raw)
  if (Number.isNaN(target)) {
    return DEFAULT_PAYMENT_WINDOW_SECONDS
  }
  const seconds = Math.max(Math.round((target - Date.now()) / 1000), 0)
  return seconds || DEFAULT_PAYMENT_WINDOW_SECONDS
}

const stopPaymentTimer = () => {
  if (paymentInterval.value) {
    clearInterval(paymentInterval.value)
    paymentInterval.value = null
  }
  if (paymentExpireTimeout.value) {
    clearTimeout(paymentExpireTimeout.value)
    paymentExpireTimeout.value = null
  }
  paymentWindowSeconds.value = DEFAULT_PAYMENT_WINDOW_SECONDS
}

const stopOrderPolling = () => {
  if (orderPollingInterval.value) {
    clearInterval(orderPollingInterval.value)
    orderPollingInterval.value = null
  }
}

const startOrderPolling = (orderId: number | null | undefined) => {
  if (!orderId) return
  stopOrderPolling()
  orderPollingInterval.value = setInterval(async () => {
    try {
      const latest = await useBackendFetchDirect<OrderResponse>(`/orders/${orderId}`)
      activeOrder.value = latest
      if (latest.statusOrder !== 'pending') {
        stopOrderPolling()
        if (latest.statusOrder === 'paid') {
          stopPaymentTimer()
          paymentExpired.value = false
        }
      }
    } catch (error) {
      console.error('[cart] status polling failed', error)
    }
  }, 4000)
}

const startPaymentFlow = (expiresAt?: string | null) => {
  stopPaymentTimer()
  const seconds = computeSecondsUntil(expiresAt)
  paymentWindowSeconds.value = seconds
  paymentCountdown.value = seconds
  paymentExpired.value = false

  paymentInterval.value = setInterval(() => {
    paymentCountdown.value -= 1
    if (paymentCountdown.value <= 0) {
      paymentCountdown.value = 0
      paymentExpired.value = true
      stopPaymentTimer()
      stopOrderPolling()
      paymentExpireTimeout.value = setTimeout(() => {
        checkoutStep.value = 'identify'
        activeOrder.value = null
        pixCode.value = ''
        pixQrImage.value = ''
      }, 4000)
    }
  }, 1000)
}

const handleGeneratePayment = async () => {
  if (!canGeneratePayment.value || !profile.value) return

  generatingPayment.value = true
  currentMessageIndex.value = 0
  paymentFlowError.value = null

  messageInterval.value = setInterval(() => {
    currentMessageIndex.value = (currentMessageIndex.value + 1) % loadingMessages.length
  }, 2000)

  try {
    const response = await createOrderFromCart({
      userId: Number(profile.value.idUser),
      items: items.value,
      totalAmountInCents: totalAmountInCents.value,
      description: `Pedido ${profile.value.nomeUser ?? ''}`.trim(),
    })
    activeOrder.value = response.order
    pixCode.value = response.pix?.qrCode ?? ''
    pixQrImage.value = response.pix?.qrCodeBase64 ?? ''
    pixExpiresAt.value = response.pix?.expiresAt ?? null

    paymentExpired.value = false
    clearCart()
    stopPaymentTimer()
    stopOrderPolling()

    if (messageInterval.value) {
      clearInterval(messageInterval.value)
      messageInterval.value = null
    }

    generatingPayment.value = false
    paymentFlowError.value = null

    await router.push({
      path: '/pedidos',
      query: { orderId: response.order.idOrder },
    })
  } catch (error) {
    console.error('[cart] order creation failed', error)
    if (messageInterval.value) {
      clearInterval(messageInterval.value)
      messageInterval.value = null
    }
    generatingPayment.value = false
    paymentFlowError.value = normalizedCheckoutError.value
  }
}

const handleCopyPix = async () => {
  if (!pixCode.value) return
  await copy(pixCode.value)
}

const handleClearCart = () => {
  clearCart()
  checkoutStep.value = 'review'
  activeOrder.value = null
  pixCode.value = ''
  pixQrImage.value = ''
  pixExpiresAt.value = null
  stopPaymentTimer()
  stopOrderPolling()
}

watch(isAuthenticated, (logged) => {
  if (logged) {
    fetchProfile().catch((error) => console.error('[cart] profile fetch failed', error))
  } else {
    clearProfile()
    activeOrder.value = null
    pixCode.value = ''
    pixQrImage.value = ''
    pixExpiresAt.value = null
    stopPaymentTimer()
    stopOrderPolling()
    if (checkoutStep.value !== 'review') {
      checkoutStep.value = 'review'
    }
  }
})

watch(profile, (value) => {
  if (value?.idUser) {
    setCartOwner(value.idUser)
  } else {
    setCartOwner(null)
  }
})

watch(
  () => items.value.length,
  (length, previous) => {
    if (length > 0 && length !== previous) {
      refreshStocks()
    }
  },
)

watch(hasItems, (value) => {
  if (!value) {
    checkoutStep.value = 'review'
    stopPaymentTimer()
    stopOrderPolling()
  } else if (route.query.step === 'identify') {
    checkoutStep.value = 'identify'
  }
})

watch(checkoutStep, (step) => {
  if (step !== 'payment') {
    stopPaymentTimer()
    paymentExpired.value = false
    stopOrderPolling()
  }
})

watch(
  () => route.query.step,
  (step) => {
    if (step === 'identify' && hasItems.value) {
      checkoutStep.value = 'identify'
    }
  },
  { immediate: true },
)

watch(activeOrder, (order) => {
  if (order && order.statusOrder && order.statusOrder !== 'pending') {
    stopOrderPolling()
    if (order.statusOrder === 'paid') {
      stopPaymentTimer()
      paymentExpired.value = false
    }
  }
})

onMounted(() => {
  if (items.value.length) {
    refreshStocks()
  }
  if (isAuthenticated.value) {
    fetchProfile().catch((error) => console.error('[cart] profile fetch failed', error))
  }
})

onBeforeUnmount(() => {
  stopPaymentTimer()
  stopOrderPolling()
  if (messageInterval.value) {
    clearInterval(messageInterval.value)
    messageInterval.value = null
  }
})
</script>
