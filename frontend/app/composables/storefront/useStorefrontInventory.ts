import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { InventoryResponse } from '~/types/inventory'
import { useBackendFetchDirect } from '~/composables/core/useBackendFetch'
import { useErrorHandler } from '~/composables/helpers/useErrorHandler'

const INVENTORY_CACHE_TTL = 1000 * 60 // 1 minuto

export interface StorefrontInventorySnapshot {
  productId: number
  qtyOnHand: number
  updatedAt: number
}

export const useStorefrontInventory = () => {
  const inventoryMap = useState<Record<number, StorefrontInventorySnapshot>>(
    'storefront:inventory:map',
    () => ({}),
  )
  const loadingMap = useState<Record<number, boolean>>(
    'storefront:inventory:loading',
    () => ({}),
  )
  const lastError = useState<ApiError | null>('storefront:inventory:error', () => null)
  const { normalizeApiError } = useErrorHandler()

  const updateLoadingState = (productId: number, isLoading: boolean) => {
    loadingMap.value = {
      ...loadingMap.value,
      [productId]: isLoading,
    }
  }

  const fetchInventoryForProduct = async (
    productId: number,
    options: { force?: boolean } = {},
  ) => {
    const now = Date.now()
    const cached = inventoryMap.value[productId]
    if (!options.force && cached && now - cached.updatedAt < INVENTORY_CACHE_TTL) {
      return cached
    }

    updateLoadingState(productId, true)
    lastError.value = null

    try {
      const inventory = await useBackendFetchDirect<InventoryResponse>(`/inventory/product/${productId}`)
      const snapshot: StorefrontInventorySnapshot = {
        productId: inventory.productId,
        qtyOnHand: Math.max(inventory.qtyOnHand ?? 0, 0),
        updatedAt: now,
      }

      inventoryMap.value = {
        ...inventoryMap.value,
        [productId]: snapshot,
      }

      return snapshot
    } catch (error) {
      lastError.value = normalizeApiError(error)
      throw error
    } finally {
      updateLoadingState(productId, false)
    }
  }

  const setInventorySnapshot = (productId: number, qtyOnHand: number) => {
    inventoryMap.value = {
      ...inventoryMap.value,
      [productId]: {
        productId,
        qtyOnHand: Math.max(qtyOnHand, 0),
        updatedAt: Date.now(),
      },
    }
  }

  return {
    inventoryMap: readonly(inventoryMap),
    loadingMap: readonly(loadingMap),
    error: readonly(lastError),
    fetchInventoryForProduct,
    setInventorySnapshot,
  }
}
