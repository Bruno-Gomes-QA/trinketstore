<script setup lang="ts">
import { useAuth } from '~/composables/api/useAuth'
import { useFormatters } from '~/composables/helpers/useFormatters'
import AppSidebar from '~/components/AppSidebar.vue'
import AppHeader from '~/components/AppHeader.vue'
import {
  SidebarInset,
  SidebarProvider,
} from '~/components/ui/sidebar'

definePageMeta({
  middleware: 'auth',
  pageTitle: 'Dashboard',
})

const { user } = useAuth()
const { formatDateTime } = useFormatters()

const loggedInAt = ref(new Date().toISOString())
</script>

<template>
  <SidebarProvider>
    <AppSidebar />
    <SidebarInset>
      <AppHeader />
      
      <div class="flex flex-1 flex-col gap-4 p-4 pt-0">
        <div class="rounded-2xl border bg-card p-6 shadow">
          <p class="text-sm uppercase tracking-wide text-muted-foreground">Painel Administrativo</p>
          <h1 class="text-3xl font-bold mt-2">
            Bem-vindo, {{ user?.usuario || 'usuário' }}!
          </h1>
          <p class="text-sm text-muted-foreground mt-1">
            Gerencie seu e-commerce, produtos, pedidos e retiradas.
          </p>
        </div>

        <div class="grid gap-4 md:grid-cols-4">
          <div class="bg-card border rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow">
            <div class="flex items-center justify-between">
              <p class="text-sm text-muted-foreground">Vendas Hoje</p>
              <div class="p-2 rounded-lg bg-gradient-to-br from-[#a4f380]/20 to-[#80d3e4]/20">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 text-[#a4f380]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
                </svg>
              </div>
            </div>
            <p class="text-2xl font-bold mt-2">R$ 0,00</p>
            <p class="text-xs text-muted-foreground mt-1">+0% vs ontem</p>
          </div>
          
          <div class="bg-card border rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow">
            <div class="flex items-center justify-between">
              <p class="text-sm text-muted-foreground">Pedidos Pendentes</p>
              <div class="p-2 rounded-lg bg-gradient-to-br from-[#f6d95c]/20 to-[#a4f380]/20">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 text-[#f6d95c]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="8" cy="21" r="1"/>
                  <circle cx="19" cy="21" r="1"/>
                  <path d="M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12"/>
                </svg>
              </div>
            </div>
            <p class="text-2xl font-bold mt-2">0</p>
            <p class="text-xs text-muted-foreground mt-1">Aguardando pagamento</p>
          </div>
          
          <div class="bg-card border rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow">
            <div class="flex items-center justify-between">
              <p class="text-sm text-muted-foreground">Produtos Ativos</p>
              <div class="p-2 rounded-lg bg-gradient-to-br from-[#80d3e4]/20 to-[#a4f380]/20">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 text-[#80d3e4]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"/>
                  <path d="M3 6h18"/>
                  <path d="M16 10a4 4 0 0 1-8 0"/>
                </svg>
              </div>
            </div>
            <p class="text-2xl font-bold mt-2">0</p>
            <p class="text-xs text-muted-foreground mt-1">Em estoque</p>
          </div>
          
          <div class="bg-card border rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow">
            <div class="flex items-center justify-between">
              <p class="text-sm text-muted-foreground">Ticket Médio</p>
              <div class="p-2 rounded-lg bg-gradient-to-br from-[#a4f380]/20 to-[#f6d95c]/20">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 text-[#a4f380]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="12" x2="12" y1="2" y2="22"/>
                  <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
                </svg>
              </div>
            </div>
            <p class="text-2xl font-bold mt-2">R$ 0,00</p>
            <p class="text-xs text-muted-foreground mt-1">Últimos 30 dias</p>
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <div class="bg-card border rounded-xl p-6 shadow-sm">
            <h3 class="text-lg font-semibold mb-4">Informações da Conta</h3>
            <div class="space-y-3">
              <div>
                <p class="text-xs text-muted-foreground">ID do Usuário</p>
                <p class="text-sm font-medium mt-1">{{ user?.usuarioKey || '—' }}</p>
              </div>
              <div>
                <p class="text-xs text-muted-foreground">E-mail</p>
                <p class="text-sm font-medium mt-1 break-all">{{ user?.email || '—' }}</p>
              </div>
              <div>
                <p class="text-xs text-muted-foreground">Logado em</p>
                <p class="text-sm font-medium mt-1">{{ formatDateTime(loggedInAt) }}</p>
              </div>
            </div>
          </div>

          <div class="bg-card border rounded-xl p-6 shadow-sm">
            <h3 class="text-lg font-semibold mb-4">Ações Rápidas</h3>
            <div class="space-y-2">
              <button class="w-full text-left px-4 py-3 rounded-lg bg-gradient-to-r from-[#a4f380] to-[#80d3e4] text-stone-900 font-medium hover:shadow-md transition-all">
                + Adicionar Produto
              </button>
              <button class="w-full text-left px-4 py-3 rounded-lg border hover:bg-muted transition-all">
                Ver Pedidos Pendentes
              </button>
              <button class="w-full text-left px-4 py-3 rounded-lg border hover:bg-muted transition-all">
                Gerar QR Code
              </button>
            </div>
          </div>
        </div>
      </div>
    </SidebarInset>
  </SidebarProvider>
</template>
