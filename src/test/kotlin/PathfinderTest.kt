import infrastructure.Railway
import infrastructure.Station
import infrastructure.Train
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PathfinderTest {

    @BeforeEach
    fun `reset Clock`(){
        Clock.reset()
    }

    @Test
    fun `test path finder algorithm`() {
        repeat(2){ Clock.tick() }
        val (home, s2, s3, s4) = List(4) { id -> Station(Pair((id/2.0).toInt(), id), "$id") }
        val train = Train(1, s4)
        home.register(2, 2, train)
        val railways = listOf(Railway(home, s2), Railway(home, s3), Railway(s3, s4))
        TrainPathfinder.deploy(home, train, railways)
        assert(train.pathCalculated)
        assert(s3.waiting.size == 1)
        assert(s4.waiting.size == 1)
        assert(s2.waiting.isEmpty())
        assert(home.waiting.size == 1)
    }

    @Test
    fun `Test path finder choose wisely between 2 parallel lines`(){
        val (s1,s2) = List(2){id -> Station(Pair(id,id),"$id")}
        val train = Train(1,s2)
        val railways = listOf(Railway(s1,s2), Railway(s1,s2), Railway(s1,s2))
        for(rw in listOf(railways.first(), railways.last())){
            for(i in 0..3){
                rw.register(i*6,2 + i*6,train.copy(),s2)
            }
        }
        TrainPathfinder.deploy(s1,train.copy(),railways)
        assert(railways[1].waiting.size == 1)

    }


}