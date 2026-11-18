package pt.mataventuras.domain.parent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.LearningModule

class ClassroomReportTest {
    @Test
    fun csvKeepsAnEmptyChildAndQuotesCommas() {
        val empty = ClassroomChild("Ana", emptyList())
        val played =
            ClassroomChild(
                "Rui, \"R\"",
                listOf(
                    MasteryCell(LearningModule.ADDITION, "Somas", MasteryBand.FROM_80, 4, 1),
                ),
            )
        val csv = classroomCsv(listOf(empty, played))
        assertTrue(csv.startsWith("name,module,band,hits,misses\n"))
        assertTrue(csv.contains("Ana,,,0,0"))
        assertTrue(csv.contains("\"Rui, \"\"R\"\"\",ADDITION,FROM_80,4,1"))
        assertEquals(3, csv.lines().size)
    }
}
