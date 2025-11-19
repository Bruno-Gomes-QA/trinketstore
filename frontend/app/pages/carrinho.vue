<template>
  <div class="min-h-screen bg-[#f3fbff]">
    <div class="flex min-h-screen flex-col">
      <StoreOnboarding ref="onboardingRef" />
      <StoreHeader @show-onboarding="showOnboarding" />

      <main class="flex-1 py-10">
        <div class="container mx-auto px-4">
          <NuxtLink
            to="/"
            class="mb-6 inline-flex items-center gap-2 text-sm font-medium text-muted-foreground transition hover:text-brand-cyan"
          >
            <ArrowLeft class="h-4 w-4" />
            Voltar para a loja
          </NuxtLink>

          <div class="rounded-3xl border border-border/60 bg-white/95 shadow-lg shadow-sky-100/70">
            <div class="border-b border-border/50 px-6 py-5 lg:px-10">
              <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <p class="text-xs font-semibold uppercase tracking-[0.3em] text-brand-cyan">Carrinho</p>
                  <h1 class="text-2xl font-bold text-foreground sm:text-3xl">Finalize sua experiência Trinket</h1>
                  <p class="text-sm text-muted-foreground">Revise os itens, conecte seu Google e gere o PIX com validade de 5 minutos.</p>
                </div>
                <div class="text-right">
                  <p class="text-xs uppercase tracking-[0.3em] text-muted-foreground">Itens</p>
                  <p class="text-3xl font-semibold text-foreground">{{ totalItems }}</p>
                </div>
              </div>
            </div>

            <div class="space-y-8 px-4 py-8 sm:px-6 lg:px-10">
              <section class="rounded-3xl border border-brand-cyan/20 bg-gradient-to-br from-sky-50 via-emerald-50 to-amber-50 p-5 shadow-[0_15px_50px_rgba(14,165,233,0.12)]">
                <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                  <div>
                    <p class="text-xs font-semibold uppercase tracking-[0.3em] text-brand-cyan">Fluxo do pedido</p>
                    <p class="text-lg font-semibold text-foreground">Passo {{ currentStepIndex + 1 }} de {{ checkoutSteps.length }}</p>
                  </div>
                  <p class="text-sm text-muted-foreground">{{ checkoutSteps[currentStepIndex]?.description }}</p>
                </div>
                <div class="mt-4 h-2 w-full overflow-hidden rounded-full bg-white/40">
                  <div
                    class="h-full rounded-full bg-gradient-to-r from-emerald-400 via-brand-cyan to-amber-300 transition-all"
                    :style="{ width: `${stepProgress}%` }"
                  />
                </div>
                <div class="mt-5 grid gap-3 md:grid-cols-3">
                  <article
                    v-for="(step, index) in checkoutSteps"
                    :key="step.id"
                    class="rounded-2xl border p-4 transition"
                    :class="[
                      index < currentStepIndex ? 'border-emerald-400 bg-white shadow-[0_0_35px_rgba(16,185,129,0.25)]' : '',
                      index === currentStepIndex ? 'border-brand-cyan bg-white/90 shadow-[0_12px_40px_rgba(14,165,233,0.3)]' : '',
                      index > currentStepIndex ? 'border-border/70 bg-white/70' : '',
                    ]"
                  >
                    <div class="flex items-center gap-3">
                      <component
                        :is="step.icon"
                        class="h-5 w-5"
                        :class="index <= currentStepIndex ? 'text-brand-cyan' : 'text-muted-foreground'"
                      />
                      <div>
                        <p class="text-sm font-semibold text-foreground">{{ step.label }}</p>
                        <p class="text-xs text-muted-foreground">{{ step.description }}</p>
                      </div>
                    </div>
                  </article>
                </div>
              </section>

              <div
                v-if="!hasItems"
                class="rounded-3xl border border-dashed border-border bg-white/80 px-6 py-12 text-center shadow-inner"
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
                <div v-if="checkoutStep === 'review'" class="grid gap-8 lg:grid-cols-[2fr_1fr]">
                  <div class="space-y-4">
                    <article
                      v-for="item in items"
                      :key="item.productId"
                      class="rounded-2xl border border-border/60 bg-white px-4 py-4 shadow-sm md:px-6"
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
                            <p class="text-sm uppercase tracking-[0.3em] text-muted-foreground">{{ item.slug }}</p>
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
                          <p class="text-sm font-semibold text-foreground">
                            {{ formatLineTotal(item.priceInCents, item.quantity) }}
                          </p>
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

                  <aside class="space-y-4">
                    <div class="rounded-3xl border border-brand-cyan/30 bg-gradient-to-br from-white via-sky-50 to-emerald-50 p-5 shadow-[0_12px_40px_rgba(14,165,233,0.15)]">
                      <div class="flex items-center justify-between text-sm">
                        <span class="text-muted-foreground">Subtotal</span>
                        <span class="font-semibold text-foreground">{{ formatCurrency(totalAmountInCents) }}</span>
                      </div>
                      <div class="mt-2 flex items-center justify-between text-sm">
                        <span class="text-muted-foreground">Retirada</span>
                        <span class="font-semibold text-foreground">No evento · Grátis</span>
                      </div>
                      <div class="mt-4 border-t border-border/50 pt-4">
                        <p class="text-xs uppercase tracking-[0.3em] text-muted-foreground">Total</p>
                        <p class="text-3xl font-bold text-foreground">{{ formatCurrency(totalAmountInCents) }}</p>
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
                          class="inline-flex items-center justify-center rounded-xl bg-brand-cyan px-4 py-2 text-sm font-semibold text-foreground shadow-sm transition hover:bg-brand-cyan/90 disabled:opacity-60"
                          :disabled="Boolean(cartBlockedMessage)"
                          @click="handleProceedToIdentify"
                        >
                          Confirmar itens
                        </button>
                        <button
                          type="button"
                          class="inline-flex items-center justify-center gap-2 rounded-xl border border-border px-4 py-2 text-sm font-semibold text-muted-foreground transition hover:text-foreground disabled:opacity-60"
                          :disabled="stockRefreshing"
                          @click="refreshStocks"
                        >
                          <RefreshCcw class="h-4 w-4" :class="stockRefreshing ? 'animate-spin text-brand-cyan' : ''" />
                          Atualizar disponibilidade
                        </button>
                        <button
                          type="button"
                          class="inline-flex items-center justify-center rounded-xl border border-destructive/30 px-4 py-2 text-xs font-semibold text-destructive transition hover:bg-destructive/5"
                          @click="handleClearCart"
                        >
                          Esvaziar carrinho
                        </button>
                      </div>
                      <p v-if="stockSyncError" class="mt-3 text-xs text-destructive">{{ stockSyncError }}</p>
                    </div>
                    <NuxtLink
                      to="/"
                      class="inline-flex w-full items-center justify-center rounded-xl border border-border px-4 py-2 text-sm font-semibold text-foreground transition hover:border-brand-cyan hover:text-brand-cyan"
                    >
                      <Sparkles class="mr-2 h-4 w-4" />
                      Continuar comprando
                    </NuxtLink>
                  </aside>
                </div>

                <div v-else-if="checkoutStep === 'identify'" class="grid gap-8 lg:grid-cols-[1.5fr_1fr]">
                  <section class="rounded-3xl border border-brand-cyan/40 bg-white/95 p-6 shadow-[0_12px_45px_rgba(14,165,233,0.1)]">
                    <p class="text-xs font-semibold uppercase tracking-[0.3em] text-brand-cyan">Passo 2</p>
                    <h2 class="text-2xl font-bold text-foreground">Identifique-se com Google</h2>
                    <p class="text-sm text-muted-foreground">Usamos seu nome e email para vincular o pedido. Nenhum outro campo é necessário.</p>

                    <div class="mt-6 flex flex-col gap-4 rounded-2xl border border-dashed border-brand-cyan/40 bg-gradient-to-r from-sky-50 via-white to-emerald-50 p-4 sm:flex-row sm:items-center sm:justify-between">
                      <div>
                        <p class="text-sm font-semibold text-foreground">Status</p>
                        <p class="text-sm text-muted-foreground">
                          {{ isAuthenticated ? 'Conta Google conectada' : 'Você ainda não fez login' }}
                        </p>
                      </div>
                      <div class="flex items-center gap-3">
                        <button
                          type="button"
                          class="inline-flex h-12 w-12 items-center justify-center rounded-xl border border-brand-cyan/60 bg-white text-brand-cyan shadow-sm transition hover:border-brand-cyan hover:bg-brand-cyan/10 disabled:opacity-60"
                          :disabled="authLoading"
                          :aria-label="authLoading ? 'Conectando ao Google' : 'Entrar com Google'"
                          @click="handleGoogleLogin"
                        >
                          <Icon
                            name="logos:google-icon"
                            :class="['h-5 w-5 transition', authLoading ? 'animate-pulse opacity-70' : '']"
                          />
                        </button>
                        <span class="text-xs text-muted-foreground">
                          {{ isAuthenticated ? 'Tudo certo! Pode continuar.' : 'Clique no botão para entrar com Google.' }}
                        </span>
                      </div>
                    </div>

                    <div v-if="isAuthenticated" class="mt-6 grid gap-4 sm:grid-cols-2">
                      <div class="rounded-2xl border border-border/60 bg-muted/40 p-4">
                        <p class="text-xs uppercase tracking-[0.3em] text-muted-foreground">Nome</p>
                        <p class="text-lg font-semibold text-foreground">{{ customerName }}</p>
                      </div>
                      <div class="rounded-2xl border border-border/60 bg-muted/40 p-4">
                        <p class="text-xs uppercase tracking-[0.3em] text-muted-foreground">E-mail</p>
                        <p class="text-base font-semibold text-foreground break-all">{{ customerEmail }}</p>
                      </div>
                    </div>
                    <p v-else class="mt-6 text-sm text-muted-foreground">Faça login com Google para liberar o pagamento.</p>

                    <p v-if="profileLoading" class="mt-3 text-xs text-muted-foreground">Carregando dados do evento...</p>
                    <div
                      v-if="checkoutError"
                      class="mt-4 rounded-2xl border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive"
                    >
                      {{ checkoutError?.message }}
                    </div>

                    <div class="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-end">
                      <button
                        type="button"
                        class="inline-flex items-center justify-center rounded-xl border border-border px-4 py-2 text-sm font-semibold text-muted-foreground transition hover:text-foreground"
                        @click="checkoutStep = 'review'"
                      >
                        Voltar para o carrinho
                      </button>
                      <button
                        type="button"
                        class="inline-flex items-center justify-center gap-2 rounded-xl bg-emerald-400 px-4 py-2 text-sm font-semibold text-emerald-950 shadow-sm transition hover:bg-emerald-300 disabled:opacity-60"
                        :disabled="!canGeneratePayment"
                        @click="handleGeneratePayment"
                      >
                        <CheckCircle2 class="h-4 w-4" />
                        Gerar pagamento PIX
                      </button>
                    </div>
                  </section>

                  <aside class="space-y-4">
                    <div class="rounded-3xl border border-border/60 bg-white/90 p-5 shadow-sm">
                      <p class="text-sm font-semibold text-foreground">Resumo</p>
                      <p class="text-3xl font-bold text-foreground">{{ formatCurrency(totalAmountInCents) }}</p>
                      <ul class="mt-3 space-y-2 text-xs text-muted-foreground">
                        <li v-for="item in items" :key="`identify-${item.productId}`">
                          {{ item.name }} · {{ Math.max(item.quantity, 0) }} un · {{ formatLineTotal(item.priceInCents, item.quantity) }}
                        </li>
                      </ul>
                    </div>
                    <div class="rounded-3xl border border-border/60 bg-white/90 p-5 shadow-sm">
                      <p class="text-sm font-semibold text-foreground">Dados obtidos do Google</p>
                      <p class="text-sm text-muted-foreground">
                        Usamos as informações fornecidas pela sua conta para ligar o pedido à sua reserva.
                      </p>
                    </div>
                  </aside>
                </div>

                <div v-else class="space-y-6">
                  <section class="rounded-3xl border border-brand-cyan/40 bg-white/95 p-6 shadow-[0_20px_60px_rgba(14,165,233,0.15)]">
                    <div class="flex flex-col gap-2 border-b border-border/40 pb-4">
                      <div class="flex flex-wrap items-center gap-3">
                        <p class="text-xs uppercase tracking-[0.3em] text-brand-cyan">Pagamento via PIX (mock)</p>
                        <span
                          v-if="activeOrder"
                          class="rounded-full bg-brand-cyan/10 px-3 py-1 text-xs font-semibold text-brand-cyan"
                        >
                          Pedido #{{ activeOrder.idOrder }}
                        </span>
                      </div>
                      <div class="flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
                        <div class="flex items-center gap-2">
                          <Clock3 class="h-4 w-4 text-brand-cyan" />
                          Código expira em <span class="font-semibold text-foreground">{{ countdownLabel }}</span>
                        </div>
                        <div class="text-xs text-muted-foreground">
                          Valor total: <span class="font-semibold text-foreground">{{ formatCurrency(totalAmountInCents) }}</span>
                        </div>
                      </div>
                      <div class="mt-4 h-2 w-full overflow-hidden rounded-full bg-muted">
                        <div
                          class="h-full rounded-full bg-gradient-to-r from-emerald-400 via-brand-cyan to-amber-300 transition-all"
                          :style="{ width: `${progressPercent}%` }"
                        />
                      </div>
                    </div>

                    <div class="mt-6 grid gap-5 lg:grid-cols-2">
                      <div class="rounded-3xl border border-brand-cyan/30 bg-gradient-to-b from-white to-sky-50 p-5 text-center shadow-[0_10px_40px_rgba(14,165,233,0.12)]">
                        <p class="text-sm font-semibold text-foreground">Escaneie o QR Code</p>
                        <div class="mt-4 flex justify-center">
                          <img
                            v-if="pixQrUrl"
                            :src="pixQrUrl"
                            alt="QR Code do PIX"
                            class="w-full max-w-xs rounded-2xl border border-border/50 bg-white p-3 shadow-inner"
                          >
                          <div v-else class="h-60 w-full max-w-xs rounded-2xl border border-dashed border-border/60 bg-muted"></div>
                        </div>
                      </div>
                      <div class="rounded-3xl border border-amber-200 bg-amber-50/80 p-5 shadow-[0_15px_35px_rgba(250,204,21,0.25)]">
                        <p class="text-sm font-semibold text-foreground">Pix copia e cola</p>
                        <p class="text-xs text-muted-foreground">Use esse código no app do seu banco.</p>
                        <pre class="mt-3 max-h-48 overflow-y-auto rounded-2xl border border-amber-200 bg-white/90 p-3 text-left text-xs font-mono text-foreground">{{ pixCode }}</pre>
                        <button
                          type="button"
                          class="mt-3 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-emerald-400 px-4 py-2 text-sm font-semibold text-emerald-950 shadow-sm transition hover:bg-emerald-300"
                          @click="handleCopyPix"
                        >
                          <Copy class="h-4 w-4" />
                          {{ copied ? 'Código copiado!' : 'Copiar código PIX' }}
                        </button>
                      </div>
                    </div>

                    <div
                      v-if="paymentExpired"
                      class="mt-4 flex items-start gap-2 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800"
                    >
                      <AlertTriangle class="h-4 w-4" />
                      <span>O tempo expirou. Gere um novo código para continuar.</span>
                    </div>

                    <div class="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-end">
                      <button
                        type="button"
                        class="inline-flex items-center justify-center rounded-xl border border-border px-4 py-2 text-sm font-semibold text-muted-foreground transition hover:text-foreground"
                        @click="checkoutStep = 'review'"
                      >
                        Voltar para o carrinho
                      </button>
                      <button
                        type="button"
                        class="inline-flex items-center justify-center rounded-xl border border-brand-cyan px-4 py-2 text-sm font-semibold text-brand-cyan transition hover:bg-brand-cyan/10 disabled:opacity-60"
                        :disabled="creatingOrder"
                        @click="handleGeneratePayment"
                      >
                        Gerar novo PIX
                      </button>
                    </div>
                  </section>

                  <aside class="space-y-4">
                    <div class="rounded-3xl border border-border/60 bg-white/90 p-5 shadow-sm">
                      <p class="text-sm font-semibold text-foreground">Resumo do pedido</p>
                      <div class="mt-3 space-y-2 text-sm">
                        <div class="flex items-center justify-between">
                          <span class="text-muted-foreground">Total</span>
                          <span class="font-semibold text-foreground">{{ formatCurrency(totalAmountInCents) }}</span>
                        </div>
                        <div class="flex items-center justify-between">
                          <span class="text-muted-foreground">Itens</span>
                          <span class="font-semibold text-foreground">{{ totalItems }}</span>
                        </div>
                      </div>
                      <ul class="mt-4 space-y-2 text-xs text-muted-foreground">
                        <li v-for="item in items" :key="`payment-${item.productId}`">
                          {{ item.name }} · {{ Math.max(item.quantity, 0) }} un · {{ formatLineTotal(item.priceInCents, item.quantity) }}
                        </li>
                      </ul>
                    </div>
                    <div class="rounded-3xl border border-border/60 bg-muted/30 p-5" v-if="customerName">
                      <p class="text-sm font-semibold text-foreground">Dados do comprador</p>
                      <p class="text-base text-foreground">{{ customerName }}</p>
                      <p class="text-sm text-muted-foreground">{{ customerEmail }}</p>
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
import type { Component } from 'vue'
import { useClipboard } from '@vueuse/core'
import { ArrowLeft, Trash2, RefreshCcw, Clock3, Copy, AlertTriangle, ShoppingCart, CheckCircle2, Sparkles, UserCheck, QrCode } from 'lucide-vue-next'
import type { OrderResponse } from '~/types/orders'

const onboardingRef = ref<any>(null)
const showOnboarding = () => onboardingRef.value?.show()

useSeoMeta({
  title: 'Carrinho de compras · Trinket Store',
  description: 'Revise seus itens e finalize o pedido com pagamento via PIX.',
})

const checkoutSteps: Array<{ id: CheckoutStepId; label: string; description: string; icon: Component }> = [
  { id: 'review', label: 'Carrinho', description: 'Revise os itens e estoque', icon: ShoppingCart },
  { id: 'identify', label: 'Identificação', description: 'Conecte-se com Google', icon: UserCheck },
  { id: 'payment', label: 'Pagamento', description: 'PIX válido por 5 minutos', icon: QrCode },
]

type CheckoutStepId = 'review' | 'identify' | 'payment'

const PAYMENT_WINDOW_SECONDS = 60 * 5

const {
  items,
  hasItems,
  totalItems,
  totalAmountInCents,
  updateItemQuantity,
  removeItem,
  clearCart,
  setItemStock,
} = useStorefrontCart()
const { fetchInventoryForProduct } = useStorefrontInventory()
const { formatCurrencyFromCents } = useFormatters()
const { user: supabaseUser, isAuthenticated, loading: authLoading, signInWithGoogle } = useSupabaseAuth()
const { profile, fetchProfile, loading: profileLoading, clearProfile } = useStorefrontCustomer()
const { createOrderFromCart, creating: creatingOrder, error: checkoutError } = useStorefrontCheckout()
const { copy, copied } = useClipboard()

const checkoutStep = ref<CheckoutStepId>('review')
const currentStepIndex = computed(() => checkoutSteps.findIndex((step) => step.id === checkoutStep.value))
const stepProgress = computed(() => {
  if (checkoutSteps.length <= 1) return 0
  return (currentStepIndex.value / (checkoutSteps.length - 1)) * 100
})

const stockRefreshing = ref(false)
const stockSyncError = ref<string | null>(null)
const pixCode = ref('')
const activeOrder = ref<OrderResponse | null>(null)
const paymentCountdown = ref(PAYMENT_WINDOW_SECONDS)
const paymentInterval = ref<ReturnType<typeof setInterval> | null>(null)
const paymentExpireTimeout = ref<ReturnType<typeof setTimeout> | null>(null)
const paymentExpired = ref(false)

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

const progressPercent = computed(() => (paymentCountdown.value / PAYMENT_WINDOW_SECONDS) * 100)
const pixQrUrl = computed(() =>
  pixCode.value ? `https://api.qrserver.com/v1/create-qr-code/?size=280x280&data=${encodeURIComponent(pixCode.value)}` : '',
)

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
  checkoutStep.value = 'identify'
  if (isAuthenticated.value && !profile.value) {
    fetchProfile().catch((error) => console.error('[cart] profile fetch failed', error))
  }
}

const handleGoogleLogin = async () => {
  if (isAuthenticated.value) return
  await signInWithGoogle()
}

const buildPixCode = (name?: string | null) => {
  const cleaned = (name || 'CLIENTE').toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 10) || 'CLIENTE'
  const randomSuffix = Math.random().toString(36).slice(2, 6).toUpperCase()
  const total = String(Math.max(totalAmountInCents.value, 0)).padStart(5, '0')
  return `00020126580014BR.GOV.BCB.PIX0114TRINKETSTORE520400005303986540${total}5802BR5915${cleaned.padEnd(10, 'X')}6009SAOPAULO62070503***6304${randomSuffix}`
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
}

const startPaymentFlow = () => {
  stopPaymentTimer()
  paymentCountdown.value = PAYMENT_WINDOW_SECONDS
  paymentExpired.value = false

  paymentInterval.value = setInterval(() => {
    paymentCountdown.value -= 1
    if (paymentCountdown.value <= 0) {
      paymentCountdown.value = 0
      paymentExpired.value = true
      stopPaymentTimer()
      paymentExpireTimeout.value = setTimeout(() => {
        checkoutStep.value = 'identify'
        activeOrder.value = null
        pixCode.value = ''
      }, 4000)
    }
  }, 1000)
}

const handleGeneratePayment = async () => {
  if (!canGeneratePayment.value || !profile.value) return

  try {
    const newPixCode = buildPixCode(profile.value.nomeUser)
    const order = await createOrderFromCart({
      userId: Number(profile.value.idUser),
      items: items.value,
      totalAmountInCents: totalAmountInCents.value,
      pixCode: newPixCode,
    })
    activeOrder.value = order
    pixCode.value = newPixCode
    checkoutStep.value = 'payment'
    paymentExpired.value = false
    startPaymentFlow()
  } catch (error) {
    console.error('[cart] order creation failed', error)
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
  stopPaymentTimer()
}

watch(isAuthenticated, (logged) => {
  if (logged) {
    fetchProfile().catch((error) => console.error('[cart] profile fetch failed', error))
  } else {
    clearProfile()
    activeOrder.value = null
    pixCode.value = ''
    stopPaymentTimer()
    if (checkoutStep.value !== 'review') {
      checkoutStep.value = 'review'
    }
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
  }
})

watch(checkoutStep, (step) => {
  if (step !== 'payment') {
    stopPaymentTimer()
    paymentExpired.value = false
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
})
</script>
