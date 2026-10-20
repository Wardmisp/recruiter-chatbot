package io.github.wardmisp.recruiterchat.ui

import androidx.compose.runtime.Composable

/**
 * Vrai quand les polices du thème sont prêtes à l'affichage. Sur le web, elles se téléchargent
 * après le premier affichage (le texte serait d'abord invisible) ; sur Android, elles le sont toujours.
 */
@Composable
internal expect fun rememberFontsLoaded(): Boolean
