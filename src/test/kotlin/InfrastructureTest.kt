import infrastructure.*
import infrastructure.container.TimeWindow
import infrastructure.container.range
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

internal class InfrastructureTest {


    @BeforeEach
    fun `reset clock`(){
        Clock.reset()
    }

    @Test
    fun `Railway test otherDestination function`(){
        val (s1,s2) = List(2){id -> Station(Pair(id,id),"$id") }
        val rw = Railway(s1,s2)
        assert(rw.otherDestination(s1) == s2)
        assert(rw.otherDestination(s2) == s1)
    }

    @Test
    fun `Railway test get transition time calculator`(){
        val (s1,s2) = List(2){id -> Station(Pair(0,id*10),"$id") }
        val rw = Railway(s1,s2)
        val train = Train(1,s1)
        val nextSlot1 = rw.availability(0,train,s1)
        assert(rw.getTransitionTime(train, nextSlot1, s1) == rw.getGeneralTransitionTime(train))

        val trainFast = Train(10,s1)
        rw.register(0,10,train,s1)
        println(rw.getTransitionTime(trainFast,6,s1))
        assert(rw.getTransitionTime(trainFast,6,s1) == 10)
    }

    //infrastructure.Railway tests
    @Test
    fun `Railway test register`(){
        val (s1,s2) = List(2){id -> Station(Pair(0,id)) }
        val t1 = Train(1,s2)
        val rw = Railway(s1,s2)
        rw.register(0,1,t1,s2)
        assert(rw.waiting.size == 1)
    }

    @Test
    fun `Railway test remove after passage`(){
        val (s1,s2) = List(2){id -> Station(Pair(0,id)) }
        val t1 = Train(1,s2)
        val rw = Railway(s1,s2)
        rw.register(0,1,t1,s2)
        Clock.tick()
        rw.tick()
        assert(rw.waiting.size == 1)
        Clock.tick()
        rw.tick()
        assert(rw.waiting.isEmpty())
    }

    @Test
    fun `Railway test get-availability`(){
        val (s1,s2) = List(2){id -> Station(Pair(0,id)) }
        val t1 = Train(1,s2)
        val rw = Railway(s1,s2)
        assert(rw.availability(0,t1,s2) == 0)
        rw.register(0,1,t1,s2)
        print(rw.availability(0,t1.copy(),s2))
        assert(rw.availability(0,t1.copy(),s2) == 6)
        //infrastructure.container.reverse direction
        val t2 = Train(1,s1)
        assert(rw.availability(0,t2,s1) == 7)
        rw.register(7,8,t2,s1)
        //check if works for start != 0
        assert(rw.availability(5,t1.copy(),s2) == 9)
        //check if it can find slots
        rw.register(20,21,t1.copy(),s2)
        assert(rw.waiting.size == 3)
        assert(rw.availability(12,t2.copy(),s1) == 1)

        rw.register(13,14,t2.copy(),s1)
        assert(rw.availability(0,t2.copy(),s1) == 27)

    }

    @Test
    fun `Railway test get-transition-time`(){
        val (s1,s2) = List(2){id -> Station(Pair(0,id)) }
        val t1 = Train(1,s2)
        val rw = Railway(s1,s2)
        assert(rw.getGeneralTransitionTime(t1) == 1)
    }

    @Test
    fun `Railway register invalid input throw IAE`(){
        val (s1,s2) = List(2){id -> Station(Pair(0,id*2)) }
        val t1 = Train(1,s2)
        val rw = Railway(s1,s2)

        assertThrows<IllegalArgumentException>("no input that is older than current time") {
            rw.register(-1,0,t1,s2)
        }
        assertThrows<IllegalArgumentException>("train can't travel backwards in time") {
            rw.register(5,4,t1,s2)
        }
        assertThrows<IllegalArgumentException>("there must be a buffer of 5 minutes between each passage") {
            rw.register(0,1,t1,s2)
            rw.register(2,3,t1,s2)
        }
        assertThrows<IllegalArgumentException>("the end-start time mustn't be smaller than the transit-time") {
            rw.register(0, 0, t1, s2)
        }
    }



    // infrastructure.Station tests

    @Test
    fun `Station calculate geo-distance`(){
        val s1 = Station(Pair(0,0))
        val s2 = Station(Pair(100,100))
        val distance = s1.distance(s2)
        //141.42 correct result, floored
        assert(distance == 141)
    }

    @Test
    fun `Station test getConnectingRailways`(){
        val (s1,s2,s3,s4) = List(4){id -> Station(Pair(id,id)) }
        val railways = listOf(Railway(s1,s2), Railway(s1,s4), Railway(s3,s2))
        assert(s1.getConnectingRailways(railways).size == 2)
    }

    @Test
    fun `Station register throw error at invalid input`(){
        val s = Station(Pair(0,0))
        val target = Station(Pair(0,0))
        val t1 = Train(200,target)
        assertThrows<IllegalArgumentException>("start must be greater than current clock-time") {
            s.register(-1,Integer.MAX_VALUE,t1)
        }
        assertThrows<IllegalArgumentException>("start <= end") {
            s.register(5,4,t1)
        }
    }



    @Test
    fun `Station drop trains when timewindow is closed`(){
        val s = Station(Pair(0,0))
        val target = Station(Pair(0,0))
        val t1 = Train(200,target,true)
        s.register(0,5,t1)
        repeat(6){
            Clock.tick()
        }
        s.tick()
        assert(s.waiting.isEmpty())
    }

    @Test
    fun `Station register trains`(){
        val s = Station(Pair(0,0))
        val target = Station(Pair(0,0))
        val t1 = Train(200,target,true)
        s.register(0,5,t1)
        assert(s.waiting.size == 1)
        s.tick()
        assert(s.waiting.size == 1)
    }


    // infrastructure.container.TimeWindow type alias

    @Test
    fun `TimeWindow typealias extension check`(){
        val tw = TimeWindow(0,10)
        assert(tw.range == (0..10))
    }

}