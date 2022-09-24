package infrastructure.container

import infrastructure.Direction
import infrastructure.Train

data class Transit(val first : TimeWindow, val second : Train, val direction : Direction = Direction.NoTransit)