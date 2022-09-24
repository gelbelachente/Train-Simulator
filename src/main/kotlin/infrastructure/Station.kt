package infrastructure

import CentralControlComputer
import Clock
import TrainPathfinder
import infrastructure.container.TimeWindow
import infrastructure.container.Transit
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


class Station(val coord: Pair<Int, Int>, val name: String = "") : Infrastructure() {

    override fun toString() = name

    private val _waiting = mutableListOf<Transit>()
    val waiting: List<Transit> get() = _waiting

    fun distance(other: Station): Int {
        val xDistance = abs(other.coord.first - this.coord.first).toDouble()
        val yDistance = abs(other.coord.second - this.coord.second).toDouble()
        //Pythagoras - rounded down/floored
        return sqrt(xDistance.pow(2) + yDistance.pow(2)).toInt()
    }

    override fun tick() {
        //find path for new trains
        _waiting.filter { !it.second.pathCalculated }
            .forEach {
                TrainPathfinder.deploy(this, it.second)
            }
         //remove trains at start
        _waiting.filter { Clock.time > it.first.second }.forEach {
            if (it.second.target == this)
                CentralControlComputer.release(this, it.second)
            _waiting.remove(it)
        }

    }

    fun register(start: Int, end: Int, train: Train) {
        require(start >= Clock.time && start <= end) { "Invalid start / end parameters for time window!" }
        _waiting.removeIf { it.second == train }
        val passage = TimeWindow(start, end)
        val transit = Transit(passage, train)
        _waiting.add(transit)
    }

    fun getConnectingRailways(railways: List<Railway>) = railways.filter { it.start == this || it.end == this }

}
