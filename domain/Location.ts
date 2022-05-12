
import LocationType from './LocationType'

export type LocationId = string

type Location = {
  id : LocationId
  type : LocationType
  groupName : string
  name : string
}

export default Location
