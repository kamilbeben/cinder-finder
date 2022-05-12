<template>
  <v-autocomplete
    :value="value"
    @input="val => $emit('input', val)"
    :label="$t('create-room.form.location')"
    :items="locations"
    class="locations-autocomplete"
    item-text="name"
    item-value="id"
  >
    <template v-slot:selection="data">
      <div
        class="location-selection"
        v-text="`${data.item.name} (${data.item.groupName})`"
      />
    </template>
    <template v-slot:item="data">
      <div class="location">
        <div
          class="icon"
          :title="$t('location-type.' + data.item.type)"
        >
          <v-icon
            v-if="data.item.type === 'BOSS'"
            v-text="'mdi-ladybug'"
          />
        </div>
        <div class="name-and-group-name">
          <div
            class="name"
            v-text="data.item.name"
          />
          <div
            class="group-name"
            v-text="data.item.groupName"
          />
        </div>
      </div>
    </template>
  </v-autocomplete>
</template>

<script lang="ts">

import { Component, Prop, Vue } from 'nuxt-property-decorator'

import Location, { LocationId } from '~/domain/Location'
import Game from '~/domain/Game'
import GameToLocations from '~/static/GameToLocations'

@Component({
  name: 'LocationPicker'
})
export default class LocationPicker extends Vue {

  @Prop()
  private readonly value !: LocationId

  @Prop({ type: String, required: true })
  private readonly game !: Game

  private get locations () : Location[] {
    return GameToLocations.get(this.game)!
  }

}
</script>

<style scoped>

  .location {
    display: flex;
  }

  .location > .icon {
    display: flex;
    width: 2em;
  }

  .location > .icon > i {
    margin: auto;
  }

  .location > .name-and-group-name {
    display: flex;
    flex-direction: column;
  }

  .location > .name-and-group-name > .group-name {
    font-size: .9em;
    opacity: .7;
  }

</style>
