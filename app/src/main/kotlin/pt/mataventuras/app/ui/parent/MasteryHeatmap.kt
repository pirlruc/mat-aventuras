package pt.mataventuras.app.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.mataventuras.app.R
import pt.mataventuras.app.ui.home.ModuleTitles
import pt.mataventuras.domain.parent.MasteryBand
import pt.mataventuras.domain.parent.MasteryCell

/**
 * Module-by-topic grid. Colour and text both name the mastery band.
 */
@Composable
internal fun MasteryHeatmap(cells: List<MasteryCell>) {
    Text(stringResource(R.string.mastery_map), fontWeight = FontWeight.Bold)
    if (cells.isEmpty()) {
        Text(stringResource(R.string.no_sessions))
        return
    }
    cells.groupBy { it.topic }.forEach { (topic, row) ->
        Text(topic, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { cell -> MasteryCellCard(cell) }
        }
    }
}

@Composable
private fun MasteryCellCard(cell: MasteryCell) {
    Column(
        modifier =
            Modifier
                .widthIn(min = 120.dp)
                .background(masteryFill(cell.band))
                .padding(8.dp)
                .semantics { contentDescription = "${cell.topic}. ${cell.band.label}" },
    ) {
        Text(ModuleTitles.noun(cell.module))
        Text(cell.band.label, fontWeight = FontWeight.Bold)
    }
}

/**
 * Fill behind a mastery cell. The label still names the band.
 */
internal fun masteryFill(band: MasteryBand): Color =
    when (band) {
        MasteryBand.BELOW_50 -> Color(0xFFFFCDD2)
        MasteryBand.FROM_50_TO_79 -> Color(0xFFFFF9C4)
        MasteryBand.FROM_80 -> Color(0xFFC8E6C9)
    }
