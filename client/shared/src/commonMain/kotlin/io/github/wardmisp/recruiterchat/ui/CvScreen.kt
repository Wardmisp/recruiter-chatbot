package io.github.wardmisp.recruiterchat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import io.github.wardmisp.recruiterchat.Config
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import recruiterchat.shared.generated.resources.Res
import recruiterchat.shared.generated.resources.cv_en_title
import recruiterchat.shared.generated.resources.cv_format
import recruiterchat.shared.generated.resources.cv_fr_title
import recruiterchat.shared.generated.resources.cv_intro
import recruiterchat.shared.generated.resources.cv_open

private data class Resume(val title: StringResource, val url: String)

@Composable
fun CvScreen(modifier: Modifier = Modifier) {
    val french = Resume(Res.string.cv_fr_title, Config.CV_FR_URL)
    val english = Resume(Res.string.cv_en_title, Config.CV_EN_URL)
    // Le CV dans la langue de l'appareil en premier.
    val resumes = if (Locale.current.language == "fr") listOf(french, english) else listOf(english, french)

    Column(
        modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(Res.string.cv_intro), style = MaterialTheme.typography.bodyMedium)
        resumes.forEach { ResumeCard(it) }
    }
}

@Composable
private fun ResumeCard(resume: Resume) {
    val uriHandler = LocalUriHandler.current
    OutlinedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(resume.title), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(Res.string.cv_format),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = { uriHandler.openUri(resume.url) }) {
                Text(stringResource(Res.string.cv_open))
            }
        }
    }
}
