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
      :mandatory="!!value"
    >
        <v-btn
          value="PSX"
          data-selenium-id="form-element.platform-picker.PSX"
        >
          <v-icon v-text="'mdi-sony-playstation'"/>
        </v-btn>
        <v-btn
          value="XBOX"
          data-selenium-id="form-element.platform-picker.XBOX"
        >
          <v-icon v-text="'mdi-microsoft-xbox'"/>
        </v-btn>
        <v-btn
          value="PC"
          data-selenium-id="form-element.platform-picker.PC"
        >
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
