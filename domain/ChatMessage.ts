
import User from './User'

type ChatMessage = {
  user : User
  timestamp : number
  content : string
}

export default ChatMessage
