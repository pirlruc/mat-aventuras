package pt.mataventuras.app.ui.parent

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.io.OutputStream
import pt.mataventuras.domain.math.Worksheet
import pt.mataventuras.domain.math.Worksheets

/**
 * Writes [worksheet] as a PDF. The page list comes from [Worksheets.pages].
 */
internal object WorksheetPdf {
    /**
     * Writes every page to [output].
     */
    fun write(
        worksheet: Worksheet,
        output: OutputStream,
    ) {
        val document = PdfDocument()
        try {
            render(worksheet) { index, lines ->
                val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
                val page = document.startPage(info)
                paintLines(page.canvas, lines)
                document.finishPage(page)
            }
            document.writeTo(output)
        } finally {
            document.close()
        }
    }

    private const val PAGE_WIDTH: Int = 595
    private const val PAGE_HEIGHT: Int = 842
}

/**
 * Paints [worksheet] one page at a time. Tests supply the page callback.
 */
internal fun render(
    worksheet: Worksheet,
    page: (Int, List<String>) -> Unit,
) {
    Worksheets.pages(worksheet).forEachIndexed { index, lines -> page(index, lines) }
}

/**
 * Draws [lines] down [canvas].
 */
internal fun paintLines(
    canvas: Canvas,
    lines: List<String>,
) {
    val paint =
        Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }
    var y = 48f
    lines.forEach { line ->
        canvas.drawText(line, 40f, y, paint)
        y += 18f
    }
}

/**
 * Writes [worksheet] when the parent picked a document. A missing stream is a no-op.
 */
internal fun writeWorksheetIfReady(
    worksheet: Worksheet?,
    uri: Uri?,
    open: (Uri) -> OutputStream?,
): Boolean {
    if (worksheet == null || uri == null) return false
    val stream = open(uri) ?: return false
    stream.use { WorksheetPdf.write(worksheet, it) }
    return true
}
