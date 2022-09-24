import infrastructure.Railway
import infrastructure.Station
import infrastructure.Train

object TrainPathfinder {


    //djikstra
    fun deploy(home: Station, train: Train, railways: List<Railway> = CentralControlComputer.railways) {

        val nodes = mutableMapOf<Station, Pair<Int, Station?>>()
        nodes[home] = Pair(0, null)

        val stationsToVerify = mutableListOf<Station>()
        stationsToVerify.add(home)

        while (stationsToVerify.isNotEmpty()) {
            val currentStation = stationsToVerify.first()
            stationsToVerify.remove(currentStation)
            val startTime = nodes[currentStation]!!.first + 1

            //end algo if target is reached
            if (currentStation == train.target) {
                val way = mutableListOf<Pair<Station, Int>>()
                var current = currentStation
                while (true) {
                    val x = nodes[current]!!
                    way.add(Pair(current, x.first + Clock.time))
                    current = x.second ?: break
                }
                way.reverse()
                var idx = 0
                while (idx < way.size - 1) {
                    val current = way[idx]
                    val next = way[idx + 1]
                    val rw = railways.filter { it.fits(current.first, next.first) }.minBy {
                        val arrivingAtCurrent = current.second
                        val transitStart =
                            it.availability(arrivingAtCurrent + 1, train, next.first) + 1 + arrivingAtCurrent
                        it.getTransitionTime(train, transitStart, next.first) + transitStart
                    }
                    val arrivingAtCurrent = current.second
                    val transitStart = rw.availability(arrivingAtCurrent + 1, train, next.first) + 1 + arrivingAtCurrent
                    val transitEnd = rw.getTransitionTime(train, transitStart, next.first) + transitStart
                    current.first.register(arrivingAtCurrent, transitStart - 1, train)
                    rw.register(transitStart, transitEnd, train, next.first)
                    next.first.register(transitEnd + 1, transitEnd + 1, train)
                    idx++
                }
                train.pathCalculated = true
                return
            }

            for (railway in currentStation.getConnectingRailways(railways)) {

                val station = railway.otherDestination(currentStation)
                val availability = railway.availability(startTime, train, station)
                val predictedTransitEnd = railway.getTransitionTime(train, availability, station)
                val timeToReachStation = availability + predictedTransitEnd + startTime + 1

                if (!nodes.containsKey(station)) {
                    nodes[station] = Pair(timeToReachStation, currentStation)
                    val idx = stationsToVerify.indexOfLast { nodes[it]!!.first < timeToReachStation }
                    stationsToVerify.add(idx + 1, station)
                } else {
                    val data = nodes[station]!!
                    if (data.first > timeToReachStation) {
                        nodes[station] = Pair(timeToReachStation, currentStation)
                    }
                }
            }

        }
        throw IllegalArgumentException("The target station may not be reachable!")
    }


}