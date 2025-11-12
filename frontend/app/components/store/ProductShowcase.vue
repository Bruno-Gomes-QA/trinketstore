<template>
  <section id="colecao-glow" class="py-12 md:py-16">
    <div class="container mx-auto px-4">
      <div class="mb-10 space-y-3 text-center">
        <p class="text-xs font-semibold uppercase tracking-[0.35em] text-brand-cyan">Coleção</p>
        <h2 class="text-3xl md:text-4xl font-bold">Produtos em destaque</h2>
        <p class="text-sm text-muted-foreground md:text-base">
          Mostruário com três opções para você ter uma ideia de preços e acabamentos.
        </p>
      </div>

      <div class="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        <article
          v-for="product in products"
          :key="product.id"
          class="flex h-full flex-col rounded-2xl border border-border bg-white p-6 shadow-sm"
        >
          <div class="flex items-center justify-between text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
            <span>{{ product.badge }}</span>
            <div class="flex items-center gap-1 text-brand-yellow">
              <Star class="h-4 w-4 fill-brand-yellow text-brand-yellow" />
              <span class="tracking-normal text-xs text-foreground">{{ product.reviews }}</span>
            </div>
          </div>

          <div class="my-5">
            <div class="flex h-20 w-20 items-center justify-center rounded-xl bg-muted text-brand-cyan">
              <ImageIcon class="h-8 w-8" />
            </div>
          </div>

          <div class="space-y-2">
            <h3 class="text-xl font-semibold text-foreground">{{ product.name }}</h3>
            <p class="text-sm text-muted-foreground">{{ product.description }}</p>
          </div>

          <div class="mt-auto flex items-end justify-between pt-6">
            <div>
              <p class="text-3xl font-bold text-foreground">{{ formatCurrency(product.price) }}</p>
              <p class="text-xs uppercase tracking-[0.3em] text-muted-foreground">{{ product.pack }}</p>
            </div>
            <button class="inline-flex items-center gap-2 rounded-lg border border-border px-3 py-2 text-sm font-semibold text-foreground transition hover:border-brand-cyan hover:text-brand-cyan">
              <ShoppingCart class="h-4 w-4" />
              <span>Adicionar</span>
            </button>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ImageIcon, ShoppingCart, Star } from 'lucide-vue-next'

const { formatCurrency } = useFormatters()

const products = [
  {
    id: 1,
    name: 'Pato liso azul',
    description: 'Modelo em resina com pintura azul clara e acabamento fosco.',
    price: 24.9,
    pack: 'Unidade',
    reviews: '42 avaliações',
    badge: 'PATO',
  },
  {
    id: 2,
    name: 'Tartaruga verde oliva',
    description: 'Peça única com casco verde e base neutra.',
    price: 34.9,
    pack: 'Unidade',
    reviews: '31 avaliações',
    badge: 'TARTARUGA',
  },
  {
    id: 3,
    name: 'Dupla pato + tartaruga',
    description: 'Combo com um pato e uma tartaruga nos tons mais pedidos.',
    price: 54.9,
    pack: 'Kit com 2 peças',
    reviews: '18 avaliações',
    badge: 'KIT',
  },
]
</script>
