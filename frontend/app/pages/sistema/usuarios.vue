<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { CalendarRange, Eye, LockKeyhole, Mail, Pencil, Plus, Search, ShieldCheck, Trash2, UserRound } from 'lucide-vue-next'
import { Button } from '~/components/ui/button'
import {
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from '~/components/ui/input-group'
import {
  Field,
  FieldContent,
  FieldDescription,
  FieldError,
  FieldLabel,
  FieldSet,
} from '~/components/ui/field'
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from '~/components/ui/sheet'
import {
  Drawer,
  DrawerClose,
  DrawerContent,
  DrawerDescription,
  DrawerFooter,
  DrawerHeader,
  DrawerTitle,
} from '~/components/ui/drawer'
import {
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '~/components/ui/table'
import { Skeleton } from '~/components/ui/skeleton'
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyTitle,
} from '~/components/ui/empty'
import { useToast } from '~/components/ui/toast/use-toast'

definePageMeta({
  layout: 'sistema',
  middleware: 'auth',
  pageTitle: 'Gestão de usuários',
})

const { toast } = useToast()
const { formatDateTime } = useFormatters()
const { isValidName, isValidEmail, hasLengthBetween } = useValidators()
const { users, filteredUsers, loading, fetchUsers, setFilters } = useUsersList()
const userDetails = useUserDetails()
const userMutations = useUserMutations()

const searchTerm = ref('')
const roleFilter = ref<'all' | 'admin' | 'customer'>('all')
const emailFilter = ref<'all' | 'with' | 'without'>('all')
const recencyFilter = ref<'all' | '7' | '30'>('all')
const sheetOpen = ref(false)
const sheetMode = ref<'create' | 'edit' | 'view'>('create')
const sheetLoading = ref(false)
const deleteDrawerOpen = ref(false)
const deleteTarget = ref<{ id: number; name: string } | null>(null)

const form = reactive({
  id: null as number | null,
  name: '',
  email: '',
  password: '',
  role: 'admin' as 'admin' | 'customer',
})

const formErrors = reactive({
  name: '',
  email: '',
  password: '',
})

const sheetTitle = computed(() => {
  switch (sheetMode.value) {
    case 'create':
      return 'Criar usuário'
    case 'edit':
      return 'Editar usuário'
    case 'view':
      return 'Detalhes do usuário'
    default:
      return ''
  }
})

const filteredList = computed(() => filteredUsers.value)
const tableEmpty = computed(() => !loading.value && filteredList.value.length === 0)

const resetForm = () => {
  form.id = null
  form.name = ''
  form.email = ''
  form.password = ''
  form.role = 'admin'
  formErrors.name = ''
  formErrors.email = ''
  formErrors.password = ''
}

const openCreateSheet = () => {
  sheetMode.value = 'create'
  resetForm()
  sheetOpen.value = true
}

const openViewSheet = async (userId: number) => {
  sheetMode.value = 'view'
  sheetOpen.value = true
  sheetLoading.value = true
  await userDetails.fetchUser(userId)
  sheetLoading.value = false
}

const openEditSheet = async (userId: number) => {
  sheetMode.value = 'edit'
  sheetOpen.value = true
  sheetLoading.value = true
  await userDetails.fetchUser(userId)
  const detail = userDetails.user.value
  if (detail) {
    form.id = detail.idUser
    form.name = detail.nomeUser
    form.email = detail.email || ''
    form.role = detail.role
  }
  sheetLoading.value = false
}

const closeSheet = () => {
  sheetOpen.value = false
}

const validateForm = () => {
  let valid = true
  formErrors.name = ''
  formErrors.email = ''
  formErrors.password = ''

  if (!isValidName(form.name)) {
    formErrors.name = 'Informe um nome válido (2-100 caracteres).'
    valid = false
  }

  if (sheetMode.value === 'create') {
    if (!isValidEmail(form.email)) {
      formErrors.email = 'Informe um e-mail válido.'
      valid = false
    }

    if (!hasLengthBetween(form.password, 6, 120)) {
      formErrors.password = 'A senha deve ter ao menos 6 caracteres.'
      valid = false
    }
  }

  return valid
}

const handleSheetSubmit = async () => {
  if (!validateForm()) return

  if (sheetMode.value === 'create') {
    const created = await userMutations.createUser({
      name: form.name,
      email: form.email,
      password: form.password,
      role: form.role,
    })
    if (created) {
      toast({ title: 'Usuário criado', description: 'Novo usuário adicionado com sucesso.' })
      closeSheet()
      await fetchUsers()
    }
    return
  }

  if (sheetMode.value === 'edit' && form.id) {
    const updated = await userMutations.updateUser(form.id, {
      name: form.name,
      role: form.role,
    })
    if (updated) {
      toast({ title: 'Usuário atualizado', description: 'Os dados foram salvos.' })
      closeSheet()
      await fetchUsers()
    }
  }
}

const confirmDelete = (userId: number, name: string) => {
  deleteTarget.value = { id: userId, name }
  deleteDrawerOpen.value = true
}

const handleDelete = async () => {
  if (!deleteTarget.value) return
  const deleted = await userMutations.deleteUser(deleteTarget.value.id)
  if (deleted === null) return

  toast({
    variant: 'destructive',
    title: 'Usuário removido',
    description: `${deleteTarget.value.name} foi deletado.`,
  })
  deleteDrawerOpen.value = false
  deleteTarget.value = null
  await fetchUsers()
}

watch([searchTerm, roleFilter, emailFilter, recencyFilter], ([search, role, emailStatus, recency]) => {
  setFilters({
    search: search || undefined,
    role,
    emailStatus,
    recency,
  })
})

watch(sheetOpen, (isOpen) => {
  if (!isOpen) {
    userDetails.reset()
    resetForm()
  }
})

watch(deleteDrawerOpen, (isOpen) => {
  if (!isOpen) {
    deleteTarget.value = null
  }
})

onMounted(() => {
  setFilters({
    search: '',
    role: 'all',
    emailStatus: 'all',
    recency: 'all',
  })
  fetchUsers()
})
</script>

<template>
  <div class="flex flex-1 flex-col gap-6">
    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
      <div>
        <h1 class="text-2xl font-semibold">Usuários</h1>
        <p class="text-sm text-muted-foreground">
          Gerencie acessos administrativos e clientes.
        </p>
      </div>
      <Button type="button" @click="openCreateSheet">
        <Plus class="h-4 w-4" />
        Novo usuário
      </Button>
    </div>

    <div class="flex flex-col gap-4 rounded-2xl border bg-card p-4 shadow-sm">
      <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
        <div class="space-y-1.5">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground">Busca rápida</p>
          <InputGroup class="h-12 bg-background">
            <InputGroupAddon class="gap-2 text-muted-foreground">
              <Search class="h-4 w-4" />
            </InputGroupAddon>
            <InputGroupInput
              v-model="searchTerm"
              type="text"
              placeholder="Filtrar por nome ou e-mail"
              class="h-12"
            />
          </InputGroup>
        </div>
        <div class="space-y-1.5">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground">Perfil</p>
          <InputGroup class="h-12 bg-background">
            <InputGroupAddon class="text-muted-foreground">
              <ShieldCheck class="h-4 w-4" />
            </InputGroupAddon>
            <select
              id="role-filter"
              v-model="roleFilter"
              class="h-full flex-1 appearance-none bg-transparent pl-2 pr-8 text-sm font-medium text-foreground outline-none"
            >
              <option value="all">Todos os perfis</option>
              <option value="admin">Administradores</option>
              <option value="customer">Clientes</option>
            </select>
          </InputGroup>
        </div>
        <div class="space-y-1.5">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground">Email cadastrado</p>
          <InputGroup class="h-12 bg-background">
            <InputGroupAddon class="text-muted-foreground">
              <Mail class="h-4 w-4" />
            </InputGroupAddon>
            <select
              id="email-filter"
              v-model="emailFilter"
              class="h-full flex-1 appearance-none bg-transparent pl-2 pr-8 text-sm font-medium text-foreground outline-none"
            >
              <option value="all">Todos</option>
              <option value="with">Somente com e-mail</option>
              <option value="without">Sem e-mail</option>
            </select>
          </InputGroup>
        </div>
        <div class="space-y-1.5">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground">Criados em</p>
          <InputGroup class="h-12 bg-background">
            <InputGroupAddon class="text-muted-foreground">
              <CalendarRange class="h-4 w-4" />
            </InputGroupAddon>
            <select
              id="recency-filter"
              v-model="recencyFilter"
              class="h-full flex-1 appearance-none bg-transparent pl-2 pr-8 text-sm font-medium text-foreground outline-none"
            >
              <option value="all">Todos os períodos</option>
              <option value="7">Últimos 7 dias</option>
              <option value="30">Últimos 30 dias</option>
            </select>
          </InputGroup>
        </div>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>ID</TableHead>
            <TableHead>Nome</TableHead>
            <TableHead>Email</TableHead>
            <TableHead>Role</TableHead>
            <TableHead>Criado em</TableHead>
            <TableHead>Atualizado em</TableHead>
            <TableHead class="text-right">Ações</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <template v-if="loading">
            <TableRow v-for="index in 5" :key="index">
              <TableCell><Skeleton class="h-5 w-16" /></TableCell>
              <TableCell><Skeleton class="h-5 w-32" /></TableCell>
              <TableCell><Skeleton class="h-5 w-32" /></TableCell>
              <TableCell><Skeleton class="h-6 w-20" /></TableCell>
              <TableCell><Skeleton class="h-5 w-28" /></TableCell>
              <TableCell><Skeleton class="h-5 w-28" /></TableCell>
              <TableCell class="text-right"><Skeleton class="h-8 w-24 ml-auto" /></TableCell>
            </TableRow>
          </template>
          <template v-else-if="filteredList.length">
            <TableRow
              v-for="item in filteredList"
              :key="item.idUser"
            >
              <TableCell class="font-semibold">#{{ item.idUser }}</TableCell>
              <TableCell>{{ item.nomeUser }}</TableCell>
              <TableCell>
                <span class="text-sm text-muted-foreground">{{ item.email || '—' }}</span>
              </TableCell>
              <TableCell>
                <span
                  class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold capitalize"
                  :class="item.role === 'admin'
                    ? 'bg-emerald-100 text-emerald-900 border border-emerald-200'
                    : 'bg-slate-100 text-slate-900 border border-slate-200'"
                >
                  {{ item.role }}
                </span>
              </TableCell>
              <TableCell>{{ formatDateTime(item.createdAt) }}</TableCell>
              <TableCell>{{ formatDateTime(item.updatedAt) }}</TableCell>
              <TableCell class="text-right">
                <div class="flex justify-end gap-2">
                  <Button variant="ghost" size="icon-sm" @click="openViewSheet(item.idUser)">
                    <Eye class="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="icon-sm" @click="openEditSheet(item.idUser)">
                    <Pencil class="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="icon-sm" class="text-destructive" @click="confirmDelete(item.idUser, item.nomeUser)">
                    <Trash2 class="h-4 w-4" />
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </template>
          <TableEmpty v-else :colspan="7">
            <Empty>
              <EmptyHeader>
                <EmptyTitle>Nenhum usuário encontrado</EmptyTitle>
                <EmptyDescription>
                  Ajuste os filtros ou crie um novo usuário.
                </EmptyDescription>
              </EmptyHeader>
            </Empty>
          </TableEmpty>
        </TableBody>
      </Table>
    </div>

    <Sheet v-model:open="sheetOpen">
      <SheetContent class="w-full overflow-y-auto sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>{{ sheetTitle }}</SheetTitle>
          <SheetDescription>
            {{ sheetMode === 'view' ? 'Visualize os dados completos do usuário.' : 'Preencha os dados abaixo.' }}
          </SheetDescription>
        </SheetHeader>

        <div v-if="sheetLoading" class="mt-6 space-y-4">
          <Skeleton class="h-10 w-full" />
          <Skeleton class="h-10 w-full" />
          <Skeleton class="h-10 w-full" />
        </div>

        <div v-else class="mt-6 space-y-5">
          <template v-if="sheetMode === 'view'">
            <div class="rounded-2xl border bg-muted/30 p-4 space-y-4">
              <div class="flex items-center gap-3">
                <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-[#a4f380] to-[#80d3e4] text-stone-900">
                  <UserRound class="h-5 w-5" />
                </div>
                <div>
                  <p class="text-lg font-semibold">{{ userDetails.user.value?.nomeUser }}</p>
                  <p class="text-sm text-muted-foreground">{{ userDetails.user.value?.email || 'Sem e-mail' }}</p>
                </div>
              </div>
              <div class="grid gap-3 text-sm md:grid-cols-2">
                <div>
                  <p class="text-[11px] uppercase tracking-[0.3em] text-muted-foreground">ID</p>
                  <p class="font-semibold">#{{ userDetails.user.value?.idUser }}</p>
                </div>
                <div>
                  <p class="text-[11px] uppercase tracking-[0.3em] text-muted-foreground">Perfil</p>
                  <p class="font-semibold capitalize">{{ userDetails.user.value?.role }}</p>
                </div>
                <div>
                  <p class="text-[11px] uppercase tracking-[0.3em] text-muted-foreground">Criado em</p>
                  <p>{{ formatDateTime(userDetails.user.value?.createdAt) }}</p>
                </div>
                <div>
                  <p class="text-[11px] uppercase tracking-[0.3em] text-muted-foreground">Atualizado em</p>
                  <p>{{ formatDateTime(userDetails.user.value?.updatedAt) }}</p>
                </div>
              </div>
            </div>
          </template>

          <template v-else>
            <FieldSet class="space-y-5">
              <Field>
                <FieldLabel>
                  <UserRound class="h-4 w-4 text-[#a4f380]" />
                  Nome completo
                </FieldLabel>
                <FieldContent>
                  <InputGroup
                    class="h-12"
                    :class="formErrors.name ? 'border-destructive ring-destructive/20' : ''"
                  >
                    <InputGroupAddon class="text-muted-foreground">
                      <UserRound class="h-4 w-4" />
                    </InputGroupAddon>
                    <InputGroupInput
                      id="user-name"
                      v-model="form.name"
                      type="text"
                      placeholder="João Silva"
                      :disabled="userMutations.loading.value"
                      :aria-invalid="Boolean(formErrors.name)"
                      class="h-12"
                    />
                  </InputGroup>
                  <FieldDescription>Use o nome que aparecerá em notificações e relatórios.</FieldDescription>
                  <FieldError v-if="formErrors.name">
                    {{ formErrors.name }}
                  </FieldError>
                </FieldContent>
              </Field>

              <Field>
                <FieldLabel>
                  <ShieldCheck class="h-4 w-4 text-[#80d3e4]" />
                  Perfil de acesso
                </FieldLabel>
                <FieldContent>
                  <InputGroup class="h-12">
                    <InputGroupAddon class="text-muted-foreground">
                      <ShieldCheck class="h-4 w-4" />
                    </InputGroupAddon>
                    <select
                      id="user-role"
                      v-model="form.role"
                      class="h-full flex-1 appearance-none bg-transparent pl-2 pr-8 text-sm font-medium text-foreground outline-none"
                      :disabled="userMutations.loading.value"
                    >
                      <option value="admin">Administrador</option>
                      <option value="customer">Cliente</option>
                    </select>
                  </InputGroup>
                  <FieldDescription>Defina os privilégios exibidos no painel.</FieldDescription>
                </FieldContent>
              </Field>

              <Field v-if="sheetMode !== 'view'">
                <FieldLabel>
                  <Mail class="h-4 w-4 text-[#5dc5db]" />
                  E-mail de acesso
                </FieldLabel>
                <FieldContent>
                  <InputGroup
                    class="h-12"
                    :class="formErrors.email ? 'border-destructive ring-destructive/20' : ''"
                  >
                    <InputGroupAddon class="text-muted-foreground">
                      <Mail class="h-4 w-4" />
                    </InputGroupAddon>
                  <InputGroupInput
                    id="user-email"
                    v-model="form.email"
                    type="email"
                    placeholder="admin@trinketstore.com"
                    :disabled="sheetMode !== 'create' || userMutations.loading.value"
                    :aria-invalid="Boolean(formErrors.email)"
                    class="h-12"
                  />
                </InputGroup>
                  <FieldDescription>
                    {{
                      sheetMode === 'create'
                        ? 'Usado para login e confirmações importantes.'
                        : 'O e-mail é definido no cadastro inicial e não pode ser alterado.'
                    }}
                  </FieldDescription>
                  <FieldError v-if="formErrors.email">
                    {{ formErrors.email }}
                  </FieldError>
                </FieldContent>
              </Field>

              <Field v-if="sheetMode === 'create'">
                <FieldLabel>
                  <LockKeyhole class="h-4 w-4 text-[#f6d95c]" />
                  Senha temporária
                </FieldLabel>
                <FieldContent>
                  <InputGroup
                    class="h-12"
                    :class="formErrors.password ? 'border-destructive ring-destructive/20' : ''"
                  >
                    <InputGroupAddon class="text-muted-foreground">
                      <LockKeyhole class="h-4 w-4" />
                    </InputGroupAddon>
                    <InputGroupInput
                      id="user-password"
                      v-model="form.password"
                      type="password"
                      placeholder="Mínimo de 6 caracteres"
                      :disabled="userMutations.loading.value"
                      :aria-invalid="Boolean(formErrors.password)"
                      class="h-12"
                    />
                  </InputGroup>
                  <FieldDescription>O usuário poderá alterar depois no perfil.</FieldDescription>
                  <FieldError v-if="formErrors.password">
                    {{ formErrors.password }}
                  </FieldError>
                </FieldContent>
              </Field>
            </FieldSet>
          </template>
        </div>

        <SheetFooter class="mt-6">
          <SheetClose as-child>
            <Button variant="ghost" type="button">
              Fechar
            </Button>
          </SheetClose>
          <Button
            v-if="sheetMode !== 'view'"
            type="button"
            :disabled="userMutations.loading.value"
            @click="handleSheetSubmit"
          >
            {{ userMutations.loading.value ? 'Salvando...' : 'Salvar' }}
          </Button>
        </SheetFooter>
      </SheetContent>
    </Sheet>

    <Drawer v-model:open="deleteDrawerOpen">
      <DrawerContent>
        <DrawerHeader>
          <DrawerTitle>Confirmar exclusão</DrawerTitle>
          <DrawerDescription>
            Esta ação removerá o usuário {{ deleteTarget?.name }} permanentemente.
          </DrawerDescription>
        </DrawerHeader>
        <DrawerFooter>
          <Button variant="destructive" :disabled="userMutations.loading.value" @click="handleDelete">
            {{ userMutations.loading.value ? 'Excluindo...' : 'Excluir usuário' }}
          </Button>
          <DrawerClose as-child>
            <Button variant="outline">Cancelar</Button>
          </DrawerClose>
        </DrawerFooter>
      </DrawerContent>
    </Drawer>
  </div>
</template>
