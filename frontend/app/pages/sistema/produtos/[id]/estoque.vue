<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Package, ToggleLeft, ToggleRight } from 'lucide-vue-next'
import { Button } from '~/components/ui/button'
import { Input } from '~/components/ui/input'
import { Label } from '~/components/ui/label'
import { Skeleton } from '~/components/ui/skeleton'
import { useToast } from '~/components/ui/toast/use-toast'

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Estoque do produto',
})

const route = useRoute()
const productId = Number(route.params.id)

const { toast } = useToast()
const { formatDateTime } = useFormatters()
const { isPositiveInteger } = useValidators()
const productDetails = useProductDetails()
const inventory = useInventoryByProduct()
const inventoryMutations = useInventoryMutations()

const absoluteForm = reactive({ qty: '' })
const manualAdjust = reactive({ amount: '', mode: 'add' as 'add' | 'remove' })
const formErrors = reactive({ absolute: '', manual: '' })
const saving = ref(false)
const product = computed(() => productDetails.product.value)

const loadData = async () => {
  await Promise.all([
    productDetails.fetchProduct(productId),
    inventory.fetchInventory(productId),
  ])
}

const handleSetAbsolute = async () => {
  formErrors.absolute = ''
  const value = Number(absoluteForm.qty)
  if (!isPositiveInteger(value)) {
    formErrors.absolute = 'Informe um número inteiro válido.'
    return
  }

  if (!inventory.inventory.value) return
  saving.value = true
  const updated = await inventoryMutations.updateInventory(inventory.inventory.value.idInventory, { qtyOnHand: value })
  saving.value = false
  if (updated) {
    toast({ title: 'Estoque atualizado', description: 'Quantidade ajustada com sucesso.' })
    absoluteForm.qty = ''
    await inventory.fetchInventory(productId)
  }
}

const applyAdjust = async (quantity: number, type: 'add' | 'remove') => {
  if (quantity <= 0 || Number.isNaN(quantity)) return
  if (!inventory.inventory.value) return
  saving.value = true
  const mutation = type === 'add' ? inventoryMutations.addStock : inventoryMutations.removeStock
  const updated = await mutation(inventory.inventory.value.idInventory, quantity)
  saving.value = false
  if (updated) {
    toast({
      title: type === 'add' ? 'Estoque incrementado' : 'Estoque decrementado',
      description: `${quantity} unidade(s) ${type === 'add' ? 'adicionadas' : 'removidas'}.`,
    })
    manualAdjust.amount = ''
    await inventory.fetchInventory(productId)
  }
}

const handleManualAdjust = async () => {
  formErrors.manual = ''
  const value = Number(manualAdjust.amount)
  if (!isPositiveInteger(value)) {
    formErrors.manual = 'Informe um valor inteiro maior que zero.'
    return
  }
  await applyAdjust(value, manualAdjust.mode)
}

onMounted(loadData)
</script>

<template>
  <div class="flex flex-1 flex-col gap-6">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold">Estoque — {{ product?.nomeProduct || 'Produto' }}</h1>
        <p class="text-sm text-muted-foreground">
          Sincronize quantidades e mantenha o inventário preparado para os pedidos.
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
            <p class="text-xs text-muted-foreground">ID #{{ product?.idProduct ?? '—' }}</p>
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
              class="inline-flex items-center gap-2 rounded-full border px-3 py-1 text-xs font-semibold"
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

    <div v-if="inventory.loading.value" class="space-y-4">
      <Skeleton class="h-24 w-full" />
      <Skeleton class="h-48 w-full" />
    </div>

    <div v-else class="grid gap-6 lg:grid-cols-[2fr,1fr]">
      <div class="rounded-2xl border bg-card p-6 shadow-sm space-y-6">
        <div>
          <h3 class="text-lg font-semibold">Atualizar estoque</h3>
          <p class="text-sm text-muted-foreground">
            Ajuste o saldo disponível definindo um valor exato ou registrando entradas e saídas.
          </p>
        </div>

        <div class="grid gap-6 md:grid-cols-2">
          <div class="rounded-xl border bg-muted/20 p-5 space-y-4">
            <div>
              <p class="text-sm font-semibold">Movimentar estoque</p>
              <p class="text-xs text-muted-foreground">Informe a quantidade e escolha o tipo de movimento.</p>
            </div>
            <div>
              <Label for="manual-amount">Quantidade</Label>
              <Input
                id="manual-amount"
                v-model="manualAdjust.amount"
                type="number"
                min="0"
                placeholder="Ex.: 12"
              />
              <p v-if="formErrors.manual" class="mt-1 text-xs text-destructive">
                {{ formErrors.manual }}
              </p>
            </div>
            <div class="flex flex-wrap gap-2">
              <Button
                type="button"
                class="flex-1"
                variant="secondary"
                :disabled="saving"
                @click="manualAdjust.mode = 'add'; handleManualAdjust()"
              >
                Adicionar
              </Button>
              <Button
                type="button"
                class="flex-1"
                variant="outline"
                :disabled="saving"
                @click="manualAdjust.mode = 'remove'; handleManualAdjust()"
              >
                Remover
              </Button>
            </div>
          </div>

          <div class="rounded-xl border bg-muted/20 p-5 space-y-4">
            <div>
              <p class="text-sm font-semibold">Definir quantidade exata</p>
              <p class="text-xs text-muted-foreground">Substitui o valor atual pelo informado abaixo.</p>
            </div>
            <div>
              <Label for="absolute-qty">Quantidade em estoque</Label>
              <Input
                id="absolute-qty"
                v-model="absoluteForm.qty"
                type="number"
                min="0"
                placeholder="Ex.: 120"
              />
              <p v-if="formErrors.absolute" class="mt-1 text-xs text-destructive">
                {{ formErrors.absolute }}
              </p>
            </div>
            <Button type="button" :disabled="saving" @click="handleSetAbsolute">
              {{ saving ? 'Salvando...' : 'Atualizar quantidade' }}
            </Button>
          </div>
        </div>
      </div>

      <div class="rounded-2xl border bg-card p-6 shadow-sm space-y-5">
        <div>
          <p class="text-xs uppercase text-muted-foreground">Quantidade atual</p>
          <p class="text-4xl font-bold tracking-tight">
            {{ inventory.inventory.value?.qtyOnHand ?? 0 }} un
          </p>
        </div>
        <div class="rounded-xl border bg-muted/30 p-4 space-y-1">
          <p class="text-xs uppercase text-muted-foreground">Última atualização do produto</p>
          <p class="text-sm font-medium">
            {{ formatDateTime(product?.updatedAt) }}
          </p>
        </div>
        <div class="rounded-xl border bg-muted/30 p-4 space-y-1">
          <p class="text-xs uppercase text-muted-foreground">Estado do estoque</p>
          <span
            class="inline-flex items-center gap-2 rounded-full border px-3 py-1 text-xs font-semibold"
            :class="(inventory.inventory.value?.qtyOnHand ?? 0) > 0
              ? 'border-emerald-200 bg-emerald-50 text-emerald-900'
              : 'border-destructive/30 bg-destructive/10 text-destructive'"
          >
            <component
              :is="(inventory.inventory.value?.qtyOnHand ?? 0) > 0 ? ToggleRight : ToggleLeft"
              class="h-3.5 w-3.5"
            />
            {{ (inventory.inventory.value?.qtyOnHand ?? 0) > 0 ? 'Em estoque' : 'Sem estoque' }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
