import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { InventoryEntity } from '~/types/inventory'

export const useInventoryByProduct = () => {
  const record = useState<InventoryEntity | null>('inventory:product:data', () => null)
  const loading = useState('inventory:product:loading', () => false)
  const error = useState<ApiError | null>('inventory:product:error', () => null)
  const { normalizeApiError } = useErrorHandler()

  const fetchInventory = async (productId: number) => {
    if (!productId) return
    loading.value = true
    error.value = null
    try {
      const data = await useBackendFetchDirect<InventoryEntity>(`/inventory/product/${productId}`)
      record.value = data
    } catch (err) {
      error.value = normalizeApiError(err)
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    record.value = null
    error.value = null
  }

  return {
    inventory: readonly(record),
    loading: readonly(loading),
    error: readonly(error),
    fetchInventory,
    reset,
  }
}
