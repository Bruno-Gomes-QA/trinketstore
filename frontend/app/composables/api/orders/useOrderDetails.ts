import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { OrderItemEntity } from '~/types/order-items'
import type { OrderEntity } from '~/types/orders'

export const useOrderDetails = () => {
  const order = useState<OrderEntity | null>('orders:details:data', () => null)
  const items = useState<OrderItemEntity[]>('orders:details:items', () => [])
  const loading = useState('orders:details:loading', () => false)
  const error = useState<ApiError | null>('orders:details:error', () => null)
  const { normalizeApiError } = useErrorHandler()

  const fetchOrder = async (orderId: number) => {
    if (!orderId) return
    loading.value = true
    error.value = null

    try {
      const [orderResponse, orderItems] = await Promise.all([
        useBackendFetchDirect<OrderEntity>(`/orders/${orderId}`),
        useBackendFetchDirect<OrderItemEntity[]>(`/order-items/order/${orderId}`),
      ])

      order.value = orderResponse
      items.value = orderItems
    } catch (err) {
      error.value = normalizeApiError(err)
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    order.value = null
    items.value = []
    error.value = null
  }

  return {
    order: readonly(order),
    items: readonly(items),
    loading: readonly(loading),
    error: readonly(error),
    fetchOrder,
    reset,
  }
}
