export interface OrderItemEntity {
  idItems: number
  orderId: number
  productId: number
  qtyItems: number
  unitAmount: number
  subtotalAmount: number
  productName?: string
}

export type OrderItemResponse = OrderItemEntity

export interface OrderItemPayload {
  productId: number
  qtyItems: number
  unitAmount: number
  subtotalAmount: number
}
