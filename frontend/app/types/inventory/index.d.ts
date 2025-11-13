export interface InventoryEntity {
  idInventory: number
  productId: number
  qtyOnHand: number
  productName?: string
  categoriaProduct?: string
}

export type InventoryResponse = InventoryEntity

export interface InventoryFilters {
  withStockOnly?: boolean
  lowStockThreshold?: number
}

export interface InventoryAdjustmentPayload {
  qtyOnHand?: number
  addQuantity?: number
  removeQuantity?: number
}

export interface CreateInventoryPayload {
  productId: number
  qtyOnHand: number
}

export interface UpdateInventoryPayload {
  qtyOnHand: number
}
