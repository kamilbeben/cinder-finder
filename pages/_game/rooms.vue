<template>
  <div class="page-default">
    
  </div>
</template>


<script lang="ts">

import GameAwarePageMixin from '~/mixin/GameAwarePageMixin'
import { Component, mixins, Ref, Vue } from 'nuxt-property-decorator'

import LocationPicker from '~/components/LocationPicker.vue'

import RuleFactory from '~/service/RuleFactory'
import { Context } from '@nuxt/types'

@Component({
  name: 'ListRoomsPage',
  components: {
    LocationPicker
  }
})
export default class ListRoomsPage extends mixins(GameAwarePageMixin) {

  private readonly RuleFactory = new RuleFactory(this)

  private async asyncData (context : Context) : Promise<any> {
    return {
      
    }
  }

  private cancel () : void {
    this.$router.push(this.game ? `/${this.lowercaseGame}` : '/')
  }

}
</script>
