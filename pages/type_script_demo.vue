<template>
  <div>
    hello ts

    <ul>
      <li v-for="elem in testData" :key="elem.uuid" v-text="elem.name"/>
    </ul>

  </div>
</template>

<script lang="ts">

import { Context } from '@nuxt/types'
import {
  Component,
  Inject,
  Model,
  Prop,
  Provide,
  Vue,
  Watch,
} from 'nuxt-property-decorator'

@Component({
  name: 'OverriddenComponentName4th',
  components: {

  }
})
export default class TypeScriptDemo extends Vue {

  private testData : { name:string, uuid : string }[] = []

  @Watch('testData', { immediate: true })
  private kek () : void {
    console.log('changed')
  }

  private async asyncData (context : Context) : Promise<any> {
    return {
      testData : await context.$axios.$get('/api/test')
    }
  }
}

</script>
