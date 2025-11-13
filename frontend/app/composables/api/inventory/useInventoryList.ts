import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { InventoryEntity, InventoryFilters } from '~/types/inventory'

export const useInventoryList = () => {
  const items = useState<InventoryEntity[]>('inventory:list:data', () => [])
  const loading = useState('inventory:list:loading', () => false)
  const error = useState<ApiError | null>('inventory:list:error', () => null)
  const filtersState = useState<InventoryFilters>('inventory:list:filters', () => ({}))
  const { normalizeApiError } = useErrorHandler()

  const resolveEndpoint = (filters?: InventoryFilters) => {
    if (filters?.lowStockThreshold) {
      return `/inventory/low-stock/${filters.lowStockThreshold}`
    }
    if (filters?.withStockOnly) {
      return '/inventory/in-stock'
    }
    return '/inventory'
  }

  const fetchInventory = async (filters?: InventoryFilters) => {
    loading.value = true
    error.value = null
    const appliedFilters = filters ?? filtersState.value
    filtersState.value = appliedFilters

    try {
      const endpoint = resolveEndpoint(appliedFilters)
      const data = await useBackendFetchDirect<InventoryEntity[]>(endpoint)
      items.value = data
    } catch (err) {
      error.value = normalizeApiError(err)
    } finally {
      loading.value = false
    }
  }

  return {
    items: readonly(items),
    loading: readonly(loading),
    error: readonly(error),
    filters: readonly(filtersState),
    fetchInventory,
  }
}
