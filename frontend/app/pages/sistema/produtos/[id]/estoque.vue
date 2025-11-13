<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
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
const adjustForm = reactive({ add: '', remove: '' })
const formErrors = reactive({ absolute: '', add: '', remove: '' })
const saving = ref(false)

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

const handleAdjust = async (type: 'add' | 'remove') => {
  formErrors.add = ''
  formErrors.remove = ''
  const value = Number(type === 'add' ? adjustForm.add : adjustForm.remove)
  if (!isPositiveInteger(value) || value === 0) {
    formErrors[type] = 'Informe um valor inteiro válido.'
    return
  }

  if (!inventory.inventory.value) return
  saving.value = true
  const mutation = type === 'add' ? inventoryMutations.addStock : inventoryMutations.removeStock
  const updated = await mutation(inventory.inventory.value.idInventory, value)
  saving.value = false
  if (updated) {
    toast({
      title: type === 'add' ? 'Estoque incrementado' : 'Estoque decrementado',
      description: 'Quantidade ajustada.',
    })
    adjustForm.add = ''
    adjustForm.remove = ''
    await inventory.fetchInventory(productId)
  }
}

onMounted(loadData)
</script>

<template>
  <div class="flex flex-1 flex-col gap-6">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold">Estoque — {{ productDetails.product.value?.nomeProduct || 'Produto' }}</h1>
        <p class="text-sm text-muted-foreground">
          Ajuste rápido das quantidades disponíveis.
        </p>
      </div>
      <Button variant="outline" as-child>
        <NuxtLink :to="`/sistema/produtos/${productId}`">Voltar ao produto</NuxtLink>
      </Button>
    </div>

    <div v-if="inventory.loading.value" class="space-y-4">
      <Skeleton class="h-10 w-full" />
      <Skeleton class="h-32 w-full" />
    </div>

    <div v-else class="grid gap-6 lg:grid-cols-[2fr,1fr]">
      <div class="rounded-2xl border bg-card p-6 shadow-sm space-y-6">
        <div>
          <h3 class="text-lg font-semibold">Definir quantidade absoluta</h3>
          <p class="text-sm text-muted-foreground">
            Define exatamente o estoque disponível para o produto.
          </p>
          <div class="mt-4 space-y-2">
            <Label for="absolute-qty">Quantidade</Label>
            <Input
              id="absolute-qty"
              v-model="absoluteForm.qty"
              type="number"
              min="0"
              placeholder="Ex.: 120"
            />
            <p v-if="formErrors.absolute" class="text-xs text-destructive">
              {{ formErrors.absolute }}
            </p>
            <Button :disabled="saving" @click="handleSetAbsolute">
              {{ saving ? 'Salvando...' : 'Atualizar quantidade' }}
            </Button>
          </div>
        </div>

        <div>
          <h3 class="text-lg font-semibold">Ajustes rápidos</h3>
          <p class="text-sm text-muted-foreground">
            Adicione ou remova unidades sem precisar redefinir tudo.
          </p>
          <div class="mt-4 grid gap-4 sm:grid-cols-2">
            <div class="rounded-lg border p-4 space-y-2">
              <Label for="add-stock">Adicionar</Label>
              <Input
                id="add-stock"
                v-model="adjustForm.add"
                type="number"
                min="0"
                placeholder="Ex.: 15"
              />
              <p v-if="formErrors.add" class="text-xs text-destructive">
                {{ formErrors.add }}
              </p>
              <Button variant="secondary" :disabled="saving" @click="handleAdjust('add')">
                {{ saving ? 'Aplicando...' : 'Adicionar' }}
              </Button>
            </div>

            <div class="rounded-lg border p-4 space-y-2">
              <Label for="remove-stock">Remover</Label>
              <Input
                id="remove-stock"
                v-model="adjustForm.remove"
                type="number"
                min="0"
                placeholder="Ex.: 5"
              />
              <p v-if="formErrors.remove" class="text-xs text-destructive">
                {{ formErrors.remove }}
              </p>
              <Button variant="destructive" :disabled="saving" @click="handleAdjust('remove')">
                {{ saving ? 'Aplicando...' : 'Remover' }}
              </Button>
            </div>
          </div>
        </div>
      </div>

      <div class="rounded-2xl border bg-card p-6 shadow-sm space-y-3">
        <div>
          <p class="text-xs text-muted-foreground uppercase">Quantidade atual</p>
          <p class="text-3xl font-bold">
            {{ inventory.inventory.value?.qtyOnHand ?? 0 }} un
          </p>
        </div>
        <div>
          <p class="text-xs text-muted-foreground uppercase">Última atualização</p>
          <p class="font-medium">
            {{ formatDateTime(productDetails.product.value?.updatedAt) }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
