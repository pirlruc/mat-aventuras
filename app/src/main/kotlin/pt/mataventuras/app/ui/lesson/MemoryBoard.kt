package pt.mataventuras.app.ui.lesson

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.mataventuras.app.ui.debugAnswerTags
import pt.mataventuras.domain.math.Exercise
import pt.mataventuras.domain.math.MemoryFace

private const val FLIP_MS: Int = 300

/**
 * Pip count encoded as `qty:N`, or null when [label] is an ordinary face.
 */
internal fun quantityPips(label: String): Int? {
    if (!label.startsWith("qty:")) return null
    return label.removePrefix("qty:").toIntOrNull()?.takeIf { it in 1..6 }
}

/** The front is visible on the near side of a Y flip. */
internal fun showsCardFront(rotation: Float): Boolean = rotation <= 90f

/** Closed debug cards expose the pair key. A release build does not. */
internal fun memoryCardTag(
    open: Boolean,
    debug: Boolean,
    pips: Int?,
    pairKey: String,
): String =
    when {
        !open && debug -> "par-$pairKey"
        pips != null -> "memory-pip"
        else -> "memory-face"
    }

internal fun memoryCardDescription(
    open: Boolean,
    debug: Boolean,
    pips: Int?,
    pairKey: String,
    label: String,
): String =
    when {
        !open && debug -> "par-$pairKey"
        !open -> "carta fechada"
        pips != null -> pips.toString()
        else -> label
    }

/**
 * Every pair stays open. The lesson advances only when the board is finished.
 */
@Composable
internal fun MemoryBoard(
    exercise: Exercise,
    onPick: (Int) -> Unit,
) {
    val boardKey = LocalExerciseId.current
    var face by remember(boardKey) { mutableStateOf(MemoryFace()) }
    val solved = face.solved(exercise.play.cells.size)
    LaunchedEffect(solved) {
        if (solved) onPick(exercise.correctIndex)
    }
    val columns = exercise.play.columns.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        exercise.play.cells.chunked(columns).forEachIndexed { row, labels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                labels.forEachIndexed { column, label ->
                    val index = row * columns + column
                    val open = index in face.matched || index in face.up
                    MemoryCard(
                        label = label,
                        pairKey = exercise.play.solution[index],
                        open = open,
                        onClick = { face = face.reveal(exercise.play, index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.MemoryCard(
    label: String,
    pairKey: String,
    open: Boolean,
    onClick: () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (open) 0f else 180f,
        animationSpec = tween(FLIP_MS),
        label = "card-flip",
    )
    val showFront = showsCardFront(rotation)
    val pips = quantityPips(label)
    val density = LocalDensity.current.density
    val tag = memoryCardTag(open, debugAnswerTags, pips, pairKey)
    val spoken = memoryCardDescription(open, debugAnswerTags, pips, pairKey, label)
    Box(
        modifier =
            Modifier
                .weight(1f)
                .height(72.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }.clip(RoundedCornerShape(12.dp))
                .background(if (showFront) Color(0xFFFFF8E1) else Color(0xFF0D47A1))
                .testTag(tag)
                .clickable(onClick = onClick)
                .semantics(mergeDescendants = true) {
                    role = Role.Button
                    contentDescription = spoken
                },
        contentAlignment = Alignment.Center,
    ) {
        if (showFront) {
            MemoryFront(label, pips)
        } else {
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) {
                paintCardBack()
            }
        }
    }
}

@Composable
private fun MemoryFront(
    label: String,
    pips: Int?,
) {
    if (pips != null) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp).testTag("pips-drawn")) {
            paintPips(pips)
        }
    } else {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color(0xFF0D47A1))
    }
}

internal fun DrawScope.paintCardBack() {
    drawRect(Color(0xFF0D47A1))
    val stripe = Color(0xFF1565C0)
    var band = 0
    while (band < 6) {
        val y = size.height * band / 6f
        drawLine(stripe, Offset(0f, y), Offset(size.width, y + 10f), strokeWidth = 8f)
        band += 1
    }
    drawCircle(Color(0xFFFFC107), radius = size.minDimension / 5f, center = center)
}

internal fun DrawScope.paintPips(count: Int) {
    val radius = size.minDimension / 7f
    pipOffsets(count).forEach { (x, y) ->
        drawCircle(
            Color(0xFF0D47A1),
            radius = radius,
            center = Offset(size.width * x, size.height * y),
        )
    }
}

internal fun pipOffsets(count: Int): List<Pair<Float, Float>> =
    when (count) {
        1 -> listOf(0.5f to 0.5f)
        2 -> listOf(0.28f to 0.28f, 0.72f to 0.72f)
        3 -> listOf(0.28f to 0.28f, 0.5f to 0.5f, 0.72f to 0.72f)
        4 -> listOf(0.28f to 0.28f, 0.72f to 0.28f, 0.28f to 0.72f, 0.72f to 0.72f)
        5 -> listOf(0.28f to 0.28f, 0.72f to 0.28f, 0.5f to 0.5f, 0.28f to 0.72f, 0.72f to 0.72f)
        else ->
            listOf(
                0.32f to 0.22f,
                0.68f to 0.22f,
                0.32f to 0.5f,
                0.68f to 0.5f,
                0.32f to 0.78f,
                0.68f to 0.78f,
            )
    }
