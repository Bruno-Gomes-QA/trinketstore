import { computed, readonly, watch } from 'vue'
import type { StorefrontCartItem } from '~/types/storefront/cart'
import type { StorefrontProduct } from './useStorefrontCatalog'

const CART_STORAGE_KEY = 'trinket-store:cart:v1'

const clampQuantity = (value: number, max: number) => {
  if (max <= 0) return 0
  const parsed = Number.isFinite(value) ? Math.floor(value) : 1
  return Math.min(Math.max(parsed, 1), max)
}

const sanitizeCartItems = (payload: unknown): StorefrontCartItem[] => {
  if (!Array.isArray(payload)) {
    return []
  }

  return payload
    .map((item) => {
      if (!item || typeof item !== 'object') return null
      const raw = item as Partial<StorefrontCartItem>
      const productId = Number(raw.productId)
      if (!Number.isFinite(productId) || productId <= 0) return null

      const baseStock = raw.availableStock == null ? raw.quantity ?? 0 : raw.availableStock
      const availableStock = Math.max(Math.floor(baseStock ?? 0), 0)
      const desiredQuantity = Number.isFinite(raw.quantity) ? Number(raw.quantity) : Number(raw.quantity ?? 1)
      const stockLimit = availableStock > 0 ? availableStock : 0
      const sanitized: StorefrontCartItem = {
        productId,
        slug: String(raw.slug ?? productId),
        name: String(raw.name ?? 'Produto'),
        image: raw.image ?? null,
        priceInCents: typeof raw.priceInCents === 'number' ? raw.priceInCents : null,
        quantity: clampQuantity(desiredQuantity || 1, stockLimit > 0 ? stockLimit : 0),
        availableStock,
      }

      return sanitized
    })
    .filter((item): item is StorefrontCartItem => Boolean(item))
}

export const useStorefrontCart = () => {
  const items = useState<StorefrontCartItem[]>('storefront:cart:items', () => [])
  const hydrated = useState('storefront:cart:hydrated', () => false)

  const isClient = typeof window !== 'undefined'

  const persist = () => {
    if (!isClient || !hydrated.value) return
    try {
      window.localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(items.value))
    } catch (error) {
      console.error('[storefront:cart] Failed to persist cart', error)
    }
  }

  const hydrateFromStorage = () => {
    if (!isClient || hydrated.value) return
    try {
      const storedValue = window.localStorage.getItem(CART_STORAGE_KEY)
      if (storedValue) {
        const parsed = JSON.parse(storedValue)
        items.value = sanitizeCartItems(parsed)
      }
    } catch (error) {
      console.error('[storefront:cart] Failed to load cart', error)
    } finally {
      hydrated.value = true
    }
  }

  if (isClient && !hydrated.value) {
    hydrateFromStorage()
  }

  watch(
    items,
    () => {
      persist()
    },
    { deep: true },
  )

  const addItem = (product: StorefrontProduct, quantity: number, availableStock: number) => {
    const stockLimit = Math.max(Math.floor(availableStock), 0)
    if (stockLimit <= 0) {
      return null
    }

    const sanitizedQuantity = clampQuantity(quantity, stockLimit)
    const existingItem = items.value.find((cartItem) => cartItem.productId === product.id)

    if (existingItem) {
      existingItem.availableStock = stockLimit
      existingItem.quantity = clampQuantity(existingItem.quantity + sanitizedQuantity, stockLimit)
      return existingItem
    }

    const newItem: StorefrontCartItem = {
      productId: product.id,
      slug: product.slug,
      name: product.name,
      image: product.image,
      priceInCents: product.priceInCents,
      quantity: sanitizedQuantity,
      availableStock: stockLimit,
    }

    items.value = [...items.value, newItem]
    return newItem
  }

  const updateItemQuantity = (productId: number, quantity: number, stockLimit?: number) => {
    const item = items.value.find((cartItem) => cartItem.productId === productId)
    if (!item) return null

    const availableStock = Math.max(Math.floor(stockLimit ?? item.availableStock), 0)
    item.availableStock = availableStock
    item.quantity = clampQuantity(quantity, availableStock)

    return item
  }

  const removeItem = (productId: number) => {
    items.value = items.value.filter((cartItem) => cartItem.productId !== productId)
  }

  const clearCart = () => {
    items.value = []
  }

  const setItemStock = (productId: number, qtyOnHand: number) => {
    const item = items.value.find((cartItem) => cartItem.productId === productId)
    if (!item) return

    const stockLimit = Math.max(Math.floor(qtyOnHand), 0)
    item.availableStock = stockLimit
    item.quantity = clampQuantity(item.quantity, stockLimit)
  }

  const hasItems = computed(() => items.value.length > 0)
  const totalItems = computed(() => items.value.reduce((acc, item) => acc + Math.max(item.quantity, 0), 0))
  const totalAmountInCents = computed(() =>
    items.value.reduce((acc, item) => acc + (item.priceInCents ?? 0) * Math.max(item.quantity, 0), 0),
  )

  return {
    items: readonly(items),
    hasItems: readonly(hasItems),
    totalItems: readonly(totalItems),
    totalAmountInCents: readonly(totalAmountInCents),
    addItem,
    updateItemQuantity,
    removeItem,
    clearCart,
    setItemStock,
    hydrateFromStorage,
  }
}
