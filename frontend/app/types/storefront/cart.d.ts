export interface StorefrontCartItem {
  productId: number
  slug: string
  name: string
  image?: string | null
  priceInCents: number | null
  quantity: number
  availableStock: number
}

export interface StorefrontCheckoutCustomer {
  name: string
  email: string
  phone?: string
  notes?: string
}
