export const useValidators = () => {
  const isRequired = (value: unknown): boolean => {
    if (typeof value === 'number') return !Number.isNaN(value)
    if (typeof value === 'boolean') return true
    if (Array.isArray(value)) return value.length > 0
    return !!value
  }

  const hasLengthBetween = (value: string | undefined | null, min: number, max: number) => {
    if (!value) return false
    const len = value.trim().length
    return len >= min && len <= max
  }

  const isValidName = (value?: string | null, min = 2, max = 100) => {
    if (!hasLengthBetween(value, min, max)) return false
    return /^[A-Za-zÀ-ÖØ-öø-ÿ'`\s]+$/.test(value!.trim())
  }

  const isValidSlug = (value?: string | null) => {
    if (!value) return false
    return /^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(value.trim())
  }

  const isValidUrl = (value?: string | null) => {
    if (!value) return false
    try {
      const url = new URL(value)
      return !!url.protocol && !!url.host
    } catch {
      return false
    }
  }

  const isPositiveInteger = (value?: number | null) => {
    if (value === null || value === undefined) return false
    return Number.isInteger(value) && value >= 0
  }

  const isValidEmail = (value?: string | null) => {
    if (!value) return false
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim().toLowerCase())
  }

  return {
    isRequired,
    hasLengthBetween,
    isValidName,
    isValidSlug,
    isValidUrl,
    isPositiveInteger,
    isValidEmail,
  }
}
