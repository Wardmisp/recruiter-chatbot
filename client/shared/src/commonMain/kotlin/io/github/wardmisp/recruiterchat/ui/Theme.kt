package io.github.wardmisp.recruiterchat.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import recruiterchat.shared.generated.resources.Res
import recruiterchat.shared.generated.resources.fraunces_semibold
import recruiterchat.shared.generated.resources.public_sans_medium
import recruiterchat.shared.generated.resources.public_sans_regular
import recruiterchat.shared.generated.resources.public_sans_semibold

/**
 * Thème noir unique (Android et web), assorti à la page d'accueil « éditoriale ».
 * L'accent est le vermillon de la page, éclairci pour rester lisible sur le noir :
 * #F06B45 fait 5,9:1 sur le fond et 5:1 sur les bulles ; le texte posé dessus est noir.
 */
private val Colors = darkColorScheme(
    primary = Color(0xFFF06B45),
    onPrimary = Color(0xFF161616),
    primaryContainer = Color(0xFF3A1F17),
    onPrimaryContainer = Color(0xFFFFDBD0),
    background = Color(0xFF161616),
    onBackground = Color(0xFFF2F1ED),
    surface = Color(0xFF161616),
    onSurface = Color(0xFFF2F1ED),
    surfaceVariant = Color(0xFF262522),
    onSurfaceVariant = Color(0xFFC9C5BB),
    surfaceContainer = Color(0xFF1F1E1C),
    surfaceContainerHigh = Color(0xFF262522),
    outline = Color(0xFF6F6C65),
    outlineVariant = Color(0xFF3A3935),
)

@Composable
private fun appTypography(): Typography {
    val body = FontFamily(
        Font(Res.font.public_sans_regular, FontWeight.Normal),
        Font(Res.font.public_sans_medium, FontWeight.Medium),
        Font(Res.font.public_sans_semibold, FontWeight.SemiBold),
    )
    val display = FontFamily(Font(Res.font.fraunces_semibold, FontWeight.SemiBold))
    val base = Typography()
    fun TextStyle.body() = copy(fontFamily = body)
    // Titres en Fraunces comme sur la page, tout le reste en Public Sans.
    fun TextStyle.display() = copy(fontFamily = display, fontWeight = FontWeight.SemiBold)
    return Typography(
        displayLarge = base.displayLarge.display(),
        displayMedium = base.displayMedium.display(),
        displaySmall = base.displaySmall.display(),
        headlineLarge = base.headlineLarge.display(),
        headlineMedium = base.headlineMedium.display(),
        headlineSmall = base.headlineSmall.display(),
        titleLarge = base.titleLarge.display(),
        titleMedium = base.titleMedium.display(),
        titleSmall = base.titleSmall.body(),
        bodyLarge = base.bodyLarge.body(),
        bodyMedium = base.bodyMedium.body(),
        bodySmall = base.bodySmall.body(),
        labelLarge = base.labelLarge.body(),
        labelMedium = base.labelMedium.body(),
        labelSmall = base.labelSmall.body(),
    )
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = appTypography(), content = content)
}
