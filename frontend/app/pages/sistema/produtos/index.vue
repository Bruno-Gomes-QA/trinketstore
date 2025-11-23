<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch, type Component } from 'vue'
import type { SupabaseClient } from '@supabase/supabase-js'
import {
  AlertTriangle,
  CheckCircle2,
  CircleDollarSign,
  Eye,
  FileText,
  Hash,
  ImageIcon,
  Link2,
  Loader2,
  Package,
  Pencil,
  Plus,
  Search,
  Shapes,
  SlidersHorizontal,
  Sparkles,
  Tag,
  ToggleLeft,
  ToggleRight,
  Trash2,
  Warehouse,
  XCircle,
} from 'lucide-vue-next'
import { Button } from '~/components/ui/button'
import {
  InputGroup,
  InputGroupAddon,
  InputGroupButton,
  InputGroupInput,
  InputGroupTextarea,
} from '~/components/ui/input-group'
import { Popover, PopoverContent, PopoverTrigger } from '~/components/ui/popover'
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
import type { ProductEntity } from '~/types/products'

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Produtos',
})

const defaultCategories = ['Colecionáveis', 'Decoração', 'Acessórios', 'Vestuário', 'Outros']

const { toast } = useToast()
const { formatDateTime } = useFormatters()
const { toSlug, sanitizeUrl, toCurrencyCents } = useNormalizers()
const { isValidName, isValidSlug, isValidUrl, hasLengthBetween } = useValidators()
const { $supabase } = useNuxtApp()
const {
  products,
  filteredProducts,
  loading,
  fetchProducts,
  setFilters,
} = useProductsList()
const productMutations = useProductMutations()
const inventoryMutations = useInventoryMutations()
const priceMutations = usePriceMutations()

const searchTerm = ref('')
const statusFilter = ref<'all' | 'active' | 'inactive'>('all')
const categoryFilter = ref<'all' | string>('all')
const sheetOpen = ref(false)
const sheetMode = ref<'create' | 'edit'>('create')
const deletePopoverOpen = ref<number | null>(null)
const deletingProductId = ref<number | null>(null)
const editingProductId = ref<number | null>(null)
const uploadingImage = ref(false)
const imageInputRef = ref<HTMLInputElement | null>(null)
const supabaseClient = ($supabase ?? null) as SupabaseClient | null
const runtimeConfig = useRuntimeConfig()

type CreationStepKey = 'product' | 'inventory' | 'price' | 'done'
type CreationStepStatus = 'pending' | 'in_progress' | 'success' | 'error'
interface CreationStep {
  key: CreationStepKey
  label: string
  icon: Component
  status: CreationStepStatus
}

const creationSteps = reactive<CreationStep[]>([
  { key: 'product', label: 'Adicionando produto', icon: Package, status: 'pending' },
  { key: 'inventory', label: 'Configurando estoque', icon: Warehouse, status: 'pending' },
  { key: 'price', label: 'Aplicando preço vigente', icon: CircleDollarSign, status: 'pending' },
  { key: 'done', label: 'Tudo pronto! Produto adicionado', icon: Sparkles, status: 'pending' },
])
const creationFlow = reactive({
  open: false,
  errorMessage: '',
})
const creationFlowRunning = ref(false)
const creationStepStatusCopy: Record<CreationStepStatus, string> = {
  pending: 'Aguardando próxima etapa',
  in_progress: 'Processando...',
  success: 'Concluído',
  error: 'Etapa interrompida',
}
const CREATION_STEP_MIN_MS = 1500

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const ensureMinStepDuration = async (startedAt: number) => {
  const elapsed = Date.now() - startedAt
  if (elapsed < CREATION_STEP_MIN_MS) {
    await sleep(CREATION_STEP_MIN_MS - elapsed)
  }
}

const setCreationStepStatus = (key: CreationStepKey, status: CreationStepStatus) => {
  const step = creationSteps.find((item) => item.key === key)
  if (step) {
    step.status = status
  }
}

const resetCreationFlow = () => {
  creationFlow.errorMessage = ''
  creationSteps.forEach((step) => {
    step.status = 'pending'
  })
}

const runCreationStep = async <T>(key: CreationStepKey, handler: () => Promise<T>): Promise<T> => {
  setCreationStepStatus(key, 'in_progress')
  const startedAt = Date.now()
  try {
    const result = await handler()
    await ensureMinStepDuration(startedAt)
    setCreationStepStatus(key, 'success')
    return result
  } catch (error) {
    await ensureMinStepDuration(startedAt)
    setCreationStepStatus(key, 'error')
    throw error
  }
}

const form = reactive({
  nomeProduct: '',
  slugProduct: '',
  descricaoProduct: '',
  imagemurlProduct: '',
  categoriaProduct: '',
  ativo: true,
  initialStock: 0,
  priceAmount: '',
})

const formErrors = reactive({
  nomeProduct: '',
  slugProduct: '',
  descricaoProduct: '',
  imagemurlProduct: '',
  categoriaProduct: '',
  initialStock: '',
  priceAmount: '',
})

const filteredList = computed(() => filteredProducts.value)
const availableCategories = computed(() => {
  const fromData = Array.from(new Set(products.value.map((p) => p.categoriaProduct))).filter(Boolean)
  return Array.from(new Set([...fromData, ...defaultCategories]))
})

const tableEmpty = computed(() => !loading.value && filteredList.value.length === 0)

const resetForm = () => {
  form.nomeProduct = ''
  form.slugProduct = ''
  form.descricaoProduct = ''
  form.imagemurlProduct = ''
  form.categoriaProduct = ''
  form.ativo = true
  form.initialStock = 0
  form.priceAmount = ''
  Object.keys(formErrors).forEach((key) => {
    formErrors[key as keyof typeof formErrors] = ''
  })
  editingProductId.value = null
}

const openCreateSheet = () => {
  resetForm()
  sheetMode.value = 'create'
  sheetOpen.value = true
}

const closeSheet = () => {
  sheetOpen.value = false
}

const openEditSheet = (product: ProductEntity) => {
  sheetMode.value = 'edit'
  editingProductId.value = product.idProduct
  form.nomeProduct = product.nomeProduct
  form.slugProduct = product.slugProduct
  form.descricaoProduct = product.descricaoProduct
  form.imagemurlProduct = product.imagemurlProduct
  form.categoriaProduct = product.categoriaProduct
  form.ativo = product.ativo
  form.initialStock = 0
  form.priceAmount = ''
  Object.keys(formErrors).forEach((key) => {
    formErrors[key as keyof typeof formErrors] = ''
  })
  sheetOpen.value = true
}

const buildImagePath = (file: File) => {
  const extension = file.name.split('.').pop()?.toLowerCase() || 'jpg'
  const base = toSlug(form.slugProduct || form.nomeProduct || 'produto')
  return `products/${base || 'produto'}-${Date.now()}.${extension}`
}

const handleImageUpload = async (file: File) => {
  if (!process.client || !supabaseClient) {
    toast({
      title: 'Upload indisponível',
      description: 'Configure o Supabase para enviar imagens.',
      variant: 'destructive',
    })
    return
  }

  if (!file.type.startsWith('image/')) {
    formErrors.imagemurlProduct = 'Envie apenas arquivos de imagem.'
    return
  }

  uploadingImage.value = true
  formErrors.imagemurlProduct = ''

  try {
    const path = buildImagePath(file)
    const signed = await $fetch<{ path: string; token: string; signedUrl: string; publicUrl: string }>(
      '/api/storage/product-upload-url',
      {
        method: 'POST',
        body: { path },
      },
    )

    const { error: uploadError } = await supabaseClient.storage
      .from('trinketstore')
      .uploadToSignedUrl(signed.path, signed.token, file)

    if (uploadError) {
      console.error('[products] upload failed', uploadError)
      formErrors.imagemurlProduct = 'Não foi possível enviar a imagem. Tente novamente.'
      return
    }

    const publicUrl = sanitizeUrl(signed.publicUrl)
    form.imagemurlProduct = publicUrl
    toast({
      title: 'Imagem enviada',
      description: 'A imagem foi salva no bucket e vinculada ao produto.',
    })
  } catch (error) {
    console.error('[products] upload unexpected error', error)
    formErrors.imagemurlProduct = 'Erro ao enviar imagem. Tente novamente.'
  } finally {
    uploadingImage.value = false
  }
}

const handleImageSelect = (event: Event) => {
  const target = event.target as HTMLInputElement | null
  const file = target?.files?.[0]
  if (file) {
    handleImageUpload(file)
  }
  if (target) {
    target.value = ''
  }
}

const handleImageDrop = (event: DragEvent) => {
  event.preventDefault()
  const file = event.dataTransfer?.files?.[0]
  if (file) {
    handleImageUpload(file)
  }
}

const triggerImagePicker = () => {
  imageInputRef.value?.click()
}

const validateForm = () => {
  let valid = true
  formErrors.nomeProduct = ''
  formErrors.slugProduct = ''
  formErrors.descricaoProduct = ''
  formErrors.imagemurlProduct = ''
  formErrors.categoriaProduct = ''
  formErrors.initialStock = ''
  formErrors.priceAmount = ''
  form.imagemurlProduct = sanitizeUrl(form.imagemurlProduct)

  if (!isValidName(form.nomeProduct)) {
    formErrors.nomeProduct = 'Informe um nome válido (2-300).'
    valid = false
  }

  if (!isValidSlug(form.slugProduct)) {
    formErrors.slugProduct = 'Slug inválido. Use letras minúsculas e hífens.'
    valid = false
  }

  if (!hasLengthBetween(form.descricaoProduct, 10, 1000)) {
    formErrors.descricaoProduct = 'Descrição precisa de ao menos 10 caracteres.'
    valid = false
  }

  if (!form.imagemurlProduct) {
    formErrors.imagemurlProduct = 'Envie ou cole uma imagem para este produto.'
    valid = false
  } else if (!isValidUrl(form.imagemurlProduct)) {
    formErrors.imagemurlProduct = 'URL inválida.'
    valid = false
  }

  if (!form.categoriaProduct) {
    formErrors.categoriaProduct = 'Selecione uma categoria.'
    valid = false
  }

  if (sheetMode.value === 'create') {
    if (!Number.isInteger(form.initialStock) || form.initialStock < 0) {
      formErrors.initialStock = 'Informe um estoque inicial válido.'
      valid = false
    }

    if (toCurrencyCents(form.priceAmount) <= 0) {
      formErrors.priceAmount = 'Informe um preço inicial válido.'
      valid = false
    }
  }

  return valid
}

const runCreationPipeline = async () => {
  const priceCents = toCurrencyCents(form.priceAmount)
  creationFlowRunning.value = true
  resetCreationFlow()
  creationFlow.open = true

  try {
    const createdProduct = await runCreationStep('product', async () => {
      const created = await productMutations.createProduct({
        nomeProduct: form.nomeProduct,
        slugProduct: form.slugProduct,
        descricaoProduct: form.descricaoProduct,
        imagemurlProduct: form.imagemurlProduct,
        categoriaProduct: form.categoriaProduct,
        ativo: form.ativo,
        initialStock: form.initialStock,
      })
      if (!created) {
        throw new Error(productMutations.error.value?.message || 'Não foi possível criar o produto.')
      }
      return created
    })

    await runCreationStep('inventory', async () => {
      const ensured = await inventoryMutations.ensureInventory(createdProduct.idProduct, form.initialStock)
      if (!ensured) {
        throw new Error(inventoryMutations.error.value?.message || 'Não foi possível configurar o estoque.')
      }
      return ensured
    })

    await runCreationStep('price', async () => {
      const createdPrice = await priceMutations.createPrice({
        productId: createdProduct.idProduct,
        amountPrice: priceCents,
        currencyPrice: 'BRL',
        vigentePrice: true,
      })
      if (!createdPrice) {
        throw new Error(priceMutations.error.value?.message || 'Não foi possível registrar o preço.')
      }
      return createdPrice
    })

    await runCreationStep('done', async () => Promise.resolve(true))

    toast({
      title: 'Produto pronto para venda',
      description: `${createdProduct.nomeProduct} já está com estoque e preço configurados.`,
    })
    closeSheet()
    await fetchProducts()
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Não foi possível concluir a configuração.'
    creationFlow.errorMessage = message
    toast({
      variant: 'destructive',
      title: 'Pipeline interrompido',
      description: message,
    })
  } finally {
    await sleep(600)
    creationFlow.open = false
    creationFlowRunning.value = false
    resetCreationFlow()
  }
}

const handleSubmitProduct = async () => {
  if (!validateForm()) return

  if (sheetMode.value === 'create') {
    await runCreationPipeline()
    return
  }

  if (editingProductId.value) {
    const updated = await productMutations.updateProduct(editingProductId.value, {
      nomeProduct: form.nomeProduct,
      slugProduct: form.slugProduct,
      descricaoProduct: form.descricaoProduct,
      imagemurlProduct: form.imagemurlProduct,
      categoriaProduct: form.categoriaProduct,
      ativo: form.ativo,
    })

    if (updated) {
      toast({ title: 'Produto atualizado', description: `${updated.nomeProduct} foi atualizado.` })
      closeSheet()
      await fetchProducts()
    }
  }
}

const handleDeleteProduct = async (product: ProductEntity) => {
  if (!product?.idProduct) return
  deletingProductId.value = product.idProduct
  const deleted = await productMutations.deleteProduct(product.idProduct)
  deletingProductId.value = null

  if (deleted !== null) {
    toast({
      title: 'Produto removido',
      description: `${product.nomeProduct} e seu inventário foram excluídos.`,
    })
    deletePopoverOpen.value = null
    await fetchProducts()
  } else {
    toast({
      variant: 'destructive',
      title: 'Não foi possível excluir',
      description: productMutations.error.value?.message || 'Verifique vínculos com pedidos antes de excluir.',
    })
  }
}

const handleToggleStatus = async (productId: number, isActive: boolean) => {
  const action = isActive ? productMutations.deactivateProduct : productMutations.activateProduct
  const updated = await action(productId)
  if (updated) {
    toast({
      title: isActive ? 'Produto desativado' : 'Produto ativado',
      description: `Status atualizado para ${updated.ativo ? 'ativo' : 'inativo'}.`,
    })
    await fetchProducts()
  }
}

const generateSlug = () => {
  form.slugProduct = toSlug(form.nomeProduct)
}

const handlePriceInput = (event: Event) => {
  const target = event.target as HTMLInputElement | null
  if (!target) return
  const digits = target.value.replace(/\D/g, '')
  if (!digits) {
    form.priceAmount = ''
    target.value = ''
    return
  }
  const cents = Number.parseInt(digits, 10)
  const formatted = (cents / 100).toFixed(2).replace('.', ',')
  form.priceAmount = formatted
  target.value = formatted
}

watch([searchTerm, statusFilter, categoryFilter], ([search, status, category]) => {
  setFilters({
    search: search || undefined,
    status: status === 'all' ? undefined : status,
    category: category === 'all' ? undefined : category,
  })
})

watch(sheetOpen, (isOpen) => {
  if (!isOpen) {
    resetForm()
    sheetMode.value = 'create'
  }
})

onMounted(() => {
  setFilters({ search: '', status: 'all', category: 'all' })
  fetchProducts()
})
</script>

<template>
  <div class="flex flex-1 flex-col gap-6">
    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
      <div>
        <h1 class="text-2xl font-semibold">Produtos</h1>
        <p class="text-sm text-muted-foreground">
          Cadastre e gerencie itens do catálogo. Use o ícone de moeda na tabela para ajustar preços rapidamente.
        </p>
      </div>
      <Button type="button" @click="openCreateSheet">
        <Plus class="h-4 w-4" />
        Novo produto
      </Button>
    </div>

    <div class="flex flex-col gap-4 rounded-2xl border bg-card p-4 shadow-sm">
      <div class="grid gap-3 md:grid-cols-2 lg:grid-cols-3">
        <div class="space-y-1.5">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground">Busca rápida</p>
          <InputGroup class="h-12 bg-background">
            <InputGroupAddon class="gap-2 text-muted-foreground">
              <Search class="h-4 w-4" />
            </InputGroupAddon>
            <InputGroupInput
              v-model="searchTerm"
              type="text"
              placeholder="Nome, slug ou palavra-chave"
              class="h-12"
            />
          </InputGroup>
        </div>
        <div class="space-y-1.5">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground">Status</p>
          <InputGroup class="h-12 bg-background">
            <InputGroupAddon class="text-muted-foreground">
              <SlidersHorizontal class="h-4 w-4" />
            </InputGroupAddon>
            <select
              v-model="statusFilter"
              class="h-full flex-1 appearance-none bg-transparent pl-2 pr-8 text-sm font-medium text-foreground outline-none"
              aria-label="Filtrar por status"
            >
              <option value="all">Todos os status</option>
              <option value="active">Somente ativos</option>
              <option value="inactive">Somente inativos</option>
            </select>
          </InputGroup>
        </div>
        <div class="space-y-1.5">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground">Categoria</p>
          <InputGroup class="h-12 bg-background">
            <InputGroupAddon class="text-muted-foreground">
              <Package class="h-4 w-4" />
            </InputGroupAddon>
            <select
              v-model="categoryFilter"
              class="h-full flex-1 appearance-none bg-transparent pl-2 pr-8 text-sm font-medium text-foreground outline-none"
              aria-label="Filtrar por categoria"
            >
              <option value="all">Todas as categorias</option>
              <option
                v-for="category in availableCategories"
                :key="category"
                :value="category"
              >
                {{ category }}
              </option>
            </select>
          </InputGroup>
        </div>
      </div>

      <Table class="mt-4">
        <TableHeader>
          <TableRow>
            <TableHead>Produto</TableHead>
            <TableHead>Categoria</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Criado em</TableHead>
            <TableHead class="text-right">Ações</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <template v-if="loading">
            <TableRow v-for="index in 5" :key="index">
              <TableCell><Skeleton class="h-12 w-full" /></TableCell>
              <TableCell><Skeleton class="h-5 w-20" /></TableCell>
              <TableCell><Skeleton class="h-6 w-24" /></TableCell>
              <TableCell><Skeleton class="h-5 w-28" /></TableCell>
              <TableCell class="text-right"><Skeleton class="h-8 w-32 ml-auto" /></TableCell>
            </TableRow>
          </template>
          <template v-else-if="filteredList.length">
            <TableRow
              v-for="product in filteredList"
              :key="product.idProduct"
            >
              <TableCell>
                <div class="flex items-center gap-4">
                  <div class="size-14 overflow-hidden rounded-lg border bg-muted">
                    <img
                      v-if="product.imagemurlProduct"
                      :src="product.imagemurlProduct"
                      :alt="product.nomeProduct"
                      class="h-full w-full object-cover"
                      loading="lazy"
                    />
                    <div
                      v-else
                      class="flex h-full w-full items-center justify-center text-muted-foreground"
                    >
                      <Package class="h-5 w-5" />
                    </div>
                  </div>
                  <div>
                    <p class="font-semibold">{{ product.nomeProduct }}</p>
                    <p class="text-xs text-muted-foreground">{{ product.slugProduct }}</p>
                  </div>
                </div>
              </TableCell>
              <TableCell>{{ product.categoriaProduct }}</TableCell>
              <TableCell>
                <span
                  class="inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-semibold"
                  :class="product.ativo
                    ? 'bg-emerald-100 text-emerald-900 border border-emerald-200'
                    : 'bg-slate-100 text-slate-900 border border-slate-200'"
                >
                  <component :is="product.ativo ? ToggleRight : ToggleLeft" class="h-3.5 w-3.5" />
                  {{ product.ativo ? 'Ativo' : 'Inativo' }}
                </span>
              </TableCell>
              <TableCell>{{ formatDateTime(product.createdAt) }}</TableCell>
              <TableCell class="text-right">
                <div class="flex flex-wrap items-center justify-end gap-2">
                  <Button variant="ghost" size="icon-sm" @click="handleToggleStatus(product.idProduct, product.ativo)">
                    <component :is="product.ativo ? ToggleLeft : ToggleRight" class="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="icon-sm" @click="openEditSheet(product)">
                    <Pencil class="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="icon-sm" as-child :title="'Gerenciar preços'">
                    <NuxtLink :to="`/sistema/produtos/${product.idProduct}/precos`">
                      <CircleDollarSign class="h-4 w-4" />
                    </NuxtLink>
                  </Button>
                  <Button variant="ghost" size="icon-sm" as-child>
                    <NuxtLink :to="`/sistema/produtos/${product.idProduct}/estoque`">
                      <Package class="h-4 w-4" />
                    </NuxtLink>
                  </Button>
                  <Popover
                    :open="deletePopoverOpen === product.idProduct"
                    @update:open="(value) => { deletePopoverOpen = value ? product.idProduct : null }"
                  >
                    <PopoverTrigger as-child>
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        class="text-destructive"
                        @click.stop
                      >
                        <Trash2 class="h-4 w-4" />
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent align="end" class="w-64 space-y-3">
                      <div class="flex items-center gap-2 text-sm font-semibold text-destructive">
                        <AlertTriangle class="h-4 w-4" />
                        Remover {{ product.nomeProduct }}?
                      </div>
                      <p class="text-xs text-muted-foreground">
                        Só é possível excluir itens sem pedidos vinculados. O inventário será removido junto com o produto.
                      </p>
                      <div class="flex gap-2">
                        <Button
                          type="button"
                          variant="ghost"
                          class="flex-1"
                          @click="deletePopoverOpen = null"
                        >
                          Cancelar
                        </Button>
                        <Button
                          type="button"
                          variant="destructive"
                          class="flex-1"
                          :disabled="productMutations.loading.value && deletingProductId === product.idProduct"
                          @click="handleDeleteProduct(product)"
                        >
                          {{ deletingProductId === product.idProduct ? 'Excluindo...' : 'Excluir' }}
                        </Button>
                      </div>
                    </PopoverContent>
                  </Popover>
                </div>
              </TableCell>
            </TableRow>
          </template>
          <TableEmpty v-else :colspan="5">
            <Empty>
              <EmptyHeader>
                <EmptyTitle>Nenhum produto encontrado</EmptyTitle>
                <EmptyDescription>Ajuste os filtros ou cadastre um novo item.</EmptyDescription>
              </EmptyHeader>
            </Empty>
          </TableEmpty>
        </TableBody>
      </Table>
    </div>

    <Sheet v-model:open="sheetOpen">
      <SheetContent class="w-full overflow-y-auto sm:max-w-xl">
        <SheetHeader>
          <SheetTitle class="text-2xl font-semibold">
            {{ sheetMode === 'create' ? 'Novo produto' : 'Editar produto' }}
          </SheetTitle>
          <SheetDescription>
            {{
              sheetMode === 'create'
                ? 'Preencha os campos abaixo para disponibilizar o item no catálogo interno.'
                : 'Atualize informações do item sem precisar sair da listagem.'
            }}
          </SheetDescription>
        </SheetHeader>

        <div class="mt-6 space-y-6">
          <div class="rounded-2xl border border-[#a4f380]/40 bg-gradient-to-br from-[#f6d95c]/15 via-[#a4f380]/10 to-[#80d3e4]/15 p-4 text-sm text-foreground">
            <p class="font-semibold">Dica rápida</p>
            <p class="text-muted-foreground">
              Use nomes objetivos, descreva diferenciais e adicione imagens com boa qualidade para facilitar a aprovação.
            </p>
          </div>

          <FieldSet class="space-y-5">
            <Field>
              <FieldLabel>
                <Tag class="h-4 w-4 text-[#a4f380]" />
                Nome do produto
              </FieldLabel>
              <FieldContent>
                <InputGroup
                  class="h-12"
                  :class="formErrors.nomeProduct ? 'border-destructive ring-destructive/20' : ''"
                >
                  <InputGroupAddon class="text-muted-foreground">
                    <Sparkles class="h-4 w-4" />
                  </InputGroupAddon>
                  <InputGroupInput
                    id="product-name"
                    v-model="form.nomeProduct"
                    type="text"
                    placeholder="Kit de poções luminescentes"
                    :aria-invalid="Boolean(formErrors.nomeProduct)"
                    class="h-12"
                  />
                </InputGroup>
                <FieldDescription>Esse nome será usado nas tabelas e cards do catálogo.</FieldDescription>
                <FieldError v-if="formErrors.nomeProduct">
                  {{ formErrors.nomeProduct }}
                </FieldError>
              </FieldContent>
            </Field>

            <Field>
              <FieldLabel>
                <Hash class="h-4 w-4 text-[#80d3e4]" />
                Slug amigável
              </FieldLabel>
              <FieldContent>
                <InputGroup
                  class="h-12"
                  :class="formErrors.slugProduct ? 'border-destructive ring-destructive/20' : ''"
                >
                  <InputGroupAddon class="text-muted-foreground">
                    <Hash class="h-4 w-4" />
                  </InputGroupAddon>
                  <InputGroupInput
                    id="product-slug"
                    v-model="form.slugProduct"
                    type="text"
                    placeholder="kit-pocoes-luminescentes"
                    :aria-invalid="Boolean(formErrors.slugProduct)"
                    class="h-12"
                  />
                  <InputGroupButton type="button" size="sm" class="gap-2" @click="generateSlug">
                    <Sparkles class="h-3.5 w-3.5" />
                    Gerar
                  </InputGroupButton>
                </InputGroup>
                <FieldDescription>Utilizado nas URLs e integrações. Apenas letras minúsculas e hífens.</FieldDescription>
                <FieldError v-if="formErrors.slugProduct">
                  {{ formErrors.slugProduct }}
                </FieldError>
              </FieldContent>
            </Field>

            <Field>
              <FieldLabel>
                <FileText class="h-4 w-4 text-[#f6d95c]" />
                Descrição detalhada
              </FieldLabel>
              <FieldContent>
                <InputGroup
                  class="min-h-[140px]"
                  :class="formErrors.descricaoProduct ? 'border-destructive ring-destructive/20' : ''"
                >
                  <InputGroupTextarea
                    id="product-description"
                    v-model="form.descricaoProduct"
                    rows="5"
                    placeholder="Fale sobre composição, diferenciais e recomendações de uso."
                    :aria-invalid="Boolean(formErrors.descricaoProduct)"
                  />
                </InputGroup>
                <FieldDescription>Conte a história do item, materiais e instruções principais.</FieldDescription>
                <FieldError v-if="formErrors.descricaoProduct">
                  {{ formErrors.descricaoProduct }}
                </FieldError>
              </FieldContent>
            </Field>

            <Field>
              <FieldLabel>
                <ImageIcon class="h-4 w-4 text-[#5dc5db]" />
                Imagem destacada
              </FieldLabel>
              <FieldContent>
                <input
                  ref="imageInputRef"
                  type="file"
                  accept="image/*"
                  class="hidden"
                  @change="handleImageSelect"
                />
                <div
                  class="relative flex flex-col gap-3 rounded-xl border border-dashed border-border/70 bg-muted/30 p-4 text-sm transition hover:border-primary/60"
                  :class="[
                    formErrors.imagemurlProduct ? 'border-destructive ring-destructive/20 bg-destructive/5' : '',
                    uploadingImage ? 'opacity-70' : '',
                  ]"
                  role="button"
                  tabindex="0"
                  @click="triggerImagePicker"
                  @dragover.prevent
                  @drop.prevent="handleImageDrop"
                >
                  <div class="flex items-center gap-3">
                    <div class="flex h-12 w-12 items-center justify-center rounded-full border bg-white shadow-sm">
                      <ImageIcon class="h-5 w-5 text-primary" />
                    </div>
                    <div class="flex-1">
                      <p class="font-semibold text-foreground">Envie uma imagem do produto</p>
                      <p class="text-xs text-muted-foreground">
                        Arraste ou clique para enviar para o bucket Trinket Store.
                      </p>
                    </div>
                    <Button type="button" variant="outline" size="sm" class="whitespace-nowrap" :disabled="uploadingImage">
                      <Loader2 v-if="uploadingImage" class="mr-2 h-4 w-4 animate-spin" />
                      <span v-else>Selecionar</span>
                    </Button>
                  </div>
                  <p class="text-[11px] text-muted-foreground">
                    Formatos JPG, PNG ou WEBP. O link público é salvo automaticamente.
                  </p>
                </div>

                <div class="mt-3 space-y-2">
                  <div class="rounded-lg border bg-muted/30 px-3 py-2 text-xs text-muted-foreground">
                    {{ form.imagemurlProduct ? 'Imagem enviada e URL salva.' : 'Envie uma imagem para gerar a URL pública.' }}
                  </div>
                </div>
                <FieldError v-if="formErrors.imagemurlProduct">
                  {{ formErrors.imagemurlProduct }}
                </FieldError>
                <div
                  v-if="form.imagemurlProduct"
                  class="mt-3 rounded-xl border bg-muted/40 p-3"
                >
                  <img
                    :src="form.imagemurlProduct"
                    alt="Pré-visualização do produto"
                    class="h-44 w-full rounded-lg object-cover"
                  />
                </div>
              </FieldContent>
            </Field>

            <Field>
              <FieldLabel>
                <Shapes class="h-4 w-4 text-[#a4f380]" />
                Categoria
              </FieldLabel>
              <FieldContent>
                <InputGroup
                  class="h-12"
                  :class="formErrors.categoriaProduct ? 'border-destructive ring-destructive/20' : ''"
                >
                  <InputGroupAddon class="text-muted-foreground">
                    <Package class="h-4 w-4" />
                  </InputGroupAddon>
                  <select
                    id="product-category"
                    v-model="form.categoriaProduct"
                    class="h-full flex-1 appearance-none bg-transparent pl-2 pr-8 text-sm font-medium text-foreground outline-none"
                    :aria-invalid="Boolean(formErrors.categoriaProduct)"
                  >
                    <option value="">Selecione a categoria</option>
                    <option
                      v-for="category in availableCategories"
                      :key="category"
                      :value="category"
                    >
                      {{ category }}
                    </option>
                  </select>
                </InputGroup>
                <FieldDescription>Ajuda a organizar filtros e relatórios.</FieldDescription>
                <FieldError v-if="formErrors.categoriaProduct">
                  {{ formErrors.categoriaProduct }}
                </FieldError>
              </FieldContent>
            </Field>

            <Field v-if="sheetMode === 'create'">
              <FieldLabel>
                <Warehouse class="h-4 w-4 text-[#a4f380]" />
                Estoque inicial
              </FieldLabel>
              <FieldContent>
                <InputGroup
                  class="h-12"
                  :class="formErrors.initialStock ? 'border-destructive ring-destructive/20' : ''"
                >
                  <InputGroupAddon class="text-muted-foreground">
                    <Warehouse class="h-4 w-4" />
                  </InputGroupAddon>
                  <InputGroupInput
                    id="initial-stock"
                    v-model.number="form.initialStock"
                    type="number"
                    min="0"
                    step="1"
                    placeholder="0"
                    :aria-invalid="Boolean(formErrors.initialStock)"
                  />
                </InputGroup>
                <FieldDescription>Valor usado para criar o inventário inicial do produto.</FieldDescription>
                <FieldError v-if="formErrors.initialStock">
                  {{ formErrors.initialStock }}
                </FieldError>
              </FieldContent>
            </Field>

            <Field v-if="sheetMode === 'create'">
              <FieldLabel>
                <CircleDollarSign class="h-4 w-4 text-[#80d3e4]" />
                Preço vigente
              </FieldLabel>
              <FieldContent>
                <InputGroup
                  class="h-12"
                  :class="formErrors.priceAmount ? 'border-destructive ring-destructive/20' : ''"
                >
                  <InputGroupAddon class="text-muted-foreground font-semibold">
                    BRL
                  </InputGroupAddon>
                  <InputGroupInput
                    id="initial-price"
                    v-model="form.priceAmount"
                    type="text"
                    inputmode="decimal"
                    placeholder="199,90"
                    :aria-invalid="Boolean(formErrors.priceAmount)"
                    @input="handlePriceInput"
                    class="h-12"
                  />
                </InputGroup>
                <FieldDescription>Será aplicado automaticamente como preço vigente após o cadastro.</FieldDescription>
                <FieldError v-if="formErrors.priceAmount">
                  {{ formErrors.priceAmount }}
                </FieldError>
              </FieldContent>
            </Field>
          </FieldSet>

          <div class="rounded-2xl border border-dashed bg-muted/40 p-4">
            <Field>
              <FieldLabel>
                <ToggleRight class="h-4 w-4 text-emerald-500" />
                Disponibilidade
              </FieldLabel>
              <FieldContent>
                <Button
                  type="button"
                  variant="outline"
                  class="w-full justify-between gap-3 border border-current"
                  :class="form.ativo ? 'border-emerald-200 bg-emerald-50 text-emerald-900' : 'text-muted-foreground'"
                  @click="form.ativo = !form.ativo"
                >
                  <span class="text-sm font-medium">
                    {{ form.ativo ? 'Disponível no catálogo' : 'Oculto temporariamente' }}
                  </span>
                  <component :is="form.ativo ? ToggleRight : ToggleLeft" class="h-5 w-5" />
                </Button>
                <FieldDescription>Itens inativos permanecem cadastrados, mas não aparecem nas buscas.</FieldDescription>
              </FieldContent>
            </Field>
          </div>
        </div>

        <SheetFooter class="mt-6">
          <SheetClose as-child>
            <Button type="button" variant="ghost" :disabled="creationFlowRunning">
              Cancelar
            </Button>
          </SheetClose>
          <Button
            type="button"
            class="gap-2"
            :disabled="productMutations.loading.value || creationFlowRunning"
            @click="handleSubmitProduct"
          >
            <Sparkles class="h-4 w-4" />
            {{
              productMutations.loading.value || creationFlowRunning
                ? 'Processando...'
                : sheetMode === 'create'
                  ? 'Salvar e configurar'
                  : 'Atualizar produto'
            }}
          </Button>
        </SheetFooter>
      </SheetContent>
    </Sheet>
    <Teleport to="body">
      <div
        v-if="creationFlow.open"
        class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 px-4 backdrop-blur-sm"
        aria-live="polite"
      >
        <div class="w-full max-w-md rounded-3xl border bg-card p-6 shadow-2xl">
          <div class="mb-5 flex items-center gap-3">
            <Sparkles class="h-5 w-5 text-[#a4f380]" />
            <div>
              <p class="text-lg font-semibold">Configurando produto</p>
              <p class="text-sm text-muted-foreground">Segure um instante, estamos finalizando as etapas.</p>
            </div>
          </div>
          <div class="space-y-3">
            <div
              v-for="step in creationSteps"
              :key="step.key"
              class="flex items-center gap-3 rounded-2xl border px-3 py-2"
              :class="step.status === 'success'
                ? 'border-emerald-200 bg-emerald-50/60'
                : step.status === 'error'
                  ? 'border-destructive/50 bg-destructive/5'
                  : 'border-border bg-background/40'"
            >
              <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-muted">
                <component :is="step.icon" class="h-5 w-5 text-muted-foreground" />
              </div>
              <div class="flex flex-1 items-center justify-between gap-3">
                <div>
                  <p class="text-sm font-semibold">{{ step.label }}</p>
                  <p class="text-xs text-muted-foreground">{{ creationStepStatusCopy[step.status] }}</p>
                </div>
                <div class="flex items-center justify-center">
                  <Loader2
                    v-if="step.status === 'in_progress'"
                    class="h-4 w-4 animate-spin text-muted-foreground"
                  />
                  <CheckCircle2
                    v-else-if="step.status === 'success'"
                    class="h-4 w-4 text-emerald-500"
                  />
                  <XCircle
                    v-else-if="step.status === 'error'"
                    class="h-4 w-4 text-destructive"
                  />
                  <span v-else class="text-xs font-semibold text-muted-foreground/60">•••</span>
                </div>
              </div>
            </div>
          </div>
          <p
            v-if="creationFlow.errorMessage"
            class="mt-4 text-center text-sm font-medium text-destructive"
          >
            {{ creationFlow.errorMessage }}
          </p>
        </div>
      </div>
    </Teleport>
  </div>
</template>
