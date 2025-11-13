<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Button } from '~/components/ui/button'
import { Input } from '~/components/ui/input'
import { Label } from '~/components/ui/label'
import { Textarea } from '~/components/ui/textarea'
import { useToast } from '~/components/ui/toast/use-toast'

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Novo pedido',
})

const router = useRouter()
const { toast } = useToast()
const { formatCurrencyFromCents } = useFormatters()
const { toCurrencyCents } = useNormalizers()
const ordersApi = useOrderMutations()
const usersApi = useUsersList()
const productsApi = useProductsList()

const currentStep = ref(1)
const stepError = ref('')
const selectedUserId = ref<number | null>(null)
const paymentForm = reactive({
  checkoutId: '',
  paymentIntent: '',
  currencyOrder: 'BRL',
})

const itemForm = reactive({
  productId: '',
  qty: '1',
  unitPrice: '',
})

const items = ref<Array<{
  productId: number
  name: string
  qty: number
  unitAmount: number
  subtotal: number
}>>([])

const orderTotal = computed(() =>
  items.value.reduce((sum, item) => sum + item.subtotal, 0),
)

const selectedUser = computed(() =>
  usersApi.users.value.find((user) => user.idUser === selectedUserId.value),
)

const addItem = () => {
  stepError.value = ''
  const productId = Number(itemForm.productId)
  const qty = Number(itemForm.qty)
  const unitCents = toCurrencyCents(itemForm.unitPrice)
  if (!productId || qty <= 0 || unitCents <= 0) {
    stepError.value = 'Informe produto, quantidade e preço válidos.'
    return
  }
  const product = productsApi.products.value.find((p) => p.idProduct === productId)
  if (!product) {
    stepError.value = 'Produto inválido.'
    return
  }
  items.value.push({
    productId,
    name: product.nomeProduct,
    qty,
    unitAmount: unitCents,
    subtotal: qty * unitCents,
  })
  itemForm.productId = ''
  itemForm.qty = '1'
  itemForm.unitPrice = ''
}

const removeItem = (index: number) => {
  items.value.splice(index, 1)
}

const nextStep = () => {
  stepError.value = ''
  if (currentStep.value === 1 && !selectedUserId.value) {
    stepError.value = 'Selecione um cliente.'
    return
  }
  if (currentStep.value === 2 && items.value.length === 0) {
    stepError.value = 'Adicione pelo menos um item.'
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
  if (!selectedUserId.value || !items.value.length) {
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
    items: items.value.map((item) => ({
      productId: item.productId,
      qtyItems: item.qty,
      unitAmount: item.unitAmount,
      subtotalAmount: item.subtotal,
    })),
  }

  const created = await ordersApi.createOrder(payload)
  if (created) {
    toast({ title: 'Pedido criado', description: `Pedido #${created.idOrder} registrado.` })
    router.push(`/sistema/pedidos/${created.idOrder}`)
  }
}

onMounted(async () => {
  await Promise.all([usersApi.fetchUsers(), productsApi.fetchProducts()])
})
</script>

<template>
  <div class="flex flex-1 flex-col gap-6">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold">Criar novo pedido</h1>
        <p class="text-sm text-muted-foreground">Processo em 4 etapas.</p>
      </div>
      <div class="text-sm text-muted-foreground">
        Etapa {{ currentStep }} / 4
      </div>
    </div>

    <div class="rounded-2xl border bg-card p-6 shadow-sm space-y-6">
      <div v-if="currentStep === 1" class="space-y-4">
        <h2 class="text-lg font-semibold">1. Selecione o cliente</h2>
        <p class="text-sm text-muted-foreground">
          Escolha o usuário que receberá este pedido.
        </p>
        <select
          v-model="selectedUserId"
          class="h-12 w-full rounded-lg border px-3 text-sm"
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

      <div v-else-if="currentStep === 2" class="space-y-4">
        <h2 class="text-lg font-semibold">2. Adicione itens</h2>
        <div class="grid gap-4 md:grid-cols-3">
          <div class="space-y-2">
            <Label>Produto</Label>
            <select
              v-model="itemForm.productId"
              class="h-12 w-full rounded-lg border px-3 text-sm"
            >
              <option value="">Selecione</option>
              <option
                v-for="product in productsApi.products.value"
                :key="product.idProduct"
                :value="product.idProduct"
              >
                {{ product.nomeProduct }}
              </option>
            </select>
          </div>
          <div class="space-y-2">
            <Label>Quantidade</Label>
            <Input v-model="itemForm.qty" type="number" min="1" />
          </div>
          <div class="space-y-2">
            <Label>Valor unitário (BRL)</Label>
            <Input v-model="itemForm.unitPrice" placeholder="Ex.: 199,90" />
          </div>
        </div>
        <Button variant="secondary" type="button" @click="addItem">
          Adicionar item
        </Button>

        <div v-if="items.length" class="rounded-xl border">
          <table class="w-full text-sm">
            <thead class="text-left text-xs text-muted-foreground">
              <tr>
                <th class="px-4 py-3">Produto</th>
                <th class="px-4 py-3">Qtd</th>
                <th class="px-4 py-3">Valor</th>
                <th class="px-4 py-3 text-right">Subtotal</th>
                <th />
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in items" :key="index" class="border-t">
                <td class="px-4 py-3">{{ item.name }}</td>
                <td class="px-4 py-3">{{ item.qty }}</td>
                <td class="px-4 py-3">{{ formatCurrencyFromCents(item.unitAmount) }}</td>
                <td class="px-4 py-3 text-right">{{ formatCurrencyFromCents(item.subtotal) }}</td>
                <td class="px-4 py-3 text-right">
                  <Button variant="ghost" size="sm" @click="removeItem(index)">Remover</Button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-else-if="currentStep === 3" class="space-y-4">
        <h2 class="text-lg font-semibold">3. Dados de pagamento</h2>
        <div class="space-y-2">
          <Label for="checkout">Checkout ID</Label>
          <Input id="checkout" v-model="paymentForm.checkoutId" placeholder="chk_123" />
        </div>
        <div class="space-y-2">
          <Label for="payment-intent">Payment Intent</Label>
          <Input id="payment-intent" v-model="paymentForm.paymentIntent" placeholder="pi_123" />
        </div>
      </div>

      <div v-else class="space-y-4">
        <h2 class="text-lg font-semibold">4. Revisão</h2>
        <div class="rounded-xl border bg-muted/40 p-4 space-y-2 text-sm">
          <p><span class="font-semibold">Cliente:</span> {{ selectedUser?.nomeUser || '—' }}</p>
          <p><span class="font-semibold">Itens:</span> {{ items.length }}</p>
          <p><span class="font-semibold">Total:</span> {{ formatCurrencyFromCents(orderTotal) }}</p>
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
          Próximo
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
