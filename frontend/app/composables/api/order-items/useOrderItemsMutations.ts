import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { OrderItemEntity, OrderItemPayload } from '~/types/order-items'

export const useOrderItemsMutations = () => {
  const loading = useState('orderItems:mutations:loading', () => false)
  const error = useState<ApiError | null>('orderItems:mutations:error', () => null)
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

  const addOrderItem = async (orderId: number, payload: OrderItemPayload) => {
    return await handleRequest(
      useBackendFetchDirect<OrderItemEntity>(`/order-items/order/${orderId}`, {
        method: 'POST',
        body: payload,
      }),
    )
  }

  const updateOrderItem = async (itemId: number, payload: Partial<OrderItemPayload>) => {
    return await handleRequest(
      useBackendFetchDirect<OrderItemEntity>(`/order-items/${itemId}`, {
        method: 'PUT',
        body: payload,
      }),
    )
  }

  const deleteOrderItem = async (itemId: number) => {
    return await handleRequest(
      useBackendFetchDirect<void>(`/order-items/${itemId}`, {
        method: 'DELETE',
      }),
    )
  }

  const clearOrderItems = async (orderId: number) => {
    return await handleRequest(
      useBackendFetchDirect<void>(`/order-items/order/${orderId}`, {
        method: 'DELETE',
      }),
    )
  }

  return {
    loading: readonly(loading),
    error: readonly(error),
    addOrderItem,
    updateOrderItem,
    deleteOrderItem,
    clearOrderItems,
  }
}
