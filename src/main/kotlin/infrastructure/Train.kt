package infrastructure

data class Train(val speed : Int, val target : Station, var pathCalculated : Boolean = false, val id : Int = Companion.id++){

    companion object{
        var id = 0
    }

}