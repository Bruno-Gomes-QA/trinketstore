import { ref, onBeforeUnmount } from 'vue'
import type { OrderResponse } from '~/types/orders'

export const useOrdersPolling = () => {
  const orderPollInterval = ref<ReturnType<typeof setInterval> | null>(null)
  const lastStatusUpdate = ref<string>('–')

  const pollOrderStatus = async (orderId: number, onUpdate: (order: OrderResponse) => void) => {
    try {
      const order = await useBackendFetchDirect<OrderResponse>(`/orders/${orderId}`)
      onUpdate(order)
      lastStatusUpdate.value = new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
      
      if (order.statusOrder && order.statusOrder !== 'pending') {
        stopOrderPolling()
      }
    } catch (error) {
      console.error('[orders] polling failed', error)
    }
  }

  const stopOrderPolling = () => {
    if (orderPollInterval.value) {
      clearInterval(orderPollInterval.value)
      orderPollInterval.value = null
    }
  }

  const startOrderPolling = (orderId: number, statusOrder: string, onUpdate: (order: OrderResponse) => void) => {
    stopOrderPolling()
    if (!orderId) return
    
    const shouldPoll = statusOrder === 'pending'
    if (!shouldPoll) return
    
    pollOrderStatus(orderId, onUpdate)
    orderPollInterval.value = setInterval(() => pollOrderStatus(orderId, onUpdate), 10000)
  }

  onBeforeUnmount(() => {
    stopOrderPolling()
  })

  return {
    lastStatusUpdate,
    startOrderPolling,
    stopOrderPolling,
    pollOrderStatus,
  }
}
