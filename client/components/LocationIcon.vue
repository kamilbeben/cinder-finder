<template>
  <v-icon v-text="iconName"/>
</template>

<script lang="ts">

import { Component, Prop, Vue } from 'nuxt-property-decorator'
import LocationType from '~/domain/LocationType'

@Component({
  name: 'LocationIcon'
})
export default class LocationIcon extends Vue {

  @Prop({ type: String, required: true })
  private readonly type !: LocationType

  private get iconName () : string {
    switch (this.type) {
      case LocationType.BOSS:
        return 'mdi-google-downasaur'
      case LocationType.BONFIRE:
        return 'mdi-fire'
      default:
        console.error(`LocationIcon :: LocationType.${this.type} is unhandled`)
        return ''
    }
  }
}
</script>