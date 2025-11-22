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
  pixQrCodeBase64?: string | null
  pixExpiresAt?: string | null
  createdAt: string
  items?: OrderItemEntity[]
  userName?: string
}

export type OrderResponse = OrderEntity

export interface OrderFilters {
  status?: OrderStatus | 'all'
  userId?: number
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

export interface PixPaymentDetails {
  paymentId: string
  status: string
  qrCode: string | null
  qrCodeBase64: string | null
  expiresAt: string | null
  checkoutId: string
}

export interface PixCheckoutResponse {
  order: OrderResponse
  pix: PixPaymentDetails
}
