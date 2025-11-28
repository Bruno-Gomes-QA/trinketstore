<template>
  <footer class="border-t bg-muted/30 mt-20">
    <div class="container mx-auto px-4 py-12">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div>
          <img 
            v-if="logoFullExists" 
            src="/logo_full.png" 
            alt="Trinket Store" 
            class="h-12 w-auto mb-4"
          />
          <div v-else class="h-12 w-28 mb-4" aria-hidden="true"></div>
          <p class="text-sm text-muted-foreground mb-4">
            Miniaturas artesanais únicas de tartarugas e patos, feitas com carinho para alegrar seu dia.
          </p>
        </div>

        <div>
          <h3 class="font-semibold mb-4">Contato</h3>
          <p class="text-sm text-muted-foreground mb-3">
            Dúvidas? Fale com a equipe pelo WhatsApp.
          </p>
          <a
            class="inline-flex items-center gap-2 rounded-xl bg-emerald-400 px-4 py-2 text-sm font-semibold text-black shadow-md transition hover:bg-emerald-500"
            :href="whatsAppLink"
            target="_blank"
            rel="noopener noreferrer"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="currentColor" viewBox="0 0 24 24"><path d="M.057 24l1.687-6.163a11.867 11.867 0 01-1.51-5.8C.234 5.318 5.29.223 11.515.223c3.181 0 6.167 1.24 8.413 3.488a11.8 11.8 0 013.48 8.401c-.003 6.225-5.098 11.28-11.37 11.28a11.9 11.9 0 01-5.688-1.448L.057 24zm6.597-3.807c1.676.995 3.276 1.591 4.988 1.593 5.034 0 9.134-4.09 9.137-9.126.002-5.033-4.082-9.125-9.114-9.128-5.04 0-9.14 4.09-9.143 9.125a9.09 9.09 0 001.59 5.18l-.999 3.648 3.54-.292zm11.387-5.464c-.074-.124-.272-.198-.57-.347-.297-.149-1.758-.868-2.03-.967-.272-.099-.47-.149-.669.148-.198.297-.768.967-.941 1.165-.173.198-.347.223-.644.074-.297-.149-1.255-.463-2.39-1.475-.883-.788-1.48-1.76-1.653-2.057-.173-.297-.018-.458.13-.606.134-.133.297-.347.446-.52.149-.173.198-.297.297-.495.099-.198.05-.372-.025-.521-.075-.149-.669-1.611-.916-2.206-.242-.579-.487-.5-.669-.51l-.57-.01c-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.095 3.2 5.076 4.487.709.306 1.262.489 1.694.626.712.227 1.36.195 1.872.118.571-.085 1.758-.719 2.006-1.413.248-.695.248-1.29.173-1.414z"/></svg>
            <span>Chamar no WhatsApp</span>
          </a>
        </div>

        <div>
          <h3 class="font-semibold mb-4">Pagamento</h3>
          <div class="grid grid-cols-3 gap-2 mb-4">
            <div class="h-10 bg-accent rounded flex items-center justify-center text-xs font-semibold">PIX</div>
          </div>
          <p class="text-xs text-muted-foreground">
            Aceitamos apenas PIX dinâmico via Mercado Pago.
          </p>
        </div>
      </div>

      <div class="border-t mt-8 pt-8 text-center text-sm text-muted-foreground">
        <p>&copy; 2025 Trinket Store. Todos os direitos reservados.</p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const logoFullExists = ref(false)
const whatsAppLink = 'https://wa.me/5511947229703?text=Ol%C3%A1%2C%20vim%20atrav%C3%A9s%20do%20site%20e%20gostaria%20de%20tirar%20algumas%20d%C3%BAvidas'

onMounted(() => {
  // Check if logo_full.png exists
  const img = new Image()
  img.onload = () => { logoFullExists.value = true }
  img.onerror = () => { logoFullExists.value = false }
  img.src = '/logo_full.png'
})
</script>
