import { computed, readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { UserEntity, UserListFilters } from '~/types/users'

export const useUsersList = () => {
  const users = useState<UserEntity[]>('users:list:data', () => [])
  const loading = useState('users:list:loading', () => false)
  const error = useState<ApiError | null>('users:list:error', () => null)
  const filtersState = useState<UserListFilters>('users:list:filters', () => ({}))

  const { normalizeApiError } = useErrorHandler()

  const buildQuery = (filters?: UserListFilters) => {
    const params = new URLSearchParams()
    if (filters?.search) params.append('search', filters.search)
    if (filters?.role && filters.role !== 'all') params.append('role', filters.role)
    return params.toString() ? `?${params.toString()}` : ''
  }

  const fetchUsers = async (filters?: UserListFilters) => {
    loading.value = true
    error.value = null

    const appliedFilters = filters ?? filtersState.value
    filtersState.value = appliedFilters

    try {
      const query = buildQuery(appliedFilters)
      const data = await useBackendFetchDirect<UserEntity[]>(`/users${query}`)
      users.value = data
    } catch (err) {
      error.value = normalizeApiError(err)
    } finally {
      loading.value = false
    }
  }

  const filteredUsers = computed(() => {
    return users.value.filter((user) => {
      const { search, role } = filtersState.value
      const matchesSearch = search
        ? user.nomeUser.toLowerCase().includes(search.toLowerCase())
        : true
      const matchesRole = role && role !== 'all'
        ? user.role === role
        : true

      return matchesSearch && matchesRole
    })
  })

  const setFilters = (filters: UserListFilters) => {
    filtersState.value = filters
  }

  return {
    users: readonly(users),
    filteredUsers: readonly(filteredUsers),
    loading: readonly(loading),
    error: readonly(error),
    filters: readonly(filtersState),
    fetchUsers,
    setFilters,
  }
}
