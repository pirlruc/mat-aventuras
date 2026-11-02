package pt.mataventuras.app.ui.lesson

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.mataventuras.app.ui.UiLogic
import pt.mataventuras.app.ui.groupTokenArgb
import pt.mataventuras.app.ui.isBoardHole
import pt.mataventuras.app.ui.playCellHeightDp
import pt.mataventuras.app.ui.shapeKind
import pt.mataventuras.app.ui.showsShapeGlyph
import pt.mataventuras.app.ui.starCenters
import pt.mataventuras.app.ui.starVertices
import pt.mataventuras.app.ui.triangleVertices
import pt.mataventuras.domain.math.Exercise
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule

@Composable
internal fun CipherPanel(exercise: Exercise) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        exercise.play.cells.forEach { line ->
            Text(line, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            exercise.play.cipherCode,
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0D47A1),
        )
    }
}

@Composable
internal fun GroupsBoard(
    exercise: Exercise,
    age: AgeBand,
) {
    val columns = exercise.play.columns.coerceAtLeast(1)
    val cellDp = playCellHeightDp(columns, age)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        exercise.play.cells.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { cell ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .size(cellDp.dp)
                                .background(Color(groupTokenArgb(cell))),
                    ) {
                        Text(cell, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}

@Composable
internal fun PuzzleFrame(
    exercise: Exercise,
    age: AgeBand,
) {
    val cells = puzzleCells(exercise.play.cells)
    val columns = exercise.play.columns.coerceAtLeast(2)
    val cellDp = playCellHeightDp(columns, age)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cells.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { cell ->
                    val hole = isBoardHole(cell)
                    Button(
                        onClick = {},
                        enabled = false,
                        colors =
                            ButtonDefaults.buttonColors(
                                disabledContainerColor = if (hole) Color(0xFFFFE082) else Color(0xFFBBDEFB),
                                disabledContentColor = Color(0xFF0D47A1),
                            ),
                        modifier = Modifier.size(cellDp.dp),
                    ) {
                        GridCellFace(module = exercise.module, cell = cell)
                    }
                }
            }
        }
    }
}

@Composable
internal fun VisualOption(
    module: LearningModule,
    text: String,
) {
    if (module == LearningModule.SIZE) {
        SizeMark(text)
        return
    }
    val shape = if (showsShapeGlyph(module)) shapeKind(text) else null
    val dots = visualDotCount(module, text)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (shape != null) {
            ShapeGlyph(shape)
        } else if (dots > 0) {
            DotStrip(dots)
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = if (shape != null) 18.sp else 28.sp)
    }
}

@Composable
internal fun NumberHero(value: Int) {
    Text(
        text = value.toString(),
        fontSize = 88.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF0D47A1),
    )
}

@Composable
internal fun DotStrip(count: Int) {
    Canvas(modifier = Modifier.size(72.dp, 36.dp)) {
        paintCountDots(count)
    }
}

internal fun DrawScope.paintCountDots(count: Int) {
    starCenters(count, size.minDimension).forEach { (cx, cy) ->
        drawCircle(Color(0xFFFFC107), radius = 7f, center = Offset(cx, cy))
    }
}

@Composable
internal fun ShapeGlyph(shape: GeometricShape) {
    Canvas(modifier = Modifier.size(48.dp)) {
        paintShapeGlyph(shape)
    }
}

internal fun DrawScope.paintShapeGlyph(shape: GeometricShape) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val radius = size.minDimension / 2.4f
    val color = Color(0xFF1565C0)
    when (shape) {
        GeometricShape.CIRCLE -> drawCircle(color, radius = radius, center = Offset(cx, cy))
        GeometricShape.SQUARE -> {
            val side = radius * 1.6f
            drawRect(
                color,
                topLeft = Offset(cx - side / 2f, cy - side / 2f),
                size = Size(side, side),
            )
        }
        GeometricShape.RECTANGLE -> {
            val width = radius * 2.1f
            val height = radius * 1.2f
            drawRect(
                color,
                topLeft = Offset(cx - width / 2f, cy - height / 2f),
                size = Size(width, height),
            )
        }
        GeometricShape.TRIANGLE -> {
            val pts = triangleVertices(cx, cy, radius)
            drawPath(
                Path().apply {
                    moveTo(pts[0].first, pts[0].second)
                    lineTo(pts[1].first, pts[1].second)
                    lineTo(pts[2].first, pts[2].second)
                    close()
                },
                color,
            )
        }
        GeometricShape.STAR -> {
            val pts = starVertices(cx, cy, radius)
            drawPath(
                Path().apply {
                    moveTo(pts[0].first, pts[0].second)
                    pts.drop(1).forEach { lineTo(it.first, it.second) }
                    close()
                },
                color,
            )
        }
    }
}

@Composable
internal fun CappedScale(content: @Composable () -> Unit) {
    val current = LocalDensity.current
    val scale = current.fontScale.coerceAtMost(UiLogic.MAX_FONT_SCALE)
    CompositionLocalProvider(LocalDensity provides Density(current.density, scale)) {
        content()
    }
}

@Composable
internal fun StarGrid(count: Int) {
    Canvas(modifier = Modifier.size(220.dp)) {
        paintStarDots(count)
    }
}

internal fun DrawScope.paintStarDots(count: Int) {
    starCenters(count, size.minDimension).forEach { (cx, cy) ->
        drawCircle(Color(0xFFFFC107), radius = 16f, center = Offset(cx, cy))
    }
}
