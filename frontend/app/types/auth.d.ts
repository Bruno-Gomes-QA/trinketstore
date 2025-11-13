import type { UserRole } from '~/types/users'

export interface User {
  usuarioKey: number
  usuario: string
  email: string
  role: UserRole
}

export interface LoginCredentials {
  email: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: {
    idUser: number
    authId: string
    nomeUser: string
    role: UserRole
    email?: string
    createdAt: string
    updatedAt: string
  }
}
