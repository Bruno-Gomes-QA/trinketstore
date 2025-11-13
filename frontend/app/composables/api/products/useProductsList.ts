import { computed, readonly } from 'vue'
import type { ApiError } from '~/types/core/api'
import type { ProductEntity, ProductListFilters } from '~/types/products'

export const useProductsList = () => {
  const products = useState<ProductEntity[]>('products:list:data', () => [])
  const loading = useState('products:list:loading', () => false)
  const error = useState<ApiError | null>('products:list:error', () => null)
  const filtersState = useState<ProductListFilters>('products:list:filters', () => ({}))
  const { normalizeApiError } = useErrorHandler()

  const fetchProducts = async (filters?: ProductListFilters) => {
    loading.value = true
    error.value = null
    const appliedFilters = filters ?? filtersState.value
    filtersState.value = appliedFilters

    try {
      const data = await useBackendFetchDirect<ProductEntity[]>('/products')
      products.value = data
    } catch (err) {
      error.value = normalizeApiError(err)
    } finally {
      loading.value = false
    }
  }

  const filteredProducts = computed(() => {
    return products.value.filter((product) => {
      const { search, category, status } = filtersState.value
      const matchesSearch = search
        ? product.nomeProduct.toLowerCase().includes(search.toLowerCase())
        : true
      const matchesCategory = category && category !== 'all'
        ? product.categoriaProduct === category
        : true
      const matchesStatus = status && status !== 'all'
        ? status === 'active'
          ? product.ativo
          : !product.ativo
        : true

      return matchesSearch && matchesCategory && matchesStatus
    })
  })

  return {
    products: readonly(products),
    filteredProducts: readonly(filteredProducts),
    loading: readonly(loading),
    error: readonly(error),
    filters: readonly(filtersState),
    fetchProducts,
    setFilters: (filters: ProductListFilters) => {
      filtersState.value = filters
    },
  }
}
