import { readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { CreateUserPayload, UpdateUserPayload, UserEntity } from '~/types/users'

export const useUserMutations = () => {
  const loading = useState('users:mutations:loading', () => false)
  const error = useState<ApiError | null>('users:mutations:error', () => null)
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

  const createUser = async (payload: CreateUserPayload) => {
    const response = await handleRequest(
      useBackendFetchDirect<{
        accessToken: string
        refreshToken: string
        expiresIn: number
        user: UserEntity
      }>('/auth/signup', {
        method: 'POST',
        body: {
          email: payload.email,
          password: payload.password,
          name: payload.name,
          role: payload.role,
        },
      }),
    )

    return response?.user ?? null
  }

  const updateUser = async (userId: number, payload: UpdateUserPayload) => {
    return await handleRequest(
      useBackendFetchDirect<UserEntity>(`/users/${userId}`, {
        method: 'PUT',
        body: payload,
      }),
    )
  }

  const deleteUser = async (userId: number) => {
    return await handleRequest(
      useBackendFetchDirect<void>(`/users/${userId}`, {
        method: 'DELETE',
      }),
    )
  }

  return {
    loading: readonly(loading),
    error: readonly(error),
    createUser,
    updateUser,
    deleteUser,
  }
}
