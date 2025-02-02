<template>
  <div class="page-default">
    <v-form
      v-if="game"
      v-model="formIsValid"
      class="form"
      ref="form"
    >
      
      <platform-picker
        v-model="platform"
      />

      <v-messages
        class="mt-2"
        color="error"
        :value="paltformErrorMessages"
      />

      <room-type-picker
        v-model="roomType"
      />

      <v-messages
        class="mt-2"
        color="error"
        :value="roomTypeErrorMessages"
      />

      <location-picker
        v-model="roomDraft.locationId"
        :game="game"
        is-required
      />

      <v-text-field
        v-model="roomDraft.name"
        :label="$t('common.room-name')"
        :rules="[
          RuleFactory.isRequired(),
          RuleFactory.maxLength(50)
        ]"
        counter="50"
        data-selenium-id="form-element.room-name"
      />

      <v-text-field
        v-model="roomDraft.description"
        :label="$t('create-room.description')"
        :rules="[
          RuleFactory.maxLength(150)
        ]"
        counter="150"
        data-selenium-id="form-element.room-description"
      />

      <v-text-field
        :value="roomDraft.password"
        @input="onPasswordInput"
        :label="$t('common.password')"
        :rules="[
          RuleFactory.isRequired(),
          RuleFactory.maxLength(10),
          RuleFactory.minLength(3)
        ]"
        counter="10"
        data-selenium-id="form-element.password"
      />

      <v-text-field
        v-model="user.inGameName"
        :label="$t('create-room.in-game-name')"
        :rules="[
          RuleFactory.isRequired(),
          RuleFactory.maxLength(50),
          RuleFactory.minLength(3)
        ]"
        counter="50"
        data-selenium-id="form-element.in-game-name"
      />

      <v-text-field
        v-model="roomDraft.hostLevel"
        :label="$t('common.host-level-field')"
        type="number"
        :rules="[
          RuleFactory.max(9999),
          RuleFactory.min(1)
        ]"
        data-selenium-id="form-element.host-level"
      />
    </v-form>

    <div class="mt-4 d-flex action-buttons">
      <v-btn
        class="flex-auto"
        color="blue darken-3"
        :loading="submitActionIsInProgress"
        dark
        @click="submit"
        data-selenium-id="form-action.submit"
      >
        {{ $t('create-room.submit') }}
      </v-btn>

      <v-btn
        class="ml-2"
        @click="cancel"
        data-selenium-id="form-action.cancel"
      >
        <v-icon v-text="'mdi-arrow-left'"/>
      </v-btn>
    </div>
  </div>
</template>


<script lang="ts">

import GameAwarePageMixin from '~/mixin/GameAwarePageMixin'
import LoggedUserAwarePageMixin from '~/mixin/LoggedUserAwarePageMixin'

import { Component, mixins, Ref, Vue } from 'nuxt-property-decorator'

import LocationPicker from '~/components/LocationPicker.vue'
import PlatformPicker from '~/components/PlatformPicker.vue'
import RoomTypePicker from '~/components/RoomTypePicker.vue'

import RuleFactory from '~/service/RuleFactory'

import Platform from '~/domain/Platform'
import RoomDraft from '~/domain/RoomDraft'
import RoomType from '~/domain/RoomType'
import User from '~/domain/User'

const STATE_LOCAL_STORAGE_KEY = 'CreateRoomState'

@Component({
  name: 'CreateRoomPage',
  components: {
    LocationPicker,
    PlatformPicker,
    RoomTypePicker
  }
})
export default class CreateRoomPage extends mixins(GameAwarePageMixin, LoggedUserAwarePageMixin) {

  private readonly RuleFactory = new RuleFactory(this)

  @Ref('form')
  private readonly formRef !: Vue

  private roomDraft : RoomDraft = {  
    game: undefined,
    platform: undefined,
    type: undefined,
    name: undefined,
    description: undefined,
    password: undefined,
    locationId: undefined
  }
  
  private formIsValid : boolean = false
  private submitActionIsInProgress : boolean = false
  private paltformErrorMessages : string[] = []
  private roomTypeErrorMessages : string[] = []

  private get platform () : Platform | undefined {
    return this.roomDraft.platform
  }

  private set platform (value : Platform | undefined) {
    if (value) {
      this.paltformErrorMessages = []
      this.user!.lastSelectedPlatform = value
    }

    this.roomDraft.platform = value
    this.persistState()
  }

  private get roomType () : RoomType | undefined {
    return this.roomDraft.type
  }

  private set roomType (value : RoomType | undefined) {
    if (value)
      this.roomTypeErrorMessages = []

    this.roomDraft.type = value
    this.persistState()
  }

  private onPasswordInput (value : string) : void {
    this.roomDraft.password = value
    this.persistState()
  }

  private cancel () : void {
    this.$router.push(`/${this.lowercaseGame}`)
  }

  private async submit () : Promise<void> {

    // @ts-ignore
    this.formRef.validate()

    let formAndCustomComponentsAreValid = this.formIsValid

    if (!this.roomDraft.platform) {
      this.paltformErrorMessages = [ <string> this.$t('validation-rule.field-is-required') ]
      formAndCustomComponentsAreValid = false
    }

    if (!this.roomDraft.type) {
      this.roomTypeErrorMessages = [ <string> this.$t('validation-rule.field-is-required') ]
      formAndCustomComponentsAreValid = false
    }

    if (!formAndCustomComponentsAreValid)
      return
    
    this.submitActionIsInProgress = true
    try {
      this.roomDraft.game = this.game!;
      const roomId : number = await this.$axios.$post<number>('/api/room/create_returning_id', this.roomDraft)
      this.$router.push(`/${this.lowercaseGame}/room/${roomId}`)
    } catch (error) {
      console.error('Couldn\'t create room', error)
      this.$nuxt.$toast.error(<string> this.$t('error.network'))
    } finally {
      this.submitActionIsInProgress = false
    }
  }

  private persistState () : void {
    if (process.server)
      return

    localStorage[STATE_LOCAL_STORAGE_KEY] = JSON.stringify({
      platform: this.roomDraft.platform,
      type: this.roomDraft.type,
      password: this.roomDraft.password
    })
  }

  private restoreState () : void {
    const stringifiedLocalStorageState : string = localStorage[STATE_LOCAL_STORAGE_KEY]

    if (!stringifiedLocalStorageState)
      return

    try {
      const localStorageState : RoomDraft = JSON.parse(stringifiedLocalStorageState)

      Object.entries(localStorageState)
        .filter(([key, value]) => this.roomDraft.hasOwnProperty(key))
        .forEach(([key, value]) => this.$set(this.roomDraft, key, value))

    } catch (error) {
      delete localStorage[STATE_LOCAL_STORAGE_KEY]
      console.error('Couldn\'t restore persisted state', error)
    }
  }

  protected mounted () : void {
    this.restoreState()
  }

}
</script>
