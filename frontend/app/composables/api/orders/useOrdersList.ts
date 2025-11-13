import { computed, readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { OrderEntity, OrderFilters } from '~/types/orders'

export const useOrdersList = () => {
  const orders = useState<OrderEntity[]>('orders:list:data', () => [])
  const loading = useState('orders:list:loading', () => false)
  const error = useState<ApiError | null>('orders:list:error', () => null)
  const filtersState = useState<OrderFilters>('orders:list:filters', () => ({}))
  const { normalizeApiError } = useErrorHandler()

  const fetchOrders = async (filters?: OrderFilters) => {
    loading.value = true
    error.value = null
    const appliedFilters = filters ?? filtersState.value
    filtersState.value = appliedFilters

    try {
      const data = await useBackendFetchDirect<OrderEntity[]>('/orders')
      orders.value = data
    } catch (err) {
      error.value = normalizeApiError(err)
    } finally {
      loading.value = false
    }
  }

  const filteredOrders = computed(() => {
    return orders.value.filter((order) => {
      const { status, checkoutId, paymentIntent, userId } = filtersState.value

      const matchesStatus = status && status !== 'all'
        ? order.statusOrder === status
        : true

      const matchesCheckout = checkoutId
        ? order.checkoutId.toLowerCase().includes(checkoutId.toLowerCase())
        : true

      const matchesPayment = paymentIntent
        ? order.paymentIntent.toLowerCase().includes(paymentIntent.toLowerCase())
        : true

      const matchesUser = userId
        ? order.userId === userId
        : true

      return matchesStatus && matchesCheckout && matchesPayment && matchesUser
    })
  })

  return {
    orders: readonly(orders),
    filteredOrders: readonly(filteredOrders),
    loading: readonly(loading),
    error: readonly(error),
    filters: readonly(filtersState),
    fetchOrders,
  }
}
