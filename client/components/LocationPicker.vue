<template>
  <v-autocomplete
    :value="value"
    @input="val => $emit('input', val)"
    :label="$t('common.location')"
    :items="locations"
    :multiple="multiple"
    :chips="multiple"
    :rules="
      isRequired
        ? [RuleFactory.isRequired()]
        : []
    "
    class="locations-autocomplete"
    item-text="name"
    item-value="id"
  >
    <template v-slot:selection="data">
      <v-chip
        v-if="multiple"
        v-bind="data.attrs"
        close
        @click:close="() => deselect(data.item.id)"
        :input-value="data.selected"
      >
        <div
          class="text-truncate"
          v-text="`${data.item.name} (${data.item.groupName})`"
        />
      </v-chip>
      <div
        v-else
        class="location-selection"
        v-text="`${data.item.name} (${data.item.groupName})`"
      />
    </template>

    <template v-slot:item="data">

      <!-- type -->
      <v-list-item-avatar>
        <location-icon :type="data.item.type"/>
      </v-list-item-avatar>

      <!-- name -->
      <v-list-item-content>
        <v-list-item-title v-html="data.item.name"></v-list-item-title>
        <v-list-item-subtitle v-html="data.item.groupName"></v-list-item-subtitle>
      </v-list-item-content>

      <!-- select action -->
      <v-list-item-action
        v-if="multiple"
      >
        <v-checkbox
          :input-value="(value || []).includes(data.item.id)"
          color="deep-purple accent-4"
        />
      </v-list-item-action>
    </template>
  </v-autocomplete>
</template>

<script lang="ts">

import { Component, Prop, Vue } from 'nuxt-property-decorator'

import RuleFactory from '~/service/RuleFactory'

import LocationIcon from './LocationIcon.vue'
import Location, { LocationId } from '~/domain/Location'
import Game from '~/domain/Game'
import GameToLocations from '~/static/GameToLocations'

@Component({
  name: 'LocationPicker',
  components: {
    LocationIcon
  }
})
export default class LocationPicker extends Vue {
  
  private readonly RuleFactory = new RuleFactory(this)

  @Prop([ String, Array ])
  private readonly value !: LocationId | LocationId[]

  @Prop({ type: String, required: true })
  private readonly game !: Game

  @Prop(Boolean)
  private readonly isRequired !: boolean

  @Prop(Boolean)
  private readonly multiple !: boolean

  @Prop(Boolean)
  private readonly clearable !: boolean

  private get locations () : Location[] {
    return GameToLocations.get(this.game)!
  }

  private deselect (locationId : LocationId) : void {
    (<string[]> this.value).splice(this.value.indexOf(locationId), 1)
  }

}
</script>
