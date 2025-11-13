import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type {
  CreateInventoryPayload,
  InventoryEntity,
  UpdateInventoryPayload,
} from '~/types/inventory'

export const useInventoryMutations = () => {
  const loading = useState('inventory:mutations:loading', () => false)
  const error = useState<ApiError | null>('inventory:mutations:error', () => null)
  const { normalizeApiError } = useErrorHandler()

  const handleRequest = async <T>(promise: Promise<T>): Promise<T | null> => {
    loading.value = true
    error.value = null
    try {
      return await promise
    } catch (err) {
      error.value = normalizeApiError(err)
      return null
    } finally {
      loading.value = false
    }
  }

  const createInventory = async (payload: CreateInventoryPayload) => {
    return await handleRequest(
      useBackendFetchDirect<InventoryEntity>('/inventory', {
        method: 'POST',
        body: payload,
      }),
    )
  }

  const updateInventory = async (inventoryId: number, payload: UpdateInventoryPayload) => {
    return await handleRequest(
      useBackendFetchDirect<InventoryEntity>(`/inventory/${inventoryId}`, {
        method: 'PUT',
        body: payload,
      }),
    )
  }

  const addStock = async (inventoryId: number, quantity: number) => {
    return await handleRequest(
      useBackendFetchDirect<InventoryEntity>(`/inventory/${inventoryId}/add-stock/${quantity}`, {
        method: 'PATCH',
      }),
    )
  }

  const removeStock = async (inventoryId: number, quantity: number) => {
    return await handleRequest(
      useBackendFetchDirect<InventoryEntity>(`/inventory/${inventoryId}/remove-stock/${quantity}`, {
        method: 'PATCH',
      }),
    )
  }

  return {
    loading: readonly(loading),
    error: readonly(error),
    createInventory,
    updateInventory,
    addStock,
    removeStock,
  }
}
