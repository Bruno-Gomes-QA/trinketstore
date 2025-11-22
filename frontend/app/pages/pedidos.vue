<template>
  <div class="min-h-screen bg-[#f8fbff]">
    <div class="flex min-h-screen flex-col">
      <StoreHeader :show-how-it-works="false" />

      <main class="flex-1 py-8 sm:py-12">
        <div class="container mx-auto px-4">
          <div class="mb-6 flex items-center justify-between gap-3">
            <NuxtLink
              to="/"
              class="inline-flex items-center gap-2 rounded-xl border border-border px-3 py-2 text-sm font-semibold text-muted-foreground transition hover:border-brand-cyan hover:text-brand-cyan"
            >
              <ArrowLeft class="h-4 w-4" />
              Voltar à loja
            </NuxtLink>
            <button
              v-if="isAuthenticated"
              type="button"
              class="inline-flex items-center gap-2 rounded-xl border border-brand-cyan px-4 py-2 text-sm font-semibold text-brand-cyan transition hover:bg-brand-cyan/10 disabled:opacity-60"
              :disabled="ordersManager.loadingOrders.value"
              @click="handleFetchOrders"
            >
              <RefreshCcw class="h-4 w-4" :class="ordersManager.loadingOrders.value ? 'animate-spin' : ''" />
              Atualizar Pedidos
            </button>
          </div>

          <div v-if="!isAuthenticated" class="rounded-3xl border border-border/60 bg-white/95 p-6 shadow-lg shadow-sky-100/80 sm:p-8">
            <p class="text-xs font-semibold uppercase tracking-[0.3em] text-brand-cyan">Área do cliente</p>
            <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">Entre para acompanhar seus pedidos</h1>
            <div class="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center">
              <button
                type="button"
                class="inline-flex items-center justify-center gap-2 rounded-xl bg-brand-cyan px-5 py-3 text-sm font-semibold text-foreground shadow hover:bg-brand-cyan/90 disabled:opacity-60"
                :disabled="authLoading"
                @click="handleLogin"
              >
                <Icon name="logos:google-icon" class="h-5 w-5" />
                Entrar com Google
              </button>
              <p class="text-xs text-muted-foreground">Precisa de ajuda? Abra o onboarding no topo.</p>
            </div>
          </div>

          <div v-else class="space-y-5">
            <OrdersHeader
              :orders-count="ordersManager.orders.value.length"
              :last-update="polling.lastStatusUpdate.value"
              :loading="selectedOrderLoading"
              @refresh="handleRefreshStatus"
            />

            <div
              v-if="ordersManager.ordersError.value"
              class="rounded-2xl border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive"
            >
              {{ ordersManager.ordersError.value }}
            </div>

            <div
              v-if="ordersManager.loadingOrders.value || profileLoading"
              class="space-y-3 rounded-3xl border border-border/60 bg-white/90 p-5 shadow-sm"
            >
              <div class="h-14 w-full animate-pulse rounded-xl bg-muted" />
              <div class="h-14 w-full animate-pulse rounded-xl bg-muted" />
              <div class="h-14 w-full animate-pulse rounded-xl bg-muted" />
            </div>

            <div
              v-else-if="!ordersManager.orders.value.length"
              class="rounded-3xl border border-dashed border-border bg-white/90 p-6 text-center shadow-inner"
            >
              <ShoppingBag class="mx-auto h-8 w-8 text-muted-foreground" />
              <p class="mt-3 text-base font-semibold text-foreground">Nenhum pedido por aqui</p>
              <p class="mt-1 text-sm text-muted-foreground">Volte à vitrine, escolha algo e gere um PIX.</p>
              <div class="mt-4 flex justify-center gap-3">
                <NuxtLink
                  to="/"
                  class="inline-flex items-center justify-center rounded-xl border border-border px-4 py-2 text-sm font-semibold text-foreground transition hover:border-brand-cyan hover:text-brand-cyan"
                >
                  Explorar produtos
                </NuxtLink>
                <NuxtLink
                  to="/carrinho"
                  class="inline-flex items-center justify-center rounded-xl bg-brand-cyan px-4 py-2 text-sm font-semibold text-foreground shadow hover:bg-brand-cyan/90"
                >
                  Abrir carrinho
                </NuxtLink>
              </div>
            </div>

            <OrdersList
              v-if="ordersManager.orders.value.length"
              :orders="ordersManager.orders.value"
              :selected-order-id="ordersManager.highlightedOrderId.value"
              @select="handleSelectOrder"
            />

            <OrderDetails
              v-if="ordersManager.selectedOrder.value"
              :order="ordersManager.selectedOrder.value"
              :product-cache="ordersManager.productCache.value"
              :canceling="canceling"
              @cancel="showCancelDialog = true"
            />

            <div v-else-if="ordersManager.orders.value.length" class="rounded-3xl border border-dashed border-border bg-white/90 p-8 text-center shadow-inner">
              <ShoppingBag class="mx-auto h-10 w-10 text-muted-foreground" />
              <p class="mt-3 text-base font-semibold text-foreground">Selecione um pedido</p>
              <p class="mt-1 text-sm text-muted-foreground">Toque em um pedido acima para ver os detalhes</p>
            </div>
          </div>
        </div>
      </main>
    </div>

    <OrderCancelDialog
      v-model:open="showCancelDialog"
      :canceling="canceling"
      @confirm="handleCancelOrder"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, RefreshCcw, ShoppingBag } from 'lucide-vue-next'
import {
  OrdersList,
  OrderDetails,
  OrdersHeader,
  OrderCancelDialog,
} from '~/components/store/pedidos'

const route = useRoute()
const router = useRouter()
const { isAuthenticated, loading: authLoading, signInWithGoogle } = useSupabaseAuth()
const { profile, fetchProfile, loading: profileLoading } = useStorefrontCustomer()

const ordersManager = useOrdersManager()
const polling = useOrdersPolling()
const canceling = ref(false)
const showCancelDialog = ref(false)
const selectedOrderLoading = ref(false)

const preferredOrderId = computed(() => {
  const raw = route.query.orderId
  const value = Array.isArray(raw) ? raw[0] : raw
  const parsed = value ? Number(value) : NaN
  return Number.isFinite(parsed) ? parsed : null
})

const handleLogin = async () => {
  await signInWithGoogle()
}

const handleFetchOrders = async () => {
  if (!profile.value?.idUser) return
  await ordersManager.fetchOrders(profile.value.idUser)
  
  if (ordersManager.highlightedOrderId.value) {
    await ordersManager.preloadItems(ordersManager.highlightedOrderId.value)
    const order = ordersManager.orders.value.find((o: any) => o.idOrder === ordersManager.highlightedOrderId.value)
    if (order) {
      polling.startOrderPolling(
        ordersManager.highlightedOrderId.value,
        order.statusOrder,
        ordersManager.updateOrderInList
      )
    }
  }
}

const handleSelectOrder = (orderId: number) => {
  ordersManager.selectOrder(orderId)
  router.replace({ query: { ...route.query, orderId } })
  
  const order = ordersManager.orders.value.find((o: any) => o.idOrder === orderId)
  if (order) {
    polling.startOrderPolling(orderId, order.statusOrder, ordersManager.updateOrderInList)
  }
}

const handleRefreshStatus = async () => {
  if (!ordersManager.selectedOrder.value) return
  selectedOrderLoading.value = true
  await polling.pollOrderStatus(
    ordersManager.selectedOrder.value.idOrder,
    ordersManager.updateOrderInList
  )
  selectedOrderLoading.value = false
}

const handleCancelOrder = async () => {
  if (!ordersManager.selectedOrder.value) return
  
  canceling.value = true
  const success = await ordersManager.cancelOrder(ordersManager.selectedOrder.value.idOrder)
  canceling.value = false
  
  if (success) {
    showCancelDialog.value = false
    polling.stopOrderPolling()
  }
}

watch(
  () => route.query.orderId,
  () => {
    if (preferredOrderId.value) {
      ordersManager.highlightedOrderId.value = preferredOrderId.value
    }
  },
  { immediate: true },
)

watch(
  () => profile.value?.idUser,
  async (id) => {
    if (id) {
      await handleFetchOrders()
    } else {
      ordersManager.orders.value = []
      ordersManager.highlightedOrderId.value = null
      polling.stopOrderPolling()
    }
  },
)

watch(
  () => isAuthenticated.value,
  async (logged) => {
    if (logged && !profile.value) {
      await fetchProfile().catch((error) => console.error('[orders] profile fetch failed', error))
      await handleFetchOrders()
    }
    if (!logged) {
      ordersManager.orders.value = []
      ordersManager.highlightedOrderId.value = null
      polling.stopOrderPolling()
    }
  },
)

onMounted(async () => {
  if (isAuthenticated.value && !profile.value) {
    await fetchProfile().catch((error) => console.error('[orders] profile fetch failed', error))
  }
  if (profile.value?.idUser) {
    await handleFetchOrders()
  }
})
</script>
