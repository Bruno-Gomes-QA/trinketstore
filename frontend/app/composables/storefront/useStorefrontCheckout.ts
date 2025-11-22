import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { PixCheckoutResponse, OrderResponse } from '~/types/orders'
import type { StorefrontCartItem } from '~/types/storefront/cart'
import { useBackendFetchDirect } from '~/composables/core/useBackendFetch'
import { useErrorHandler } from '~/composables/helpers/useErrorHandler'

const buildReference = (prefix: string) => {
  const random = typeof crypto !== 'undefined' && crypto.randomUUID
    ? crypto.randomUUID().replace(/-/g, '').slice(0, 12)
    : Math.random().toString(36).slice(2, 14)
  return `${prefix}-${random}`.toUpperCase()
}

export const useStorefrontCheckout = () => {
  const lastOrder = useState<OrderResponse | null>('storefront:checkout:last', () => null)
  const creating = useState('storefront:checkout:creating', () => false)
  const error = useState<ApiError | null>('storefront:checkout:error', () => null)
  const { normalizeApiError } = useErrorHandler()

  const createOrderFromCart = async (input: {
    userId: number
    items: readonly StorefrontCartItem[]
    totalAmountInCents: number
    description?: string
  }) => {
    if (!input.items.length) {
      throw new Error('Nenhum item no carrinho para criar o pedido.')
    }

    creating.value = true
    error.value = null

    const payload = {
      userId: input.userId,
      totalAmountInCents: Math.max(input.totalAmountInCents, 0),
      currency: 'BRL',
      description: input.description,
      items: input.items.map((item) => {
        const unitAmount = item.priceInCents ?? 0
        const safeQuantity = Math.min(
          Math.max(Math.floor(item.quantity), 1),
          Math.max(item.availableStock, 1),
        )
        return {
          productId: item.productId,
          qtyItems: safeQuantity,
          unitAmount,
          subtotalAmount: unitAmount * safeQuantity,
        }
      }),
    }

    try {
      const response = await useBackendFetchDirect<PixCheckoutResponse>('/checkout/pix', {
        method: 'POST',
        body: payload,
        headers: {
          'Content-Type': 'application/json',
        },
      })
      lastOrder.value = response.order
      return response
    } catch (err) {
      error.value = normalizeApiError(err)
      throw err
    } finally {
      creating.value = false
    }
  }

  return {
    lastOrder: readonly(lastOrder),
    creating: readonly(creating),
    error: readonly(error),
    createOrderFromCart,
  }
}
