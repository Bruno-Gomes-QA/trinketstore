import type { ApiError } from '~/types/core/api'
import { useToast } from '~/components/ui/toast/use-toast'

interface NormalizeOptions {
  fallbackMessage?: string
}

const DEFAULT_MESSAGE = 'Ocorreu um erro inesperado. Tente novamente.'

const statusMessages: Record<number | '500', string> = {
  400: 'Verifique os dados informados e tente novamente.',
  401: 'Sua sessão expirou. Faça login novamente.',
  403: 'Você não tem permissão para executar essa ação.',
  404: 'O recurso solicitado não foi encontrado.',
  409: 'Já existe um registro com essas informações.',
  500: 'Estamos enfrentando instabilidades. Tente novamente em instantes.',
}

const statusTitles: Record<number | '500', string> = {
  400: 'Dados inválidos',
  401: 'Sessão expirada',
  403: 'Acesso negado',
  404: 'Não encontrado',
  409: 'Conflito de dados',
  500: 'Erro interno',
}

const pickStatus = (rawStatus?: number | null): number | undefined => {
  if (!rawStatus && rawStatus !== 0) return undefined
  if (rawStatus >= 500) return 500
  return rawStatus
}

const resolveStatusMessage = (status?: number, fallback = DEFAULT_MESSAGE) => {
  if (!status) return fallback
  return statusMessages[status] ?? fallback
}

const resolveStatusTitle = (status?: number) => {
  if (!status) return 'Algo deu errado'
  return statusTitles[status] ?? 'Algo deu errado'
}

const extractStatus = (error: any): number | undefined => {
  return (
    error?.statusCode ??
    error?.status ??
    error?.response?.status ??
    error?.data?.status ??
    error?.cause?.status ??
    undefined
  )
}

const extractMessage = (error: any): string | undefined => {
  return (
    error?.data?.message ??
    error?.data?.error ??
    error?.message ??
    undefined
  )
}

export const useErrorHandler = () => {
  const { toast } = useToast()

  const normalizeApiError = (error: unknown, options?: NormalizeOptions): ApiError => {
    if (
      error &&
      typeof error === 'object' &&
      'message' in error &&
      'status' in error &&
      'details' in error
    ) {
      const candidate = error as ApiError
      if (candidate.message) {
        return candidate
      }
    }

    const raw = error as Record<string, any> | null
    const detectedStatus = pickStatus(extractStatus(raw))
    const rawMessage = extractMessage(raw)
    const fallbackMessage = options?.fallbackMessage ?? resolveStatusMessage(detectedStatus)

    return {
      status: detectedStatus,
      message: rawMessage || fallbackMessage,
      details: raw?.data ?? raw,
    }
  }

  const showApiErrorToast = (error: unknown, customOptions?: { title?: string; description?: string }) => {
    const normalized = normalizeApiError(error)
    const title = customOptions?.title ?? resolveStatusTitle(normalized.status)
    const description = customOptions?.description ?? normalized.message

    toast({
      variant: 'destructive',
      title,
      description,
    })

    return normalized
  }

  return {
    normalizeApiError,
    showApiErrorToast,
  }
}
