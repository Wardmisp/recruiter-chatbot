package io.github.wardmisp.recruiterchat.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.wardmisp.recruiterchat.AnswerPhase
import io.github.wardmisp.recruiterchat.ChatMessage
import io.github.wardmisp.recruiterchat.ChatUiState
import io.github.wardmisp.recruiterchat.ChatViewModel
import io.github.wardmisp.recruiterchat.Config
import io.github.wardmisp.recruiterchat.Role
import io.github.wardmisp.recruiterchat.ServerStatus
import io.github.wardmisp.recruiterchat.data.ChatError
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import recruiterchat.shared.generated.resources.Res
import recruiterchat.shared.generated.resources.char_count
import recruiterchat.shared.generated.resources.checking
import recruiterchat.shared.generated.resources.contact_email
import recruiterchat.shared.generated.resources.contact_linkedin
import recruiterchat.shared.generated.resources.disclaimer
import recruiterchat.shared.generated.resources.dismiss
import recruiterchat.shared.generated.resources.error_busy
import recruiterchat.shared.generated.resources.error_rate_limited
import recruiterchat.shared.generated.resources.error_timeout
import recruiterchat.shared.generated.resources.error_too_long
import recruiterchat.shared.generated.resources.error_unavailable
import recruiterchat.shared.generated.resources.input_placeholder
import recruiterchat.shared.generated.resources.retry
import recruiterchat.shared.generated.resources.send
import recruiterchat.shared.generated.resources.suggestion_1
import recruiterchat.shared.generated.resources.suggestion_2
import recruiterchat.shared.generated.resources.suggestion_3
import recruiterchat.shared.generated.resources.thinking
import recruiterchat.shared.generated.resources.unavailable
import recruiterchat.shared.generated.resources.welcome

@Composable
fun ChatScreen(viewModel: ChatViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ChatContent(
        state = state,
        onSend = viewModel::send,
        onRetry = viewModel::checkServer,
        onDismissError = viewModel::dismissError,
        modifier = modifier,
    )
}

@Composable
fun ChatContent(
    state: ChatUiState,
    onSend: (String) -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        when (state.server) {
            ServerStatus.Checking -> CheckingPanel(Modifier.weight(1f))
            ServerStatus.Unavailable -> UnavailablePanel(onRetry, Modifier.weight(1f))
            ServerStatus.Available -> {
                MessageList(state, onSend, Modifier.weight(1f))
                state.error?.let { ErrorBanner(it, onDismissError) }
                InputBar(canSend = state.canSend, onSend = onSend)
            }
        }
        Text(
            stringResource(Res.string.disclaimer),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MessageList(state: ChatUiState, onSend: (String) -> Unit, modifier: Modifier) {
    val listState = rememberLazyListState()
    // Affichage inversé : l'élément 0 est en bas, la liste reste donc collée à la réponse
    // qui s'écrit. On y revient à chaque nouveau message.
    LaunchedEffect(state.messages.size) { listState.scrollToItem(0) }

    val waiting = state.phase == AnswerPhase.Waiting || state.phase == AnswerPhase.Thinking
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        reverseLayout = true,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (waiting) {
            item(key = "typing") { TypingIndicator(thinking = state.phase == AnswerPhase.Thinking) }
        }
        items(state.messages.filter { it.text.isNotEmpty() }.asReversed(), key = ChatMessage::id) {
            MessageBubble(it.role, it.text)
        }
        if (state.messages.isEmpty()) {
            item(key = "suggestions") { Suggestions(enabled = state.canSend, onSend = onSend) }
        }
        item(key = "welcome") { MessageBubble(Role.Assistant, stringResource(Res.string.welcome)) }
    }
}

@Composable
private fun MessageBubble(role: Role, text: String) {
    val isUser = role == Role.User
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(start = if (isUser) 48.dp else 0.dp, end = if (isUser) 0.dp else 48.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = if (isUser) colors.primary else colors.surfaceVariant,
            contentColor = if (isUser) colors.onPrimary else colors.onSurfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
            ),
        ) {
            val content = remember(text, isUser, colors.primary) {
                if (isUser) AnnotatedString(text) else markdownToAnnotatedString(text, colors.primary)
            }
            SelectionContainer {
                Text(
                    content,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator(thinking: Boolean) {
    val transition = rememberInfiniteTransition()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                repeat(3) { index ->
                    val alpha by transition.animateFloat(
                        initialValue = 0.25f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 500),
                            repeatMode = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(index * 170),
                        ),
                    )
                    Box(
                        Modifier.size(8.dp).alpha(alpha)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape),
                    )
                }
            }
        }
        if (thinking) {
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(Res.string.thinking),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Suggestions(enabled: Boolean, onSend: (String) -> Unit) {
    val suggestions = listOf(Res.string.suggestion_1, Res.string.suggestion_2, Res.string.suggestion_3)
        .map { stringResource(it) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        suggestions.forEach { suggestion ->
            SuggestionChip(
                onClick = { onSend(suggestion) },
                label = { Text(suggestion) },
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun ErrorBanner(error: ChatError, onDismiss: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(error.message),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
            )
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.dismiss)) }
        }
    }
}

private val ChatError.message: StringResource
    get() = when (this) {
        ChatError.Unavailable -> Res.string.error_unavailable
        ChatError.Timeout -> Res.string.error_timeout
        ChatError.Busy -> Res.string.error_busy
        ChatError.RateLimited -> Res.string.error_rate_limited
        ChatError.MessageTooLong -> Res.string.error_too_long
    }

@Composable
private fun InputBar(canSend: Boolean, onSend: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    val submit = {
        if (canSend && text.isNotBlank()) {
            onSend(text)
            text = ""
        }
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= Config.MAX_MESSAGE_CHARS) text = it },
            modifier = Modifier.weight(1f).onPreviewKeyEvent { event ->
                // Entrée envoie, Maj+Entrée passe à la ligne (clavier physique, web).
                if (event.type == KeyEventType.KeyDown && event.key == Key.Enter && !event.isShiftPressed) {
                    submit()
                    true
                } else {
                    false
                }
            },
            placeholder = { Text(stringResource(Res.string.input_placeholder)) },
            supportingText = if (text.length > Config.MAX_MESSAGE_CHARS * 8 / 10) {
                { Text(stringResource(Res.string.char_count, text.length, Config.MAX_MESSAGE_CHARS)) }
            } else {
                null
            },
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { submit() }),
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = submit, enabled = canSend && text.isNotBlank()) {
            Text(stringResource(Res.string.send))
        }
    }
}

@Composable
private fun CheckingPanel(modifier: Modifier) {
    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.size(12.dp))
        Text(stringResource(Res.string.checking), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun UnavailablePanel(onRetry: () -> Unit, modifier: Modifier) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(Res.string.unavailable),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { uriHandler.openUri("mailto:${Config.CONTACT_EMAIL}") }) {
                Text(stringResource(Res.string.contact_email))
            }
            OutlinedButton(onClick = { uriHandler.openUri(Config.LINKEDIN_URL) }) {
                Text(stringResource(Res.string.contact_linkedin))
            }
        }
        Spacer(Modifier.size(8.dp))
        TextButton(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
    }
}
