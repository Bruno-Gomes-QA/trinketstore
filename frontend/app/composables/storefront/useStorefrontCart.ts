import { computed, readonly, watch } from 'vue'
import type { StorefrontCartItem } from '~/types/storefront/cart'
import type { StorefrontProduct } from './useStorefrontCatalog'
import { useSupabaseAuth } from '../core/useSupabaseAuth'

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
  const cartOwner = useState<string>('storefront:cart:owner', () => 'guest')
  const lastHydratedOwner = useState<string | null>('storefront:cart:last-owner', () => null)

  const { user: supabaseUser } = useSupabaseAuth()
  const isClient = typeof window !== 'undefined'
  const storageKey = computed(() => `${CART_STORAGE_KEY}:${cartOwner.value}`)

  const persist = () => {
    if (!isClient || lastHydratedOwner.value !== cartOwner.value) return
    try {
      if (!items.value.length) {
        window.localStorage.removeItem(storageKey.value)
        return
      }
      window.localStorage.setItem(storageKey.value, JSON.stringify(items.value))
    } catch (error) {
      console.error('[storefront:cart] Failed to persist cart', error)
    }
  }

  const hydrateFromStorage = (ownerKey = cartOwner.value) => {
    if (!isClient) return
    try {
      const storedValue = window.localStorage.getItem(`${CART_STORAGE_KEY}:${ownerKey}`)
      if (storedValue) {
        const parsed = JSON.parse(storedValue)
        items.value = sanitizeCartItems(parsed)
      } else {
        items.value = []
      }
    } catch (error) {
      console.error('[storefront:cart] Failed to load cart', error)
    } finally {
      lastHydratedOwner.value = ownerKey
    }
  }

  const resolveOwnerKey = (ownerId?: string | number | null) => {
    if (ownerId === null || ownerId === undefined || ownerId === '') return 'guest'
    return `user:${String(ownerId)}`
  }

  const setCartOwner = (ownerId?: string | number | null) => {
    const normalized = resolveOwnerKey(ownerId)
    if (cartOwner.value === normalized && lastHydratedOwner.value === normalized) {
      return normalized
    }

    // Move itens do dono anterior para o novo dono se o novo estiver vazio
    const previousOwner = cartOwner.value
    const previousItems = [...items.value]

    cartOwner.value = normalized
    hydrateFromStorage(normalized)

    if (previousItems.length && items.value.length === 0) {
      items.value = previousItems
      persist()
      // opcional: limpar carrinho do dono anterior para evitar duplicar
      if (isClient) {
        window.localStorage.removeItem(`${CART_STORAGE_KEY}:${previousOwner}`)
      }
    }

    return normalized
  }

  if (isClient) {
    hydrateFromStorage(cartOwner.value)
  }

  watch(cartOwner, (ownerKey) => {
    if (lastHydratedOwner.value !== ownerKey) {
      hydrateFromStorage(ownerKey)
    }
  })

  watch(
    items,
    () => {
      persist()
    },
    { deep: true },
  )

  watch(
    () => supabaseUser.value?.id,
    (id) => {
      const key = id ? `auth:${id}` : null
      setCartOwner(key)
    },
    { immediate: true },
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
    persist()
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
    setCartOwner,
  }
}
