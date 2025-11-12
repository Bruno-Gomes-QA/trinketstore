export default defineNuxtRouteMiddleware(() => {
  const token = useCookie('auth_token')
  const { user } = useAuth()

  if (!token.value || !user.value) {
    return navigateTo('/sistema/login')
  }

  // Verificar se o usuário é admin
  if (user.value.role !== 'admin') {
    return navigateTo('/sistema/login')
  }
})
