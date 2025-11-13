import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { UpdateUserPayload, UserEntity } from '~/types/users'

export const useCurrentUserProfile = () => {
  const profile = useState<UserEntity | null>('users:profile:data', () => null)
  const loading = useState('users:profile:loading', () => false)
  const mutationLoading = useState('users:profile:mutation', () => false)
  const error = useState<ApiError | null>('users:profile:error', () => null)
  const { normalizeApiError } = useErrorHandler()

  const fetchProfile = async () => {
    loading.value = true
    error.value = null
    try {
      const data = await useBackendFetchDirect<UserEntity>('/users/me')
      profile.value = data
    } catch (err) {
      error.value = normalizeApiError(err)
    } finally {
      loading.value = false
    }
  }

  const updateProfile = async (payload: UpdateUserPayload) => {
    mutationLoading.value = true
    error.value = null
    try {
      const data = await useBackendFetchDirect<UserEntity>('/users/me', {
        method: 'PUT',
        body: payload,
      })
      profile.value = data
      return data
    } catch (err) {
      error.value = normalizeApiError(err)
      return null
    } finally {
      mutationLoading.value = false
    }
  }

  const deleteProfile = async () => {
    mutationLoading.value = true
    error.value = null
    try {
      await useBackendFetchDirect<void>('/users/me', { method: 'DELETE' })
      profile.value = null
      return true
    } catch (err) {
      error.value = normalizeApiError(err)
      return false
    } finally {
      mutationLoading.value = false
    }
  }

  return {
    profile: readonly(profile),
    loading: readonly(loading),
    mutationLoading: readonly(mutationLoading),
    error: readonly(error),
    fetchProfile,
    updateProfile,
    deleteProfile,
  }
}
