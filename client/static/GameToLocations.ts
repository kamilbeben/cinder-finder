
import Game from '~/domain/Game'
import Location from '~/domain/Location'

import EldenRingBosses from './EldenRingBosses'

export default  new Map<Game, Location[]>([
  [ Game.ELDEN_RING, <Location[]> [...EldenRingBosses] ]
])
