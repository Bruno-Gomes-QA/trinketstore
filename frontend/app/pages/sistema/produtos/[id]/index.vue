<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { FileText, Hash, ImageIcon, Link2, Package, Sparkles, Tag, ToggleLeft, ToggleRight } from 'lucide-vue-next'
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
import { Skeleton } from '~/components/ui/skeleton'
import { useToast } from '~/components/ui/toast/use-toast'

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Editar produto',
})

const route = useRoute()
const productId = Number(route.params.id)

const { toast } = useToast()
const { formatDateTime } = useFormatters()
const { toSlug, sanitizeUrl } = useNormalizers()
const { isValidName, isValidSlug, isValidUrl, hasLengthBetween } = useValidators()
const { product, loading, fetchProduct, reset } = useProductDetails()
const productMutations = useProductMutations()

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

const saving = ref(false)

const mapProductToForm = () => {
  if (!product.value) return
  form.nomeProduct = product.value.nomeProduct
  form.slugProduct = product.value.slugProduct
  form.descricaoProduct = product.value.descricaoProduct
  form.imagemurlProduct = product.value.imagemurlProduct
  form.categoriaProduct = product.value.categoriaProduct
  form.ativo = product.value.ativo
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
    formErrors.nomeProduct = 'Nome deve ter entre 2 e 300 caracteres.'
    valid = false
  }

  if (!isValidSlug(form.slugProduct)) {
    formErrors.slugProduct = 'Slug inválido.'
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
    formErrors.categoriaProduct = 'Informe a categoria.'
    valid = false
  }

  return valid
}

const handleSave = async () => {
  if (!validateForm()) return
  saving.value = true
  const updated = await productMutations.updateProduct(productId, { ...form })
  saving.value = false
  if (updated) {
    toast({ title: 'Produto atualizado', description: 'As alterações foram salvas.' })
    await fetchProduct(productId)
  }
}

const handleStatusToggle = async () => {
  if (!product.value) return
  const action = product.value.ativo ? productMutations.deactivateProduct : productMutations.activateProduct
  const updated = await action(productId)
  if (updated) {
    toast({
      title: updated.ativo ? 'Produto ativado' : 'Produto desativado',
      description: 'Status atualizado com sucesso.',
    })
    await fetchProduct(productId)
  }
}

const generateSlug = () => {
  form.slugProduct = toSlug(form.nomeProduct)
}

watch(product, (value) => {
  if (value) {
    mapProductToForm()
  }
})

onMounted(async () => {
  await fetchProduct(productId)
})

onBeforeUnmount(() => {
  reset()
})
</script>

<template>
  <div class="flex flex-1 flex-col gap-6">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold">{{ product?.nomeProduct || 'Produto' }}</h1>
        <p class="text-sm text-muted-foreground">
          Atualize dados, preços e estoque deste produto.
        </p>
      </div>
      <div class="flex flex-wrap gap-2">
        <Button variant="outline" as-child>
          <NuxtLink :to="`/sistema/produtos/${productId}/precos`">
            Gerenciar preços
          </NuxtLink>
        </Button>
        <Button variant="outline" as-child>
          <NuxtLink :to="`/sistema/produtos/${productId}/estoque`">
            Ajustar estoque
          </NuxtLink>
        </Button>
        <Button variant="ghost" @click="handleStatusToggle">
          {{ product?.ativo ? 'Desativar' : 'Ativar' }}
        </Button>
      </div>
    </div>

    <div v-if="loading" class="space-y-4">
      <Skeleton class="h-10 w-full" />
      <Skeleton class="h-32 w-full" />
      <Skeleton class="h-40 w-full" />
    </div>

    <div v-else class="grid gap-6 lg:grid-cols-[2fr,1fr]">
      <div class="rounded-2xl border bg-card p-6 shadow-sm space-y-6">
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
                  <Tag class="h-4 w-4" />
                </InputGroupAddon>
                <InputGroupInput
                  id="name"
                  v-model="form.nomeProduct"
                  placeholder="Nome do produto"
                  :aria-invalid="Boolean(formErrors.nomeProduct)"
                  class="h-12"
                />
              </InputGroup>
              <FieldDescription>Edite o título exibido nas listagens internas.</FieldDescription>
              <FieldError v-if="formErrors.nomeProduct">
                {{ formErrors.nomeProduct }}
              </FieldError>
            </FieldContent>
          </Field>

          <Field>
            <FieldLabel>
              <Hash class="h-4 w-4 text-[#80d3e4]" />
              Slug
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
                  id="slug"
                  v-model="form.slugProduct"
                  placeholder="slug-do-produto"
                  :aria-invalid="Boolean(formErrors.slugProduct)"
                  class="h-12"
                />
                <InputGroupButton type="button" size="sm" class="gap-2" @click="generateSlug">
                  <Sparkles class="h-3.5 w-3.5" />
                  Atualizar
                </InputGroupButton>
              </InputGroup>
              <FieldDescription>Útil para rotas e integrações com o e-commerce.</FieldDescription>
              <FieldError v-if="formErrors.slugProduct">
                {{ formErrors.slugProduct }}
              </FieldError>
            </FieldContent>
          </Field>

          <Field>
            <FieldLabel>
              <FileText class="h-4 w-4 text-[#f6d95c]" />
              Descrição
            </FieldLabel>
            <FieldContent>
              <InputGroup
                class="min-h-[150px]"
                :class="formErrors.descricaoProduct ? 'border-destructive ring-destructive/20' : ''"
              >
                <InputGroupTextarea
                  id="description"
                  v-model="form.descricaoProduct"
                  rows="5"
                  placeholder="Detalhes do produto"
                  :aria-invalid="Boolean(formErrors.descricaoProduct)"
                />
              </InputGroup>
              <FieldDescription>Conteúdo exibido na ficha completa do item.</FieldDescription>
              <FieldError v-if="formErrors.descricaoProduct">
                {{ formErrors.descricaoProduct }}
              </FieldError>
            </FieldContent>
          </Field>

          <Field>
            <FieldLabel>
              <ImageIcon class="h-4 w-4 text-[#5dc5db]" />
              Imagem (URL)
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
                  id="image"
                  v-model="form.imagemurlProduct"
                  type="url"
                  placeholder="https://..."
                  :aria-invalid="Boolean(formErrors.imagemurlProduct)"
                  class="h-12"
                />
              </InputGroup>
              <FieldDescription>O link precisa ser público e seguro (HTTPS).</FieldDescription>
              <FieldError v-if="formErrors.imagemurlProduct">
                {{ formErrors.imagemurlProduct }}
              </FieldError>
              <div v-if="form.imagemurlProduct" class="mt-3 rounded-xl border bg-muted/40 p-3">
                <img
                  :src="form.imagemurlProduct"
                  alt="Pré-visualização"
                  class="h-48 w-full rounded-lg object-cover"
                />
              </div>
            </FieldContent>
          </Field>

          <Field>
            <FieldLabel>
              <Package class="h-4 w-4 text-[#a4f380]" />
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
                <InputGroupInput
                  id="category"
                  v-model="form.categoriaProduct"
                  placeholder="Categoria"
                  :aria-invalid="Boolean(formErrors.categoriaProduct)"
                  class="h-12"
                />
              </InputGroup>
              <FieldDescription>Use nomes consistentes com as listagens.</FieldDescription>
              <FieldError v-if="formErrors.categoriaProduct">
                {{ formErrors.categoriaProduct }}
              </FieldError>
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
                :class="form.ativo ? 'border-emerald-200 bg-emerald-50 text-emerald-900' : 'text-muted-foreground'"
                @click="form.ativo = !form.ativo"
              >
                <span class="text-sm font-medium">
                  {{ form.ativo ? 'Produto ativo' : 'Produto inativo' }}
                </span>
                <component :is="form.ativo ? ToggleRight : ToggleLeft" class="h-5 w-5" />
              </Button>
              <FieldDescription>Ativos aparecem no catálogo e podem receber pedidos.</FieldDescription>
            </FieldContent>
          </Field>
        </FieldSet>

        <div class="flex items-center gap-3 pt-2">
          <Button :disabled="saving" @click="handleSave">
            {{ saving ? 'Salvando...' : 'Salvar alterações' }}
          </Button>
          <Button variant="ghost" type="button" @click="fetchProduct(productId)">
            Recarregar
          </Button>
        </div>
      </div>

      <div class="rounded-2xl border bg-card p-6 shadow-sm space-y-4">
        <div>
          <p class="text-xs text-muted-foreground uppercase">ID</p>
          <p class="font-semibold">#{{ product?.idProduct }}</p>
        </div>
        <div>
          <p class="text-xs text-muted-foreground uppercase">Criado em</p>
          <p>{{ product?.createdAt ? formatDateTime(product.createdAt) : '—' }}</p>
        </div>
        <div>
          <p class="text-xs text-muted-foreground uppercase">Slug</p>
          <p class="font-mono text-xs break-all">{{ product?.slugProduct }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
