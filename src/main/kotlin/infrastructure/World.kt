package infrastructure

data class World(
    val stations: List<Station>,
    val railways: MutableList<Railway>,
    val cities : MutableList<Station> = mutableListOf())