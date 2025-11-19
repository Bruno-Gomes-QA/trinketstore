// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },

  modules: [
    '@nuxt/eslint',
    '@nuxt/fonts',
    '@nuxt/icon',
    '@nuxt/image',
    '@nuxt/scripts',
    '@nuxt/test-utils',
    '@nuxtjs/tailwindcss',
    'shadcn-nuxt',
    'nuxt-auth-utils',
  ],
  
  shadcn: {
    /**
     * Prefix for all the imported component
     */
    prefix: '',
    /**
     * Directory that the component lives in.
     * @default "./components/ui"
     */
    componentDir: './app/components/ui'
  },

  runtimeConfig: {
    //(server-side) NUXT_BACKEND_URL
    backendUrl: `${process.env.NUXT_BACKEND_URL}`,
    
    //(client-side) NUXT_PUBLIC_BACKEND_URL
    public: {
      backendUrl: `${process.env.NUXT_PUBLIC_BACKEND_URL}`,
      supabaseUrl: `${process.env.NUXT_PUBLIC_SUPABASE_URL || ''}`,
      supabaseAnonKey: `${process.env.NUXT_PUBLIC_SUPABASE_ANON_KEY || ''}`,
    },
  },
})
