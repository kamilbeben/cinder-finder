<template>
  <v-app xdark>

    <v-navigation-drawer
      v-model="drawerIsVisible"
      fixed
      app
    >
      <v-list>
        <v-list-item
          v-for="(drawerItem, index) in drawerItems"
          :key="index"
          :to="drawerItem.to"
          router
          exact
        >
          <v-list-item-action>
            <v-icon>{{ drawerItem.icon }}</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title v-text="drawerItem.title" />
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-app-bar
      fixed
      app
    >
      
      <v-app-bar-nav-icon
        @click.stop="drawerIsVisible = !drawerIsVisible"
      />
      
      <v-toolbar-title
        v-text="title"
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

type DrawerItem = {
  icon : string
  title : string
  to ?: string
}

@Component({
  name: 'DefaultLayout'
})
export default class DefaultLayout extends Vue {

  private drawerIsVisible : boolean = false
  private drawerItems : DrawerItem[] = [
    {
      icon: 'mdi-menu',
      title: 'Demo',
      to: '/type_script_demo'
    }
  ]

  private title : string = this.$tc('toolbar.title')

  private mounted () {
    const previousWindowOnFocus = window.onfocus

    window.onfocus = function () {
      if (previousWindowOnFocus)
        // @ts-ignore
        previousWindowOnFocus.call(this, arguments)

      document.dispatchEvent(new CustomEvent('window.onfocus'))
    }
  }

}
</script>
