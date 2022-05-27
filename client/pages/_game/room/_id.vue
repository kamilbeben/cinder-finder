<template>
  <div
    v-if="!room"
    class="page-default"
  >
    <div 
      class="not-found mx-auto my-auto d-flex flex-column"
    >
      <v-icon
        class="mx-auto text-h1"
        v-text="'mdi-emoticon-sad'"
      />
      <div
        class="mx-auto mt-4 text-h7"
        v-text="$t('room.not-found')"
      />
      <a @click="() => $router.back()" v-text="$t('room.go-back')"/>
      
    </div>
  </div>
  <div
    v-else
    class="page-default"
  >

    <div
      class="room-name"
      v-text="room.name"
    />

    <div
      class="muted"
      v-text="room.description"
    />

    <div
      class="muted mt-4"
      v-text="$t('common.location')"
    />
    <a
      class="location d-flex"
      :href="locationGoogleSearchHref"
      target="_blank"
    >
      <v-icon
        class="my-auto"
        v-text="'mdi-google-downasaur'"
      />
      <div
        class="location-name ml-2 my-auto"
        v-text="`${location.name} (${location.groupName})`"
      />
      <v-icon
        class="my-auto ml-2"
        v-text="'mdi-search-web'"
      />
    </a>
    
    <div
      class="muted mt-4"
      v-text="$t('common.password')"
    />
    <div
      class="passowrd"
      v-text="room.password"
    />

    <div class="lists-wrapper">
      <div
        class="muted mt-4"
        v-text="$t('room.members')"
      />

      <div class="list">
        <div
          v-if="members.length === 0"
          class="muted px-4 py-2"
          v-text="$t('room.members-placeholder')"
        />
        <div
          v-for="member in members"
          :key="member.userName"
          class="member d-flex"
        >
          <v-icon
            :color="
              member.isOnline
                ? 'green'
                : 'red'
            "
            :title="$t(
              room.host.userName === member.userName
                ? 'room.host'
                : 'room.guest'
            )"
            v-text="
              room.host.userName === member.userName
                ? 'mdi-crown'
                : 'mdi-web'
            "
          />
          <div class="d-flex ml-2 flex-column py-1">
            <div
              class="in-game-name mt-auto"
              :class="{
                'unset': !member.inGameName
              }"
              v-text="
                (
                  member.inGameName ||
                  $t('room.in-game-name-placeholder')
                ) +
                (
                  room.host.userName === member.userName && room.hostLevel
                    ? ` (${$t('common.host-level-value', { value: room.hostLevel })})`
                    : ''
                )
              "
            />
            <div
              class="muted"
              v-text="'@' + member.userName"
            />
          </div>
          <v-btn
            v-if="false && roomIsOwnedByLoggedUser && room.host.userName !== member.userName"
            color="error"
            class="ml-auto"
            :title="$t('room.kick')"
            @click="() => kickUser(member)"
          >
            <v-icon v-text="'mdi-karate'"/>
          </v-btn>
        </div>
      </div>

      <div
        class="muted mt-4"
        v-text="$t('room.messages')"
      />
      <chat
        :messages="room.messages"
        :room-id="room.id"
        class="chat"
      />
    </div>

    <div class="mt-4 d-flex">
      <v-btn
        class="ml-auto"
        color="error"
        @click="leave"
      >
        {{ $t(roomIsOwnedByLoggedUser ? 'room.close' : 'room.leave') }}
      </v-btn>
    </div>

  </div>
</template>

<script lang="ts">

import GameAwarePageMixin from '~/mixin/GameAwarePageMixin'
import LoggedUserAwarePageMixin, { asyncData as LoggedUserAwarePageMixinAsyncData } from '~/mixin/LoggedUserAwarePageMixin'
import WindowFocusAwareMixin from '~/mixin/WindowFocusAwareMixin'

import { Component, mixins } from 'nuxt-property-decorator'
import { Context } from '@nuxt/types'

import Location from '~/domain/Location'
import GameToLocations from '~/static/GameToLocations'

import Chat from '~/components/Chat.vue'
import LongPoller from '~/service/LongPoller'
import LongPollingEvent, { Type as LongPollingEventType } from '~/domain/LongPollingEvent'

import RoomDetails from '~/domain/RoomDetails'
import User from '~/domain/User'
import RoomMember from '~/domain/RoomMember'

@Component({
  name: 'RoomDetailsPage',
  components: {
    Chat
  }
})
export default class RoomDetailsPage extends mixins(GameAwarePageMixin, LoggedUserAwarePageMixin, WindowFocusAwareMixin) {

  private longPoller ?: LongPoller<LongPollingEvent<User | any>[]> = undefined
  private room ?: RoomDetails = undefined

  protected async asyncData (context : Context) : Promise<any> {

    return {
      ...(await LoggedUserAwarePageMixinAsyncData(context)),
      room: await context.$axios.$post<RoomDetails>(`/api/room/register_to_and_get_details?id=${context.params.id}`)
    }
  }

  private get roomIsOwnedByLoggedUser () : boolean {
    return this.user?.userName === this.room?.host.userName
  }

  private get members () : RoomMember[] {
    return [
      this.room?.host!,
      ...this.room?.guests!
    ]
      .filter(member => member.userName !== this.user?.userName)
  }

  private get location () : Location {
    return this.locations.find(location => location.id === this.room!.locationId)!
  }

  private get locationGoogleSearchHref () : string {
    const queryText = `${this.lowercaseGame.replace(/_/g, ' ')} ${this.location.groupName} ${this.location.name}`
    return `https://www.google.com/search?q=${encodeURIComponent(queryText)}`
  }

  private get locations () : Location[] {
    return GameToLocations.get(this.game!)!
  }

  private async kickUser (member : User) : Promise<void> {
    try {
      await this.$axios.$delete<void>(`/api/room/owned_by_current_user/kick_guest?guest_user_name=${encodeURIComponent(member.userName)}`)
      this.room!.guests = this.room!.guests
        .filter(guest => guest.userName !== member.userName)
    } catch (error) {
      console.error('kickUser', error)
    }
  }

  private leave (reason ?: string) : void {

    if (reason)
      this.$nuxt.$toast.show(reason!, {
        type: 'error',
        position: 'top-center'
      })

    this.$router.push(
      this.lowercaseGame
        ? `/${this.lowercaseGame}/${
            this.roomIsOwnedByLoggedUser
              ? 'create_room'
              : 'rooms'
          }`
        : '/'
    )
  }

  protected onWindowFocus () : void {  
    this.$axios.$post<RoomDetails>(`/api/room/register_to_and_get_details?id=${this.room!.id}`)
      .then(room => this.$set(this, 'room', room))
  }

  private consumeLongPollingEvents (events : LongPollingEvent<User | any>[]) : void {
    let
      user : User,
      message : any

    events.forEach(event => {
      switch (event.type) {
        case LongPollingEventType.ROOM_HAS_BEEN_REMOVED:
          this.leave(<string> this.$t('rooms.room-has-been-closed'))
          return
        case LongPollingEventType.CHAT_MESSAGE:
          message = event.payload
          this.room!.messages.push(message)
          return
        case LongPollingEventType.USER_HAS_JOINED:
          user = event.payload
          const userIsAlreadyAGuest = !!this.room?.guests.find(guest => guest.userName === user.userName)
          if (!userIsAlreadyAGuest)
            this.room?.guests.push({
              ...user,
              isOnline: true
            })
          return
        case LongPollingEventType.USER_HAS_LEFT:
          user = event.payload
          const indexOfUserInGuests = this.room?.guests.findIndex(guest => guest.userName === user.userName)
          if (indexOfUserInGuests !== -1)
            this.room?.guests.splice(indexOfUserInGuests!, 1)
          return
        case LongPollingEventType.USER_IS_ONLINE:
        case LongPollingEventType.USER_IS_OFFLINE:
          user = event.payload
          this.members
            .filter(member => member.userName === user.userName)
            .forEach(member => { member.isOnline = event.type === LongPollingEventType.USER_IS_ONLINE })
          return
        default:
          throw new Error('Unhandled LongPollingEventType.' + event.type)
      }
    })
  }

  private async beforeRouteLeave (to : any, from : any, next : any) : Promise<void> {
    this.longPoller?.unsubscribe()
    clearTimeout(this.pingTimeoutId)

    try {
      if (this.roomIsOwnedByLoggedUser)
        await this.$axios.$delete<void>('/api/room/owned_by_current_user/close')
      else
        await this.$axios.$delete<void>(`/api/room/leave?id=${this.room?.id}`)
    } finally {
      next()
    }
  }

  private pingTimeoutId : number = 0

  protected beforeDestroy () : void {
    this.longPoller?.unsubscribe()
    clearTimeout(this.pingTimeoutId)
  }

  private setupLongPoller () : void {
    this.longPoller = new LongPoller<LongPollingEvent<User | any>[]>(
      this.$axios,
      `/api/room/subscribe_to_event?id=${this.room!.id}`,
      events => this.consumeLongPollingEvents(events)
    )
  }

  private setupPing () : void {
    const pingIntervalInMs = 10_000

    const doPing = () => {
      if (this.room)
        this.$axios.$post<void>(`/api/room/ping?id=${this.room!.id}`)
          .finally(() => {
            this.pingTimeoutId = <any> setTimeout(doPing, pingIntervalInMs)
          })
      else
        this.pingTimeoutId = <any> setTimeout(doPing, pingIntervalInMs)
    }

    doPing()
  }

  protected mounted () : void {

    console.error('TODO client', [
      'try-catch every asyncData & redirect to custom error page or an interceptor redirecting on error if process.server',
      'custom error page 404',
      'custom error api request'
    ])

    this.setupLongPoller()
    this.setupPing()
  }

}
</script>

<style scoped>

  .room-name {
    font-size: 1.3em;
  }

  .location {
    text-decoration: none;
    color: unset;
  }

  .in-game-name {
    font-size: .9em;
  }

  .in-game-name.unset {
    opacity: .5;
  }

  .member {
    line-height: .9em;
    overflow: hidden;
  }

  .lists-wrapper {
    flex: auto;
    overflow: hidden;
  }

  .lists-wrapper > .list {
    max-height: 40%;
    overflow-y: auto;
    overflow-x: hidden;
  }
</style>

<style>

  .main-application-container.mobile .chat {
    /* prevents it to be shrinked down to a 30px line when on screen keyboard is open */
    min-height: 200px;
    background: #121212;
    z-index: 999;
  }
</style>