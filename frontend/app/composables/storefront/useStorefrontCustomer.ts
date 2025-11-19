import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { UserResponse } from '~/types/users'
import { useBackendFetchDirect } from '~/composables/core/useBackendFetch'
import { useErrorHandler } from '~/composables/helpers/useErrorHandler'

export const useStorefrontCustomer = () => {
  const profile = useState<UserResponse | null>('storefront:customer:profile', () => null)
  const loading = useState('storefront:customer:loading', () => false)
  const error = useState<ApiError | null>('storefront:customer:error', () => null)
  const { normalizeApiError } = useErrorHandler()

  const fetchProfile = async () => {
    loading.value = true
    error.value = null

    try {
      const response = await useBackendFetchDirect<UserResponse>('/users/me')
      profile.value = response
      return response
    } catch (err) {
      error.value = normalizeApiError(err)
      profile.value = null
      throw err
    } finally {
      loading.value = false
    }
  }

  const clearProfile = () => {
    profile.value = null
    error.value = null
  }

  return {
    profile: readonly(profile),
    loading: readonly(loading),
    error: readonly(error),
    fetchProfile,
    clearProfile,
  }
}
