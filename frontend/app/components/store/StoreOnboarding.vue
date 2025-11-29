<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="showOnboarding" class="fixed inset-0 z-[9999] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm" @click.self="closeOnboarding">
        <div class="relative bg-background rounded-2xl w-full max-w-xl shadow-[0_0_50px_rgba(6,182,212,0.3),0_0_100px_rgba(6,182,212,0.2)] overflow-hidden max-h-[85vh] flex flex-col" @click.stop>
          <!-- Close button -->
          <button 
            @click="closeOnboarding"
            class="absolute top-4 right-4 p-2 hover:bg-accent rounded-lg transition-colors z-10"
            aria-label="Fechar"
          >
            <X class="h-5 w-5" />
          </button>

          <!-- Content -->
          <div class="p-6 md:p-8 overflow-y-auto">
            <!-- Header -->
            <div class="text-center mb-8 space-y-3">
              <div>
                <h2 class="text-2xl font-bold text-foreground">Como funciona?</h2>
              </div>
              <p class="text-sm text-muted-foreground max-w-md mx-auto">
                Processo rápido: você escolhe, paga, recebe o QR Code e retira no espaço da feira.
              </p>
            </div>

            <!-- Steps -->
            <div class="grid gap-3 mb-8 md:grid-cols-2">
              <article
                v-for="(step, index) in steps"
                :key="step.title"
                class="rounded-xl border border-border/70 bg-muted/20 p-4"
              >
                <div class="flex items-center gap-3 mb-3">
                  <div class="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-brand-cyan/15 text-brand-cyan">
                    <component :is="step.icon" class="h-5 w-5" />
                  </div>
                  <h3 class="font-semibold text-foreground">{{ step.tag }}</h3>
                </div>
                <p class="text-sm text-muted-foreground leading-relaxed">{{ step.description }}</p>
              </article>
            </div>  
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { X, Sparkles, ShoppingBag, CreditCard, QrCode, MapPin } from 'lucide-vue-next'

const showOnboarding = ref(false)

const closeOnboarding = () => {
  showOnboarding.value = false
  localStorage.setItem('trinket-onboarding-seen', 'true')
}

const steps = [
  {
    title: 'Escolha seus itens',
    description: 'Selecione os patos ou tartarugas e confirme as quantidades na página principal.',
    tag: 'Seleção',
    icon: ShoppingBag,
  },
  {
    title: 'Faça o pagamento',
    description: 'PIX ou cartão. Você recebe a confirmação em poucos segundos.',
    tag: 'Pagamento',
    icon: CreditCard,
  },
  {
    title: 'Receba o QR Code',
    description: 'Enviamos um QR Code com o resumo do pedido para apresentar no evento.',
    tag: 'Código',
    icon: QrCode,
  },
  {
    title: 'Retire no evento',
    description: 'Leve o QR Code até a Sala 4, à direita após a passarela, e retire os itens.',
    tag: 'Retirada',
    icon: MapPin,
  },
]

onMounted(() => {
  const hasSeenOnboarding = localStorage.getItem('trinket-onboarding-seen')
  if (!hasSeenOnboarding) {
    setTimeout(() => {
      showOnboarding.value = true
    }, 800)
  }
})

defineExpose({
  show: () => { showOnboarding.value = true }
})
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active > div,
.modal-leave-active > div {
  transition: transform 0.2s ease;
}

.modal-enter-from > div,
.modal-leave-to > div {
  transform: scale(0.95);
}
</style>
