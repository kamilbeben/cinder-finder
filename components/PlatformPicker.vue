<template>
  <div>
    <label
      class="button-group-label"
      v-text="$t('platform.picker-label')"
    />
    <v-btn-toggle
      :value="value"
      @change="val => $emit('input', val)"
      class="button-group"
      mandatory
    >
        <v-btn value="PSX">
          <v-icon v-text="'mdi-sony-playstation'"/>
        </v-btn>
        <v-btn value="XBOX">
          <v-icon v-text="'mdi-microsoft-xbox'"/>
        </v-btn>
        <v-btn value="PC">
          <v-icon v-text="'mdi-laptop'"/>
        </v-btn>
    </v-btn-toggle>
  </div>
</template>

<script lang="ts">

import { Component, Prop, Vue } from 'nuxt-property-decorator'

import Platform from '~/domain/Platform'

@Component({
  name: 'PlatformPicker'
})
export default class PlatformPicker extends Vue {

  @Prop()
  private readonly value !: Platform 

}
</script>
