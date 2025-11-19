import { computed, readonly } from 'vue'
import type { User } from '@supabase/supabase-js'

export const useSupabaseAuth = () => {
  const nuxtApp = useNuxtApp()
  const supabaseClient = process.client ? nuxtApp.$supabase : null
  const user = useState<User | null>('supabase:user', () => null)
  const loading = useState('supabase:auth:loading', () => false)
  const error = useState<string | null>('supabase:auth:error', () => null)

  const ensureClient = () => {
    if (!process.client || !supabaseClient) {
      error.value = 'Configuração de login indisponível. Verifique as variáveis de ambiente.'
      return null
    }
    return supabaseClient
  }

  const fetchCurrentUser = async () => {
    const client = ensureClient()
    if (!client) return null

    const { data, error: fetchError } = await client.auth.getUser()
    if (fetchError) {
      error.value = fetchError.message
      console.error('[supabase] getUser error', fetchError)
      return null
    }
    user.value = data.user ?? null
    return data.user ?? null
  }

  const signInWithGoogle = async () => {
    const client = ensureClient()
    if (!client) return null
    loading.value = true
    error.value = null
    try {
      const { error: authError } = await client.auth.signInWithOAuth({
        provider: 'google',
        options: {
          redirectTo: typeof window !== 'undefined' ? `${window.location.origin}` : undefined,
          queryParams: { prompt: 'select_account' },
        },
      })
      if (authError) {
        error.value = authError.message
        console.error('[supabase] signIn error', authError)
        return null
      }
      return true
    } finally {
      loading.value = false
    }
  }

  const signOut = async () => {
    const client = ensureClient()
    if (!client) return null
    loading.value = true
    error.value = null
    try {
      const { error: signOutError } = await client.auth.signOut()
      if (signOutError) {
        error.value = signOutError.message
        console.error('[supabase] signOut error', signOutError)
        return null
      }
      user.value = null
      return true
    } finally {
      loading.value = false
    }
  }

  const isAuthenticated = computed(() => Boolean(user.value))

  return {
    user: readonly(user),
    loading: readonly(loading),
    error: readonly(error),
    isAuthenticated,
    signInWithGoogle,
    signOut,
    fetchCurrentUser,
    supabaseClient,
  }
}
