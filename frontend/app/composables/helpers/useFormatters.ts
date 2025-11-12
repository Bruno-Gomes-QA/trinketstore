export const useFormatters = () => {
  const formatDate = (dateString?: string | null): string => {
    if (!dateString) return '-'
    
    try {
      return new Date(dateString).toLocaleDateString('pt-BR')
    } catch {
      return '-'
    }
  }

  const formatDateTime = (dateString?: string | null): string => {
    if (!dateString) return '-'
    
    try {
      return new Date(dateString).toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    } catch {
      return '-'
    }
  }

  const formatCpf = (cpf?: string | null): string => {
    if (!cpf) return '-'
    
    const cleanCpf = cpf.replace(/\D/g, '')
    
    if (cleanCpf.length !== 11) return cpf
    
    return cleanCpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
  }

  const formatCnpj = (cnpj?: string | null): string => {
    if (!cnpj) return '-'
    
    const cleanCnpj = cnpj.replace(/\D/g, '')
    
    if (cleanCnpj.length !== 14) return cnpj
    
    return cleanCnpj.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5')
  }

  const formatCpfCnpj = (cpf?: string | null, cnpj?: string | null): string => {
    if (cnpj) return formatCnpj(cnpj)
    if (cpf) return formatCpf(cpf)
    return '-'
  }

  const formatCurrency = (value?: number | null): string => {
    if (value === null || value === undefined) return 'R$ 0,00'
    
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value)
  }

  const formatPhone = (phone?: string | null): string => {
    if (!phone) return '-'
    
    const cleanPhone = phone.replace(/\D/g, '')
    
    if (cleanPhone.length === 11) {
      return cleanPhone.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3')
    }
    
    if (cleanPhone.length === 10) {
      return cleanPhone.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3')
    }
    
    return phone
  }

  return {
    formatDate,
    formatDateTime,
    formatCpf,
    formatCnpj,
    formatCpfCnpj,
    formatCurrency,
    formatPhone,
  }
}
