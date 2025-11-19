<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { CircleDollarSign, Package, Sparkles, ToggleLeft, ToggleRight, Wallet } from 'lucide-vue-next'
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
import {
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '~/components/ui/table'
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyTitle,
} from '~/components/ui/empty'
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
const product = computed(() => productDetails.product.value)

const form = reactive({
  amountDisplay: '',
  currency: 'BRL',
})

const formErrors = reactive({
  amountDisplay: '',
})

const sheetTitle = computed(() =>
  sheetMode.value === 'create' ? 'Novo preço' : 'Editar preço',
)

const sortedPrices = computed(() => [...prices.value].sort((a, b) => b.idPrice - a.idPrice))

const historicalPrices = computed(() => {
  const currentId = currentPrice.value?.idPrice
  if (!currentId) {
    return sortedPrices.value
  }

  return sortedPrices.value.filter((price) => price.idPrice !== currentId)
})

const limitedHistoricalPrices = computed(() => historicalPrices.value.slice(0, 5))

const formatCentsToDisplay = (value?: number | null) => {
  if (typeof value !== 'number' || Number.isNaN(value)) return ''
  return (value / 100).toFixed(2).replace('.', ',')
}

const handleAmountInput = (event: Event) => {
  const target = event.target as HTMLInputElement | null
  if (!target) return
  const digits = target.value.replace(/\D/g, '')
  if (!digits) {
    form.amountDisplay = ''
    target.value = ''
    return
  }
  const cents = Number.parseInt(digits, 10)
  form.amountDisplay = formatCentsToDisplay(cents)
  target.value = form.amountDisplay
}

const setCurrentPriceInForm = () => {
  if (!currentPrice.value) return
  form.amountDisplay = formatCentsToDisplay(currentPrice.value.amountPrice)
}

const resetForm = () => {
  form.amountDisplay = ''
  form.currency = 'BRL'
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
  form.amountDisplay = formatCentsToDisplay(price.amountPrice)
  form.currency = price.currencyPrice
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
  const successCopy = () => ({
    title: 'Preço vigente aplicado',
    description: 'O valor atualizado já está disponível para pedidos e relatórios.',
  })

  if (sheetMode.value === 'create') {
    const created = await priceMutations.createPrice({
      productId,
      amountPrice: cents,
      currencyPrice: form.currency,
      vigentePrice: true,
    })
    if (created) {
      toast(successCopy())
      sheetOpen.value = false
      await fetchPrices(productId)
    }
  } else if (editingPriceId.value) {
    const updated = await priceMutations.updatePrice(editingPriceId.value, {
      amountPrice: cents,
      currencyPrice: form.currency,
      vigentePrice: true,
    })
    if (updated) {
      toast(successCopy())
      sheetOpen.value = false
      await fetchPrices(productId)
    }
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
        <h1 class="text-2xl font-semibold">Preços — {{ product?.nomeProduct || 'Produto' }}</h1>
        <p class="text-sm text-muted-foreground">
          Gerencie o histórico de preços e defina o valor vigente.
        </p>
      </div>
    </div>

    <div class="rounded-2xl border bg-card p-6 shadow-sm">
      <div class="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
        <div class="flex items-start gap-4">
          <div class="size-20 overflow-hidden rounded-2xl border bg-muted">
            <img
              v-if="product?.imagemurlProduct"
              :src="product.imagemurlProduct"
              :alt="product.nomeProduct"
              class="h-full w-full object-cover"
              loading="lazy"
            />
            <div
              v-else
              class="flex h-full w-full items-center justify-center text-muted-foreground"
            >
              <Package class="h-6 w-6" />
            </div>
          </div>
          <div class="space-y-1">
            <p class="text-2xl font-semibold">{{ product?.nomeProduct || 'Produto sem nome' }}</p>
            <p class="text-sm text-muted-foreground">
              {{ product?.slugProduct || 'Slug não definido' }}
            </p>
            <p class="text-xs text-muted-foreground">
              ID #{{ product?.idProduct ?? '—' }}
            </p>
            <p class="text-sm text-muted-foreground line-clamp-2">
              {{ product?.descricaoProduct || 'Sem descrição cadastrada para este item.' }}
            </p>
          </div>
        </div>
        <div class="flex flex-wrap gap-4 text-sm">
          <div class="min-w-[140px] space-y-1">
            <p class="text-xs uppercase text-muted-foreground tracking-[0.3em]">Categoria</p>
            <p class="font-semibold">{{ product?.categoriaProduct || 'Não informada' }}</p>
          </div>
          <div class="min-w-[140px] space-y-1">
            <p class="text-xs uppercase text-muted-foreground tracking-[0.3em]">Status</p>
            <span
              class="inline-flex items-center gap-1 rounded-full border px-3 py-1 text-xs font-semibold"
              :class="product?.ativo
                ? 'border-emerald-200 bg-emerald-50 text-emerald-900'
                : 'border-slate-200 bg-slate-800 text-slate-100'"
            >
              <component :is="product?.ativo ? ToggleRight : ToggleLeft" class="h-3.5 w-3.5" />
              {{ product?.ativo ? 'Ativo' : 'Inativo' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="rounded-2xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
      <p class="font-semibold">Regra de vigência</p>
      <p>Apenas um preço pode ficar vigente. Ao aplicar um novo valor, o anterior vira histórico automaticamente.</p>
    </div>

    <div class="rounded-2xl border bg-card p-6 shadow-sm">
      <template v-if="loading">
        <Skeleton class="h-5 w-40" />
        <Skeleton class="mt-4 h-10 w-64" />
        <Skeleton class="mt-6 h-8 w-full" />
      </template>
      <template v-else>
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p class="text-xs uppercase text-muted-foreground">Preço vigente</p>
            <p class="text-3xl font-semibold leading-tight">
              {{ currentPrice ? formatCurrencyFromCents(currentPrice.amountPrice) : 'Nenhum valor ativo' }}
            </p>
            <p class="text-sm text-muted-foreground">
              {{ currentPrice?.createdAt ? `Desde ${formatDateTime(currentPrice.createdAt)}` : 'Cadastre um preço para começar.' }}
            </p>
          </div>
          <span
            class="inline-flex items-center gap-2 rounded-full border px-3 py-1 text-xs font-semibold"
            :class="currentPrice
              ? 'border-emerald-200 bg-emerald-50 text-emerald-900'
              : 'border-slate-200 bg-slate-100 text-slate-900'"
          >
            <component :is="currentPrice ? ToggleRight : ToggleLeft" class="h-3.5 w-3.5" />
            {{ currentPrice ? 'Vigente' : 'Sem preço' }}
          </span>
        </div>

        <div class="mt-6 flex flex-wrap gap-2">
          <Button type="button" size="sm" class="gap-2" @click="openCreateSheet">
            <Sparkles class="h-4 w-4" />
            Adicionar preço
          </Button>
          <Button
            type="button"
            size="sm"
            variant="outline"
            class="gap-2"
            :disabled="!currentPrice"
            @click="currentPrice && openEditSheet(currentPrice.idPrice)"
          >
            <CircleDollarSign class="h-4 w-4" />
            Editar vigente
          </Button>
        </div>

        <p class="mt-4 text-sm text-muted-foreground">
          Ao ativar um novo preço, o anterior passa automaticamente para o histórico e deixa de ser utilizado nas vendas.
        </p>
      </template>
    </div>

    <div class="rounded-2xl border bg-card p-6 shadow-sm">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h3 class="text-lg font-semibold">Histórico aplicável</h3>
          <p class="text-sm text-muted-foreground">
            Exibindo os últimos 5 valores registrados para o produto.
          </p>
        </div>
      </div>

      <Table class="mt-6">
        <TableHeader>
          <TableRow>
            <TableHead>Valor</TableHead>
            <TableHead>Registrado em</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <template v-if="loading">
            <TableRow v-for="index in 4" :key="index">
              <TableCell><Skeleton class="h-6 w-32" /></TableCell>
              <TableCell><Skeleton class="h-6 w-40" /></TableCell>
            </TableRow>
          </template>
          <template v-else-if="limitedHistoricalPrices.length">
            <TableRow
              v-for="price in limitedHistoricalPrices"
              :key="price.idPrice"
            >
              <TableCell>
                <p class="font-semibold">{{ formatCurrencyFromCents(price.amountPrice) }}</p>
                <p class="text-xs text-muted-foreground">{{ price.currencyPrice }}</p>
              </TableCell>
              <TableCell>
                <p class="text-sm font-medium">
                  {{ price.createdAt ? formatDateTime(price.createdAt) : 'Sem data registrada' }}
                </p>
              </TableCell>
            </TableRow>
          </template>
          <TableEmpty v-else :colspan="2">
            <Empty>
              <EmptyHeader>
                <EmptyTitle>Sem histórico ainda</EmptyTitle>
                <EmptyDescription>Cadastre novos preços para montar o histórico e aplicar quando quiser.</EmptyDescription>
              </EmptyHeader>
            </Empty>
          </TableEmpty>
        </TableBody>
      </Table>
    </div>

    <Sheet v-model:open="sheetOpen">
      <SheetContent class="w-full sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{{ sheetTitle }}</SheetTitle>
          <SheetDescription>Defina o valor que será aplicado imediatamente. O preço anterior vai para o histórico automaticamente.</SheetDescription>
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
                    @input="handleAmountInput"
                    :aria-invalid="Boolean(formErrors.amountDisplay)"
                    class="h-12"
                  />
                  <InputGroupButton type="button" size="sm" class="gap-2" @click="setCurrentPriceInForm">
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
