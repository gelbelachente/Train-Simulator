import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

internal class ClockTest {

    @Test
    fun `Clock tick works`() {
        assert(Clock.time == 0)
        repeat(5) {
            Clock.tick()
        }
        assert(Clock.time == 5)
    }

    @Test
    fun `Clock reset works`() {
        repeat(99) {
            Clock.tick()
        }
        Clock.reset()
        assert(Clock.time == 0)
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun `reset Clock after test completion`() {
            Clock.reset()
        }
    }

}

