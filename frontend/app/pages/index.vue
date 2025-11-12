<script setup lang="ts">
import { useAuth } from '~/composables/api/useAuth'

const { isAuthenticated } = useAuth()

onMounted(() => {
  if (isAuthenticated.value) {
    navigateTo('/dashboard')
  } else {
    navigateTo('/login')
  }
})
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-background text-muted-foreground">
    Redirecionando...
  </div>
</template>
