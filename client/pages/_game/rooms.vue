<template>
  <div class="page-default">

    <v-text-field
      v-model="user.inGameName"
      :label="$t('rooms.in-game-name')"
      :rules="[
        RuleFactory.maxLength(50),
        RuleFactory.minLength(3)
      ]"
      counter="50"
      style="flex: unset;"
      class="mb-4"
    />
    
    <platform-picker
      v-if="!user.lastSelectedPlatform"
      v-model="user.lastSelectedPlatform"
    />
    <template v-else>
      
      <div class="d-flex">
        <div
          class="muted"
          v-text="$t('rooms.available-rooms')"
          data-selenium-id="label.available-rooms"
        />
        <v-icon
          class="ml-auto pointer"
          v-text="'mdi-filter-menu'"
          @click="openFilterMenu"
        />
      </div>

      <div class="room-list list">
        <div
          v-if="rooms.length === 0"
          class="muted px-4 py-2"
          v-text="$t('rooms.available-rooms-placeholder')"
        />

        <nuxt-link
          v-for="room in rooms"
          :key="room.id"
          :to="`/${lowercaseGame}/room/${room.id}`"
          class="room d-flex pointer clear-css-nuxt-link overflow-hidden"
          :data-selenium-id="`room-link.${room.id}`"
        >

          <!-- is host online && room type -->
          <v-icon
            size="2em"
            :color="
              $vuetify.theme.isDark
                ? room.host.isOnline
                  ? 'green'
                  : 'red'
                : room.host.isOnline
                    ? 'blue accent-1'
                    : ''
            "
            v-text="
              room.type === 'COOP'
                ? 'mdi-handshake'
                : 'mdi-fencing'
            "
          />

          <div class="ml-2 d-flex flex-column flex-fill overflow-hidden">

            <div class="d-flex">
              <!-- room name -->
              <div
                class="ml-2 my-auto ellipsis"
                v-text="room.name"
              />
              <!-- host name -->
              <div
                class="my-auto ml-auto ellipsis"
                v-text="
                  (
                    room.host.inGameName ||
                    room.host.userName
                  ) + (
                    room.hostLevel
                      ? ` (${$t('common.host-level-value', { value: room.hostLevel })})`
                      : ''
                  )
                "
              />
            </div>

            <div class="d-flex ml-2 py-1">
              <!-- location -->
              <div
                class="mt-1 mb-auto muted d-flex"
              >
                <location-icon
                  :type="room.location.type"
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

          <label
            class="mt-2 button-group-label"
            v-text="$t('common.host-level-field')"
          />

          <div class="d-flex">
            <v-text-field
              v-model="minHostLevel"
              :label="$t('rooms.min-host-level')"
              type="number"
              class="pt-2 mt-0"
              :rules="[
                value => isNaN(parseInt(value)) || isNaN(parseInt(maxHostLevel)) || parseInt(value) <= parseInt(maxHostLevel) || $t('rooms.validation.min-is-greater-than-max'),
                RuleFactory.max(9999),
                RuleFactory.min(1)
              ]"
            />
            <v-text-field
              v-model="maxHostLevel"
              :label="$t('rooms.max-host-level')"
              type="number"
              class="pt-2 mt-0 ml-1"
              :rules="[
                value => isNaN(parseInt(value)) || isNaN(parseInt(minHostLevel)) || parseInt(value) >= parseInt(minHostLevel) || $t('rooms.validation.max-is-smaller-than-min'),
                RuleFactory.max(9999),
                RuleFactory.min(1)
              ]"
            />
          </div>

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

    <div
      class="mt-4 d-flex action-buttons"
    >
      <v-btn
        class="ml-auto"
        @click="() => $router.push(`/${lowercaseGame}`)"
      >
        <v-icon v-text="'mdi-arrow-left'"/>
      </v-btn>
    </div>
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
import LocationIcon from '~/components/LocationIcon.vue'

import Location, { LocationId } from '~/domain/Location'
import GameToLocations from '~/static/GameToLocations'

import LongPoller from '~/service/LongPoller'
import RuleFactory from '~/service/RuleFactory'
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
        type: LocationType.BOSS
      }
  }))
}

async function fetchRooms (
  $axios : NuxtAxiosInstance,
  game : Game,
  platform : Platform,
  selectedRoomTypes : RoomType[],
  selectedLocationIds : LocationId[],
  hostQuery : string | null,
  roomQuery : string | null,
  minHostLevel : number | null,
  maxHostLevel : number | null
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
        ) +
        (
          minHostLevel
            ? `&min_host_level=${minHostLevel}`
            : ''
        ) +
        (
          maxHostLevel
            ? `&max_host_level=${maxHostLevel}`
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
    LocationIcon,
    PlatformPicker,
    RoomTypePicker
  }
})
export default class RoomsPage extends mixins(GameAwarePageMixin, LoggedUserAwarePageMixin, WindowFocusAwareMixin) {

  private readonly RuleFactory = new RuleFactory(this)

  private longPoller ?: LongPoller<LongPollingEvent<User | any>[]> = undefined

  private filterMenuIsExpanded : boolean = false
  private selectedRoomTypes : RoomType[] = [ RoomType.COOP, RoomType.PVP ]
  private selectedLocationIds : string[]  = []
  private hostQuery : string = ''
  private roomQuery : string = ''
  private minHostLevel : number | null = null
  private maxHostLevel : number | null = null
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
              [],
              null,
              null,
              null,
              null
            )
          : []
    }
  }

  private get filterKey () : string {
    return [ this.game, this.user?.lastSelectedPlatform, this.hostQuery, this.roomQuery, this.selectedRoomTypes.join(','), this.selectedLocationIds.join(','), this.minHostLevel, this.maxHostLevel ].join(';')
  }

  private doFilterDebounceTimeoutId : number = 0

  @Watch('filterKey')
  private onFilterKeyChanged () : void {
    clearTimeout(this.doFilterDebounceTimeoutId)
    this.doFilterDebounceTimeoutId = <number> <any> setTimeout(
      () => this.doFilter(),
      250
    )
  }

  private async doFilter () : Promise<void> {
    this.$set(
      this,
      'rooms',
      await fetchRooms(
        this.$axios,
        this.game!,
        this.user!.lastSelectedPlatform,
        this.selectedRoomTypes,
        this.selectedLocationIds,
        this.hostQuery,
        this.roomQuery,
        this.minHostLevel,
        this.maxHostLevel
      )
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
    this.minHostLevel = null
    this.maxHostLevel = null
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

<style scoped>

  .page-default, .room-list {
    flex: 1 1 auto;
    height: 0;
  }

  .room-list {
    overflow-x: hidden;
    overflow-y: auto;
  }

</style>
