export type UserRole = 'admin' | 'customer'

export interface UserEntity {
  idUser: number
  authId: string
  nomeUser: string
  role: UserRole
  email?: string
  createdAt: string
  updatedAt: string
}

export type UserResponse = UserEntity

export interface UserListFilters {
  search?: string
  role?: UserRole | 'all'
}

export interface CreateUserPayload {
  name: string
  email: string
  password: string
  role: UserRole
}

export interface UpdateUserPayload {
  name: string
  role?: UserRole
  email?: string
}
