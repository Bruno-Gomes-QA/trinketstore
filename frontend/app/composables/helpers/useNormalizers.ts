export const useNormalizers = () => {
  const trimAndCollapse = (value?: string | null): string => {
    if (!value) return ''
    return value.trim().replace(/\s+/g, ' ')
  }

  const toSlug = (value?: string | null): string => {
    if (!value) return ''
    return value
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '')
  }

  const onlyDigits = (value?: string | null): string => {
    if (!value) return ''
    return value.replace(/\D/g, '')
  }

  const toCurrencyCents = (value?: string | number | null): number => {
    if (typeof value === 'number') {
      return Math.max(0, Math.round(value))
    }

    const digits = onlyDigits(value)
    if (!digits) return 0
    return parseInt(digits, 10)
  }

  const sanitizeUrl = (value?: string | null): string => {
    if (!value) return ''
    return value.trim()
  }

  return {
    trimAndCollapse,
    toSlug,
    onlyDigits,
    toCurrencyCents,
    sanitizeUrl,
  }
}
