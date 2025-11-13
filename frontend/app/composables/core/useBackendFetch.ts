import type { UseFetchOptions } from '#app'
import type { FetchOptions } from 'ofetch'
import { readonly, ref, toValue, watch } from 'vue'
import type { ApiError } from '~/types/core/api'
import { useErrorHandler } from '~/composables/helpers/useErrorHandler'

type BackendFetchOptions<T> = UseFetchOptions<T> & {
  immediate?: boolean
}

const normalizeHeaders = (input?: HeadersInit): Record<string, string> => {
  if (!input) return {}

  if (Array.isArray(input)) {
    return input.reduce<Record<string, string>>((acc, [key, value]) => {
      acc[key] = value
      return acc
    }, {})
  }

  const isHeadersInstance =
    typeof Headers !== 'undefined' &&
    input instanceof Headers

  if (isHeadersInstance) {
    const acc: Record<string, string> = {}
    ;(input as Headers).forEach((value, key) => {
      acc[key] = value
    })
    return acc
  }

  return { ...(input as Record<string, string>) }
}

const buildHeaders = (token?: string | null, extra?: HeadersInit) => {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...normalizeHeaders(extra),
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  return headers
}

export const useBackendFetch = <T = unknown>(
  endpoint: string,
  options: BackendFetchOptions<T> = {},
) => {
  const config = useRuntimeConfig()
  const token = useCookie('auth_token')
  const { normalizeApiError, showApiErrorToast } = useErrorHandler()
  const errorState = ref<ApiError | null>(null)

  const url = `${config.public.backendUrl}${endpoint}`
  const headers = buildHeaders(token.value, toValue(options.headers) as HeadersInit | undefined)

  const fetchOptions = {
    ...(options as UseFetchOptions<any>),
    headers,
    immediate: options.immediate ?? true,
    watch: options.watch ?? false,
  } as UseFetchOptions<any>

  const fetchResult = useFetch<T>(url, fetchOptions as any)

  watch(
    fetchResult.error,
    (fetchError) => {
      if (fetchError) {
        console.error('[useBackendFetch] Request failed', endpoint, fetchError)
        const normalized = normalizeApiError(fetchError)
        errorState.value = normalized
        showApiErrorToast(normalized)
      } else {
        errorState.value = null
      }
    },
    { immediate: true },
  )

  const refresh = async () => {
    errorState.value = null
    return await fetchResult.refresh()
  }

  return {
    ...fetchResult,
    error: readonly(errorState),
    refresh,
  }
}

export const useBackendFetchDirect = async <T = unknown>(
  endpoint: string,
  options?: FetchOptions<'json'>,
): Promise<T> => {
  const config = useRuntimeConfig()
  const token = useCookie('auth_token')
  const { normalizeApiError, showApiErrorToast } = useErrorHandler()

  const url = `${config.public.backendUrl}${endpoint}`
  const extraHeaders = options?.headers
    ? (toValue(options.headers as HeadersInit | undefined) as HeadersInit)
    : undefined
  const headers = buildHeaders(token.value, extraHeaders)

  try {
    const requestOptions = {
      ...(options as FetchOptions<'json'>),
      headers,
    }
    return await $fetch<T>(url, requestOptions as any)
  } catch (error) {
    console.error('[useBackendFetchDirect] Request failed', endpoint, error)
    const normalized = normalizeApiError(error)
    showApiErrorToast(normalized)
    throw normalized
  }
}
