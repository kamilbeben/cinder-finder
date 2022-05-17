
import RoomType from './RoomType'
import Platform from './Platform'
import User from './User'
import { LocationId } from '~/domain/Location'

type RoomDraft = {

  platform ?: Platform
  type ?: RoomType

  name ?: string
  description ?: string
  password ?: string

  locationId ?: LocationId

}

export default RoomDraft
