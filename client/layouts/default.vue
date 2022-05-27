<template>
  <v-app
    dark
    class="main-application-container"
    :class="
        $device.isDesktop ? 'desktop'
      : $device.isTablet  ? 'tablet'
      :                     'mobile'
    "
  >

    <v-app-bar
      fixed
      app
    >
      <v-toolbar-title v-text="$t('toolbar.title')"/>
      <v-spacer/>
    </v-app-bar>

    <v-main>
      <v-container fill-height>
        <nuxt/>
      </v-container>
    </v-main>

  </v-app>
</template>

<script lang="ts">

import { Component, Vue } from 'nuxt-property-decorator'

import '~/static/main.css'

@Component({
  name: 'DefaultLayout'
})
export default class DefaultLayout extends Vue {

  private setupWindowOnFocusEventListener () : void {
    const previousWindowOnFocus = window.onfocus

    window.onfocus = function () {
      if (previousWindowOnFocus)
        // @ts-ignore
        previousWindowOnFocus.call(this, arguments)

      document.dispatchEvent(new CustomEvent('window.onfocus'))
    }
  }

  private mounted () {
    this.setupWindowOnFocusEventListener()
  }

}
</script>
