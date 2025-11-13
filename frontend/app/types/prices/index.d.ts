export interface PriceEntity {
  idPrice: number
  productId: number
  amountPrice: number
  currencyPrice: string
  vigentePrice: boolean
  productName?: string
  createdAt?: string
}

export type PriceResponse = PriceEntity

export interface PricePayload {
  productId: number
  amountPrice: number
  currencyPrice: string
  vigentePrice: boolean
}
