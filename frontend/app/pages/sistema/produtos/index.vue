<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import {
  Eye,
  FileText,
  Hash,
  ImageIcon,
  Link2,
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
} from 'lucide-vue-next'
import { Button } from '~/components/ui/button'
import {
  InputGroup,
  InputGroupAddon,
  InputGroupButton,
  InputGroupInput,
  InputGroupTextarea,
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

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Produtos',
})

const defaultCategories = ['Colecionáveis', 'Decoração', 'Acessórios', 'Vestuário', 'Outros']

const { toast } = useToast()
const { formatDateTime } = useFormatters()
const { toSlug, sanitizeUrl } = useNormalizers()
const { isValidName, isValidSlug, isValidUrl, hasLengthBetween } = useValidators()
const {
  products,
  filteredProducts,
  loading,
  fetchProducts,
  setFilters,
} = useProductsList()
const productMutations = useProductMutations()

const searchTerm = ref('')
const statusFilter = ref<'all' | 'active' | 'inactive'>('all')
const categoryFilter = ref<'all' | string>('all')
const sheetOpen = ref(false)

const form = reactive({
  nomeProduct: '',
  slugProduct: '',
  descricaoProduct: '',
  imagemurlProduct: '',
  categoriaProduct: '',
  ativo: true,
})

const formErrors = reactive({
  nomeProduct: '',
  slugProduct: '',
  descricaoProduct: '',
  imagemurlProduct: '',
  categoriaProduct: '',
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
  Object.keys(formErrors).forEach((key) => {
    formErrors[key as keyof typeof formErrors] = ''
  })
}

const openCreateSheet = () => {
  resetForm()
  sheetOpen.value = true
}

const closeSheet = () => {
  sheetOpen.value = false
}

const validateForm = () => {
  let valid = true
  formErrors.nomeProduct = ''
  formErrors.slugProduct = ''
  formErrors.descricaoProduct = ''
  formErrors.imagemurlProduct = ''
  formErrors.categoriaProduct = ''
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

  if (!isValidUrl(form.imagemurlProduct)) {
    formErrors.imagemurlProduct = 'URL inválida.'
    valid = false
  }

  if (!form.categoriaProduct) {
    formErrors.categoriaProduct = 'Selecione uma categoria.'
    valid = false
  }

  return valid
}

const handleCreateProduct = async () => {
  if (!validateForm()) return

  const created = await productMutations.createProduct({
    ...form,
  })

  if (created) {
    toast({ title: 'Produto criado', description: `${created.nomeProduct} adicionado com sucesso.` })
    closeSheet()
    await fetchProducts()
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
          Cadastre e gerencie itens do catálogo.
        </p>
      </div>
      <Button type="button" @click="openCreateSheet">
        <Plus class="h-4 w-4" />
        Novo produto
      </Button>
    </div>

    <div class="rounded-2xl border bg-card p-4 shadow-sm">
      <div class="grid gap-3 lg:grid-cols-[2fr,1fr,1fr]">
        <InputGroup class="h-12 bg-background">
          <InputGroupAddon class="gap-2 text-muted-foreground">
            <Search class="h-4 w-4" />
            <span class="hidden text-[10px] font-semibold uppercase tracking-[0.4em] text-muted-foreground/70 md:inline-flex">
              Buscar
            </span>
          </InputGroupAddon>
          <InputGroupInput
            v-model="searchTerm"
            type="text"
            placeholder="Nome, slug ou palavra-chave"
            class="h-12"
          />
        </InputGroup>
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
                  <Button variant="ghost" size="icon-sm" as-child>
                    <NuxtLink :to="`/sistema/produtos/${product.idProduct}`">
                      <Pencil class="h-4 w-4" />
                    </NuxtLink>
                  </Button>
                  <Button variant="ghost" size="icon-sm" as-child>
                    <NuxtLink :to="`/sistema/produtos/${product.idProduct}/precos`">
                      <Eye class="h-4 w-4" />
                    </NuxtLink>
                  </Button>
                  <Button variant="ghost" size="icon-sm" as-child>
                    <NuxtLink :to="`/sistema/produtos/${product.idProduct}/estoque`">
                      <Package class="h-4 w-4" />
                    </NuxtLink>
                  </Button>
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
          <SheetTitle class="text-2xl font-semibold">Novo produto</SheetTitle>
          <SheetDescription>
            Preencha os campos abaixo para disponibilizar o item no catálogo interno.
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
                <InputGroup
                  class="h-12"
                  :class="formErrors.imagemurlProduct ? 'border-destructive ring-destructive/20' : ''"
                >
                  <InputGroupAddon class="text-muted-foreground">
                    <Link2 class="h-4 w-4" />
                  </InputGroupAddon>
                  <InputGroupInput
                    id="product-image"
                    v-model="form.imagemurlProduct"
                    type="url"
                    placeholder="https://cdn.trinketstore.com/produto.png"
                    :aria-invalid="Boolean(formErrors.imagemurlProduct)"
                    class="h-12"
                  />
                </InputGroup>
                <FieldDescription>Suporte a imagens públicas (JPG, PNG ou WEBP).</FieldDescription>
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
            <Button type="button" variant="ghost">
              Cancelar
            </Button>
          </SheetClose>
          <Button
            type="button"
            class="gap-2"
            :disabled="productMutations.loading.value"
            @click="handleCreateProduct"
          >
            <Sparkles class="h-4 w-4" />
            {{ productMutations.loading.value ? 'Salvando...' : 'Salvar produto' }}
          </Button>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  </div>
</template>
