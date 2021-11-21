import junit.framework.Assert.assertEquals
import org.junit.Test

class SimpleTest {

    @Test
    fun dropTest() {
        val list = arrayListOf<Int>(1, 2, 3, 4, 5, 6, 7)
        list.drop(3)
        assertEquals(4, list.size)
    }

    @Test
    fun raneTest() {
        for (i in 1..4) println(i)
    }
}
