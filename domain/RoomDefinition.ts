
import RoomType from './RoomType'
import Platform from './Platform'
import { LocationId } from '~/domain/Location'

type RoomDefinition = {

  platform ?: Platform
  type ?: RoomType

  name ?: string
  description ?: string
  password ?: string

  locationId ?: LocationId

  hostNickname ?: string
}

export default RoomDefinition
