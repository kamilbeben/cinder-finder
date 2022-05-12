<template>
  <div class="page-default">
    <v-form
      v-if="game"
      v-model="formIsValid"
      ref="form"
    >
      
      <label
        class="button-group-label"
        v-text="$t('create-room.form.platform')"
      />
      <v-btn-toggle
        v-model="platform"
        class="button-group"
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

      <v-messages
        class="mt-2"
        color="error"
        :value="paltformErrorMessages"
      />

      <label
        class="button-group-label"
        v-text="$t('create-room.form.type')"
      />
      <v-btn-toggle
        v-model="roomType"
        class="button-group"
      >
        <v-btn value="COOP">
          <v-icon v-text="'mdi-handshake'"/>
        </v-btn>
        <v-btn value="PVP">
          <v-icon v-text="'mdi-fencing'"/>
        </v-btn>
      </v-btn-toggle>

      <v-messages
        class="mt-2"
        color="error"
        :value="roomTypeErrorMessages"
      />

      <location-picker
        v-model="roomDefinition.locationId"
        :game="game"
      />

      <v-text-field
        v-model="roomDefinition.name"
        :label="$t('create-room.form.name')"
        :rules="[
          RuleFactory.isRequired(),
          RuleFactory.maxLength(50)
        ]"
        counter="50"
      />

      <v-text-field
        v-model="roomDefinition.description"
        :label="$t('create-room.form.description')"
        :rules="[
          RuleFactory.maxLength(150)
        ]"
        counter="150"
      />

      <v-text-field
        :value="roomDefinition.password"
        @input="onPasswordInput"
        :label="$t('create-room.form.password')"
        :rules="[
          RuleFactory.isRequired(),
          RuleFactory.maxLength(10),
          RuleFactory.minLength(3)
        ]"
        counter="10"
      />

      <v-text-field
        :value="roomDefinition.hostNickname"
        @input="onHostNickNameInput"
        :label="$t('create-room.form.host-nickname')"
        :rules="[
          RuleFactory.isRequired(),
          RuleFactory.maxLength(50),
          RuleFactory.minLength(3)
        ]"
        counter="50"
      />

      <div class="mt-4 flex equal-width margin-between-sm">
        <v-btn
          color="success"
          @click="submit"
        >
          {{ $t('create-room.form.submit') }}
        </v-btn>

        <v-btn
          color="error"
          @click="cancel"
        >
          {{ $t('create-room.form.cancel') }}
        </v-btn>
      </div>

    </v-form>
  </div>
</template>


<script lang="ts">

import GameAwarePageMixin from '~/mixin/GameAwarePageMixin'
import { Component, mixins, Ref, Vue } from 'nuxt-property-decorator'

import LocationPicker from '~/components/LocationPicker.vue'

import RuleFactory from '~/service/RuleFactory'
import { Context } from '@nuxt/types'

import Platform from '~/domain/Platform'
import RoomDefinition from '~/domain/RoomDefinition'
import RoomType from '~/domain/RoomType'

const STATE_LOCAL_STORAGE_KEY = 'CreateRoomState'

@Component({
  name: 'CreateRoomPage',
  components: {
    LocationPicker
  }
})
export default class CreateRoomPage extends mixins(GameAwarePageMixin) {

  private readonly RuleFactory = new RuleFactory(this)

  @Ref('form')
  private formRef !: Vue

  private roomDefinition : RoomDefinition = {  
    platform: undefined,
    type: undefined,
    name: undefined,
    description: undefined,
    password: undefined,
    hostNickname: undefined,
    locationId: undefined
  }

  private formIsValid : boolean = false
  private paltformErrorMessages : string[] = []
  private roomTypeErrorMessages : string[] = []

  private async asyncData (context : Context) : Promise<any> {
    return {
      
    }
  }

  private get platform () : Platform | undefined {
    return this.roomDefinition.platform
  }

  private set platform (value : Platform | undefined) {
    if (value)
      this.paltformErrorMessages = []

    this.roomDefinition.platform = value
    this.persistState()
  }

  private get roomType () : RoomType | undefined {
    return this.roomDefinition.type
  }

  private set roomType (value : RoomType | undefined) {
    if (value)
      this.roomTypeErrorMessages = []

    this.roomDefinition.type = value
    this.persistState()
  }

  private onPasswordInput (value : string) : void {
    this.roomDefinition.password = value
    this.persistState()
  }

  private onHostNickNameInput (value : string) : void {
    this.roomDefinition.hostNickname = value
    this.persistState()
  }

  private cancel () : void {
    this.$router.push(this.game ? `/${this.lowercaseGame}` : '/')
  }

  private submit () : void {
    
    // @ts-ignore
    this.formRef.validate()

    if (!this.roomDefinition.platform) {
      this.paltformErrorMessages = [ <string> this.$t('validation-rule.field-is-required') ]
      this.formIsValid = false
    }

    if (!this.roomDefinition.type) {
      this.roomTypeErrorMessages = [ <string> this.$t('validation-rule.field-is-required') ]
      this.formIsValid = false
    }
  }

  private persistState () : void {
    localStorage[STATE_LOCAL_STORAGE_KEY] = JSON.stringify({
      platform: this.roomDefinition.platform,
      type: this.roomDefinition.type,
      password: this.roomDefinition.password,
      hostNickname: this.roomDefinition.hostNickname
    })
  }

  private restoreState () : void {
    const stringifiedLocalStorageState : string = localStorage[STATE_LOCAL_STORAGE_KEY]

    if (!stringifiedLocalStorageState)
      return

    try {
      const localStorageState : RoomDefinition = JSON.parse(stringifiedLocalStorageState)

      Object.entries(localStorageState)
        .filter(([key, value]) => this.roomDefinition.hasOwnProperty(key))
        .forEach(([key, value]) => this.$set(this.roomDefinition, key, value))

    } catch (error) {
      delete localStorage[STATE_LOCAL_STORAGE_KEY]
      console.error('Couldn\'t restore persisted state', error)
    }
  }

}
</script>
