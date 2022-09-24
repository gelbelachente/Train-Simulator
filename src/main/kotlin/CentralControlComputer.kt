import infrastructure.Railway
import infrastructure.Station
import infrastructure.Train
import infrastructure.World
import paint.MapPainter
import java.io.File

object CentralControlComputer {

    val railways: List<Railway> get() = world.railways
    val stations: List<Station> get() = world.stations
    lateinit var world: World

    var money: Int = 0
    var turn = 0
    const val MAX_TURNS = 200
    var nextTask: Pair<Int, Pair<Station, Station>>? = null
    const val railwayCost = 10


    fun parse(file: File) {
        world = WorldParser.parse(file)
    }

    fun run() {
        while (true) {
            tick()
            if (MAX_TURNS <= turn) break
            if (stations.all { it.waiting.isEmpty() } && railways.all { it.waiting.isEmpty() }) {
                break
            }
        }
        println("All the cargo has reached its destination after $turn turns")
        println("${railways.size} railways in service")
    }

    fun tick() {
        MapPainter.draw(world, turn++)
        stations.forEach { it.tick() }
        railways.forEach { it.tick() }
        buildIfPossible()
        Clock.tick()
    }


    fun calculateNewBuildingProject() {
        // 5 == rushed..... higher number means more relaxed traffic
        val mostRushedStation = railways.map {
            if (it.waiting.size <= 1) {
                Pair(Pair(it.start, it.end), Integer.MAX_VALUE)
            } else {
                val startRange = it.waiting.sumOf { it.first.first } - it.waiting.minOf { it.first.first }
                Pair(it.start, it.end) to (startRange / (it.waiting.size - 1))
            }
        }.minBy { it.second }
        //which station is most often targeted
        val mostTargetedStation = stations.map {
            val transiting = it.waiting.filter { a -> it != a.second.target }
            if (transiting.size <= 1) {
                Pair(Pair(it, it), 0)
            } else {
                val target = transiting.groupBy { it.second.target }.maxBy { it.value.size }
                Pair(Pair(it, target.key), target.value.size)
            }
        }.maxBy { it.second }

        //assign new construction task
        nextTask = if (mostTargetedStation.second * 10 / mostRushedStation.second < 1) {
            val cost = mostRushedStation.first.first.distance(mostRushedStation.first.second) * railwayCost
            Pair(cost, mostRushedStation.first)
        } else {
            val cost = mostTargetedStation.first.first.distance(mostTargetedStation.first.second) * railwayCost * 5
            Pair(cost, mostTargetedStation.first)
        }
        buildIfPossible()
    }

    private fun buildIfPossible() {
        if (nextTask == null) {
            calculateNewBuildingProject()
            return
        }
        if (nextTask!!.first <= money) {
            money -= nextTask!!.first
            world.railways.add(Railway(nextTask!!.second.first, nextTask!!.second.second))
            println("Build Railway from ${nextTask!!.second.first.name} to ${nextTask!!.second.second.name}")
            calculateNewBuildingProject()
        }

    }

    fun release(station: Station, train: Train) {
        money += 100
        station.register(Clock.time, Clock.time + 5, train.copy(target = stations.random(), pathCalculated = false))
    }


}