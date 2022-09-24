import infrastructure.Railway
import infrastructure.Station
import infrastructure.Train
import infrastructure.World
import java.io.File

object WorldParser {
    fun parse(f: File): World {
        val world = f.readLines()
        val stations = mutableListOf<Station>()
        val railways = mutableListOf<Railway>()
        val trains = mutableListOf<Pair<String,String>>()
        val cities = mutableListOf<Station>()

        world.forEach {
            if(it.trim().startsWith("#")){
                //commet
            }else if(it.contains(";")){
                //infrastructure.Station
                val (data,destinations) = it.split(";")
                val (name,coords) = data.split(":")
                val (x,y) = coords.split(",").map { it.trim().toInt() }
                val station = Station(Pair(x,y),name.trim())
                stations.add(station)
                destinations.split(",").map { it.trim() }.filter { it != "" }.forEach {
                    trains.add(Pair(name.trim(),it.trim()))
                }
            }else if(it.contains("-")){
                //infrastructure.Railway
                val(start,end) = it.split("-").map { it.trim() }.map { stations.find {station -> station.name == it  }!! }
                val rw = Railway(start,end)
                railways.add(rw)
            }else{
                //cities without connection to railways
                val (name,coords) = it.split(":")
                val (x,y) = coords.split(",").map { it.trim().toInt() }
                cities.add(Station(Pair(x,y),name))
            }
        }

        trains.forEach {
            val start = stations.find { station -> station.name == it.first }!!
            val destination = stations.find { station -> station.name == it.second }!!
            val train = Train(5,destination)
            start.register(Clock.time,Clock.time,train)
        }

        return World(stations, railways, cities)
    }


}