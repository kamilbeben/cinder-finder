
import Vue from 'vue'

import { Mixin } from 'vue-mixin-decorator'
import { Component, Watch } from 'nuxt-property-decorator'
import { Context } from '@nuxt/types'

import User from '~/domain/User'
import Platform from '~/domain/Platform'

export async function asyncData (context : Context) : Promise<any> {

  return {
    user: await context.$axios.$get<User>('/api/user/me')
  }
}

const USER_IN_GAME_NAME_LOCAL_STORAGE_KEY = 'LoggedUserAwarePageMixin_userInGameName'
const PLATFORM_LOCAL_STORAGE_KEY = 'LoggedUserAwarePageMixin_paltform'

@Component
export default class LoggedUserAwarePageMixin extends Vue {

  protected user ?: User = undefined

  protected async asyncData (context : Context) : Promise<any> {
    return await asyncData(context)
  }

  private persistUserInGameNameTimeoutId ?: number = undefined

  @Watch('user.inGameName')
  private userInGameNameChangedWatcher (nextValue ?: string) : void {
    // persist client state in case of server restart (guest user configs aren't persisted in the database)
    if (nextValue)
      localStorage[USER_IN_GAME_NAME_LOCAL_STORAGE_KEY] = nextValue
    else
      delete localStorage[USER_IN_GAME_NAME_LOCAL_STORAGE_KEY]
    
    // update server state
    clearTimeout(this.persistUserInGameNameTimeoutId)
    this.persistUserInGameNameTimeoutId = <any> setTimeout(
      () => this.$axios.$post<void>(`/api/user/in_game_name?value=${encodeURIComponent(nextValue || '')}`),
      250
    )
  }

  private persistLastSelectedPlatformTimeoutId ?: number = undefined

  @Watch('user.lastSelectedPlatform')
  private lastSelectedPlatformWatcher (nextValue ?: Platform) : void {
    if (!nextValue)
      return

    // persist client state in case of server restart (guest user configs aren't persisted in the database)
    localStorage[PLATFORM_LOCAL_STORAGE_KEY] = nextValue

    // update server state
    clearTimeout(this.persistLastSelectedPlatformTimeoutId)
    this.persistLastSelectedPlatformTimeoutId = <any> setTimeout(
      () => this.$axios.$post<void>(`/api/user/last_selected_platform?value=${encodeURIComponent(nextValue || '')}`),
      250
    )
  }

  protected mounted () : void {
    const localStorageUserInGameName = localStorage[USER_IN_GAME_NAME_LOCAL_STORAGE_KEY]
    const localStoragePlatform = localStorage[PLATFORM_LOCAL_STORAGE_KEY]

    if (localStorageUserInGameName)
      this.$set(this.user!, 'inGameName', localStorageUserInGameName)

    if (localStoragePlatform)
      this.$set(this.user!, 'lastSelectedPlatform', localStoragePlatform)
  }

}
