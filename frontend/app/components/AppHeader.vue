<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Bell, ClipboardList, Plus } from 'lucide-vue-next'
import { Button } from '~/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '~/components/ui/dropdown-menu'
import { Separator } from '~/components/ui/separator'
import { SidebarTrigger } from '~/components/ui/sidebar'
import {
  Avatar,
  AvatarFallback,
} from '~/components/ui/avatar'

const route = useRoute()
const { user, logout } = useAuth()

const pageTitle = computed(() => {
  const metaTitle = route.meta?.pageTitle as string | undefined
  if (metaTitle) return metaTitle
  if (route.path === '/dashboard') {
    return 'Dashboard'
  }
  return 'Aplicação'
})

const userInitials = computed(() => {
  if (!user.value?.usuario) return 'T'
  return user.value.usuario
    .split(' ')
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('')
})
</script>

<template>
  <header class="flex h-16 shrink-0 items-center justify-between gap-8 mb-5 mt-2 px-4 bg-white relative transition-[width,height] ease-linear group-has-[[data-collapsible=icon]]/sidebar-wrapper:h-12">
    <div class="flex items-center gap-3">
      <SidebarTrigger class="-ml-1" />
      <Separator orientation="vertical" class="mr-2 h-4" />
    </div>

    <div class="flex items-center gap-2">
      <Button
        as-child
        size="sm"
        variant="outline"
        class="hidden md:inline-flex"
      >
        <NuxtLink to="/sistema/produtos">
          <Plus class="h-4 w-4" />
          Criar produto
        </NuxtLink>
      </Button>
      <Button
        as-child
        size="sm"
        variant="secondary"
        class="hidden md:inline-flex"
      >
        <NuxtLink to="/sistema/pedidos/criar">
          <ClipboardList class="h-4 w-4" />
          Novo pedido
        </NuxtLink>
      </Button>
      <Button
        variant="ghost"
        size="icon-sm"
        class="text-muted-foreground"
        aria-label="Notificações"
      >
        <Bell class="h-4 w-4" />
      </Button>

      <DropdownMenu>
        <DropdownMenuTrigger as-child>
          <Button variant="ghost" class="gap-3 px-2">
            <Avatar class="h-8 w-8">
              <AvatarFallback>{{ userInitials }}</AvatarFallback>
            </Avatar>
            <div class="hidden text-left text-sm leading-tight md:flex flex-col">
              <span class="font-semibold text-foreground truncate max-w-[140px]">
                {{ user?.usuario || 'Administrador' }}
              </span>
              <span class="text-xs text-muted-foreground truncate max-w-[140px]">
                {{ user?.email || 'admin@trinketstore.com' }}
              </span>
            </div>
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" class="w-56">
          <DropdownMenuLabel>
            <p class="text-sm font-semibold">{{ user?.usuario || 'Administrador' }}</p>
            <p class="text-xs text-muted-foreground">{{ user?.email || 'admin@trinketstore.com' }}</p>
          </DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuItem as-child>
            <NuxtLink to="/sistema/perfil">
              Meu perfil
            </NuxtLink>
          </DropdownMenuItem>
          <DropdownMenuItem @click="logout()">
            Sair
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>

    <!-- Efeito de glow na borda inferior do header -->
    <div class="absolute bottom-0 left-0 right-0 h-[3px]">
      <!-- Camada de glow externa (mais forte) -->
      <div class="absolute inset-0 h-[3px] bg-gradient-to-r from-[#f6d95c] via-[#a4f380] to-[#80d3e4] opacity-60 blur-[8px] animate-pulse" />
      <!-- Camada de glow média -->
      <div class="absolute inset-0 h-[2px] bg-gradient-to-r from-[#f6d95c] via-[#a4f380] to-[#80d3e4] opacity-80 blur-[4px]" />
      <!-- Linha central sólida com gradiente -->
      <div class="absolute inset-0 h-[1px] bg-gradient-to-r from-[#f6d95c] via-[#a4f380] to-[#80d3e4]" />
    </div>
  </header>
</template>
