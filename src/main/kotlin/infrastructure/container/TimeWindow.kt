package infrastructure.container

typealias TimeWindow = Pair<Int,Int>

val TimeWindow.range : IntRange get() = this.first .. this.second