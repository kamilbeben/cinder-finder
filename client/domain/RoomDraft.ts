
import RoomType from './RoomType'
import Platform from './Platform'
import User from './User'
import { LocationId } from '~/domain/Location'
import Game from './Game'

type RoomDraft = {

  game ?: Game
  platform ?: Platform
  type ?: RoomType

  name ?: string
  description ?: string
  password ?: string

  locationId ?: LocationId
  hostLevel ?: number

}

export default RoomDraft
