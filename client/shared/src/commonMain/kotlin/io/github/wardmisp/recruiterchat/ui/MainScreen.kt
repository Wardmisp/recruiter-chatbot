package io.github.wardmisp.recruiterchat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.wardmisp.recruiterchat.ChatViewModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import recruiterchat.shared.generated.resources.Res
import recruiterchat.shared.generated.resources.new_conversation
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
    val chatState by chatViewModel.state.collectAsStateWithLifecycle()

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        // Sur un grand écran (web), le contenu reste lisible au centre.
        Box(Modifier.safeDrawingPadding().imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.fillMaxHeight().widthIn(max = 760.dp).fillMaxWidth()) {
                Header(
                    // Visible seulement sur l'onglet Chat, où l'on voit ce qui sera effacé.
                    showNewConversation = selectedTab == MainTab.Chat,
                    newConversationEnabled = chatState.canStartNewConversation,
                    onNewConversation = chatViewModel::newConversation,
                )
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
private fun Header(
    showNewConversation: Boolean,
    newConversationEnabled: Boolean,
    onNewConversation: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TitleBlock(Modifier.weight(1f))
        if (showNewConversation) {
            TextButton(onClick = onNewConversation, enabled = newConversationEnabled) {
                Text(stringResource(Res.string.new_conversation))
            }
        }
    }
}

@Composable
private fun TitleBlock(modifier: Modifier) {
    Column(modifier) {
        Text(
            stringResource(Res.string.title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            stringResource(Res.string.subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
