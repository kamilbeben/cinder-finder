
import RoomDraft from './RoomDraft'
import RoomMember from './RoomMember'

type IdentifiedRoom = RoomDraft & {
  id : number
  host : RoomMember
}

export default IdentifiedRoom
