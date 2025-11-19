<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { CheckCircle2, ClipboardCheck, CreditCard, Package, Search, ShoppingBag, UserRound } from 'lucide-vue-next'
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

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Novo pedido',
})

const router = useRouter()
const { toast } = useToast()
const { formatCurrencyFromCents } = useFormatters()
const ordersApi = useOrderMutations()
const usersApi = useUsersList()
const { catalog, loading: catalogLoading, fetchCatalog } = useStorefrontCatalog()

const steps = [
  { id: 1, title: 'Cliente', description: 'Selecione quem receberá o pedido.', icon: UserRound },
  { id: 2, title: 'Itens', description: 'Monte o catálogo personalizado.', icon: ShoppingBag },
  { id: 3, title: 'Pagamento', description: 'Associe checkout e intent.', icon: CreditCard },
  { id: 4, title: 'Revisão', description: 'Confirme todos os dados.', icon: ClipboardCheck },
] as const

type SelectedOrderItem = {
  id: number
  name: string
  description: string
  image: string
  priceInCents: number
  qty: number
}

const currentStep = ref(1)
const stepError = ref('')
const selectedUserId = ref<number | null>(null)
const productSearch = ref('')
const paymentForm = reactive({
  checkoutId: '',
  paymentIntent: '',
  currencyOrder: 'BRL',
})

const selectedItemsState = ref<Record<number, SelectedOrderItem>>({})

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

const progressPercent = computed(() => {
  if (steps.length <= 1) return 100
  return ((currentStep.value - 1) / (steps.length - 1)) * 100
})

const orderTotal = computed(() =>
  selectedItemsList.value.reduce((sum, item) => sum + item.priceInCents * item.qty, 0),
)

const selectedUser = computed(() =>
  usersApi.users.value.find((user) => user.idUser === selectedUserId.value),
)

const isProductSelected = (productId: number) => Boolean(selectedItemsState.value[productId])

const getShortDescription = (text: string) => {
  if (!text) return 'Sem descrição'
  return text.length > 70 ? `${text.slice(0, 70)}…` : text
}

const toggleProductSelection = (product: StorefrontProduct) => {
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

  const updated = { ...selectedItemsState.value }
  updated[product.id] = {
    id: product.id,
    name: product.name,
    description: product.description,
    image: product.image,
    priceInCents: product.priceInCents,
    qty: 1,
  }
  selectedItemsState.value = updated
}

const updateQuantity = (productId: number, qty: number) => {
  const current = selectedItemsState.value[productId]
  if (!current) return
  const sanitizedQty = Number.isNaN(qty) ? current.qty : Math.max(1, qty)
  selectedItemsState.value = {
    ...selectedItemsState.value,
    [productId]: {
      ...current,
      qty: sanitizedQty,
    },
  }
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
}

const nextStep = () => {
  stepError.value = ''
  if (currentStep.value === 1 && !selectedUserId.value) {
    stepError.value = 'Selecione um cliente.'
    return
  }
  if (currentStep.value === 2 && !hasSelectedItems.value) {
    stepError.value = 'Adicione ao menos um produto com preço ativo.'
    return
  }
  if (currentStep.value === 3 && (!paymentForm.checkoutId || !paymentForm.paymentIntent)) {
    stepError.value = 'Informe checkout e payment intent.'
    return
  }
  currentStep.value = Math.min(currentStep.value + 1, 4)
}

const prevStep = () => {
  stepError.value = ''
  currentStep.value = Math.max(currentStep.value - 1, 1)
}

const createOrder = async () => {
  if (!selectedUserId.value || !hasSelectedItems.value) {
    stepError.value = 'Preencha as etapas anteriores.'
    return
  }
  const payload = {
    userId: selectedUserId.value,
    statusOrder: 'pending' as const,
    totalOrders: orderTotal.value,
    currencyOrder: paymentForm.currencyOrder,
    checkoutId: paymentForm.checkoutId,
    paymentIntent: paymentForm.paymentIntent,
    items: selectedItemsList.value.map((item) => ({
      productId: item.id,
      qtyItems: item.qty,
      unitAmount: item.priceInCents,
      subtotalAmount: item.priceInCents * item.qty,
    })),
  }

  const created = await ordersApi.createOrder(payload)
  if (created) {
    toast({ title: 'Pedido criado', description: `Pedido #${created.idOrder} registrado.` })
    router.push(`/sistema/pedidos/${created.idOrder}`)
  }
}

watch(selectedUserId, (value) => {
  if (value && currentStep.value === 1) {
    stepError.value = ''
    currentStep.value = 2
  }
})

onMounted(async () => {
  await Promise.all([usersApi.fetchUsers(), fetchCatalog(true)])
})
</script>

<template>
  <div class="flex flex-1 flex-col gap-6">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold">Criar novo pedido</h1>
        <p class="text-sm text-muted-foreground">Acompanhe cada etapa e mantenha o cliente informado.</p>
      </div>
      <div class="text-sm text-muted-foreground">
        Etapa {{ currentStep }} / 4
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
                currentStep > step.id ? 'border-emerald-400 bg-emerald-50 text-emerald-700' : '',
                currentStep === step.id ? 'border-primary bg-primary/10 text-primary' : '',
                currentStep < step.id ? 'border-muted text-muted-foreground' : '',
              ]"
            >
              <CheckCircle2
                v-if="currentStep > step.id"
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
      <div v-if="currentStep === 1" class="space-y-5">
        <div>
          <h2 class="text-lg font-semibold">1. Selecione o cliente</h2>
          <p class="text-sm text-muted-foreground">
            Apenas usuários administradores podem gerar pedidos em nome dos clientes.
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

      <div v-else-if="currentStep === 2" class="space-y-6">
        <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <h2 class="text-lg font-semibold">2. Monte o pedido</h2>
            <p class="text-sm text-muted-foreground">Selecione produtos e ajuste quantidades antes de seguir.</p>
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
                  </TableCell>
                  <TableCell>
                    <Input
                      type="number"
                      min="1"
                      class="h-9"
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

      <div v-else-if="currentStep === 3" class="space-y-6">
        <div>
          <h2 class="text-lg font-semibold">3. Dados de pagamento</h2>
          <p class="text-sm text-muted-foreground">Associe as referências do Stripe para conciliar o pedido.</p>
        </div>
        <div class="grid gap-4 md:grid-cols-2">
          <div class="space-y-2">
            <Label for="checkout">Checkout ID</Label>
            <Input id="checkout" v-model="paymentForm.checkoutId" placeholder="chk_123" />
          </div>
          <div class="space-y-2">
            <Label for="payment-intent">Payment Intent</Label>
            <Input id="payment-intent" v-model="paymentForm.paymentIntent" placeholder="pi_123" />
          </div>
        </div>
        <div class="rounded-2xl border bg-muted/40 p-4 text-sm">
          <p class="text-xs uppercase text-muted-foreground">Resumo financeiro</p>
          <p class="text-2xl font-semibold">{{ formatCurrencyFromCents(orderTotal) }}</p>
          <p class="text-xs text-muted-foreground">
            Confirme se o valor bate com o checkout vinculado antes de avançar.
          </p>
        </div>
      </div>

      <div v-else class="space-y-6">
        <div>
          <h2 class="text-lg font-semibold">4. Revisão final</h2>
          <p class="text-sm text-muted-foreground">Confira cliente, itens, pagamento e total antes de criar o pedido.</p>
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
            <div>
              <p class="text-xs uppercase text-muted-foreground">Cliente</p>
              <p class="text-lg font-semibold">{{ selectedUser?.nomeUser || '—' }}</p>
              <p class="text-xs text-muted-foreground">ID {{ selectedUser?.idUser || '—' }}</p>
            </div>
            <div class="space-y-2 text-sm">
              <div class="flex items-center justify-between">
                <span class="text-muted-foreground">Checkout ID</span>
                <span class="font-semibold">{{ paymentForm.checkoutId || '—' }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-muted-foreground">Payment Intent</span>
                <span class="font-semibold">{{ paymentForm.paymentIntent || '—' }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-muted-foreground">Itens</span>
                <span class="font-semibold">{{ selectedItemsList.length }}</span>
              </div>
            </div>
            <div class="border-t pt-4 text-base font-semibold">
              <div class="flex items-center justify-between">
                <span>Total do pedido</span>
                <span>{{ formatCurrencyFromCents(orderTotal) }}</span>
              </div>
              <p class="mt-1 text-xs text-muted-foreground">
                Pagamentos são conciliados em BRL ({{ paymentForm.currencyOrder }}).
              </p>
            </div>
          </div>
        </div>
      </div>

      <p v-if="stepError" class="text-sm text-destructive">{{ stepError }}</p>

      <div class="flex items-center justify-between pt-2">
        <Button variant="ghost" type="button" :disabled="currentStep === 1" @click="prevStep">
          Voltar
        </Button>
        <Button
          v-if="currentStep < 4"
          type="button"
          @click="nextStep"
        >
          {{ currentStep === 1 ? 'Ir para produtos' : currentStep === 2 ? 'Ir para pagamento' : 'Revisar pedido' }}
        </Button>
        <Button
          v-else
          type="button"
          :disabled="ordersApi.loading.value"
          @click="createOrder"
        >
          {{ ordersApi.loading.value ? 'Criando...' : 'Criar pedido' }}
        </Button>
      </div>
    </div>
  </div>
</template>
