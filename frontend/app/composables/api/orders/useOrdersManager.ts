import { ref, computed } from 'vue'
import type { OrderResponse } from '~/types/orders'
import type { ProductResponse } from '~/types/products'

export const useOrdersManager = () => {
  const orders = ref<OrderResponse[]>([])
  const loadingOrders = ref(false)
  const ordersError = ref<string | null>(null)
  const highlightedOrderId = ref<number | null>(null)
  const productCache = ref<Record<number, ProductResponse>>({})

  const selectedOrder = computed(() => {
    if (!orders.value.length) return null
    if (highlightedOrderId.value) {
      const found = orders.value.find((order) => order.idOrder === highlightedOrderId.value)
      if (found) return found
    }
    return orders.value[0]
  })

  const fetchOrders = async (userId: number) => {
    loadingOrders.value = true
    ordersError.value = null
    try {
      const response = await useBackendFetchDirect<OrderResponse[]>(`/orders/user/${userId}`)
      orders.value = [...response].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      
      if (!highlightedOrderId.value && orders.value.length) {
        highlightedOrderId.value = orders.value[0]?.idOrder ?? null
      }
      
      return orders.value
    } catch (error) {
      console.error('[orders] fetch failed', error)
      ordersError.value = 'Não foi possível carregar seus pedidos agora.'
      return []
    } finally {
      loadingOrders.value = false
    }
  }

  const preloadItems = async (orderId: number) => {
    const order = orders.value.find((o) => o.idOrder === orderId)
    if (!order?.items) return

    await Promise.all(
      order.items.map(async (item) => {
        if (productCache.value[item.productId]) return
        try {
          const product = await useBackendFetchDirect<ProductResponse>(`/products/${item.productId}`)
          productCache.value[item.productId] = product
        } catch (error) {
          console.error('[orders] produto não encontrado', item.productId, error)
        }
      }),
    )
  }

  const selectOrder = (orderId: number) => {
    highlightedOrderId.value = orderId
    preloadItems(orderId)
  }

  const updateOrderInList = (order: OrderResponse) => {
    const index = orders.value.findIndex((o) => o.idOrder === order.idOrder)
    if (index >= 0) {
      orders.value[index] = order
    } else {
      orders.value.push(order)
    }
  }

  const cancelOrder = async (orderId: number) => {
    try {
      await useBackendFetchDirect(`/orders/${orderId}/cancel`, { method: 'DELETE' })
      orders.value = orders.value.filter((o) => o.idOrder !== orderId)
      if (highlightedOrderId.value === orderId) {
        highlightedOrderId.value = orders.value[0]?.idOrder ?? null
      }
      return true
    } catch (error) {
      console.error('[orders] cancel failed', error)
      return false
    }
  }

  return {
    orders,
    loadingOrders,
    ordersError,
    highlightedOrderId,
    productCache,
    selectedOrder,
    fetchOrders,
    preloadItems,
    selectOrder,
    updateOrderInList,
    cancelOrder,
  }
}
