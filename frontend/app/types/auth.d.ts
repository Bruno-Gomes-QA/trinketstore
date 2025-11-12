export interface User {
  usuarioKey: number
  usuario: string
  email: string
  role: 'admin' | 'customer'
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
    role: 'admin' | 'customer'
    email?: string
    createdAt: string
    updatedAt: string
  }
}
