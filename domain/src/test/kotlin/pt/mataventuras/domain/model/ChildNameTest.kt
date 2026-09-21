package pt.mataventuras.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ChildNameTest {
    @Test
    fun dropsControlsCapsLengthAndKeepsAccents() {
        assertEquals(ChildName.PLACEHOLDER, ChildName.sanitize("  \n\t "))
        assertEquals(ChildName.PLACEHOLDER, ChildName.sanitize("\u0000"))
        assertEquals("Ana", ChildName.sanitize(" Ana\u0007 "))
        assertEquals("João", ChildName.sanitize("  João  "))
        assertEquals("Ana Maria", ChildName.sanitize("Ana   Maria"))
        assertEquals("B".repeat(ChildName.MAX_LENGTH), ChildName.sanitize("B".repeat(40)))
        assertEquals("C".repeat(23), ChildName.sanitize("C".repeat(23) + "   D"))
    }
}
