<template>
  <section class="bg-gradient-to-br from-muted/20 via-brand-cyan/5 to-muted/20 py-12 md:py-16">
    <div class="container mx-auto px-4">
      <div class="grid items-center gap-10 rounded-3xl bg-white p-6 md:p-10 shadow-[0_8px_30px_rgba(0,0,0,0.05),0_0_40px_rgba(6,182,212,0.2)] border border-brand-cyan/10 lg:grid-cols-[1.1fr_0.9fr]">
        <!-- Text -->
        <div class="space-y-6 text-center lg:text-left">
          <div class="inline-flex items-center gap-2 rounded-full bg-gradient-to-r from-brand-cyan to-cyan-500 px-4 py-1.5 text-xs font-semibold uppercase tracking-wider text-black shadow-lg shadow-brand-cyan/30">
            ✨ Coleção 2025
          </div>
          <div class="space-y-4">
            <h1 class="text-3xl md:text-4xl lg:text-5xl font-bold bg-gradient-to-r from-foreground via-brand-cyan to-cyan-600 bg-clip-text text-transparent leading-tight">
              Patos e tartarugas que brilham no escuro
            </h1>
            <div class="space-y-3 text-base md:text-lg text-muted-foreground">
              <p class="leading-relaxed">
                Descubra nossa coleção exclusiva em <strong class="text-brand-cyan">5 cores vibrantes</strong>. 
                De clássicos patos amarelos a modelos coloridos que criam magia quando a luz se apaga.
              </p>
              <div class="grid gap-2 text-sm md:text-base">
                <div class="flex items-center gap-2">
                  <span class="flex h-6 w-6 items-center justify-center rounded-full bg-yellow-400/20 text-yellow-600">🦆</span>
                  <span><strong class="text-foreground">Patos amarelos</strong> · Clássicos atemporais</span>
                </div>
                <div class="flex items-center gap-2">
                  <span class="flex h-6 w-6 items-center justify-center rounded-full bg-brand-cyan/20 text-brand-cyan">🎨</span>
                  <span><strong class="text-foreground">Patos coloridos</strong> · 5 cores diferentes</span>
                </div>
                <div class="flex items-center gap-2">
                  <span class="flex h-6 w-6 items-center justify-center rounded-full bg-green-400/20 text-green-600">✨</span>
                  <span><strong class="text-foreground">Tartarugas glow</strong> · Brilham no escuro</span>
                </div>
              </div>
            </div>
          </div>

          <div class="flex flex-col gap-3 sm:flex-row sm:justify-center lg:justify-start">
            <button
              @click="emit('show-onboarding')"
              class="inline-flex items-center justify-center gap-2 rounded-xl border-2 border-brand-cyan/30 bg-white px-5 py-3 text-sm font-semibold text-foreground transition-all hover:border-brand-cyan hover:bg-brand-cyan/5 hover:shadow-lg hover:shadow-brand-cyan/20 hover:-translate-y-0.5"
            >
              <span>Como funciona?</span>
            </button>
            <button
              @click="scrollToProducts"
              class="inline-flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-brand-cyan to-cyan-500 px-5 py-3 text-sm font-semibold text-black shadow-lg shadow-brand-cyan/30 transition-all hover:shadow-xl hover:shadow-brand-cyan/50 hover:-translate-y-0.5 hover:from-cyan-500 hover:to-brand-cyan"
            >
              Ver produtos
            </button>
          </div>

          <!-- Stats - Desktop only here -->
          <div class="hidden lg:grid gap-4 border-t border-brand-cyan/20 pt-6 text-left sm:grid-cols-3">
            <div
              v-for="stat in quickStats"
              :key="stat.id"
              class="rounded-2xl border border-brand-cyan/10 bg-gradient-to-br from-brand-cyan/5 to-cyan-50 px-4 py-3 shadow-sm hover:shadow-md hover:shadow-brand-cyan/10 transition-all"
            >
              <div class="flex items-center gap-2 mb-1">
                <component :is="stat.icon" class="h-4 w-4 text-brand-cyan" />
                <p class="text-xs uppercase tracking-wider text-brand-cyan font-semibold">{{ stat.label }}</p>
              </div>
              <p class="text-base font-semibold text-foreground">{{ stat.value }}</p>
            </div>
          </div>
        </div>

        <!-- Image block -->
        <div class="relative">
          <div class="rounded-2xl border border-dashed border-border/80 bg-muted/20 p-4">
            <img
              src="/image_prin_commerce.png"
              alt="Produtos de resina"
              class="h-full w-full rounded-2xl object-cover"
            />
          </div>
          <p class="mt-3 text-center text-xs text-muted-foreground">
            Visual simples para referência. A retirada é feita no evento.
          </p>
        </div>

        <!-- Stats - Mobile only, after image -->
        <div class="grid gap-4 lg:hidden sm:grid-cols-3">
          <div
            v-for="stat in quickStats"
            :key="stat.id"
            class="rounded-2xl border border-brand-cyan/10 bg-gradient-to-br from-brand-cyan/5 to-cyan-50 px-4 py-3 shadow-sm"
          >
            <div class="flex items-center gap-2 mb-1">
              <component :is="stat.icon" class="h-4 w-4 text-brand-cyan" />
              <p class="text-xs uppercase tracking-wider text-brand-cyan font-semibold">{{ stat.label }}</p>
            </div>
            <p class="text-base font-semibold text-foreground">{{ stat.value }}</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { Palette, Sparkles, Clock } from 'lucide-vue-next'

const emit = defineEmits(['show-onboarding'])

const scrollToProducts = () => {
  const section = document.getElementById('colecao-glow')
  section?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const quickStats = [
  { id: 'cores', label: 'Variedade', value: '5 cores vibrantes', icon: Palette },
  { id: 'glow', label: 'Especial', value: 'Brilha no escuro', icon: Sparkles },
  { id: 'retirada', label: 'Retirada', value: 'Em até 5 minutos', icon: Clock },
]
</script>
