<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useClipboard } from '@vueuse/core'
import { CheckCircle2, CreditCard, Package, QrCode, Search, ShoppingBag, UserRound } from 'lucide-vue-next'
import { Button } from '~/components/ui/button'
import { Input } from '~/components/ui/input'
import { Label } from '~/components/ui/label'
import {
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '~/components/ui/table'
import { Skeleton } from '~/components/ui/skeleton'
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyTitle,
} from '~/components/ui/empty'
import { useToast } from '~/components/ui/toast/use-toast'
import { useStorefrontCatalog } from '~/composables/storefront/useStorefrontCatalog'
import type { StorefrontProduct } from '~/composables/storefront/useStorefrontCatalog'
import { useStorefrontInventory } from '~/composables/storefront/useStorefrontInventory'
import { useStorefrontCheckout } from '~/composables/storefront/useStorefrontCheckout'
import type { PixCheckoutResponse } from '~/types/orders'

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Novo pedido',
})

const router = useRouter()
const { toast } = useToast()
const { copy, copied } = useClipboard()
const { formatCurrencyFromCents, formatDateTime } = useFormatters()
const usersApi = useUsersList()
const { catalog, loading: catalogLoading, fetchCatalog } = useStorefrontCatalog()
const { inventoryMap, fetchInventoryForProduct } = useStorefrontInventory()
const {
  createOrderFromCart,
  creating: creatingOrder,
  error: checkoutError,
} = useStorefrontCheckout()

const steps = [
  { id: 'customer', title: 'Cliente', description: 'Defina quem receberá o pedido.', icon: UserRound },
  { id: 'items', title: 'Itens', description: 'Monte o carrinho com os produtos.', icon: ShoppingBag },
  { id: 'payment', title: 'Pagamento', description: 'Gere o PIX e finalize.', icon: CreditCard },
] as const

type StepId = (typeof steps)[number]['id']

type SelectedOrderItem = {
  id: number
  slug: string
  name: string
  description: string
  image: string
  priceInCents: number
  qty: number
  availableStock: number
}

const currentStep = ref<StepId>('customer')
const stepError = ref('')
const productSearch = ref('')
const selectedUserId = ref<number | null>(null)
const selectedItemsState = ref<Record<number, SelectedOrderItem>>({})
const paymentResult = ref<PixCheckoutResponse | null>(null)
const paymentError = ref('')

const filteredCatalog = computed(() => {
  const query = productSearch.value.trim().toLowerCase()
  if (!query) return catalog.value
  return catalog.value.filter((product) => {
    return product.name.toLowerCase().includes(query)
      || product.description.toLowerCase().includes(query)
      || product.category.toLowerCase().includes(query)
  })
})

const selectedItemsList = computed(() => Object.values(selectedItemsState.value))
const hasSelectedItems = computed(() => selectedItemsList.value.length > 0)
const selectedUser = computed(() =>
  usersApi.users.value.find((user) => user.idUser === selectedUserId.value),
)
const stepIndex = computed(() => Math.max(steps.findIndex((step) => step.id === currentStep.value), 0))
const progressPercent = computed(() => (steps.length <= 1 ? 100 : (stepIndex.value / (steps.length - 1)) * 100))
const orderTotal = computed(() =>
  selectedItemsList.value.reduce((sum, item) => sum + item.priceInCents * item.qty, 0),
)
const pixCode = computed(() => paymentResult.value?.pix?.qrCode ?? '')
const pixQrUrl = computed(() => {
  if (paymentResult.value?.pix?.qrCodeBase64) {
    return `data:image/png;base64,${paymentResult.value.pix.qrCodeBase64}`
  }
  if (pixCode.value) {
    return `https://api.qrserver.com/v1/create-qr-code/?size=280x280&data=${encodeURIComponent(pixCode.value)}`
  }
  return ''
})

const isProductSelected = (productId: number) => Boolean(selectedItemsState.value[productId])

const getShortDescription = (text: string) => {
  if (!text) return 'Sem descrição'
  return text.length > 70 ? `${text.slice(0, 70)}…` : text
}

const clampQuantity = (qty: number, max: number) => {
  const parsed = Number.isFinite(qty) ? qty : 1
  const upper = Math.max(max || 1, 1)
  return Math.min(Math.max(parsed, 1), upper)
}

const ensureInventory = async (productId: number) => {
  try {
    const cached = inventoryMap.value[productId]
    if (cached) return cached
    return await fetchInventoryForProduct(productId, { force: true })
  } catch (error) {
    console.error('[admin:orders:create] inventory fetch failed', error)
    return null
  }
}

const toggleProductSelection = async (product: StorefrontProduct) => {
  stepError.value = ''
  paymentResult.value = null

  if (!product.priceInCents) {
    toast({
      title: 'Preço indisponível',
      description: 'Defina um preço ativo antes de adicionar este produto ao pedido.',
      variant: 'destructive',
    })
    return
  }

  if (selectedItemsState.value[product.id]) {
    const updated = { ...selectedItemsState.value }
    delete updated[product.id]
    selectedItemsState.value = updated
    return
  }

  const snapshot = await ensureInventory(product.id)
  if (snapshot && snapshot.qtyOnHand <= 0) {
    toast({
      title: 'Sem estoque',
      description: 'Atualize o estoque deste item antes de adicioná-lo.',
      variant: 'destructive',
    })
    return
  }

  const availableStock = snapshot ? snapshot.qtyOnHand : 1

  selectedItemsState.value = {
    ...selectedItemsState.value,
    [product.id]: {
      id: product.id,
      slug: product.slug,
      name: product.name,
      description: product.description,
      image: product.image,
      priceInCents: product.priceInCents,
      qty: 1,
      availableStock,
    },
  }
}

const updateQuantity = (productId: number, qty: number) => {
  const current = selectedItemsState.value[productId]
  if (!current) return
  const sanitizedQty = clampQuantity(qty, current.availableStock || qty)
  selectedItemsState.value = {
    ...selectedItemsState.value,
    [productId]: {
      ...current,
      qty: sanitizedQty,
    },
  }
  paymentResult.value = null
}

const handleQtyInput = (productId: number, event: Event) => {
  const target = event.target as HTMLInputElement | null
  if (!target) return
  updateQuantity(productId, Number(target.value))
}

const removeSelectedItem = (productId: number) => {
  if (!selectedItemsState.value[productId]) return
  const updated = { ...selectedItemsState.value }
  delete updated[productId]
  selectedItemsState.value = updated
  paymentResult.value = null
}

const nextStep = () => {
  stepError.value = ''
  if (currentStep.value === 'customer' && !selectedUserId.value) {
    stepError.value = 'Selecione um cliente.'
    return
  }
  if (currentStep.value === 'items' && !hasSelectedItems.value) {
    stepError.value = 'Adicione ao menos um produto com estoque.'
    return
  }
  const next = steps[stepIndex.value + 1]
  if (next) currentStep.value = next.id
}

const prevStep = () => {
  stepError.value = ''
  const previous = steps[stepIndex.value - 1]
  if (previous) currentStep.value = previous.id
}

const handleGeneratePayment = async () => {
  paymentError.value = ''
  stepError.value = ''

  if (!selectedUserId.value) {
    stepError.value = 'Escolha um cliente antes de gerar pagamento.'
    currentStep.value = 'customer'
    return
  }
  if (!hasSelectedItems.value) {
    stepError.value = 'Selecione produtos para gerar o pedido.'
    currentStep.value = 'items'
    return
  }

  try {
    const response = await createOrderFromCart({
      userId: selectedUserId.value,
      items: selectedItemsList.value.map((item) => ({
        productId: item.id,
        slug: item.slug,
        name: item.name,
        image: item.image,
        priceInCents: item.priceInCents,
        quantity: item.qty,
        availableStock: Math.max(item.availableStock, item.qty),
      })),
      totalAmountInCents: orderTotal.value,
      description: `Pedido via painel para ${selectedUser.value?.nomeUser ?? 'cliente'}`,
    })
    paymentResult.value = response
    toast({
      title: 'Pedido criado',
      description: `PIX gerado para o pedido #${response.order.idOrder}.`,
    })
  } catch (error) {
    console.error('[admin:orders:create] payment generation failed', error)
    paymentError.value = checkoutError.value?.message || 'Não foi possível gerar o pagamento agora.'
  }
}

watch(selectedUserId, (value) => {
  if (value && currentStep.value === 'customer') {
    stepError.value = ''
    currentStep.value = 'items'
  }
})

watch(
  selectedItemsState,
  () => {
    paymentError.value = ''
    paymentResult.value = null
  },
  { deep: true },
)

onMounted(async () => {
  await Promise.all([usersApi.fetchUsers(), fetchCatalog(true)])
})
</script>

<template>
  <div class="flex flex-1 flex-col gap-6">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold">Criar novo pedido</h1>
        <p class="text-sm text-muted-foreground">Siga o mesmo fluxo do ecommerce para registrar pedidos.</p>
      </div>
      <div class="text-sm text-muted-foreground">
        Etapa {{ stepIndex + 1 }} / {{ steps.length }}
      </div>
    </div>

    <div class="rounded-2xl border bg-card p-5 shadow-sm">
      <div class="relative py-4">
        <div class="absolute left-4 right-4 top-9 h-1 rounded-full bg-muted">
          <div
            class="h-1 rounded-full bg-primary transition-all"
            :style="{ width: `${progressPercent}%` }"
          />
        </div>
        <ol class="relative z-10 flex flex-col gap-6 px-2 sm:flex-row sm:items-start sm:justify-between">
          <li
            v-for="step in steps"
            :key="step.id"
            class="flex flex-1 flex-col items-center text-center"
          >
            <div
              class="flex size-12 items-center justify-center rounded-full border-2 text-sm font-semibold transition"
              :class="[
                stepIndex > steps.indexOf(step) ? 'border-emerald-400 bg-emerald-50 text-emerald-700' : '',
                currentStep === step.id ? 'border-primary bg-primary/10 text-primary' : '',
                stepIndex < steps.indexOf(step) ? 'border-muted text-muted-foreground' : '',
              ]"
            >
              <CheckCircle2
                v-if="stepIndex > steps.indexOf(step)"
                class="h-5 w-5"
              />
              <component v-else :is="step.icon" class="h-5 w-5" />
            </div>
            <p class="mt-2 text-xs font-semibold uppercase tracking-wide">{{ step.title }}</p>
            <p class="text-[11px] text-muted-foreground max-w-[180px]">{{ step.description }}</p>
          </li>
        </ol>
      </div>
    </div>

    <div class="rounded-2xl border bg-card p-6 shadow-sm space-y-6">
      <div v-if="currentStep === 'customer'" class="space-y-5">
        <div>
          <h2 class="text-lg font-semibold">1. Selecione o cliente</h2>
          <p class="text-sm text-muted-foreground">
            Apenas administradores geram pedidos em nome dos clientes. Selecione o usuário para continuar.
          </p>
        </div>
        <div class="space-y-2">
          <Label class="text-xs text-muted-foreground uppercase tracking-wide">Cliente</Label>
          <select
            v-model="selectedUserId"
            class="h-12 w-full rounded-xl border bg-background px-3 text-sm focus:border-primary focus:outline-none"
          >
            <option :value="null">Selecione</option>
            <option
              v-for="user in usersApi.users.value"
              :key="user.idUser"
              :value="user.idUser"
            >
              {{ user.nomeUser }} (#{{ user.idUser }})
            </option>
          </select>
        </div>
        <div
          v-if="selectedUser"
          class="rounded-2xl border bg-muted/40 p-4 text-sm"
        >
          <p class="text-xs uppercase text-muted-foreground">Pedido para</p>
          <p class="text-lg font-semibold">{{ selectedUser.nomeUser }}</p>
          <p class="text-xs text-muted-foreground">ID {{ selectedUser.idUser }}</p>
        </div>
      </div>

      <div v-else-if="currentStep === 'items'" class="space-y-6">
        <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <h2 class="text-lg font-semibold">2. Monte o pedido</h2>
            <p class="text-sm text-muted-foreground">Selecione produtos, confira estoque e ajuste quantidades.</p>
          </div>
          <div
            v-if="selectedUser"
            class="rounded-2xl border bg-muted/40 px-4 py-2 text-sm"
          >
            <p class="text-xs uppercase text-muted-foreground">Cliente</p>
            <p class="font-semibold">{{ selectedUser.nomeUser }} (#{{ selectedUser.idUser }})</p>
          </div>
        </div>

        <div class="space-y-4">
          <div class="relative">
            <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              v-model="productSearch"
              placeholder="Busque por nome, categoria ou descrição"
              class="pl-9"
            />
          </div>

          <div v-if="catalogLoading" class="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
            <div v-for="index in 6" :key="index" class="rounded-2xl border bg-muted/30 p-4">
              <Skeleton class="h-24 w-full" />
              <Skeleton class="mt-3 h-4 w-3/4" />
              <Skeleton class="mt-2 h-4 w-1/2" />
            </div>
          </div>

          <template v-else>
            <div
              v-if="filteredCatalog.length"
              class="grid gap-3 sm:grid-cols-2 xl:grid-cols-3"
            >
              <label
                v-for="product in filteredCatalog"
                :key="product.id"
                class="rounded-2xl border bg-background p-4 transition hover:border-primary/60 focus-within:ring-2 focus-within:ring-primary/20"
                :class="isProductSelected(product.id) ? 'border-primary bg-primary/5' : ''"
              >
                <input
                  type="checkbox"
                  class="sr-only"
                  :checked="isProductSelected(product.id)"
                  :aria-label="`Selecionar ${product.name}`"
                  @change.prevent="toggleProductSelection(product)"
                />
                <div class="flex items-start gap-3">
                  <div class="flex size-16 items-center justify-center overflow-hidden rounded-xl border bg-muted/50">
                    <img
                      v-if="product.image"
                      :src="product.image"
                      :alt="product.name"
                      class="h-full w-full object-cover"
                      loading="lazy"
                    />
                    <Package v-else class="h-5 w-5 text-muted-foreground" />
                  </div>
                  <div class="flex-1 space-y-1">
                    <p class="font-semibold leading-tight">{{ product.name }}</p>
                    <p class="text-xs text-muted-foreground leading-snug">
                      {{ getShortDescription(product.description) }}
                    </p>
                    <p class="text-[11px] text-muted-foreground">
                      Estoque:
                      <span class="font-semibold text-foreground">
                        {{ inventoryMap[product.id]?.qtyOnHand ?? '—' }}
                      </span>
                    </p>
                  </div>
                </div>
                <div class="mt-3 flex items-center justify-between text-sm font-semibold">
                  <span>
                    {{ product.priceInCents ? formatCurrencyFromCents(product.priceInCents) : 'Sem preço' }}
                  </span>
                  <span
                    class="text-xs"
                    :class="isProductSelected(product.id) ? 'text-primary' : 'text-muted-foreground'"
                  >
                    {{ isProductSelected(product.id) ? 'Adicionado' : 'Selecionar' }}
                  </span>
                </div>
                <p v-if="!product.priceInCents" class="mt-2 text-xs text-amber-600">
                  Cadastre um preço vigente para disponibilizar este item.
                </p>
              </label>
            </div>
            <Empty v-else class="rounded-2xl border border-dashed py-10">
              <EmptyHeader>
                <EmptyTitle>Nenhum produto encontrado</EmptyTitle>
                <EmptyDescription>
                  Ajuste o termo de busca ou ative novos itens no catálogo.
                </EmptyDescription>
              </EmptyHeader>
            </Empty>
          </template>
        </div>

        <div class="rounded-2xl border p-4 shadow-inner space-y-4">
          <div class="flex items-center justify-between">
            <h3 class="text-base font-semibold">Itens selecionados</h3>
            <span class="text-sm text-muted-foreground">
              {{ selectedItemsList.length }} {{ selectedItemsList.length === 1 ? 'item' : 'itens' }}
            </span>
          </div>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Produto</TableHead>
                <TableHead class="w-28">Qtd</TableHead>
                <TableHead>Valor</TableHead>
                <TableHead class="text-right">Subtotal</TableHead>
                <TableHead class="sr-only">Remover</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <template v-if="hasSelectedItems">
                <TableRow v-for="item in selectedItemsList" :key="item.id">
                  <TableCell>
                    <div class="font-semibold">{{ item.name }}</div>
                    <p class="text-xs text-muted-foreground">{{ getShortDescription(item.description) }}</p>
                    <p class="text-[11px] text-muted-foreground">Estoque: {{ item.availableStock }}</p>
                  </TableCell>
                  <TableCell>
                    <Input
                      type="number"
                      min="1"
                      class="h-9"
                      :max="item.availableStock || undefined"
                      :value="item.qty"
                      @input="handleQtyInput(item.id, $event)"
                    />
                  </TableCell>
                  <TableCell>{{ formatCurrencyFromCents(item.priceInCents) }}</TableCell>
                  <TableCell class="text-right font-semibold">
                    {{ formatCurrencyFromCents(item.priceInCents * item.qty) }}
                  </TableCell>
                  <TableCell class="text-right">
                    <Button variant="ghost" size="sm" @click="removeSelectedItem(item.id)">
                      Remover
                    </Button>
                  </TableCell>
                </TableRow>
              </template>
              <TableEmpty v-else :colspan="5">
                <Empty>
                  <EmptyHeader>
                    <EmptyTitle>Nenhum item escolhido</EmptyTitle>
                    <EmptyDescription>Selecione um produto do catálogo acima.</EmptyDescription>
                  </EmptyHeader>
                </Empty>
              </TableEmpty>
            </TableBody>
          </Table>
          <div class="flex items-center justify-between border-t pt-4 text-sm">
            <span class="text-muted-foreground">Total parcial</span>
            <span class="text-base font-semibold">{{ formatCurrencyFromCents(orderTotal) }}</span>
          </div>
        </div>
      </div>

      <div v-else class="space-y-6">
        <div>
          <h2 class="text-lg font-semibold">3. Pagamento PIX</h2>
          <p class="text-sm text-muted-foreground">Geraremos o checkout usando o mesmo fluxo do ecommerce.</p>
        </div>
        <div class="grid gap-6 lg:grid-cols-[1.5fr,1fr]">
          <div class="rounded-2xl border p-4 space-y-4">
            <div class="flex items-center justify-between">
              <h3 class="font-semibold">Itens</h3>
              <span class="text-sm text-muted-foreground">{{ selectedItemsList.length }} selecionados</span>
            </div>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Produto</TableHead>
                  <TableHead class="w-24">Qtd</TableHead>
                  <TableHead class="text-right">Subtotal</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <template v-if="hasSelectedItems">
                  <TableRow v-for="item in selectedItemsList" :key="item.id">
                    <TableCell>
                      <div class="font-semibold">{{ item.name }}</div>
                      <p class="text-xs text-muted-foreground">{{ getShortDescription(item.description) }}</p>
                    </TableCell>
                    <TableCell>{{ item.qty }} un</TableCell>
                    <TableCell class="text-right font-semibold">
                      {{ formatCurrencyFromCents(item.priceInCents * item.qty) }}
                    </TableCell>
                  </TableRow>
                </template>
                <TableEmpty v-else :colspan="3">
                  <Empty>
                    <EmptyHeader>
                      <EmptyTitle>Nenhum item selecionado</EmptyTitle>
                      <EmptyDescription>Retorne e escolha pelo menos um produto.</EmptyDescription>
                    </EmptyHeader>
                  </Empty>
                </TableEmpty>
              </TableBody>
            </Table>
          </div>

          <div class="space-y-4 rounded-2xl border p-4">
            <div class="space-y-1">
              <p class="text-xs uppercase text-muted-foreground">Cliente</p>
              <p class="text-lg font-semibold">{{ selectedUser?.nomeUser || 'Selecione um cliente' }}</p>
              <p class="text-xs text-muted-foreground">ID {{ selectedUser?.idUser || '—' }}</p>
            </div>
            <div class="space-y-2 text-sm">
              <div class="flex items-center justify-between">
                <span class="text-muted-foreground">Itens</span>
                <span class="font-semibold">{{ selectedItemsList.length }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-muted-foreground">Total</span>
                <span class="font-semibold">{{ formatCurrencyFromCents(orderTotal) }}</span>
              </div>
            </div>
            <div class="space-y-3 rounded-xl border bg-muted/30 p-3 text-sm">
              <p class="text-sm font-semibold text-foreground">Gerar pagamento</p>
              <p class="text-xs text-muted-foreground">
                Criaremos o pedido, checkout e payment intent automaticamente, igual ao fluxo do cliente.
              </p>
              <div class="flex flex-col gap-2 sm:flex-row">
                <Button
                  type="button"
                  class="flex-1"
                  :disabled="creatingOrder.value"
                  @click="handleGeneratePayment"
                >
                  {{ creatingOrder.value ? 'Gerando PIX...' : 'Gerar PIX' }}
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  class="flex-1"
                  :disabled="creatingOrder.value"
                  @click="router.push('/sistema/pedidos')"
                >
                  Ver listagem
                </Button>
              </div>
              <p v-if="paymentError || checkoutError?.message" class="text-xs text-destructive">
                {{ paymentError || checkoutError?.message }}
              </p>
            </div>

            <div
              v-if="paymentResult"
              class="space-y-3 rounded-xl border bg-muted/40 p-4"
            >
              <div class="flex items-start justify-between gap-3">
                <div>
                  <p class="text-xs uppercase text-muted-foreground">Pedido criado</p>
                  <p class="text-lg font-semibold">#{{ paymentResult.order.idOrder }}</p>
                  <p class="text-xs text-muted-foreground">Checkout: {{ paymentResult.order.checkoutId }}</p>
                  <p class="text-xs text-muted-foreground">Payment intent: {{ paymentResult.order.paymentIntent }}</p>
                </div>
                <span class="rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">
                  PIX gerado
                </span>
              </div>

              <div class="rounded-xl border bg-background p-3">
                <div class="flex items-center gap-3">
                  <div class="flex h-24 w-24 items-center justify-center overflow-hidden rounded-lg border bg-white">
                    <img
                      v-if="pixQrUrl"
                      :src="pixQrUrl"
                      alt="QR Code PIX"
                      class="h-full w-full object-contain"
                    />
                    <QrCode v-else class="h-8 w-8 text-muted-foreground" />
                  </div>
                  <div class="flex-1 space-y-1 text-sm">
                    <p class="font-semibold">Código PIX</p>
                    <p class="text-xs break-all text-muted-foreground">{{ pixCode || 'Aguardando geração' }}</p>
                    <div class="flex items-center gap-2 pt-1">
                      <Button variant="secondary" size="sm" :disabled="!pixCode" @click="copy(pixCode)">
                        {{ copied ? 'Copiado' : 'Copiar código' }}
                      </Button>
                      <Button variant="outline" size="sm" as-child>
                        <NuxtLink :to="`/sistema/pedidos/${paymentResult.order.idOrder}`">
                          Ver pedido
                        </NuxtLink>
                      </Button>
                    </div>
                  </div>
                </div>
                <p v-if="paymentResult.pix?.expiresAt" class="mt-2 text-[11px] text-muted-foreground">
                  Expira em: {{ formatDateTime(paymentResult.pix.expiresAt) }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <p v-if="stepError" class="text-sm text-destructive">{{ stepError }}</p>

      <div class="flex items-center justify-between pt-2">
        <Button variant="ghost" type="button" :disabled="stepIndex === 0" @click="prevStep">
          Voltar
        </Button>
        <Button
          v-if="currentStep !== 'payment'"
          type="button"
          @click="nextStep"
        >
          {{ currentStep === 'customer' ? 'Ir para produtos' : 'Ir para pagamento' }}
        </Button>
        <Button
          v-else
          type="button"
          :disabled="creatingOrder.value"
          @click="handleGeneratePayment"
        >
          {{ creatingOrder.value ? 'Gerando PIX...' : 'Gerar pagamento' }}
        </Button>
      </div>
    </div>
  </div>
</template>
