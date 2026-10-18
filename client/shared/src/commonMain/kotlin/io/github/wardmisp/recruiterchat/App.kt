package io.github.wardmisp.recruiterchat

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.wardmisp.recruiterchat.ui.AppTheme
import io.github.wardmisp.recruiterchat.ui.MainScreen

/** Point d'entrée commun : appelé par MainActivity (Android) et par main() (web). */
@Composable
fun App() {
    AppTheme {
        MainScreen(viewModel { ChatViewModel() })
    }
}
