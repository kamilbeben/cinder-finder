<template>
  <div>
    <label
      class="button-group-label"
      v-text="$t(
        multiple
          ? 'game-mode.picker-label.multiple'
          : 'game-mode.picker-label.single'
      )"
    />
    <v-container
      v-if="multiple"
      class="checkbox-group"
      fluid
    >
      <v-checkbox
        :input-value="value"
        @change="val => $emit('input', val)"
        value="COOP"
        :label="$t('game-mode.co-op')"
        class="mt-0"
        data-selenium-id="form-element.room-type.COOP"
      />
      <v-checkbox
        :input-value="value"
        @change="val => $emit('input', val)"
        value="PVP"
        :label="$t('game-mode.pvp')"
        class="mt-0"
        data-selenium-id="form-element.room-type.PVP"
      />
    </v-container>
    <v-btn-toggle
      v-else
      :value="value"
      @change="val => $emit('input', val)"
      class="button-group"
    >
      <v-btn
        value="COOP"
        data-selenium-id="form-element.room-type.COOP"
      >
        <v-icon v-text="'mdi-handshake'"/>
      </v-btn>
      <v-btn
        value="PVP"
        data-selenium-id="form-element.room-type.PVP"
      >
        <v-icon v-text="'mdi-fencing'"/>
      </v-btn>
    </v-btn-toggle>
  </div>
</template>

<script lang="ts">

import { Component, Prop, Vue } from 'nuxt-property-decorator'

import RoomType from '~/domain/RoomType'

@Component({
  name: 'RoomTypePicker'
})
export default class RoomTypePicker extends Vue {

  @Prop()
  private readonly value !: RoomType 

  @Prop(Boolean)
  private readonly multiple !: boolean

}
</script>
