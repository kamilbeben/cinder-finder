<template>
  <v-app
    class="main-application-container"
    :class="{
      'desktop': $device.isDesktop,
      'tablet': $device.isTablet,
      'mobile': !$device.isDesktop && !$device.isTablet
    }
    "
  >
    <!--
      height is set because it's skipping on load which was reported as a bug and then ignored
      https://github.com/vuetifyjs/vuetify/issues/5198
    -->
    <v-app-bar
      fixed
      app
      class="app-bar"
      :height="$device.isDesktopOrTablet ? 64 : 56"
    >
      <v-toolbar-title
        class="toolbar-title"
        v-text="$t('toolbar.title')"
        data-selenium-id="toolbar.title"
      />
      <v-spacer/>
    </v-app-bar>

    <v-main>
      <v-container class="nuxt-container">
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

<style scoped>

  .main-application-container .app-bar {
    display: flex;
    background-color: #062828;
    background-image: linear-gradient(45deg, #011515 0%, #0a413f 36%, #799679 60%, #011515 100%);
  }

  .main-application-container >>> .app-bar > .v-toolbar__content {
    margin: auto;
  }

  .nuxt-container {
    min-height: 100%;
    display: flex;
    flex-direction: column;
  }

  .toolbar-title {
    text-shadow: 0 0 .25rem #eaf67e;
    color: #fcfef6;
    overflow: visible;
    font-family: 'Cinzel', serif;
    font-weight: bold;
  }

</style>
