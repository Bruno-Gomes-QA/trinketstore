import type { OrderItemEntity, OrderItemPayload } from '~/types/order-items'

export type OrderStatus = 'pending' | 'paid' | 'canceled' | 'fulfilled' | 'picked_up'

export interface OrderEntity {
  idOrder: number
  userId: number
  statusOrder: OrderStatus
  totalOrders: number
  currencyOrder: string
  checkoutId: string
  paymentIntent: string
  pickupQrToken?: string
  createdAt: string
  items?: OrderItemEntity[]
  userName?: string
}

export type OrderResponse = OrderEntity

export interface OrderFilters {
  status?: OrderStatus | 'all'
  userId?: number
  paymentIntent?: string
  checkoutId?: string
  search?: string
}

export interface CreateOrderPayload {
  userId: number
  statusOrder?: OrderStatus
  totalOrders: number
  currencyOrder: string
  checkoutId: string
  paymentIntent: string
  pickupQrToken?: string
  items: OrderItemPayload[]
}

export interface UpdateOrderStatusPayload {
  statusOrder: OrderStatus
}
