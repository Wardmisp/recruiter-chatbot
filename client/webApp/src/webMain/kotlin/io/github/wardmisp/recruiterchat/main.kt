package io.github.wardmisp.recruiterchat

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

private external class URLSearchParams(init: String) {
    fun get(name: String): String?
}

private external interface Location {
    val search: String
    val pathname: String
    val origin: String
}

private external interface History {
    fun replaceState(data: String?, unused: String, url: String)
}

private external interface Window {
    val parent: Window
    fun postMessage(message: String, targetOrigin: String)
}

private external val location: Location
private external val history: History
private external val window: Window

/** Lu par la page d'accueil (site/index.html, READY_MESSAGE) pour remplacer sa copie du composant. */
private const val READY_MESSAGE = "recruiterchat:ready"

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val historyStore = LocalStorageChatHistoryStore()
    // chat/?q=… : suggestion cliquée sur la page d'accueil, envoyée dès l'ouverture.
    val params = URLSearchParams(location.search)
    val initialQuestion = params.get("q")?.takeIf { it.isNotBlank() }
    // chat/?tab=cv : onglet CV cliqué dans la copie du composant.
    val startOnCv = params.get("tab") == "cv"
    // Retirée de l'adresse (la langue, lue par index.html, y reste) : un rechargement de la page
    // ne la renverrait pas une seconde fois.
    if (initialQuestion != null) {
        val lang = params.get("lang")
        history.replaceState(null, "", location.pathname + if (lang != null) "?lang=$lang" else "")
    }
    ComposeViewport {
        // Appli affichée avec ses polices et ses textes : la page d'accueil peut montrer l'iframe.
        // Ouverte seule (sans page parente), l'appli s'envoie le message à elle-même, sans effet.
        App(historyStore, initialQuestion, startOnCv, onReady = {
            window.parent.postMessage(READY_MESSAGE, location.origin)
        })
    }
}
