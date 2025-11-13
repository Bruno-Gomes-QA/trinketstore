<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { CircleDollarSign, Sparkles, ToggleLeft, ToggleRight, Wallet } from 'lucide-vue-next'
import { Button } from '~/components/ui/button'
import {
  InputGroup,
  InputGroupAddon,
  InputGroupButton,
  InputGroupInput,
} from '~/components/ui/input-group'
import {
  Field,
  FieldContent,
  FieldDescription,
  FieldError,
  FieldLabel,
  FieldSet,
} from '~/components/ui/field'
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from '~/components/ui/sheet'
import { Skeleton } from '~/components/ui/skeleton'
import { useToast } from '~/components/ui/toast/use-toast'

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Preços do produto',
})

const route = useRoute()
const productId = Number(route.params.id)

const { toast } = useToast()
const { formatCurrencyFromCents, formatDateTime } = useFormatters()
const { toCurrencyCents } = useNormalizers()
const productDetails = useProductDetails()
const { prices, currentPrice, loading, fetchPrices } = useProductPrices()
const priceMutations = usePriceMutations()

const sheetOpen = ref(false)
const sheetMode = ref<'create' | 'edit'>('create')
const editingPriceId = ref<number | null>(null)

const form = reactive({
  amountDisplay: '',
  currency: 'BRL',
  vigente: true,
})

const formErrors = reactive({
  amountDisplay: '',
})

const sheetTitle = computed(() =>
  sheetMode.value === 'create' ? 'Novo preço' : 'Editar preço',
)

const resetForm = () => {
  form.amountDisplay = ''
  form.currency = 'BRL'
  form.vigente = true
  formErrors.amountDisplay = ''
  editingPriceId.value = null
}

const openCreateSheet = () => {
  sheetMode.value = 'create'
  resetForm()
  sheetOpen.value = true
}

const openEditSheet = (priceId: number) => {
  const price = prices.value.find((p) => p.idPrice === priceId)
  if (!price) return
  sheetMode.value = 'edit'
  form.amountDisplay = (price.amountPrice / 100).toFixed(2).replace('.', ',')
  form.currency = price.currencyPrice
  form.vigente = price.vigentePrice
  editingPriceId.value = price.idPrice
  sheetOpen.value = true
}

const validateForm = () => {
  formErrors.amountDisplay = ''
  if (!form.amountDisplay) {
    formErrors.amountDisplay = 'Informe um valor.'
    return false
  }

  const cents = toCurrencyCents(form.amountDisplay)
  if (cents <= 0) {
    formErrors.amountDisplay = 'Valor precisa ser maior que zero.'
    return false
  }

  return true
}

const submitForm = async () => {
  if (!validateForm()) return
  const cents = toCurrencyCents(form.amountDisplay)

  if (sheetMode.value === 'create') {
    const created = await priceMutations.createPrice({
      productId,
      amountPrice: cents,
      currencyPrice: form.currency,
      vigentePrice: form.vigente,
    })
    if (created) {
      toast({ title: 'Preço criado', description: 'Valor adicionado com sucesso.' })
      sheetOpen.value = false
      await fetchPrices(productId)
    }
  } else if (editingPriceId.value) {
    const updated = await priceMutations.updatePrice(editingPriceId.value, {
      amountPrice: cents,
      currencyPrice: form.currency,
      vigentePrice: form.vigente,
    })
    if (updated) {
      toast({ title: 'Preço atualizado', description: 'Valor editado com sucesso.' })
      sheetOpen.value = false
      await fetchPrices(productId)
    }
  }
}

const handleActivation = async (priceId: number, active: boolean) => {
  const action = active ? priceMutations.deactivatePrice : priceMutations.activatePrice
  const updated = await action(priceId)
  if (updated) {
    toast({
      title: updated.vigentePrice ? 'Preço vigente' : 'Preço pausado',
      description: 'Status atualizado.',
    })
    await fetchPrices(productId)
  }
}

watch(sheetOpen, (isOpen) => {
  if (!isOpen) resetForm()
})

onMounted(async () => {
  await Promise.all([productDetails.fetchProduct(productId), fetchPrices(productId)])
})
</script>

<template>
  <div class="flex flex-1 flex-col gap-6">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold">Preços — {{ productDetails.product.value?.nomeProduct || 'Produto' }}</h1>
        <p class="text-sm text-muted-foreground">
          Gerencie o histórico de preços e defina o valor vigente.
        </p>
      </div>
      <Button type="button" @click="openCreateSheet">
        Novo preço
      </Button>
    </div>

    <div class="grid gap-6 lg:grid-cols-[2fr,1fr]">
      <div class="rounded-2xl border bg-card p-6 shadow-sm">
        <h3 class="text-lg font-semibold">Histórico de preços</h3>
        <p class="text-sm text-muted-foreground mb-4">
          Últimos valores cadastrados para o produto.
        </p>

        <div v-if="loading" class="space-y-3">
          <Skeleton class="h-12 w-full" v-for="index in 4" :key="index" />
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="price in prices"
            :key="price.idPrice"
            class="flex items-center justify-between rounded-xl border p-4"
          >
            <div>
              <p class="text-lg font-semibold">
                {{ formatCurrencyFromCents(price.amountPrice) }}
                <span class="text-xs text-muted-foreground">/{{ price.currencyPrice }}</span>
              </p>
              <p class="text-xs text-muted-foreground">
                Atualizado em {{ formatDateTime(price.createdAt) }}
              </p>
            </div>
            <div class="flex items-center gap-2">
              <span
                class="rounded-full px-2 py-0.5 text-xs font-semibold"
                :class="price.vigentePrice
                  ? 'bg-emerald-100 text-emerald-900 border border-emerald-200'
                  : 'bg-slate-100 text-slate-900 border border-slate-200'"
              >
                {{ price.vigentePrice ? 'Vigente' : 'Inativo' }}
              </span>
              <Button variant="ghost" size="sm" @click="openEditSheet(price.idPrice)">
                Editar
              </Button>
              <Button variant="ghost" size="sm" @click="handleActivation(price.idPrice, price.vigentePrice)">
                {{ price.vigentePrice ? 'Desativar' : 'Tornar vigente' }}
              </Button>
            </div>
          </div>
        </div>
      </div>

      <div class="rounded-2xl border bg-card p-6 shadow-sm space-y-4">
        <div>
          <p class="text-xs uppercase text-muted-foreground">Preço atual</p>
          <p class="text-3xl font-semibold">
            {{ currentPrice ? formatCurrencyFromCents(currentPrice.amountPrice) : '—' }}
          </p>
          <p class="text-sm text-muted-foreground">
            Atualizado em {{ currentPrice?.createdAt ? formatDateTime(currentPrice.createdAt) : '—' }}
          </p>
        </div>
        <div>
          <p class="text-xs uppercase text-muted-foreground">Produto</p>
          <p class="font-medium">{{ productDetails.product.value?.nomeProduct || '—' }}</p>
        </div>
        <div>
          <p class="text-xs uppercase text-muted-foreground">Categoria</p>
          <p class="font-medium">{{ productDetails.product.value?.categoriaProduct || '—' }}</p>
        </div>
      </div>
    </div>

    <Sheet v-model:open="sheetOpen">
      <SheetContent class="w-full sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{{ sheetTitle }}</SheetTitle>
          <SheetDescription>Defina o valor, a moeda e quem será o preço vigente.</SheetDescription>
        </SheetHeader>

        <div class="mt-6 space-y-5">
          <FieldSet class="space-y-5">
            <Field>
              <FieldLabel>
                <CircleDollarSign class="h-4 w-4 text-[#a4f380]" />
                Valor unitário
              </FieldLabel>
              <FieldContent>
                <InputGroup
                  class="h-12"
                  :class="formErrors.amountDisplay ? 'border-destructive ring-destructive/20' : ''"
                >
                  <InputGroupAddon class="text-muted-foreground font-semibold">
                    BRL
                  </InputGroupAddon>
                  <InputGroupInput
                    id="price-amount"
                    v-model="form.amountDisplay"
                    type="text"
                    placeholder="199,90"
                    inputmode="decimal"
                    :aria-invalid="Boolean(formErrors.amountDisplay)"
                    class="h-12"
                  />
                  <InputGroupButton type="button" size="sm" class="gap-2" @click="form.amountDisplay = currentPrice ? (currentPrice.amountPrice / 100).toFixed(2).replace('.', ',') : form.amountDisplay">
                    <Sparkles class="h-3.5 w-3.5" />
                    Atual
                  </InputGroupButton>
                </InputGroup>
                <FieldDescription>Informe o valor com vírgula para centavos.</FieldDescription>
                <FieldError v-if="formErrors.amountDisplay">
                  {{ formErrors.amountDisplay }}
                </FieldError>
              </FieldContent>
            </Field>

            <Field>
              <FieldLabel>
                <Wallet class="h-4 w-4 text-[#80d3e4]" />
                Moeda
              </FieldLabel>
              <FieldContent>
                <InputGroup class="h-12">
                  <InputGroupAddon class="text-muted-foreground">
                    <Wallet class="h-4 w-4" />
                  </InputGroupAddon>
                  <select
                    id="price-currency"
                    v-model="form.currency"
                    class="h-full flex-1 appearance-none bg-transparent pl-2 pr-8 text-sm font-medium text-foreground outline-none"
                  >
                    <option value="BRL">Real (BRL)</option>
                    <option value="USD">Dólar (USD)</option>
                  </select>
                </InputGroup>
                <FieldDescription>Combina com a moeda de cobrança do pedido.</FieldDescription>
              </FieldContent>
            </Field>

            <Field>
              <FieldLabel>
                <ToggleRight class="h-4 w-4 text-emerald-500" />
                Status
              </FieldLabel>
              <FieldContent>
                <Button
                  type="button"
                  variant="outline"
                  class="w-full justify-between gap-3 border border-current"
                  :class="form.vigente ? 'border-emerald-200 bg-emerald-50 text-emerald-900' : 'text-muted-foreground'"
                  @click="form.vigente = !form.vigente"
                >
                  <span class="text-sm font-medium">
                    {{ form.vigente ? 'Marca como vigente' : 'Deixar como histórico' }}
                  </span>
                  <component :is="form.vigente ? ToggleRight : ToggleLeft" class="h-5 w-5" />
                </Button>
                <FieldDescription>
                  Apenas um preço vigente por vez é exibido nas vitrines.
                </FieldDescription>
              </FieldContent>
            </Field>
          </FieldSet>
        </div>

        <SheetFooter class="mt-6">
          <SheetClose as-child>
            <Button variant="ghost">Cancelar</Button>
          </SheetClose>
          <Button
            type="button"
            class="gap-2"
            :disabled="priceMutations.loading.value"
            @click="submitForm"
          >
            <CircleDollarSign class="h-4 w-4" />
            {{ priceMutations.loading.value ? 'Salvando...' : 'Salvar' }}
          </Button>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  </div>
</template>
