import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

internal class ParserTest {

    @Test
    fun `test train simulator world parser`(){
        val f = File("test.train")
        val (stations,railways,cities) = WorldParser.parse(f)
        assert(stations.size == 2)
        assert(railways.size == 1)
        val (brazil,panama) = stations.sortedBy { it.name }
        assert(brazil.coord == Pair(33,50))
        assert(panama.coord == Pair(66,46))
        assert(brazil.waiting.size == 1)
        assert(panama.waiting.size == 1)
        val rw = railways.first()
        assert(rw.start == brazil)
        assert(rw.end == panama)
        assert(cities.size == 1)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun `create test world file`() {
            Clock.reset()
            File("test.train").apply {
                createNewFile()
                writeText("Brazil: 33,50; Panama" + System.lineSeparator() + "Panama: 66,46; Brazil" + System.lineSeparator() +
                        "Kairo: 88,99" + System.lineSeparator() + "#sdfsdf" + System.lineSeparator() +
                "Brazil-Panama")
            }
        }
        @JvmStatic
        @AfterAll
        fun `delete test world file`() {
            Clock.reset()
            File("test.train").delete()
        }

    }

}