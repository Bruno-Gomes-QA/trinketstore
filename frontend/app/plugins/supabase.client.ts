import { createClient } from '@supabase/supabase-js'
import type { Session, SupabaseClient, User } from '@supabase/supabase-js'

export default defineNuxtPlugin(() => {
  const runtimeConfig = useRuntimeConfig()
  const supabaseUrl = runtimeConfig.public.supabaseUrl
  const supabaseAnonKey = runtimeConfig.public.supabaseAnonKey

  if (!supabaseUrl || !supabaseAnonKey) {
    if (process.dev) {
      console.warn('[supabase] Missing env vars. Add NUXT_PUBLIC_SUPABASE_URL and NUXT_PUBLIC_SUPABASE_ANON_KEY.')
    }

    return {
      provide: {
        supabase: null as SupabaseClient | null,
      },
    }
  }

  const supabase = createClient(supabaseUrl, supabaseAnonKey, {
    auth: {
      detectSessionInUrl: true,
      persistSession: true,
      autoRefreshToken: true,
    },
  })

  if (process.client) {
    const userState = useState<User | null>('supabase:user', () => null)
    const authCookie = useCookie<string | null>('auth_token', {
      sameSite: 'lax',
      maxAge: 60 * 60 * 4,
    })

    const ensureUserRecord = async (currentUser: User | null) => {
      if (!currentUser) return
      const displayName = (currentUser.user_metadata?.full_name
        || currentUser.user_metadata?.name
        || currentUser.email?.split('@')[0]
        || 'Cliente Trinket') as string

      try {
        const { data, error } = await supabase
          .from('users')
          .select('id_user')
          .eq('auth_id', currentUser.id)
          .maybeSingle()

        if (error && error.code !== 'PGRST116') {
          console.error('[supabase] Failed to check user record', error)
          return
        }

        if (!data) {
          const insertPayload = {
            auth_id: currentUser.id,
            nome_user: displayName,
            role: 'customer',
            email: currentUser.email,
            updated_at: new Date().toISOString(),
          }

          const { error: insertError } = await supabase
            .from('users')
            .insert(insertPayload)

          if (insertError) {
            console.error('[supabase] Failed to create user record', insertError)
          }
        }
      } catch (err) {
        console.error('[supabase] ensureUserRecord unexpected error', err)
      }
    }

    const syncAuthCookie = (session: Session | null) => {
      authCookie.value = session?.access_token ?? null
    }

    const handleUserChange = async (nextUser: User | null) => {
      userState.value = nextUser
      await ensureUserRecord(nextUser)
    }

    supabase.auth.getSession().then(async ({ data, error }) => {
      if (error) {
        console.error('[supabase] Failed to fetch user', error)
        return
      }
      syncAuthCookie(data.session ?? null)
      await handleUserChange(data.session?.user ?? null)
    })

    supabase.auth.onAuthStateChange(async (_event, session) => {
      syncAuthCookie(session ?? null)
      await handleUserChange(session?.user ?? null)
    })
  }

  return {
    provide: {
      supabase,
    },
  }
})
