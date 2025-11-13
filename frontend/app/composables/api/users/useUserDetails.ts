import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { UserEntity } from '~/types/users'

export const useUserDetails = () => {
  const user = useState<UserEntity | null>('users:details:data', () => null)
  const loading = useState('users:details:loading', () => false)
  const error = useState<ApiError | null>('users:details:error', () => null)
  const { normalizeApiError } = useErrorHandler()

  const fetchUser = async (userId: number) => {
    if (!userId) return
    loading.value = true
    error.value = null
    try {
      const data = await useBackendFetchDirect<UserEntity>(`/users/${userId}`)
      user.value = data
    } catch (err) {
      error.value = normalizeApiError(err)
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    user.value = null
    error.value = null
  }

  return {
    user: readonly(user),
    loading: readonly(loading),
    error: readonly(error),
    fetchUser,
    reset,
  }
}
