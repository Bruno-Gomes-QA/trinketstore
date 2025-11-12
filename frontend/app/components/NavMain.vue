<script setup lang="ts">
import type { Component } from 'vue'
import { useRoute } from 'vue-router'
import {
  SidebarGroup,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '~/components/ui/sidebar'

const props = defineProps<{
  title?: string
  items: {
    title: string
    url: string
    icon?: Component
  }[]
}>()

const route = useRoute()

const isActive = (url: string) => route.path === url
</script>

<template>
  <SidebarGroup>
    <SidebarGroupLabel>
      {{ props.title || 'Menu' }}
    </SidebarGroupLabel>
    <SidebarMenu>
      <SidebarMenuItem
        v-for="item in props.items"
        :key="item.title"
      >
        <SidebarMenuButton
          as-child
          :tooltip="item.title"
          :class="[
            isActive(item.url)
              ? 'bg-sidebar-accent text-sidebar-accent-foreground'
              : ''
          ]"
        >
          <NuxtLink :to="item.url" class="flex items-center gap-3">
            <component
              :is="item.icon"
              v-if="item.icon"
              class="h-4 w-4"
            />
            <span>{{ item.title }}</span>
          </NuxtLink>
        </SidebarMenuButton>
      </SidebarMenuItem>
    </SidebarMenu>
  </SidebarGroup>
</template>
