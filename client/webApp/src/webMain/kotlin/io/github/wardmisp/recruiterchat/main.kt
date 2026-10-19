package io.github.wardmisp.recruiterchat

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

private external class URLSearchParams(init: String) {
    fun get(name: String): String?
}

private external interface Location {
    val search: String
    val pathname: String
}

private external interface History {
    fun replaceState(data: String?, unused: String, url: String)
}

private external val location: Location
private external val history: History

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val historyStore = LocalStorageChatHistoryStore()
    // chat/?q=… : suggestion cliquée sur la page d'accueil, envoyée dès l'ouverture.
    val params = URLSearchParams(location.search)
    val initialQuestion = params.get("q")?.takeIf { it.isNotBlank() }
    // Retirée de l'adresse (la langue, lue par index.html, y reste) : un rechargement de la page
    // ne la renverrait pas une seconde fois.
    if (initialQuestion != null) {
        val lang = params.get("lang")
        history.replaceState(null, "", location.pathname + if (lang != null) "?lang=$lang" else "")
    }
    ComposeViewport {
        App(historyStore, initialQuestion)
    }
}
