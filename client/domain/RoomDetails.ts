
import ChatMessage from './ChatMessage'
import IdentifiedRoom from './IdentifiedRoom'
import RoomMember from './RoomMember'

type RoomDetails = IdentifiedRoom & {
  guests : RoomMember[]
  messages : ChatMessage[]
  updateTimestamp : number
}

export default RoomDetails
