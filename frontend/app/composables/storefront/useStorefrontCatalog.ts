import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { ProductEntity } from '~/types/products'
import type { PriceEntity } from '~/types/prices'
import { useBackendFetchDirect } from '~/composables/core/useBackendFetch'
import { useErrorHandler } from '~/composables/helpers/useErrorHandler'

const CACHE_TTL_MS = 1000 * 60 * 5 // 5 minutos

export interface StorefrontProduct {
  id: number
  slug: string
  name: string
  description: string
  image: string
  category: string
  priceInCents: number | null
}

export const useStorefrontCatalog = () => {
  const catalog = useState<StorefrontProduct[]>('storefront:catalog:data', () => [])
  const lastFetchedAt = useState<number | null>('storefront:catalog:lastFetched', () => null)
  const loading = useState('storefront:catalog:loading', () => false)
  const error = useState<ApiError | null>('storefront:catalog:error', () => null)
  const { normalizeApiError } = useErrorHandler()

  const fetchCatalog = async (force = false) => {
    const now = Date.now()
    if (!force && catalog.value.length && lastFetchedAt.value && now - lastFetchedAt.value < CACHE_TTL_MS) {
      return catalog.value
    }

    loading.value = true
    error.value = null

    try {
      const [products, prices] = await Promise.all([
        useBackendFetchDirect<ProductEntity[]>('/products/active'),
        useBackendFetchDirect<PriceEntity[]>('/prices/active'),
      ])

      const priceMap = prices.reduce<Record<number, number>>((acc, price) => {
        if (price.vigentePrice) {
          acc[price.productId] = price.amountPrice
        }
        return acc
      }, {})

      catalog.value = products.map((product) => ({
        id: product.idProduct,
        slug: product.slugProduct,
        name: product.nomeProduct,
        description: product.descricaoProduct,
        image: product.imagemurlProduct,
        category: product.categoriaProduct,
        priceInCents: priceMap[product.idProduct] ?? null,
      }))
      lastFetchedAt.value = now
    } catch (err) {
      error.value = normalizeApiError(err)
    } finally {
      loading.value = false
    }

    return catalog.value
  }

  const invalidateCatalog = () => {
    lastFetchedAt.value = null
  }

  return {
    catalog: readonly(catalog),
    loading: readonly(loading),
    error: readonly(error),
    fetchCatalog,
    invalidateCatalog,
  }
}
