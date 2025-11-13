import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { ProductEntity } from '~/types/products'

export const useProductDetails = () => {
  const product = useState<ProductEntity | null>('products:details:data', () => null)
  const loading = useState('products:details:loading', () => false)
  const error = useState<ApiError | null>('products:details:error', () => null)
  const { normalizeApiError } = useErrorHandler()

  const fetchProduct = async (productId: number) => {
    if (!productId) return
    loading.value = true
    error.value = null
    try {
      const data = await useBackendFetchDirect<ProductEntity>(`/products/${productId}`)
      product.value = data
    } catch (err) {
      error.value = normalizeApiError(err)
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    product.value = null
    error.value = null
  }

  return {
    product: readonly(product),
    loading: readonly(loading),
    error: readonly(error),
    fetchProduct,
    reset,
  }
}
