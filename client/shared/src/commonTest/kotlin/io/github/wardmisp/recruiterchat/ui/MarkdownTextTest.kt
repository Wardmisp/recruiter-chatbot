package io.github.wardmisp.recruiterchat.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownTextTest {

    private fun urls(text: String) = markdownToAnnotatedString(text, Color.Blue).let { result ->
        result.getLinkAnnotations(0, result.length).map { (it.item as LinkAnnotation.Url).url }
    }

    @Test
    fun stripsMarkdownSyntax() {
        val result = markdownToAnnotatedString("### Profil\n- **Kotlin** et [GitHub](https://github.com/Wardmisp)", Color.Blue)
        assertEquals("Profil\n• Kotlin et GitHub", result.text)
    }

    @Test
    fun linksUrlsAndEmails() {
        assertEquals(
            listOf("https://github.com/Wardmisp", "mailto:remi.mialonevan@gmail.com"),
            urls("Voir https://github.com/Wardmisp. Contact : remi.mialonevan@gmail.com."),
        )
    }
}
