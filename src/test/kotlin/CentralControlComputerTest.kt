
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class CentralControlComputerTest {

    @BeforeEach
    fun `reset CentralControlComputer`(){
        CentralControlComputer.parse(File("test.txt"))
        CentralControlComputer.money = 0
    }

    @Test
    fun `CCC release train`(){
        CentralControlComputer.run()
        assert(CentralControlComputer.money == 200)
    }

    @Test
    fun `CCC find building project`(){
        CentralControlComputer.stations.forEach { it.tick() }
        Clock.tick()
        CentralControlComputer.calculateNewBuildingProject()
        val nt = CentralControlComputer.nextTask!!
        assert(nt.first == 3 * CentralControlComputer.railwayCost)
    }

    @Test
    fun `CCC build task when affordable and update`(){
        CentralControlComputer.stations.forEach { it.tick() }
        Clock.tick()
        CentralControlComputer.money = 3 * CentralControlComputer.railwayCost
        CentralControlComputer.calculateNewBuildingProject()
        assert(CentralControlComputer.railways.size == 3)
        assert(CentralControlComputer.money == 0)

        CentralControlComputer.money = 3 * CentralControlComputer.railwayCost
        CentralControlComputer.tick()
        assert(CentralControlComputer.railways.size == 4)
        assert(CentralControlComputer.railways.filter { it.start.name == "Panama" || it.end.name == "Panama" }.size == 3)
    }



    companion object {
        @JvmStatic
        @AfterAll
        fun `delete test-txt`() {
            File("test.txt").delete()
        }
        @JvmStatic
        @BeforeAll
        fun `create test-txt`(){
            File("test.txt").apply {
                createNewFile()
                writeText("Panama: 0,0; Brazil" +
                        System.lineSeparator() + "Brazil: 2,3; Panama" +
                        System.lineSeparator() + "Astana: 8,8;" +
                        System.lineSeparator() + "Brazil-Panama" +
                        System.lineSeparator() + "Brazil-Astana")
            }
        }
    }
}