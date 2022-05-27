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
      class="app-bar"
    >
      <v-toolbar-title
        class="toolbar-title"
        v-text="$t('toolbar.title')"
      />
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

<style scoped>

  .main-application-container .app-bar {
    display: flex;
    background-color: #062828;
    background-image: linear-gradient(45deg, #011515 0%, #0a413f 36%, #799679 60%, #011515 100%);
  }

  .main-application-container >>> .app-bar > .v-toolbar__content {
    margin: auto;
  }

  .toolbar-title {
    text-shadow: 0 0 1rem #eaf67e;
    color: #fcfef6;
    overflow: visible;
  }

</style>
