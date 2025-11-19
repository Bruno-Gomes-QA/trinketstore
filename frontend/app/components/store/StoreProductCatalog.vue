<template>
  <section id="catalogo" class="bg-muted/30 py-12 md:py-16">
    <div class="container mx-auto px-4">
      <div class="mb-10 space-y-3 text-center">
        <p class="text-xs font-semibold uppercase tracking-[0.35em] text-brand-cyan">Catálogo</p>
        <h2 class="text-3xl font-bold md:text-4xl text-foreground">Nossos produtos</h2>
        <p class="text-sm text-muted-foreground md:text-base">
          Veja os itens cadastrados com fotos, descrição e valores atualizados direto do estoque.
        </p>
      </div>

      <div
        v-if="error"
        class="mb-8 rounded-2xl border border-destructive/30 bg-destructive/5 px-4 py-3 text-sm text-destructive"
      >
        Não foi possível carregar os produtos agora. {{ error.message }}
      </div>

      <div
        v-if="loading"
        class="grid gap-5 sm:grid-cols-2 lg:grid-cols-3"
      >
        <div
          v-for="index in 3"
          :key="index"
          class="rounded-2xl border border-border bg-white p-6 shadow-sm"
        >
          <Skeleton class="mb-4 h-48 w-full rounded-2xl" />
          <Skeleton class="h-5 w-1/3" />
          <Skeleton class="mt-3 h-6 w-3/4" />
          <Skeleton class="mt-2 h-4 w-full" />
          <Skeleton class="mt-6 h-7 w-1/2" />
        </div>
      </div>

      <div
        v-else-if="!products.length"
        class="rounded-2xl border border-dashed border-border bg-white/70 px-6 py-12 text-center text-muted-foreground"
      >
        Ainda não temos produtos publicados, mas em breve você verá as peças da Trinket Store por aqui.
      </div>

      <div
        v-else
        class="grid gap-5 sm:grid-cols-2 lg:grid-cols-3"
      >
        <article
          v-for="product in products"
          :key="product.id"
          class="flex h-full flex-col rounded-2xl border border-border bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:shadow-md"
        >
          <div class="relative mb-5 overflow-hidden rounded-2xl border border-border/60 bg-muted">
            <img
              v-if="product.image"
              :src="product.image"
              :alt="`Foto do produto ${product.name}`"
              loading="lazy"
              class="aspect-square w-full object-cover"
            >
            <div
              v-else
              class="flex aspect-square items-center justify-center text-muted-foreground"
            >
              <ImageIcon class="h-10 w-10" />
            </div>
          </div>

          <div class="space-y-3">
            <div class="flex items-center justify-between text-xs uppercase tracking-[0.3em] text-muted-foreground">
              <span>{{ product.category || 'Coleção' }}</span>
              <span class="rounded-full bg-brand-cyan/10 px-2 py-0.5 text-[0.65rem] font-semibold tracking-tight text-brand-cyan">
                Disponível
              </span>
            </div>

            <div>
              <h3 class="text-xl font-semibold text-foreground">{{ product.name }}</h3>
              <p class="mt-1 text-sm text-muted-foreground">
                {{ product.description || 'Descrição indisponível no momento.' }}
              </p>
            </div>
          </div>

          <div class="mt-6">
            <p class="text-2xl font-bold text-foreground">
              {{ formatPrice(product.priceInCents) }}
            </p>
            <p class="text-xs uppercase tracking-[0.3em] text-muted-foreground">Valor unitário</p>
          </div>

          <button
            type="button"
            class="mt-6 inline-flex items-center justify-center gap-2 rounded-xl border border-brand-cyan/40 px-4 py-2 text-sm font-semibold text-brand-cyan transition hover:border-brand-cyan hover:bg-brand-cyan/10"
            @click="openAddDialog(product)"
          >
            <ShoppingCart class="h-4 w-4" />
            <span>Adicionar</span>
          </button>
        </article>
      </div>
    </div>

    <Dialog v-model:open="addDialogOpen">
      <DialogContent v-if="selectedProduct" class="max-w-2xl space-y-6">
        <DialogHeader>
          <DialogTitle>
            {{ dialogState === 'success' ? 'Produto reservado com sucesso' : 'Adicionar ao carrinho' }}
          </DialogTitle>
          <DialogDescription>
            {{ dialogState === 'success'
              ? 'Seu carrinho já foi atualizado, escolha o próximo passo.'
              : 'Informe quantas unidades deseja levar. Consultamos o estoque em tempo real.' }}
          </DialogDescription>
        </DialogHeader>

        <div class="flex flex-col gap-4 rounded-2xl border border-border/70 bg-muted/40 p-4 sm:flex-row">
          <div class="flex h-32 w-full items-center justify-center overflow-hidden rounded-xl border border-border/60 bg-white sm:w-48">
            <img
              v-if="selectedProduct.image"
              :src="selectedProduct.image"
              :alt="selectedProduct.name"
              class="h-full w-full object-cover"
            >
            <div v-else class="flex h-full w-full items-center justify-center text-muted-foreground">
              <ImageIcon class="h-10 w-10" />
            </div>
          </div>
          <div class="flex-1 space-y-2">
            <p class="text-xs uppercase tracking-[0.3em] text-muted-foreground">
              {{ selectedProduct.category || 'Coleção' }}
            </p>
            <p class="text-xl font-semibold text-foreground">{{ selectedProduct.name }}</p>
            <p class="text-sm text-muted-foreground line-clamp-3">
              {{ selectedProduct.description || 'Peça exclusiva da Trinket Store.' }}
            </p>
            <div>
              <p class="text-2xl font-bold text-foreground">{{ formatPrice(selectedProduct.priceInCents) }}</p>
              <p class="text-xs uppercase tracking-[0.3em] text-muted-foreground">Valor unitário</p>
            </div>
          </div>
        </div>

        <div v-if="dialogState === 'select'" class="space-y-5">
          <div class="rounded-2xl border border-border/70 bg-white px-4 py-3 text-sm">
            <div class="flex items-center justify-between gap-4">
              <div>
                <p class="font-semibold text-foreground">Estoque disponível</p>
                <p class="text-foreground">
                  <span v-if="inventoryLoading">Consultando...</span>
                  <span v-else>{{ availableStock }} unidade{{ availableStock === 1 ? '' : 's' }}</span>
                </p>
              </div>
              <button
                type="button"
                class="text-xs font-semibold text-brand-cyan transition hover:underline disabled:opacity-50"
                :disabled="inventoryLoading"
                @click="refreshInventory"
              >
                Atualizar
              </button>
            </div>
            <p class="mt-1 text-xs text-muted-foreground">Você nunca conseguirá pedir acima do estoque imediato.</p>
          </div>

          <div
            v-if="!inventoryLoading && availableStock === 0"
            class="flex items-center gap-2 rounded-2xl border border-destructive/30 bg-destructive/5 px-4 py-3 text-sm text-destructive"
          >
            <AlertTriangle class="h-4 w-4" />
            <span>Esse produto está indisponível no momento. Assim que repormos você poderá adicionar novamente.</span>
          </div>

          <div>
            <p class="text-sm font-semibold text-foreground">Quantidade desejada</p>
            <div class="mt-2 w-full max-w-xs">
              <NumberField
                :model-value="selectedQuantity"
                :min="1"
                :max="Math.max(availableStock, 1)"
                :disabled="!hasStock || inventoryLoading"
                @update:model-value="handleQuantityChange"
              >
                <NumberFieldContent>
                  <NumberFieldDecrement />
                  <NumberFieldInput />
                  <NumberFieldIncrement />
                </NumberFieldContent>
              </NumberField>
            </div>
            <p class="mt-2 text-xs text-muted-foreground">
              Se precisar de mais unidades, fale com a equipe no evento ;)
            </p>
          </div>

          <div
            v-if="inventoryError"
            class="rounded-2xl border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive"
          >
            {{ inventoryError }}
          </div>
        </div>

        <div v-else class="space-y-4">
          <div class="flex items-start gap-3 rounded-2xl border border-brand-cyan/40 bg-brand-cyan/5 px-4 py-3">
            <CheckCircle2 class="h-5 w-5 text-brand-cyan" />
            <div>
              <p class="font-semibold text-foreground">Produto adicionado ao carrinho!</p>
              <p class="text-sm text-muted-foreground">
                {{ lastAddedQuantity }} unidade{{ lastAddedQuantity > 1 ? 's' : '' }} de
                {{ selectedProduct.name }} estão reservadas para você.
              </p>
            </div>
          </div>
          <div class="flex flex-col gap-3 sm:flex-row sm:justify-end">
            <button
              type="button"
              class="inline-flex items-center justify-center rounded-xl border border-border px-4 py-2 text-sm font-semibold text-foreground transition hover:border-brand-cyan hover:text-brand-cyan"
              @click="handleContinueShopping"
            >
              Continuar comprando
            </button>
            <button
              type="button"
              class="inline-flex items-center justify-center gap-2 rounded-xl border border-brand-cyan bg-brand-cyan px-4 py-2 text-sm font-semibold text-foreground shadow-sm hover:bg-brand-cyan/90"
              @click="goToCart"
            >
              <ShoppingCart class="h-4 w-4" />
              Ir para o carrinho
            </button>
          </div>
        </div>

        <DialogFooter v-if="dialogState === 'select'">
          <button
            type="button"
            class="inline-flex items-center justify-center rounded-xl border border-border px-4 py-2 text-sm font-medium text-muted-foreground transition hover:text-foreground"
            @click="addDialogOpen = false"
          >
            Cancelar
          </button>
          <button
            type="button"
            class="inline-flex items-center justify-center gap-2 rounded-xl bg-brand-cyan px-5 py-2 text-sm font-semibold text-foreground shadow-md transition hover:bg-brand-cyan/90 disabled:opacity-60"
            :disabled="!hasStock || inventoryLoading"
            @click="handleConfirmAdd"
          >
            <ShoppingCart class="h-4 w-4" />
            Adicionar ao carrinho
          </button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ImageIcon, ShoppingCart, CheckCircle2, AlertTriangle } from 'lucide-vue-next'
import { Skeleton } from '~/components/ui/skeleton'
import type { StorefrontProduct } from '~/composables/storefront/useStorefrontCatalog'
import type { StorefrontInventorySnapshot } from '~/composables/storefront/useStorefrontInventory'

const router = useRouter()
const { formatCurrencyFromCents } = useFormatters()
const { catalog, loading, error, fetchCatalog } = useStorefrontCatalog()
const { addItem } = useStorefrontCart()
const { fetchInventoryForProduct } = useStorefrontInventory()

await fetchCatalog()

const products = computed(() => catalog.value)

const addDialogOpen = ref(false)
const selectedProduct = ref<StorefrontProduct | null>(null)
const selectedInventory = ref<StorefrontInventorySnapshot | null>(null)
const selectedQuantity = ref(1)
const dialogState = ref<'select' | 'success'>('select')
const inventoryLoading = ref(false)
const inventoryError = ref<string | null>(null)
const lastAddedQuantity = ref(0)

const availableStock = computed(() => selectedInventory.value?.qtyOnHand ?? 0)
const hasStock = computed(() => availableStock.value > 0)

const openAddDialog = async (product: StorefrontProduct) => {
  selectedProduct.value = product
  selectedInventory.value = null
  selectedQuantity.value = 1
  dialogState.value = 'select'
  inventoryError.value = null
  addDialogOpen.value = true

  await loadInventory(product.id)
}

const loadInventory = async (productId: number) => {
  inventoryLoading.value = true
  inventoryError.value = null

  try {
    const snapshot = await fetchInventoryForProduct(productId, { force: true })
    selectedInventory.value = snapshot
    if (snapshot.qtyOnHand > 0) {
      selectedQuantity.value = Math.min(Math.max(selectedQuantity.value, 1), snapshot.qtyOnHand)
    }
  } catch (err) {
    console.error('[storefront] inventory fetch failed', err)
    inventoryError.value = 'Não foi possível consultar o estoque agora. Tente novamente em instantes.'
  } finally {
    inventoryLoading.value = false
  }
}

const refreshInventory = async () => {
  if (!selectedProduct.value) return
  await loadInventory(selectedProduct.value.id)
}

const handleQuantityChange = (value?: number | string) => {
  if (!hasStock.value) {
    selectedQuantity.value = 1
    return
  }

  const parsed = typeof value === 'number' ? value : Number(value)
  if (!Number.isFinite(parsed)) return

  selectedQuantity.value = Math.min(
    Math.max(Math.floor(parsed), 1),
    availableStock.value,
  )
}

const handleConfirmAdd = () => {
  if (!selectedProduct.value || !hasStock.value || inventoryLoading.value) return

  const quantity = Math.min(Math.max(Math.floor(selectedQuantity.value), 1), availableStock.value)
  addItem(selectedProduct.value, quantity, availableStock.value)
  lastAddedQuantity.value = quantity
  dialogState.value = 'success'
}

const handleContinueShopping = () => {
  addDialogOpen.value = false
}

const goToCart = () => {
  addDialogOpen.value = false
  router.push('/carrinho')
}

const resetAddDialog = () => {
  selectedProduct.value = null
  selectedInventory.value = null
  selectedQuantity.value = 1
  dialogState.value = 'select'
  inventoryError.value = null
  inventoryLoading.value = false
}

watch(addDialogOpen, (isOpen) => {
  if (!isOpen) {
    resetAddDialog()
  }
})

watch(availableStock, (stock) => {
  if (stock > 0 && dialogState.value === 'select') {
    selectedQuantity.value = Math.min(Math.max(selectedQuantity.value, 1), stock)
  }
})

const formatPrice = (amount: number | null) => {
  if (amount === null || amount === undefined) {
    return 'Valor sob consulta'
  }
  return formatCurrencyFromCents(amount)
}
</script>
