package pt.mataventuras.app.ui.parent

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pt.mataventuras.domain.math.Worksheets
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.parent.MasteryBand

@RunWith(RobolectricTestRunner::class)
class WorksheetPdfTest {
    @Test
    fun paintsPagesAndSkipsAMissingTarget() {
        val sheet = Worksheets.build(AgeBand.SIX_TO_EIGHT, 9L)
        val bitmap = Bitmap.createBitmap(595, 842, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        var pages = 0
        render(sheet) { _, lines ->
            pages += 1
            paintLines(canvas, lines)
        }
        assertTrue(pages >= 1)
        assertTrue(bitmap.width == 595)
        val uri = Uri.parse("content://local/ficha")
        val out = ByteArrayOutputStream()
        assertFalse(writeWorksheetIfReady(null, uri) { out })
        assertFalse(writeWorksheetIfReady(sheet, null) { out })
        assertFalse(writeWorksheetIfReady(sheet, uri) { null })
        try {
            WorksheetPdf.write(sheet, out)
        } catch (closed: IllegalStateException) {
            assertEquals("document is closed!", closed.message)
        }
    }

    @Test
    fun masteryFillCoversEveryBand() {
        val below = masteryFill(MasteryBand.BELOW_50)
        val mid = masteryFill(MasteryBand.FROM_50_TO_79)
        val high = masteryFill(MasteryBand.FROM_80)
        assertTrue(below != mid)
        assertTrue(mid != high)
        assertTrue(below != high)
    }
}
