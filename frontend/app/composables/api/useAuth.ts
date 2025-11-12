import type { User, LoginCredentials, LoginResponse } from '~/types/auth'

export const useAuth = () => {
  const config = useRuntimeConfig()
  const router = useRouter()
  
  const token = useCookie('auth_token', { 
    maxAge: 60 * 60 * 24 * 7,
    sameSite: 'lax'
  })
  
  const userCookie = useCookie<User | null>('user_data', {
    maxAge: 60 * 60 * 24 * 7,
    sameSite: 'lax',
    encode: (value) => {
      if (!value) return ''
      try {
        return btoa(encodeURIComponent(JSON.stringify(value)))
      } catch (e) {
        console.error('Error encoding user data:', e)
        return ''
      }
    },
    decode: (value) => {
      if (!value) return null
      try {
        return JSON.parse(decodeURIComponent(atob(value)))
      } catch (e) {
        console.error('Error decoding user data:', e)
        return null
      }
    }
  })
  
  const user = useState<User | null>('user', () => userCookie.value)
  
  watch(user, (newValue) => {
    userCookie.value = newValue
  }, { deep: true })
  
  const login = async (credentials: LoginCredentials) => {
    try {
      const { data, error } = await useFetch<LoginResponse>(`${config.public.backendUrl}/auth/signin`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: credentials.email,
          password: credentials.password,
        }),
      })
      
      if (error.value) {
        // Tratamento de erros do backend
        const errorData = error.value.data as any
        const statusCode = error.value.statusCode || error.value.status || 500
        
        if (errorData?.message) {
          throw new Error(errorData.message)
        } else if (statusCode === 401) {
          throw new Error('Email ou senha incorretos')
        } else if (statusCode === 400) {
          throw new Error('Dados inválidos. Verifique os campos e tente novamente.')
        } else if (statusCode >= 500) {
          throw new Error('Erro no servidor. Tente novamente mais tarde.')
        } else {
          throw new Error('Erro ao realizar login. Tente novamente.')
        }
      }
      
      if (data.value) {
        const response = data.value
        
        // Verificar se o usuário é admin
        if (response.user.role !== 'admin') {
          throw new Error('CUSTOMER_ACCESS')
        }
        
        token.value = response.accessToken
        
        user.value = {
          usuarioKey: response.user.idUser,
          usuario: response.user.nomeUser,
          email: response.user.email || credentials.email,
          role: response.user.role,
        }
      }
      
      return data.value
    } catch (err: any) {
      // Re-throw o erro para ser tratado no componente
      throw new Error(err.message || 'Erro ao realizar login')
    }
  }
  
  const logout = async () => {
    token.value = null
    user.value = null
    userCookie.value = null
    
    await router.push('/login')
  }

  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'admin')
  
  return {
    login,
    logout,
    isAuthenticated,
    isAdmin,
    token,
    user,
  }
}
