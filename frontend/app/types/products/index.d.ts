export type ProductStatus = 'active' | 'inactive'

export interface ProductEntity {
  idProduct: number
  nomeProduct: string
  slugProduct: string
  descricaoProduct: string
  imagemurlProduct: string
  categoriaProduct: string
  ativo: boolean
  createdAt: string
  updatedAt?: string
}

export type ProductResponse = ProductEntity

export interface ProductPayload {
  nomeProduct: string
  slugProduct: string
  descricaoProduct: string
  imagemurlProduct: string
  categoriaProduct: string
  ativo: boolean
}

export interface CreateProductPayload extends ProductPayload {
  initialStock: number
}

export type UpdateProductPayload = ProductPayload

export interface ProductListFilters {
  search?: string
  category?: string
  status?: ProductStatus | 'all'
}
