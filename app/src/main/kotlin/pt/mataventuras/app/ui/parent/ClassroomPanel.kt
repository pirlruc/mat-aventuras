package pt.mataventuras.app.ui.parent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.OutputStream
import kotlinx.coroutines.flow.first
import pt.mataventuras.app.R
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.domain.parent.ClassroomChild
import pt.mataventuras.domain.parent.classroomCsv
import pt.mataventuras.domain.parent.masteryGrid

/**
 * Teacher view of every Room profile. Export uses the document picker.
 */
@Composable
internal fun ClassroomPanel(
    container: AppContainer,
    unlocked: Boolean,
) {
    if (!unlocked) return
    var children by remember { mutableStateOf<List<ClassroomChild>>(emptyList()) }
    LaunchedEffect(Unit) {
        children =
            container.repository.observeProfiles().first().map { profile ->
                val sessions = container.repository.sessionsFor(profile.id)
                ClassroomChild(profile.name, masteryGrid(container.analytics.summarise(profile.id, sessions)))
            }
    }
    val csv = classroomCsv(children)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.classroom_heading), fontWeight = FontWeight.Bold)
        if (children.isEmpty()) {
            Text(stringResource(R.string.classroom_no_play))
        }
        children.forEach { child ->
            Text(child.name, fontWeight = FontWeight.Bold)
            MasteryHeatmap(child.cells, heading = false)
        }
        ClassroomExport(csv)
    }
}

@Composable
private fun ClassroomExport(csv: String) {
    val context = LocalContext.current
    val open = { uri: Uri -> context.contentResolver.openOutputStream(uri) }
    val csvPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            writeTextIfReady(csv, uri, open)
        }
    val pdfPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
            writeClassroomPdf(csv, uri, open)
        }
    Button(
        onClick = { csvPicker.launch("turma.csv") },
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
    ) { Text(stringResource(R.string.classroom_csv)) }
    Button(
        onClick = { pdfPicker.launch("turma.pdf") },
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
    ) { Text(stringResource(R.string.classroom_pdf)) }
}

internal fun writeClassroomPdf(
    csv: String,
    uri: Uri?,
    open: (Uri) -> OutputStream?,
) {
    if (uri == null) return
    val stream = open(uri) ?: return
    stream.use { writeLinesPdf(csv.lines(), it) }
}
