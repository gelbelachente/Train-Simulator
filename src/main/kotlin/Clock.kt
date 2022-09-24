object Clock {

    private var _time = 0

    fun tick(){
        _time++
    }

    fun reset(){
        _time = 0
    }

    val time : Int get() = _time

}