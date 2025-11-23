// Explicit ESM entry to avoid extension resolution errors on Nitro
import { createClient } from '@supabase/supabase-js/dist/module/index.js'

export default defineEventHandler(async (event) => {
  const body = await readBody<{ path?: string }>(event)
  const config = useRuntimeConfig()

  if (!config.public.supabaseUrl || !config.supabaseServiceKey) {
    throw createError({
      statusCode: 500,
      statusMessage: 'Supabase não configurado para upload.',
    })
  }

  const supabase = createClient(config.public.supabaseUrl, config.supabaseServiceKey)
  const targetPath = body.path || `products/upload-${Date.now()}.webp`

  const { data, error } = await supabase.storage.from('trinketstore').createSignedUploadUrl(targetPath, 60 * 5)

  if (error || !data) {
    console.error('[storage] signed url error', error)
    throw createError({
      statusCode: 500,
      statusMessage: 'Não foi possível criar URL de upload.',
    })
  }

  const { data: publicUrlData } = supabase.storage.from('trinketstore').getPublicUrl(targetPath)

  return {
    ...data,
    publicUrl: publicUrlData.publicUrl,
  }
})
