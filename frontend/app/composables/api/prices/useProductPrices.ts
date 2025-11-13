import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { PriceEntity } from '~/types/prices'

export const useProductPrices = () => {
  const prices = useState<PriceEntity[]>('prices:list:data', () => [])
  const currentPrice = useState<PriceEntity | null>('prices:current:data', () => null)
  const loading = useState('prices:list:loading', () => false)
  const error = useState<ApiError | null>('prices:list:error', () => null)
  const { normalizeApiError } = useErrorHandler()

  const fetchPrices = async (productId: number) => {
    if (!productId) return
    loading.value = true
    error.value = null

    try {
      const [list, current] = await Promise.all([
        useBackendFetchDirect<PriceEntity[]>(`/prices/product/${productId}`),
        useBackendFetchDirect<PriceEntity | null>(`/prices/product/${productId}/current`),
      ])
      prices.value = list
      currentPrice.value = current
    } catch (err) {
      error.value = normalizeApiError(err)
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    prices.value = []
    currentPrice.value = null
    error.value = null
  }

  return {
    prices: readonly(prices),
    currentPrice: readonly(currentPrice),
    loading: readonly(loading),
    error: readonly(error),
    fetchPrices,
    reset,
  }
}
