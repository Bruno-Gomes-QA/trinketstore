export const useBackendFetch = <T = any>(
  endpoint: string,
  options?: any
) => {
  const config = useRuntimeConfig()
  const token = useCookie('auth_token')
  
  const url = `${config.public.backendUrl}${endpoint}`
  
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...options?.headers,
  }
  
  if (token.value) {
    headers['Authorization'] = `Bearer ${token.value}`
  }
  
  return useFetch<T>(url, {
    ...options,
    headers,
    watch: false,
  })
}

export const useBackendFetchDirect = async <T = any>(
  endpoint: string,
  options?: any
): Promise<T> => {
  const config = useRuntimeConfig()
  const token = useCookie('auth_token')
  
  const url = `${config.public.backendUrl}${endpoint}`
  
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...options?.headers,
  }
  
  if (token.value) {
    headers['Authorization'] = `Bearer ${token.value}`
    console.log('🔑 Token present, length:', token.value.length)
  } else {
    console.warn('⚠️ No token available for request to:', endpoint)
  }
  
  console.log('📡 Fetching:', endpoint, 'with headers:', Object.keys(headers))
  
  return await $fetch<T>(url, {
    ...options,
    headers,
  })
}
