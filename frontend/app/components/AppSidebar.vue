<script setup lang="ts">
import { LayoutDashboard, Package, ShoppingCart, Users, Boxes } from "lucide-vue-next"
import type { SidebarProps } from '~/components/ui/sidebar'
import { useAuth } from '~/composables/api/useAuth'
import NavMain from '~/components/NavMain.vue'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarRail,
} from '~/components/ui/sidebar'

const props = withDefaults(defineProps<SidebarProps>(), {
  collapsible: "icon",
})

const { user } = useAuth()

const showLogoFallback = ref(false)
const showIconFallback = ref(false)

const navItems = [
  { title: 'Dashboard', url: '/sistema/dashboard', icon: LayoutDashboard },
  { title: 'Usuários', url: '/sistema/usuarios', icon: Users },
  { title: 'Produtos', url: '/sistema/produtos', icon: Package },
  { title: 'Inventário', url: '/sistema/inventario', icon: Boxes },
  { title: 'Pedidos', url: '/sistema/pedidos', icon: ShoppingCart },
]

const userData = computed(() => ({
  name: user.value?.usuario || '',
  email: user.value?.email || '',
  avatar: '',
}))
</script>

<template>
  <Sidebar v-bind="props">
    <SidebarHeader>
      <div class="flex items-center gap-3 px-4 py-2 group-data-[collapsible=icon]:px-2 group-data-[collapsible=icon]:justify-center">
        <!-- Logo quando sidebar está aberta -->
        <img
          v-if="!showLogoFallback"
          src="/logo_full.png"
          alt="Trinket Store"
          class="h-10 w-auto object-contain group-data-[collapsible=icon]:hidden"
          @error="showLogoFallback = true"
        />
        <!-- Fallback quando logo não existe (sidebar aberta) -->
        <div v-if="showLogoFallback" class="flex items-center gap-2 group-data-[collapsible=icon]:hidden">
          <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-[#a4f380] to-[#80d3e4]">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-stone-900" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"/>
              <path d="M3 6h18"/>
              <path d="M16 10a4 4 0 0 1-8 0"/>
            </svg>
          </div>
          <span class="text-sm font-bold bg-gradient-to-r from-[#a4f380] via-[#80d3e4] to-[#f6d95c] bg-clip-text text-transparent">
            Trinket Store
          </span>
        </div>
        
        <!-- Logo quando sidebar está fechada (ícone) -->
        <img
          v-if="!showIconFallback"
          src="/logo_square.png"
          alt="Trinket Store"
          class="hidden h-10 w-10 object-contain group-data-[collapsible=icon]:block"
          @error="showIconFallback = true"
        />
        <!-- Fallback quando logo não existe (sidebar fechada) -->
        <div v-if="showIconFallback" class="hidden group-data-[collapsible=icon]:flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-to-br from-[#a4f380] to-[#80d3e4]">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6 text-stone-900" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"/>
            <path d="M3 6h18"/>
            <path d="M16 10a4 4 0 0 1-8 0"/>
          </svg>
        </div>
      </div>
    </SidebarHeader>
    <SidebarContent>
      <NavMain :items="navItems" />
    </SidebarContent>
    <SidebarRail />
  </Sidebar>
</template>
