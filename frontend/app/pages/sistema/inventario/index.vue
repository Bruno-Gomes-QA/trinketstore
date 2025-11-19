<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useIntervalFn } from '@vueuse/core'
import {
  ArrowDownToLine,
  ArrowUpToLine,
  CheckCircle,
  Package,
  RefreshCw,
  Search,
  SlidersHorizontal,
  Warehouse,
} from 'lucide-vue-next'
import { Button } from '~/components/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '~/components/ui/table'
import { InputGroup, InputGroupAddon, InputGroupInput } from '~/components/ui/input-group'
import { Skeleton } from '~/components/ui/skeleton'
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyTitle,
} from '~/components/ui/empty'
import { useToast } from '~/components/ui/toast/use-toast'
import type { InventoryEntity } from '~/types/inventory'

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Inventário',
})

const { toast } = useToast()
const { formatDateTime } = useFormatters()

const {
  items,
  loading,
  fetchInventory,
} = useInventoryList()
const inventoryMutations = useInventoryMutations()

const searchTerm = ref('')
const viewFilter = ref<'all' | 'available' | 'out'>('all')
const quickAdjustAmount = ref(10)
const autoRefreshEnabled = ref(true)
const lastSyncAt = ref<string | null>(null)
const rowProcessingId = ref<number | null>(null)

const stats = computed(() => {
  const total = items.value.length
  const available = items.value.filter((item) => item.qtyOnHand > 0).length
  const out = items.value.filter((item) => item.qtyOnHand === 0).length

  return {
    total,
    available,
    out,
  }
})

const matchesSearch = (item: InventoryEntity) => {
  if (!searchTerm.value) return true
  const search = searchTerm.value.toLowerCase()
  const source = [
    item.nomeProduct,
    item.slugProduct,
    item.categoriaProduct,
    `#${item.productId}`,
  ]
  return source.some((field) => field?.toLowerCase().includes(search))
}

const matchesView = (item: InventoryEntity) => {
  switch (viewFilter.value) {
    case 'available':
      return item.qtyOnHand > 0
    case 'out':
      return item.qtyOnHand === 0
    default:
      return true
  }
}

const displayedItems = computed(() => {
  return [...items.value]
    .filter((item) => matchesSearch(item) && matchesView(item))
    .sort((a, b) => {
      if (viewFilter.value === 'all') {
        return a.qtyOnHand - b.qtyOnHand
      }
      return b.qtyOnHand - a.qtyOnHand
    })
})

const tableEmpty = computed(() => !loading.value && displayedItems.value.length === 0)

const syncInventory = async (options?: { silent?: boolean }) => {
  await fetchInventory()
  lastSyncAt.value = new Date().toISOString()
  if (!options?.silent) {
    toast({
      title: 'Inventário sincronizado',
      description: 'Dados atualizados com sucesso.',
    })
  }
}

const AUTO_REFRESH_MS = 60000
const { pause: pauseAutoRefresh, resume: resumeAutoRefresh } = useIntervalFn(
  async () => {
    if (!autoRefreshEnabled.value) return
    await syncInventory({ silent: true })
  },
  AUTO_REFRESH_MS,
  { immediate: false },
)

watch(autoRefreshEnabled, (enabled) => {
  if (enabled) {
    resumeAutoRefresh()
  } else {
    pauseAutoRefresh()
  }
}, { immediate: true })

watch(quickAdjustAmount, (value) => {
  if (!Number.isFinite(value) || value < 1) {
    quickAdjustAmount.value = 1
  }
})

const handleQuickAdjust = async (item: InventoryEntity, delta: number) => {
  if (!item?.idInventory || delta === 0) return
  rowProcessingId.value = item.idInventory
  const quantity = Math.abs(delta)
  const mutation = delta > 0
    ? inventoryMutations.addStock
    : inventoryMutations.removeStock

  const result = await mutation(item.idInventory, quantity)
  rowProcessingId.value = null

  if (result) {
    toast({
      title: delta > 0 ? 'Estoque incrementado' : 'Estoque decrementado',
      description: `${item.nomeProduct || `Produto #${item.productId}`} atualizado.`,
    })
    await syncInventory({ silent: true })
  } else {
    toast({
      variant: 'destructive',
      title: 'Não foi possível ajustar',
      description: inventoryMutations.error.value?.message || 'Revise o inventário e tente novamente.',
    })
  }
}

onMounted(async () => {
  await syncInventory({ silent: true })
  if (autoRefreshEnabled.value) {
    resumeAutoRefresh()
  }
})

onBeforeUnmount(() => {
  pauseAutoRefresh()
})
</script>

<template>
  <div class="flex flex-1 flex-col gap-6">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold">Controle de inventário</h1>
        <p class="text-sm text-muted-foreground">
          Monitore o estoque em tempo real e ajuste cada produto com apenas alguns cliques.
        </p>
      </div>
      <div class="flex flex-wrap gap-3">
        <label class="flex items-center gap-2 rounded-full border px-3 py-1.5 text-xs font-medium">
          <input
            v-model="autoRefreshEnabled"
            type="checkbox"
            class="h-4 w-4 rounded border-muted-foreground/40 text-emerald-500 focus:ring-emerald-200"
          />
          Atualização automática
        </label>
        <Button type="button" variant="outline" :disabled="loading" @click="syncInventory()">
          <RefreshCw
            class="mr-2 h-4 w-4"
            :class="loading ? 'animate-spin' : ''"
          />
          Atualizar agora
        </Button>
      </div>
    </div>

    <div class="grid gap-4 md:grid-cols-3">
      <div class="rounded-2xl border bg-card p-4 shadow-sm">
        <p class="text-xs uppercase text-muted-foreground">SKUs monitorados</p>
        <div class="mt-2 flex items-end gap-2">
          <Warehouse class="h-8 w-8 text-[#80d3e4]" />
          <p class="text-3xl font-bold">{{ stats.total }}</p>
        </div>
        <p class="text-xs text-muted-foreground">Sincronizado {{ formatDateTime(lastSyncAt) }}</p>
      </div>
      <div class="rounded-2xl border bg-card p-4 shadow-sm">
        <p class="text-xs uppercase text-muted-foreground">Em estoque</p>
        <div class="mt-2 flex items-center gap-2">
          <CheckCircle class="h-6 w-6 text-[#a4f380]" />
          <p class="text-2xl font-semibold">{{ stats.available }}</p>
        </div>
        <p class="text-xs text-muted-foreground">Produtos com quantidade disponível</p>
      </div>
      <div class="rounded-2xl border bg-card p-4 shadow-sm">
        <p class="text-xs uppercase text-muted-foreground">Sem estoque</p>
        <div class="mt-2 flex items-center gap-2">
          <Package class="h-6 w-6 text-destructive" />
          <p class="text-2xl font-semibold">{{ stats.out }}</p>
        </div>
        <p class="text-xs text-muted-foreground">Itens que precisam de reposição</p>
      </div>
    </div>

    <div class="flex flex-col gap-4 rounded-2xl border bg-card p-5 shadow-sm">
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
              placeholder="Buscar por nome, slug ou ID"
              class="h-12"
            />
          </InputGroup>
        </div>
        <div class="space-y-1.5">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground">Disponibilidade</p>
          <InputGroup class="h-12 bg-background">
            <InputGroupAddon class="text-muted-foreground">
              <SlidersHorizontal class="h-4 w-4" />
            </InputGroupAddon>
            <select
              id="view-filter"
              v-model="viewFilter"
              class="h-full flex-1 appearance-none bg-transparent pl-2 pr-8 text-sm font-medium text-foreground outline-none"
            >
              <option value="all">Todos os produtos</option>
              <option value="available">Somente em estoque</option>
              <option value="out">Somente esgotados</option>
            </select>
          </InputGroup>
        </div>
        <div class="space-y-1.5">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground">Passo rápido (±)</p>
          <InputGroup class="h-12 bg-background">
            <InputGroupAddon class="text-muted-foreground font-semibold">
              ±
            </InputGroupAddon>
            <InputGroupInput
              id="quick-step"
              v-model.number="quickAdjustAmount"
              type="number"
              min="1"
              inputmode="numeric"
              class="h-12 text-right"
            />
          </InputGroup>
        </div>
      </div>

      <Table class="mt-4">
        <TableHeader>
          <TableRow>
            <TableHead>Produto</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Quantidade</TableHead>
            <TableHead class="text-right">Ações</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <template v-if="loading">
            <TableRow v-for="index in 5" :key="index">
              <TableCell><Skeleton class="h-12 w-full" /></TableCell>
              <TableCell><Skeleton class="h-6 w-24" /></TableCell>
              <TableCell><Skeleton class="h-6 w-16" /></TableCell>
              <TableCell class="text-right"><Skeleton class="h-8 w-32 ml-auto" /></TableCell>
            </TableRow>
          </template>
          <template v-else-if="displayedItems.length">
            <TableRow
              v-for="item in displayedItems"
              :key="item.idInventory"
              :class="item.qtyOnHand === 0 ? 'bg-destructive/5' : ''"
            >
              <TableCell>
                <div class="flex items-center gap-4">
                  <div class="size-12 overflow-hidden rounded-lg border bg-muted">
                    <img
                      v-if="item.imagemurlProduct"
                      :src="item.imagemurlProduct"
                      :alt="item.nomeProduct"
                      class="h-full w-full object-cover"
                      loading="lazy"
                    />
                    <div
                      v-else
                      class="flex h-full w-full items-center justify-center text-muted-foreground"
                    >
                      <Package class="h-4 w-4" />
                    </div>
                  </div>
                  <div>
                    <p class="font-semibold">
                      {{ item.nomeProduct || `Produto #${item.productId}` }}
                    </p>
                    <p class="text-xs text-muted-foreground">
                      {{ item.categoriaProduct || 'Sem categoria' }} · {{ item.slugProduct || 'Sem slug' }}
                    </p>
                  </div>
                </div>
              </TableCell>
              <TableCell>
                <span
                  class="inline-flex items-center rounded-full px-3 py-0.5 text-xs font-semibold"
                  :class="item.qtyOnHand === 0
                    ? 'bg-destructive/10 text-destructive'
                    : 'bg-emerald-100 text-emerald-900'"
                >
                  {{ item.qtyOnHand === 0 ? 'Sem estoque' : 'Em estoque' }}
                </span>
              </TableCell>
              <TableCell class="font-semibold">
                {{ item.qtyOnHand }} un
              </TableCell>
              <TableCell class="text-right">
                <div class="flex items-center justify-end gap-2">
                  <Button
                    type="button"
                    size="icon-sm"
                    variant="outline"
                    :disabled="rowProcessingId === item.idInventory"
                    @click="handleQuickAdjust(item, -quickAdjustAmount)"
                  >
                    <ArrowDownToLine class="h-4 w-4" />
                  </Button>
                  <Button
                    type="button"
                    size="icon-sm"
                    variant="secondary"
                    :disabled="rowProcessingId === item.idInventory"
                    @click="handleQuickAdjust(item, quickAdjustAmount)"
                  >
                    <ArrowUpToLine class="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="sm" as-child>
                    <NuxtLink :to="`/sistema/produtos/${item.productId}/estoque`">
                      Ajustar manualmente
                    </NuxtLink>
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </template>
          <TableEmpty v-else :colspan="4">
            <Empty>
              <EmptyHeader>
                <EmptyTitle>Nenhum item encontrado</EmptyTitle>
                <EmptyDescription>
                  Ajuste filtros ou cadastre novos produtos para começar a monitorar.
                </EmptyDescription>
              </EmptyHeader>
            </Empty>
          </TableEmpty>
        </TableBody>
      </Table>
    </div>
  </div>

</template>
