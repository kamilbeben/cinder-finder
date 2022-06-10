
import colors from 'vuetify/es5/util/colors'

// https://github.com/nuxt-community/vuetify-module#optionspath
export default {
  customVariables: ['~/assets/variables.scss'],
  treeShake: true,
  theme: {
    dark: false,
    themes: {
      light: {
        primary: colors.blue.darken2,
        accent: colors.grey.darken3,
        secondary: colors.amber.darken3,
        info: colors.teal.lighten1,
        warning: colors.amber.base,
        error: colors.red.darken1,
        success: colors.green.darken1
      }
    }
  }
}
