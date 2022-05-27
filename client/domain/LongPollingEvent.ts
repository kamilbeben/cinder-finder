
import User from './User'

export enum Type {
  CHAT_MESSAGE = 'CHAT_MESSAGE',

  USER_HAS_JOINED = 'USER_HAS_JOINED',
  USER_HAS_LEFT = 'USER_HAS_LEFT',
  USER_IS_ONLINE = 'USER_IS_ONLINE',
  USER_IS_OFFLINE = 'USER_IS_OFFLINE',

  ROOM_HAS_BEEN_CREATED = 'ROOM_HAS_BEEN_CREATED',
  ROOM_HAS_BEEN_REMOVED = 'ROOM_HAS_BEEN_REMOVED'
}

type LongPollingEvent<PAYLOAD> = {
  type : Type,
  payload : PAYLOAD
}

export default LongPollingEvent
