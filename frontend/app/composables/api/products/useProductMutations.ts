import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { ProductEntity, ProductPayload } from '~/types/products'

export const useProductMutations = () => {
  const loading = useState('products:mutations:loading', () => false)
  const error = useState<ApiError | null>('products:mutations:error', () => null)
  const { normalizeApiError } = useErrorHandler()

  const handleRequest = async <T>(promise: Promise<T>): Promise<T | null> => {
    loading.value = true
    error.value = null
    try {
      return await promise
    } catch (err) {
      error.value = normalizeApiError(err)
      return null
    } finally {
      loading.value = false
    }
  }

  const createProduct = async (payload: ProductPayload) => {
    return await handleRequest(
      useBackendFetchDirect<ProductEntity>('/products', {
        method: 'POST',
        body: payload,
      }),
    )
  }

  const updateProduct = async (productId: number, payload: ProductPayload) => {
    return await handleRequest(
      useBackendFetchDirect<ProductEntity>(`/products/${productId}`, {
        method: 'PUT',
        body: payload,
      }),
    )
  }

  const deleteProduct = async (productId: number) => {
    return await handleRequest(
      useBackendFetchDirect<void>(`/products/${productId}`, {
        method: 'DELETE',
      }),
    )
  }

  const activateProduct = async (productId: number) => {
    return await handleRequest(
      useBackendFetchDirect<ProductEntity>(`/products/${productId}/activate`, {
        method: 'PATCH',
      }),
    )
  }

  const deactivateProduct = async (productId: number) => {
    return await handleRequest(
      useBackendFetchDirect<ProductEntity>(`/products/${productId}/deactivate`, {
        method: 'PATCH',
      }),
    )
  }

  return {
    loading: readonly(loading),
    error: readonly(error),
    createProduct,
    updateProduct,
    deleteProduct,
    activateProduct,
    deactivateProduct,
  }
}
