package io.github.wardmisp.recruiterchat

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.wardmisp.recruiterchat.data.ChatHistoryStore
import io.github.wardmisp.recruiterchat.ui.AppTheme
import io.github.wardmisp.recruiterchat.ui.MainScreen

/**
 * Point d'entrée commun : appelé par MainActivity (Android) et par main() (web),
 * qui fournissent chacun le stockage de la conversation propre à leur plateforme.
 */
@Composable
fun App(historyStore: ChatHistoryStore = ChatHistoryStore.None) {
    AppTheme {
        MainScreen(viewModel { ChatViewModel(history = historyStore) })
    }
}
