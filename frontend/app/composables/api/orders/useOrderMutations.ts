import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { CreateOrderPayload, OrderEntity, OrderStatus } from '~/types/orders'

export const useOrderMutations = () => {
  const loading = useState('orders:mutations:loading', () => false)
  const error = useState<ApiError | null>('orders:mutations:error', () => null)
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

  const createOrder = async (payload: CreateOrderPayload) => {
    return await handleRequest(
      useBackendFetchDirect<OrderEntity>('/orders', {
        method: 'POST',
        body: payload,
      }),
    )
  }

  const updateOrderStatus = async (orderId: number, status: OrderStatus) => {
    return await handleRequest(
      useBackendFetchDirect<OrderEntity>(`/orders/${orderId}/status`, {
        method: 'PATCH',
        query: { status },
      }),
    )
  }

  return {
    loading: readonly(loading),
    error: readonly(error),
    createOrder,
    updateOrderStatus,
  }
}
