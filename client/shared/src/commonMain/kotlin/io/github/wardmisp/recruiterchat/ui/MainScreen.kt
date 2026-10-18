package io.github.wardmisp.recruiterchat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.wardmisp.recruiterchat.ChatViewModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import recruiterchat.shared.generated.resources.Res
import recruiterchat.shared.generated.resources.subtitle
import recruiterchat.shared.generated.resources.tab_chat
import recruiterchat.shared.generated.resources.tab_cv
import recruiterchat.shared.generated.resources.title

private enum class MainTab(val label: StringResource) {
    Chat(Res.string.tab_chat),
    Cv(Res.string.tab_cv),
}

@Composable
fun MainScreen(chatViewModel: ChatViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Chat) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        // Sur un grand écran (web), le contenu reste lisible au centre.
        Box(Modifier.safeDrawingPadding().imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.fillMaxHeight().widthIn(max = 760.dp).fillMaxWidth()) {
                Header()
                PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
                    MainTab.entries.forEach { tab ->
                        Tab(
                            selected = tab == selectedTab,
                            onClick = { selectedTab = tab },
                            text = { Text(stringResource(tab.label)) },
                        )
                    }
                }
                // Le ViewModel vit au-dessus des onglets : la conversation survit au passage par l'onglet CV.
                when (selectedTab) {
                    MainTab.Chat -> ChatScreen(chatViewModel, Modifier.weight(1f))
                    MainTab.Cv -> CvScreen(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            stringResource(Res.string.title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            stringResource(Res.string.subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
