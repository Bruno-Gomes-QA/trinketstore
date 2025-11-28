<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RotateCcw, Sparkles, Star, Timer } from 'lucide-vue-next'

type GameStatus = 'playing' | 'win' | 'lose'

type CardBase = {
  key: string
  label: string
  image: string
}

type MemoryCard = CardBase & {
  id: number
  flipped: boolean
  matched: boolean
}

const baseCards: CardBase[] = [
  { key: 'pato-amarelo', label: 'Pato amarelo', image: '/pato_amarelo.png' },
  { key: 'pato-azul', label: 'Pato azul', image: '/pato_azul.png' },
  { key: 'pato-roxo', label: 'Pato roxo', image: '/pato_roxo.png' },
  { key: 'tartaruga-verde', label: 'Tartaruga verde', image: '/tartaruga_verde.png' },
  { key: 'tartaruga-azul', label: 'Tartaruga azul', image: '/tartaruga_azul.png' },
  { key: 'tartaruga-roxa', label: 'Tartaruga roxa', image: '/tartaruga_roxa.png' },
]

const cards = ref<MemoryCard[]>([])
const firstPickId = ref<number | null>(null)
const secondPickId = ref<number | null>(null)
const attempts = ref(0)
const matchesFound = ref(0)
const MAX_TIME = 60
const timeLeft = ref(MAX_TIME)
const lockBoard = ref(false)
const phase = ref<'loader' | 'intro' | 'countdown' | 'playing' | 'result'>('loader')
const gameStatus = ref<GameStatus>('playing')
const countdownValue = ref<string | number>('3')

const timerInterval = ref<ReturnType<typeof setInterval> | null>(null)
const loaderTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const countdownTimer = ref<ReturnType<typeof setInterval> | null>(null)

const totalPairs = computed(() => baseCards.length)

useHead({
  title: 'Desafio da Memória | Trinket Store',
  link: [
    { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
    { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' },
    {
      rel: 'stylesheet',
      href: 'https://fonts.googleapis.com/css2?family=Chakra+Petch:wght@500;700&family=Space+Grotesk:wght@400;600;700&display=swap',
    },
  ],
})

onMounted(() => {
  loaderTimer.value = setTimeout(() => {
    phase.value = 'intro'
  }, 4000)
})

onBeforeUnmount(() => {
  clearTimers()
})

const starCount = computed(() => {
  if (gameStatus.value !== 'win') return 0
  if (attempts.value <= 10 && timeLeft.value > 0) return 3
  if (attempts.value <= 14) return 2
  return 1
})

const hasPrize = computed(() => gameStatus.value === 'win' && starCount.value === 3)

const timerProgress = computed(() => Math.max(0, (timeLeft.value / MAX_TIME) * 100))

const timerToneClass = computed(() =>
  timeLeft.value <= 5 ? 'text-red-600 drop-shadow-[0_0_12px_rgba(239,68,68,0.5)]' : 'text-black',
)

const startGame = () => {
  clearTimers()
  buildDeck()
  attempts.value = 0
  matchesFound.value = 0
  timeLeft.value = MAX_TIME
  gameStatus.value = 'playing'
  phase.value = 'playing'
  lockBoard.value = false
  firstPickId.value = null
  secondPickId.value = null

  timerInterval.value = setInterval(() => {
    timeLeft.value -= 1
    if (timeLeft.value <= 0) {
      timeLeft.value = 0
      finishAsLoss()
    }
  }, 1000)
}

const buildDeck = () => {
  const deck: MemoryCard[] = [...baseCards, ...baseCards]
    .map((card, index) => ({
      ...card,
      id: index + 1,
      flipped: false,
      matched: false,
    }))
    .sort(() => Math.random() - 0.5)

  cards.value = deck
}

const finishAsLoss = () => {
  clearTimerInterval()
  lockBoard.value = true
  gameStatus.value = 'lose'
  phase.value = 'result'
}

const clearTimerInterval = () => {
  if (timerInterval.value) {
    clearInterval(timerInterval.value)
    timerInterval.value = null
  }
}

const clearTimers = () => {
  clearTimerInterval()
  if (loaderTimer.value) {
    clearTimeout(loaderTimer.value)
    loaderTimer.value = null
  }
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
}

const cardGlow = (card: MemoryCard) =>
  card.key.includes('pato')
    ? '0 0 28px rgba(246, 217, 92, 0.65)'
    : '0 0 28px rgba(128, 211, 228, 0.65)'

const cardBackGradient = (card: MemoryCard) =>
  card.key.includes('pato')
    ? 'linear-gradient(135deg, rgba(246, 217, 92, 0.95), rgba(164, 243, 128, 0.92))'
    : 'linear-gradient(135deg, rgba(128, 211, 228, 0.95), rgba(164, 243, 128, 0.92))'

const handleCardClick = (cardId: number) => {
  if (lockBoard.value || phase.value !== 'playing' || gameStatus.value !== 'playing') return

  const card = cards.value.find((item) => item.id === cardId)
  if (!card || card.flipped || card.matched) return

  flipCard(cardId, true)

  if (firstPickId.value === null) {
    firstPickId.value = cardId
    return
  }

  secondPickId.value = cardId
  attempts.value += 1
  lockBoard.value = true

  setTimeout(resolvePair, 450)
}

const flipCard = (cardId: number, flipped: boolean) => {
  cards.value = cards.value.map((card) =>
    card.id === cardId
      ? {
          ...card,
          flipped,
        }
      : card,
  )
}

const resolvePair = () => {
  const first = cards.value.find((card) => card.id === firstPickId.value)
  const second = cards.value.find((card) => card.id === secondPickId.value)

  if (!first || !second) {
    resetSelection()
    lockBoard.value = false
    return
  }

  const isMatch = first.key === second.key

  if (isMatch) {
    const willFinish = matchesFound.value + 1 === totalPairs.value
    cards.value = cards.value.map((card) =>
      card.id === first.id || card.id === second.id
        ? {
            ...card,
            matched: true,
            flipped: true,
          }
        : card,
    )
    matchesFound.value += 1
    setTimeout(() => {
      resetSelection()
      lockBoard.value = false
      if (willFinish) {
        gameStatus.value = 'win'
        clearTimerInterval()
        phase.value = 'result'
      }
    }, 300)
    return
  }

  setTimeout(() => {
    flipCard(first.id, false)
    flipCard(second.id, false)
    resetSelection()
    lockBoard.value = false
  }, 700)
}

const resetSelection = () => {
  firstPickId.value = null
  secondPickId.value = null
}

const startCountdown = () => {
  clearTimers()
  countdownValue.value = '3'
  phase.value = 'countdown'
  const sequence: Array<number | string> = [3, 2, 1, 'Vai!']
  let index = 0

  countdownTimer.value = setInterval(() => {
    countdownValue.value = sequence[index]
    index += 1
    if (index >= sequence.length) {
      clearInterval(countdownTimer.value as ReturnType<typeof setInterval>)
      countdownTimer.value = null
      setTimeout(() => {
        startGame()
      }, 250)
    }
  }, 650)
}

const restartCycle = () => {
  clearTimers()
  gameStatus.value = 'playing'
  phase.value = 'loader'
  loaderTimer.value = setTimeout(() => {
    phase.value = 'intro'
  }, 4000)
}
</script>

<template>
  <div class="memory-page min-h-screen h-screen text-slate-900 relative overflow-hidden">
    <div class="absolute inset-0 pointer-events-none opacity-70">
      <div class="absolute -left-10 -top-10 h-72 w-72 rounded-full bg-brand-yellow blur-[120px]" />
      <div class="absolute right-10 top-20 h-80 w-80 rounded-full bg-brand-cyan blur-[140px]" />
      <div class="absolute left-1/2 bottom-0 h-72 w-72 -translate-x-1/2 rounded-full bg-brand-green blur-[120px]" />
    </div>

    <Transition name="fade">
      <div
        v-if="phase === 'loader'"
        class="fixed inset-0 z-40 flex flex-col items-center justify-center gap-6 bg-white overflow-hidden"
      >
        <div class="absolute inset-0 pointer-events-none">
          <div class="absolute left-10 top-10 h-48 w-48 rounded-full bg-brand-yellow blur-[120px] opacity-70 animate-pulse" />
          <div class="absolute right-10 bottom-12 h-56 w-56 rounded-full bg-brand-cyan blur-[140px] opacity-70 animate-pulse" />
          <div class="absolute left-1/2 top-1/2 h-64 w-64 -translate-x-1/2 -translate-y-1/2 rounded-full bg-brand-green blur-[150px] opacity-60 animate-[floaty_4s_ease-in-out_infinite]" />
        </div>
        <div class="flex flex-col items-center gap-3 text-center">
          <img
            src="/logo_full.png"
            alt="Logo Trinket Store"
            class="h-24 w-auto logo-glow"
          />
        </div>
      </div>
    </Transition>

    <Transition name="fade">
      <div
        v-if="phase === 'intro'"
        class="fixed inset-0 z-30 flex flex-col items-center justify-center bg-white/90 backdrop-blur overflow-hidden"
      >
        <div class="absolute inset-0 pointer-events-none">
          <div class="absolute -left-10 top-10 h-48 w-48 rounded-full bg-brand-yellow blur-[120px] opacity-70 animate-pulse" />
          <div class="absolute right-6 bottom-6 h-56 w-56 rounded-full bg-brand-cyan blur-[140px] opacity-70 animate-pulse" />
          <div class="absolute left-1/2 top-1/2 h-64 w-64 -translate-x-1/2 -translate-y-1/2 rounded-full bg-brand-green blur-[150px] opacity-60 animate-[floaty_4s_ease-in-out_infinite]" />
        </div>
        <div class="relative flex max-w-2xl flex-col items-center gap-4 px-6 text-center">
          <Sparkles class="h-10 w-10 text-brand-yellow drop-shadow-[0_0_16px_rgba(246,217,92,0.9)]" />
          <h1 class="text-3xl font-semibold text-brand-foreground drop-shadow-[0_0_18px_rgba(128,211,228,0.6)]">
            Duelo de Memória: Patos vs. Tartarugas
          </h1>
          <p class="text-sm text-slate-600">
            60 segundos para virar todos os pares em menos de 10 tentativas. Bora jogar?
          </p>
          <button
            type="button"
            class="mt-2 inline-flex items-center justify-center gap-2 rounded-full bg-brand-green px-6 py-3 text-base font-semibold text-brand-foreground shadow-brand transition hover:-translate-y-0.5 hover:shadow-brand-strong"
            @click="startCountdown"
          >
            Começar!
          </button>
        </div>
      </div>
    </Transition>

    <Transition name="fade">
      <div
        v-if="phase === 'countdown'"
        class="fixed inset-0 z-30 flex flex-col items-center justify-center bg-white/90 backdrop-blur overflow-hidden"
      >
        <div class="absolute inset-0 pointer-events-none">
          <div class="absolute left-8 top-8 h-32 w-32 rounded-full bg-brand-yellow blur-[100px] opacity-70" />
          <div class="absolute right-8 bottom-8 h-40 w-40 rounded-full bg-brand-cyan blur-[120px] opacity-70" />
        </div>
        <div class="flex h-32 w-32 items-center justify-center rounded-full bg-brand-gradient-soft text-5xl font-bold text-brand-foreground shadow-brand">
          {{ countdownValue }}
        </div>
      </div>
    </Transition>

    <Transition name="fade">
      <div
        v-if="phase === 'result'"
        class="fixed inset-0 z-30 flex items-center justify-center bg-white/85 backdrop-blur overflow-hidden"
      >
        <div class="absolute inset-0 pointer-events-none">
          <div class="absolute -left-8 top-8 h-48 w-48 rounded-full bg-brand-yellow blur-[120px] opacity-70 animate-pulse" />
          <div class="absolute right-6 bottom-6 h-60 w-60 rounded-full bg-brand-cyan blur-[150px] opacity-70 animate-pulse" />
          <div class="absolute left-1/2 top-1/2 h-72 w-72 -translate-x-1/2 -translate-y-1/2 rounded-full bg-brand-green blur-[160px] opacity-60 animate-[floaty_4s_ease-in-out_infinite]" />
        </div>
        <div class="relative flex max-w-2xl flex-col items-center gap-4 px-6 text-center">
          <h2 class="text-2xl font-semibold text-brand-foreground">
            {{ gameStatus === 'win' ? 'Você completou todos os pares!' : 'Tempo esgotou, tente novamente.' }}
          </h2>
          <p class="text-sm text-slate-600">
            Tempo restante: {{ timeLeft }}s • Tentativas: {{ attempts }}
          </p>
          <div class="mt-2 flex items-center justify-center gap-3 text-brand-yellow drop-shadow-[0_0_18px_rgba(246,217,92,0.75)]">
            <Star
              v-for="index in 3"
              :key="index"
              :class="[
                index === 2 ? 'h-12 w-12' : 'h-10 w-10',
                index <= starCount ? 'text-brand-yellow drop-shadow-[0_0_18px_rgba(246,217,92,0.85)]' : 'text-slate-300',
              ]"
            />
          </div>
          <div
            v-if="hasPrize"
            class="inline-flex items-center gap-3 rounded-2xl border border-brand-yellow/40 bg-brand-yellow/20 px-4 py-3 shadow-[0_0_20px_rgba(246,217,92,0.4)]"
          >
            <img src="/patinho_do_premio.png" alt="Patinho dourado" class="h-12 w-12 drop-shadow-lg" />
            <div class="text-left text-sm text-brand-foreground">
              <p class="font-semibold">Patinho dourado é seu!</p>
              <p>3 estrelas em menos de 10 tentativas. Arrasou!</p>
            </div>
          </div>
          <button
            type="button"
            class="mt-2 inline-flex items-center justify-center gap-2 rounded-full bg-brand-green px-6 py-3 text-base font-semibold text-brand-foreground shadow-brand transition hover:-translate-y-0.5 hover:shadow-brand-strong"
            @click="restartCycle"
          >
            <RotateCcw class="h-4 w-4" />
            Jogar novamente
          </button>
        </div>
      </div>
    </Transition>

    <div class="relative z-10 flex min-h-screen flex-col items-center justify-center px-4 py-6">
      <div
        v-if="phase === 'playing'"
        class="mb-4 flex items-center gap-2 rounded-full border border-slate-200 bg-white/80 px-4 py-2 text-lg font-semibold shadow-lg"
      >
        <Timer class="h-5 w-5 text-slate-500" />
        <span :class="timerToneClass">
          {{ timeLeft }}s
        </span>
        <div class="h-1.5 w-28 overflow-hidden rounded-full bg-slate-200">
          <div class="h-1.5 bg-gradient-to-r from-brand-yellow via-brand-green to-brand-cyan transition-all duration-300" :style="{ width: `${timerProgress}%` }" />
        </div>
      </div>

      <section class="w-full max-w-5xl rounded-3xl border border-white/60 bg-white/70 p-6 shadow-2xl backdrop-blur">
        <div class="grid grid-cols-3 gap-3 sm:grid-cols-4 lg:grid-cols-6">
          <button
            v-for="card in cards"
            :key="card.id"
            type="button"
            class="card-outer group"
            :disabled="card.matched || gameStatus !== 'playing'"
            :aria-label="`Carta ${card.label}`"
            @click="handleCardClick(card.id)"
          >
            <div
              class="card-inner"
              :class="{ 'is-flipped': card.flipped || card.matched, 'is-matched': card.matched }"
              :style="{ boxShadow: cardGlow(card) }"
            >
              <div class="card-face card-front">
                <div class="relative flex h-full w-full items-center justify-center overflow-hidden rounded-2xl border border-white/40 bg-gradient-to-br from-white via-white to-brand-surface">
                  <span class="absolute inset-0 -rotate-6 bg-[radial-gradient(circle_at_20%_20%,rgba(246,217,92,0.35),transparent_35%),radial-gradient(circle_at_80%_20%,rgba(128,211,228,0.35),transparent_35%),radial-gradient(circle_at_30%_80%,rgba(164,243,128,0.35),transparent_40%)]" />
                  <span class="text-3xl font-bold text-slate-700 drop-shadow-[0_0_10px_rgba(164,243,128,0.45)]">?</span>
                </div>
              </div>
              <div
                class="card-face card-back"
                :style="{ background: cardBackGradient(card), boxShadow: cardGlow(card) }"
              >
                <img :src="card.image" :alt="card.label" class="h-16 w-16 object-contain drop-shadow-lg" />
                <span class="mt-2 text-xs font-semibold text-brand-foreground shadow-[0_0_10px_rgba(0,0,0,0.15)]">
                  {{ card.label }}
                </span>
              </div>
            </div>
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.memory-page {
  background: radial-gradient(circle at 20% 20%, rgba(246, 217, 92, 0.18), transparent 35%),
    radial-gradient(circle at 80% 0%, rgba(128, 211, 228, 0.16), transparent 40%),
    radial-gradient(circle at 50% 80%, rgba(164, 243, 128, 0.18), transparent 40%),
    linear-gradient(135deg, #ffffff 0%, #f7f8ff 50%, #ffffff 100%);
  font-family: 'Chakra Petch', 'Space Grotesk', var(--font-sans), system-ui, -apple-system, sans-serif;
  overflow: hidden;
}

.card-outer {
  position: relative;
  height: 8rem;
  width: 100%;
  overflow: hidden;
  border-radius: 1rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  will-change: transform;
  perspective: 900px;
}

.card-outer:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(128, 211, 228, 0.25);
}

.card-inner {
  position: relative;
  height: 100%;
  width: 100%;
  transform-style: preserve-3d;
  border-radius: 1rem;
}

.card-inner.is-matched {
  filter: drop-shadow(0 0 14px rgba(164, 243, 128, 0.5));
}

.card-face {
  position: absolute;
  inset: 0;
  backface-visibility: hidden;
  border-radius: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.6s ease;
}

.card-front {
  transform: rotateY(0deg);
}

.card-back {
  transform: rotateY(180deg);
  flex-direction: column;
}

.card-inner.is-flipped .card-front {
  transform: rotateY(180deg);
}

.card-inner.is-flipped .card-back {
  transform: rotateY(0deg);
}

.logo-glow {
  filter: drop-shadow(0 0 35px rgba(246, 217, 92, 0.95));
  animation: softPulse 2.4s ease-in-out infinite, floaty 3s ease-in-out infinite;
}

.loader-glow {
  background: radial-gradient(circle at 50% 50%, rgba(246, 217, 92, 0.12), transparent 50%),
    linear-gradient(135deg, rgba(246, 217, 92, 0.08), rgba(128, 211, 228, 0.1));
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.animate-blink {
  animation: blink 1s ease-in-out infinite, floaty 3s ease-in-out infinite;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.35s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes softPulse {
  0% {
    transform: scale(0.98);
    filter: drop-shadow(0 0 24px rgba(246, 217, 92, 0.55));
  }
  50% {
    transform: scale(1.02);
    filter: drop-shadow(0 0 36px rgba(246, 217, 92, 0.9));
  }
  100% {
    transform: scale(0.98);
    filter: drop-shadow(0 0 24px rgba(246, 217, 92, 0.55));
  }
}

@keyframes blink {
  0% {
    opacity: 0.4;
    transform: scale(0.98);
  }
  50% {
    opacity: 1;
    transform: scale(1.04);
  }
  100% {
    opacity: 0.45;
    transform: scale(0.98);
  }
}

@keyframes floaty {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6px);
  }
  100% {
    transform: translateY(0);
  }
}
</style>
