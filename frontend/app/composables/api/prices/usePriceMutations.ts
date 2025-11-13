import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { PriceEntity, PricePayload } from '~/types/prices'

export const usePriceMutations = () => {
  const loading = useState('prices:mutations:loading', () => false)
  const error = useState<ApiError | null>('prices:mutations:error', () => null)
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

  const createPrice = async (payload: PricePayload) => {
    return await handleRequest(
      useBackendFetchDirect<PriceEntity>('/prices', {
        method: 'POST',
        body: payload,
      }),
    )
  }

  const updatePrice = async (priceId: number, payload: Partial<PricePayload>) => {
    return await handleRequest(
      useBackendFetchDirect<PriceEntity>(`/prices/${priceId}`, {
        method: 'PUT',
        body: payload,
      }),
    )
  }

  const activatePrice = async (priceId: number) => {
    return await handleRequest(
      useBackendFetchDirect<PriceEntity>(`/prices/${priceId}/activate`, {
        method: 'PATCH',
      }),
    )
  }

  const deactivatePrice = async (priceId: number) => {
    return await handleRequest(
      useBackendFetchDirect<PriceEntity>(`/prices/${priceId}/deactivate`, {
        method: 'PATCH',
      }),
    )
  }

  return {
    loading: readonly(loading),
    error: readonly(error),
    createPrice,
    updatePrice,
    activatePrice,
    deactivatePrice,
  }
}
