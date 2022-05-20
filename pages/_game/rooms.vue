<template>
  <div class="page-default">
    <platform-picker
      v-if="!user.lastSelectedPlatform"
      v-model="user.lastSelectedPlatform"
    />
    <template v-else>
      
      <div class="d-flex">
        <div
          class="muted"
          v-text="$t('rooms.available-rooms')"
        />
        <v-icon
          class="ml-auto pointer"
          v-text="'mdi-filter-menu'"
          @click="openFilterMenu"
        />
      </div>

      <div class="list">
        <div
          v-if="rooms.length === 0"
          class="muted px-4 py-2"
          v-text="$t('rooms.available-rooms-placeholder')"
        />

        <nuxt-link
          v-for="room in rooms"
          :key="room.id"
          :to="`/${lowercaseGame}/room/${room.id}`"
          class="room d-flex pointer clear-css-nuxt-link"
        >

          <!-- is host online && room type -->
          <v-icon
            size="2em"
            :color="
              room.host.isOnline
                ? 'green'
                : 'red'
            "
            v-text="
              room.type === 'COOP'
                ? 'mdi-handshake'
                : 'mdi-fencing'
            "
          />

          <div class="ml-2 d-flex flex-column flex-fill">

            <div class="d-flex">
              <!-- room name -->
              <div
                class="ml-2 my-auto"
                v-text="room.name"
              />
              <!-- host name -->
              <div
                class="my-auto ml-auto"
                v-text="room.host.inGameName || room.host.userName"
              />
            </div>

            <div class="d-flex ml-2 py-1">
              <!-- location -->
              <div
                class="mt-1 mb-auto muted d-flex"
              >
                <v-icon
                  class="my-auto"
                  v-text="'mdi-google-downasaur'"
                />
                <div
                  class="location-name ml-2 my-auto"
                  v-text="`${room.location.name} (${room.location.groupName})`"
                />
              </div>
              <!-- description -->
              <div
                class="ml-auto my-auto muted"
                v-text="room.description"
              />
            </div>

          </div>
        </nuxt-link>
      </div>

      <div class="mt-4 d-flex">
        <v-btn
          class="ml-auto"
          color="error"
          @click="() => $router.push(`/${lowercaseGame}`)"
        >
          {{ $t('room.go-back') }}
        </v-btn>
      </div>

      <v-navigation-drawer
        v-model="filterMenuIsExpanded"
        absolute
        temporary
        right
      >
        <div class="d-flex flex-column px-4 py-4">
          <platform-picker
            v-model="user.lastSelectedPlatform"
          />

          <room-type-picker
            v-model="selectedRoomTypes"
            multiple
            class="mt-4"
          />

          <location-picker
            v-model="selectedLocationIds"
            :game="game"
            clearable
            multiple
          />

          <v-text-field
            v-model="roomQuery"
            :label="$t('common.room-name')"
            counter="50"
          />

          <v-text-field
            v-model="hostQuery"
            :label="$t('common.user-name')"
            counter="50"
          />

          <v-btn
            class="mt-4"
            color="error"
            @click="clearFilter"
          >
            {{ $t('rooms.clear-filter') }}
          </v-btn>

        </div>
      </v-navigation-drawer>

    </template>
  </div>
</template>

<script lang="ts">

import GameAwarePageMixin from '~/mixin/GameAwarePageMixin'
import LoggedUserAwarePageMixin, { asyncData as LoggedUserAwarePageMixinAsyncData } from '~/mixin/LoggedUserAwarePageMixin'
import WindowFocusAwareMixin from '~/mixin/WindowFocusAwareMixin'
import { Component, mixins, Ref, Vue, Watch } from 'nuxt-property-decorator'

import PlatformPicker from '~/components/PlatformPicker.vue'
import RoomTypePicker from '~/components/RoomTypePicker.vue'
import LocationPicker from '~/components/LocationPicker.vue'

import Location, { LocationId } from '~/domain/Location'
import GameToLocations from '~/static/GameToLocations'

import LongPoller from '~/service/LongPoller'
import LongPollingEvent, { Type as LongPollingEventType } from '~/domain/LongPollingEvent'

import IdentifiedRoom from '~/domain/IdentifiedRoom'
import { Context } from '@nuxt/types'
import User from '~/domain/User'
import Game from '~/domain/Game'
import LocationType from '~/domain/LocationType'
import Platform from '~/domain/Platform'
import { NuxtAxiosInstance } from '@nuxtjs/axios'
import RoomType from '~/domain/RoomType'

type IdentifiedRoomWithLocation = IdentifiedRoom & {
  location : Location
}

function enrichRoomsWithLocation (rooms : IdentifiedRoom[], game : Game) : IdentifiedRoomWithLocation[] {
  return rooms.map(room => ({
    ...room,
    location:
      GameToLocations.get(game)!.find(location => location.id === room.locationId) ||
      <Location> {
        id: room.locationId,
        name: room.locationId,
        groupName: '',
        type: LocationType.AREA
      }
  }))
}

async function fetchRooms (
  $axios : NuxtAxiosInstance,
  game : Game,
  platform : Platform,
  selectedRoomTypes : RoomType[],
  selectedLocationIds : LocationId[],
  hostQuery ?: string,
  roomQuery ?: string
) : Promise<IdentifiedRoomWithLocation[]> {
  return enrichRoomsWithLocation(
    await $axios.$get<IdentifiedRoom[]>(
      `/api/room/all?game=${game}&platform=${platform}` +
        (
          hostQuery
            ? `&host_query=${encodeURIComponent(hostQuery)}`
            : ''
        ) +
        (
          roomQuery
            ? `&room_query=${encodeURIComponent(roomQuery)}`
            : ''
        ) +
        (selectedRoomTypes || [])
          .map(roomType => `&room_type=${encodeURIComponent(roomType)}`)
          .join('') +
        (
          selectedLocationIds && selectedLocationIds.length
            ? `&location_ids=${encodeURIComponent(selectedLocationIds.join(','))}`
            : ''
        )
    ),
    game
  )
}

@Component({
  name: 'ListRoomsPage',
  components: {
    LocationPicker,
    PlatformPicker,
    RoomTypePicker
  }
})
export default class RoomsPage extends mixins(GameAwarePageMixin, LoggedUserAwarePageMixin, WindowFocusAwareMixin) {


  private longPoller ?: LongPoller<LongPollingEvent<User | any>[]> = undefined

  private filterMenuIsExpanded : boolean = false
  private selectedRoomTypes : RoomType[] = [ RoomType.COOP, RoomType.PVP ]
  private selectedLocationIds : string[]  = []
  private hostQuery : string = ''
  private roomQuery : string = ''
  private updateTimestamp : number | null = null
  private rooms : IdentifiedRoomWithLocation[] = []

  protected async asyncData (context : Context) : Promise<any> {
    const { user } : { user : User } = await LoggedUserAwarePageMixinAsyncData(context)
    const game = <Game> context.params.game.toUpperCase()
    return {
      user,
      rooms:
        context.params.game && user.lastSelectedPlatform
          ? await fetchRooms(
              context.$axios,
              game,
              user.lastSelectedPlatform,
              [],
              []
            )
          : []
    }
  }

  private get filterKey () : string {
    return [ this.game, this.user?.lastSelectedPlatform, this.hostQuery, this.roomQuery, this.selectedRoomTypes.join(','), this.selectedLocationIds.join(',') ].join(';')
  }

  @Watch('filterKey')
  private async doFilter () : Promise<void> {
    this.$set(
      this,
      'rooms',
      await fetchRooms(this.$axios, this.game!, this.user!.lastSelectedPlatform, this.selectedRoomTypes, this.selectedLocationIds, this.hostQuery, this.roomQuery)
    )
  }

  private openFilterMenu () {
    this.filterMenuIsExpanded = true
  }

  private closeFilterMenu () {
    this.filterMenuIsExpanded = false
  }

  private clearFilter () : void {
    this.hostQuery = ''
    this.roomQuery = ''
    this.closeFilterMenu()
  }

  protected onWindowFocus () : void {
    this.$nextTick(() => this.doFilter())
  }

  private consumeLongPollingEvents (events : LongPollingEvent<IdentifiedRoom>[]) : void {
    events.forEach(event => {
      const room = event.payload
      switch (event.type) {
        case LongPollingEventType.ROOM_HAS_BEEN_CREATED:
        case LongPollingEventType.ROOM_HAS_BEEN_REMOVED:
          // TODO subscribe to those only
          if (room.game === this.game && room.platform === this.user?.lastSelectedPlatform)
            this.doFilter()
          return
        default:
          throw new Error('Unhandled LongPollingEventType.' + event.type)
      }
    })
  }

  private get longPollerEndpoint () : string | null {
    return this.game && this.user?.lastSelectedPlatform
      ? `/api/room/all/subscribe_to_event?game=${this.game}&platform=${this.user!.lastSelectedPlatform}`
      : null
  }

  @Watch('longPollerEndpoint')
  private subscribeToLongPollingEvents () : void {
    const endpoint = this.longPollerEndpoint
    if (!endpoint) {
      if (this.longPoller)
        this.longPoller.unsubscribe()

      return
    }

    if (this.longPoller) {
      this.longPoller!.setEndpoint(endpoint)
    } else {
      this.longPoller = new LongPoller<LongPollingEvent<IdentifiedRoom>[]>(
        this.$axios,
        endpoint,
        events => this.consumeLongPollingEvents(events)
      )
    }
  }

  private unsubscribeFromLongPollingEvents () : void {
    this.longPoller?.unsubscribe()
  }

  protected beforeDestroy () : void {
    this.unsubscribeFromLongPollingEvents()
  }

  protected mounted () {
    this.subscribeToLongPollingEvents()
  }

}
</script>
