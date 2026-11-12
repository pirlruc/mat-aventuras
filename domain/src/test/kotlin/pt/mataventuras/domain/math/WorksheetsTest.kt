package pt.mataventuras.domain.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.AgeBand

class WorksheetsTest {
    @Test
    fun sameSeedRepeatsBothAgeBands() {
        val young = Worksheets.build(AgeBand.THREE_TO_FIVE, 11L)
        val older = Worksheets.build(AgeBand.SIX_TO_EIGHT, 11L)
        assertEquals(young, Worksheets.build(AgeBand.THREE_TO_FIVE, 11L))
        assertEquals(older, Worksheets.build(AgeBand.SIX_TO_EIGHT, 11L))
        assertTrue(young.exercises.isNotEmpty())
        assertTrue(older.exercises.isNotEmpty())
        assertTrue(young.exercises.all { it.prompt.isNotBlank() && it.options.isNotEmpty() })
        assertTrue(older.exercises.all { it.prompt.isNotBlank() && it.options.isNotEmpty() })
    }

    @Test
    fun pagesStartWithTheTitleAndSplit() {
        val sheet = Worksheets.build(AgeBand.THREE_TO_FIVE, 4L)
        val pages = Worksheets.pages(sheet, linesPerPage = 2)
        assertTrue(pages.size > 1)
        assertEquals(Worksheets.TITLE, pages.first().first())
        assertEquals(Worksheets.pages(sheet), Worksheets.pages(sheet))
    }
}
