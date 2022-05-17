<template>
  <div class="page-default">
    <div 
      class="mx-auto my-auto d-flex flex-column"
    >
      <v-icon
        class="mx-auto text-h1"
        v-text="'mdi-emoticon-sad'"
      />
      <div
        class="mx-auto mt-4 text-h7"
        v-text="$t('error.unknown')"
      />
      <a @click="() => $router.back()" v-text="$t('room.go-back')"/>
      
    </div>
  </div>
</template>

<script lang="ts">

import { Component, Vue } from 'nuxt-property-decorator'

@Component({
  name: 'ErrorPage'
})
export default class ErrorPage extends Vue {

}
</script>
