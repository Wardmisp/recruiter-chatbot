package io.github.wardmisp.recruiterchat.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle

/*
 * Rendu minimal du Markdown produit par le modèle : **gras**, [texte](url), URL et
 * adresses e-mail nues, puces « - » et titres « ### ». Le reste est affiché tel quel.
 */

// Crochets et tirets toujours échappés : Kotlin/JS compile les regex en mode Unicode (flag u),
// plus strict que la JVM.
private val INLINE = Regex(
    """\*\*(.+?)\*\*""" +
        """|\[([^\]]+)\]\(((?:https?://|mailto:)[^)\s]+)\)""" +
        """|(https?://[^\s)\]]*[^\s)\].,;:!?])""" +
        """|([\w.+\-]+@[\w\-]+(?:\.[\w\-]+)+)""",
)
private val HEADING = Regex("""^#{1,6}\s+""")

fun markdownToAnnotatedString(text: String, linkColor: Color): AnnotatedString {
    val linkStyles = TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
    val normalized = text.lines().joinToString("\n") { line ->
        val trimmed = line.trimStart()
        when {
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> "• " + trimmed.drop(2)
            else -> line.replace(HEADING, "")
        }
    }
    return buildAnnotatedString {
        var cursor = 0
        for (match in INLINE.findAll(normalized)) {
            append(normalized.substring(cursor, match.range.first))
            val (bold, label, url, bareUrl, email) = match.destructured
            when {
                bold.isNotEmpty() -> withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(bold) }
                label.isNotEmpty() -> withLink(LinkAnnotation.Url(url, linkStyles)) { append(label) }
                bareUrl.isNotEmpty() -> withLink(LinkAnnotation.Url(bareUrl, linkStyles)) { append(bareUrl) }
                else -> withLink(LinkAnnotation.Url("mailto:$email", linkStyles)) { append(email) }
            }
            cursor = match.range.last + 1
        }
        append(normalized.substring(cursor))
    }
}
