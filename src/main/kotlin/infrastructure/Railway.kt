package infrastructure

import Clock
import infrastructure.container.TimeWindow
import infrastructure.container.Transit
import kotlin.math.ceil
import kotlin.math.max

class Railway(val start: Station, val end: Station) : Infrastructure() {

    private val length: Int = start.distance(end)
    private val _waiting = mutableListOf<Transit>()
    val waiting: List<Transit> get() = _waiting

    override fun tick() {
        //remove trains after transit
        _waiting.filter { it.first.second < Clock.time }.forEach {
            println("RE ${it.second.id} |${it.second.target}|   ${start.name} to ${end.name}")
            _waiting.remove(it)
        }
    }

    private fun Station.getDirection() = if (end == this) Direction.Up else Direction.Down

    fun getGeneralTransitionTime(train: Train) = ceil(length.toDouble() / train.speed.toDouble()).toInt()

    //returns earliest possible start time
    fun availability(start: Int, train: Train, target: Station): Int {
        val requestedDirection = target.getDirection()
        //free passage since no scheduled trains
        if (waiting.isEmpty()) return 0
        /* initialisation of three list:
            blocked: all time windows of scheduled trains in infrastructure.container.reverse direction == no parallel transit possible
            start/end-Fields: all start/end-transit slots
         */
        var (waitingRequestedDirection, waitingReverseDirection) = waiting.partition { it.direction == requestedDirection }
        waitingRequestedDirection = waitingRequestedDirection.filter { it.first.first >= start - 5 }

        val blocked = waitingReverseDirection.map { (it.first.first - 5)..(it.first.second + 5) }
        val startFields = waitingRequestedDirection.map { it.first.first }.sorted()
        val endFields = waitingRequestedDirection.map { it.first.second }.sorted()

            var idx = start
            val startRange = startFields.map {  (it-5) .. (it+5) }
            val endRange = endFields.map { (it-5) .. (it+5) }
            while (true){
                if( blocked.none { idx in it } && startRange.none{ idx in it}){
                    val predictedTransitTime = getGeneralTransitionTime(train)
                    if(endRange.none { idx + predictedTransitTime in it })
                        return idx-start
                }
                idx++
            }
    }

    fun register(start: Int, end: Int, train: Train, target: Station) {
        require(start >= Clock.time && start <= end) { "Invalid start / end parameters for time window!" }
        require(waiting.none { it.second === train }) { "infrastructure.Train may be added only once to the waiting queue" }
        require(availability(start,train,target) == 0){"This start slot is not open!"}
        require(start + getGeneralTransitionTime(train) <= end){"Train may not exceed its maximum speed!"}
        val requestedDirection = target.getDirection()

        val time = TimeWindow(start, end)
        val transit = Transit(time, train, requestedDirection)

        _waiting.add(transit)
    }

    fun getTransitionTime(train : Train, startTime : Int, target : Station) : Int{
        val successor = waiting.filter { it.first.first < startTime }.maxByOrNull {startTime -  it.first.first } ?: return getGeneralTransitionTime(train)
        val time = successor.first.second
        val transitionTime = getGeneralTransitionTime(train)
        return max(time + 6, startTime + transitionTime) - startTime
    }

    fun fits(s1 : Station, s2 : Station) = (s1 == start && s2 == end) || (s1 == end && s2 == start)

    fun otherDestination(hub : Station) = if(start == hub) end else start

}