
import I18nEN from './i18n/en'

export default {
  // Global page headers: https://go.nuxtjs.dev/config-head
  head: {
    title: 'Furled Finger',
    meta: [
      { charset: 'utf-8' },
      { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      { hid: 'description', name: 'description', content: '' },
      { name: 'format-detection', content: 'telephone=no' }
    ],
    link: [
      { rel: 'icon', type: 'image/svg+xml', href: '/favicon.svg' }
    ]
  },

  // Global CSS: https://go.nuxtjs.dev/config-css
  css: [
  ],

  // Plugins to run before rendering page: https://go.nuxtjs.dev/config-plugins
  plugins: [
    '~/plugins/ApiSessionIdSettingAxiosInterceptor',
    '~/plugins/SeleniumEventTriggeringAxiosInterceptor',
    '~/plugins/ErrorPageRedirectingInterceptor'
  ],

  // Auto import components: https://go.nuxtjs.dev/config-components
  components: true,

  // Modules for dev and build (recommended): https://go.nuxtjs.dev/config-modules
  buildModules: [
    // https://go.nuxtjs.dev/vuetify
    '@nuxtjs/vuetify',
    // https://typescript.nuxtjs.org/guide/setup
    '@nuxt/typescript-build',
    // https://google-fonts.nuxtjs.org/setup
    '@nuxtjs/google-fonts'
  ],

  // Modules: https://go.nuxtjs.dev/config-modules
  modules: [
    // https://go.nuxtjs.dev/axios
    '@nuxtjs/axios',
    // https://go.nuxtjs.dev/pwa
    '@nuxtjs/pwa',
    '@nuxtjs/i18n',
    '@nuxtjs/toast',
    'cookie-universal-nuxt',
    'nuxt-device-detect'
  ],

  i18n: {
    locales: [ 'en' ],
    defaultLocale: 'en',
    vueI18n: {
      fallbackLocale: 'en',
      messages: {
        en: I18nEN
      }
    }
  },

  // Axios module configuration: https://go.nuxtjs.dev/config-axios, https://axios.nuxtjs.org/options
  axios: {
    proxy: true,
    proxyHeaders: true,
    debug: !true,
    credentials: true
  },

  proxy: {
    '/api/': 'http://localhost:8001'
  },

  publicRuntimeConfig: {
    proxy: {
      '/api/': process.env.EE_MATCHMAKER_SERVER_URL
    }
  },

  toast: {
    position: 'bottom-left',
    className: 'toast-message',
    duration: 5000
  },

  // PWA module configuration: https://go.nuxtjs.dev/pwa
  pwa: {
    manifest: {
      lang: 'en'
    }
  },

  // Vuetify module configuration: https://go.nuxtjs.dev/config-vuetify
  vuetify: {
    optionsPath: './vuetify.options.js'
  },

  googleFonts: {
    prefetch: true,
    preconnect: true,
    preload: true,
    overwriting: false,
    families: {
      'Cinzel': {
        wght: [ 600 ]    
      },
      'Open+Sans': true
    }
  },

  // Build Configuration: https://go.nuxtjs.dev/config-build
  build: {
  }
}
