package io.github.wardmisp.recruiterchat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.wardmisp.recruiterchat.data.ChatHistoryStore
import io.github.wardmisp.recruiterchat.ui.AppTheme
import io.github.wardmisp.recruiterchat.ui.MainScreen
import io.github.wardmisp.recruiterchat.ui.rememberFontsLoaded
import org.jetbrains.compose.resources.getString
import recruiterchat.shared.generated.resources.Res
import recruiterchat.shared.generated.resources.welcome

/**
 * Point d'entrée commun : appelé par MainActivity (Android) et par main() (web),
 * qui fournissent chacun le stockage de la conversation propre à leur plateforme.
 *
 * [onReady] est appelé une fois l'écran affiché avec ses polices et ses textes
 * (chargés de façon asynchrone sur le web) : la page d'accueil attend ce signal
 * pour remplacer sa copie du composant par l'appli.
 */
@Composable
fun App(
    historyStore: ChatHistoryStore = ChatHistoryStore.None,
    initialQuestion: String? = null,
    startOnCv: Boolean = false,
    onReady: () -> Unit = {},
) {
    val fontsLoaded = rememberFontsLoaded()
    AppTheme {
        MainScreen(viewModel { ChatViewModel(history = historyStore, initialQuestion = initialQuestion) }, startOnCv)
    }
    LaunchedEffect(fontsLoaded) {
        if (!fontsLoaded) return@LaunchedEffect
        getString(Res.string.welcome)
        // Deux images : la recomposition avec les polices, puis son affichage.
        withFrameNanos { }
        withFrameNanos { }
        onReady()
    }
}
