<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Button } from '~/components/ui/button'
import { Input } from '~/components/ui/input'
import { Label } from '~/components/ui/label'
import { Skeleton } from '~/components/ui/skeleton'
import { useToast } from '~/components/ui/toast/use-toast'

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Meu perfil',
})

const auth = useAuth()
const { toast } = useToast()
const { isValidName, hasLengthBetween } = useValidators()
const { formatDateTime } = useFormatters()
const {
  profile,
  loading,
  mutationLoading,
  error,
  fetchProfile,
  updateProfile,
  deleteProfile,
} = useCurrentUserProfile()

const form = reactive({
  name: '',
})
const formError = ref('')
const showDeleteConfirm = ref(false)

const email = computed(() => profile.value?.email || auth.user.value?.email || '—')
const roleLabel = computed(() => (profile.value?.role === 'admin' ? 'Administrador' : 'Cliente'))
const roleBadgeClass = computed(() =>
  profile.value?.role === 'admin'
    ? 'bg-emerald-100 text-emerald-900 border border-emerald-200'
    : 'bg-slate-100 text-slate-900 border border-slate-200',
)

const isDirty = computed(() => form.name !== (profile.value?.nomeUser || ''))
const canSubmit = computed(() => isDirty.value && !mutationLoading.value && hasLengthBetween(form.name, 2, 100))

watch(
  profile,
  (value) => {
    if (value) {
      form.name = value.nomeUser || ''
    }
  },
  { immediate: true },
)

watch(
  () => form.name,
  () => {
    formError.value = ''
  },
)

const handleSubmit = async () => {
  formError.value = ''
  if (!isValidName(form.name)) {
    formError.value = 'Informe um nome entre 2 e 100 caracteres.'
    return
  }

  const updated = await updateProfile({ name: form.name })
  if (updated) {
    toast({
      title: 'Perfil atualizado',
      description: 'Suas informações foram salvas com sucesso.',
    })

    if (auth.user.value) {
      auth.user.value = {
        ...auth.user.value,
        usuario: updated.nomeUser,
      }
    }
  }
}

const handleDelete = async () => {
  const deleted = await deleteProfile()
  if (deleted) {
    toast({
      variant: 'destructive',
      title: 'Conta removida',
      description: 'Sua conta foi deletada. Até breve!',
    })
    await auth.logout()
  }
}

onMounted(fetchProfile)
</script>

<template>
  <div class="flex flex-1 flex-col gap-6">
    <div class="grid gap-6 lg:grid-cols-[2fr,1fr]">
      <div class="rounded-2xl border bg-card p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-semibold">Informações pessoais</h1>
            <p class="text-sm text-muted-foreground">
              Atualize o nome exibido em toda a plataforma.
            </p>
          </div>
          <span
            class="inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold capitalize"
            :class="roleBadgeClass"
          >
            {{ roleLabel }}
          </span>
        </div>

        <div v-if="loading" class="mt-6 space-y-4">
          <Skeleton class="h-10 w-full" />
          <Skeleton class="h-10 w-full" />
          <Skeleton class="h-10 w-32" />
        </div>

        <form v-else class="mt-6 space-y-4" @submit.prevent="handleSubmit">
          <div class="space-y-2">
            <Label for="name">Nome completo</Label>
            <Input
              id="name"
              v-model="form.name"
              type="text"
              placeholder="Seu nome"
              :disabled="mutationLoading"
              :maxlength="100"
              autocapitalize="words"
            />
            <p v-if="formError" class="text-sm text-red-500">
              {{ formError }}
            </p>
          </div>

          <div class="space-y-2">
            <Label for="email">E-mail</Label>
            <Input
              id="email"
              type="email"
              :value="email"
              disabled
            />
          </div>

          <div class="flex items-center gap-3 pt-2">
            <Button type="submit" :disabled="!canSubmit">
              {{ mutationLoading ? 'Salvando...' : 'Salvar alterações' }}
            </Button>
            <Button type="button" variant="ghost" :disabled="mutationLoading" @click="fetchProfile">
              Recarregar dados
            </Button>
          </div>

          <p v-if="error" class="text-sm text-destructive">
            {{ error.message }}
          </p>
        </form>
      </div>

      <div class="rounded-2xl border bg-card p-6 shadow-sm space-y-4">
        <div>
          <h3 class="text-base font-semibold">Registro</h3>
          <p class="text-sm text-muted-foreground">
            Informações fornecidas pelo backend.
          </p>
        </div>
        <div class="space-y-3 text-sm">
          <div>
            <p class="text-xs uppercase text-muted-foreground">Criado em</p>
            <p class="font-medium">
              {{ profile?.createdAt ? formatDateTime(profile.createdAt) : '—' }}
            </p>
          </div>
          <div>
            <p class="text-xs uppercase text-muted-foreground">Atualizado em</p>
            <p class="font-medium">
              {{ profile?.updatedAt ? formatDateTime(profile.updatedAt) : '—' }}
            </p>
          </div>
          <div>
            <p class="text-xs uppercase text-muted-foreground">Auth ID</p>
            <p class="font-mono text-xs">
              {{ profile?.authId || '—' }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <div class="rounded-2xl border bg-card p-6 shadow-sm">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-xl font-semibold">Segurança</h2>
          <p class="text-sm text-muted-foreground">
            Exclua permanentemente sua conta. Esta ação não pode ser desfeita.
          </p>
        </div>
        <Button
          variant="destructive"
          type="button"
          :disabled="mutationLoading"
          @click="showDeleteConfirm = !showDeleteConfirm"
        >
          {{ showDeleteConfirm ? 'Cancelar' : 'Excluir conta' }}
        </Button>
      </div>

      <div
        v-if="showDeleteConfirm"
        class="mt-6 rounded-lg border border-destructive/40 bg-destructive/5 p-4 text-sm"
      >
        <p class="font-semibold text-destructive">
          Tem certeza? Essa ação apagará todos os seus dados.
        </p>
        <p class="text-muted-foreground mt-2">
          Digite seu nome para confirmar e clique em "Confirmar exclusão".
        </p>
        <div class="mt-4 flex flex-wrap gap-3">
          <Button variant="outline" type="button" @click="showDeleteConfirm = false">
            Voltar
          </Button>
          <Button
            variant="destructive"
            type="button"
            :disabled="mutationLoading"
            @click="handleDelete"
          >
            {{ mutationLoading ? 'Removendo...' : 'Confirmar exclusão' }}
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>
