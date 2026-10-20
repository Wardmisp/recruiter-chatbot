package io.github.wardmisp.recruiterchat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont
import recruiterchat.shared.generated.resources.Res
import recruiterchat.shared.generated.resources.fraunces_semibold
import recruiterchat.shared.generated.resources.public_sans_medium
import recruiterchat.shared.generated.resources.public_sans_regular
import recruiterchat.shared.generated.resources.public_sans_semibold

/** Mêmes polices et graisses que [AppTheme]. */
@OptIn(ExperimentalResourceApi::class)
@Composable
internal actual fun rememberFontsLoaded(): Boolean {
    val fonts = listOf(
        preloadFont(Res.font.public_sans_regular, FontWeight.Normal),
        preloadFont(Res.font.public_sans_medium, FontWeight.Medium),
        preloadFont(Res.font.public_sans_semibold, FontWeight.SemiBold),
        preloadFont(Res.font.fraunces_semibold, FontWeight.SemiBold),
    )
    return fonts.all { it.value != null }
}
