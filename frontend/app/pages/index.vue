<template>
  <div class="min-h-screen flex flex-col">
    <StoreOnboarding ref="onboardingRef" />
    <StoreHeader @show-onboarding="showOnboarding" />
    <main class="flex-1">
      <StoreBanners @show-onboarding="showOnboarding" />
      <ProductShowcase />
      <StoreProductCatalog />
      
    </main>
    <StoreFooter />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Package, Heart, Shield } from 'lucide-vue-next'

const onboardingRef = ref<any>(null)

const showOnboarding = () => {
  if (onboardingRef.value) {
    onboardingRef.value.show()
  }
}
</script>
