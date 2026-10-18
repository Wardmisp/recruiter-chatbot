package io.github.wardmisp.recruiterchat.data

import io.github.wardmisp.recruiterchat.Config
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.Json

/** Accès au backend du chatbot ; interface pour pouvoir le remplacer dans les tests. */
interface ChatService {
    /** GET /health : vrai si le serveur et le modèle sont prêts. */
    suspend fun isAvailable(): Boolean

    /** POST /chat : la réponse arrive morceau par morceau. Ne lève pas d'exception réseau. */
    fun ask(history: List<ApiMessage>): Flow<ChatEvent>
}

class ChatApi(
    private val baseUrl: String = Config.API_BASE_URL,
    // Sans moteur explicite, Ktor prend celui de la plateforme : OkHttp sur Android, fetch() sur le web.
    private val client: HttpClient = HttpClient {
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            // Le serveur coupe lui-même une génération trop longue (REQUEST_TIMEOUT_SECONDS).
            requestTimeoutMillis = 180_000
        }
    },
) : ChatService {

    override suspend fun isAvailable(): Boolean = try {
        client.get("$baseUrl/health") { timeout { requestTimeoutMillis = 5_000 } }
            .status == HttpStatusCode.OK
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        false
    }

    // channelFlow plutôt que flow : les événements sont émis depuis le bloc execute de Ktor.
    override fun ask(history: List<ApiMessage>): Flow<ChatEvent> = channelFlow {
        try {
            client.preparePost("$baseUrl/chat") {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(ChatRequest(history)))
            }.execute { response ->
                if (!response.status.isSuccess()) {
                    send(ChatEvent.Failure(httpError(response.status.value, response.bodyAsText())))
                    return@execute
                }
                val parser = SseParser()
                val body = response.bodyAsChannel()
                while (true) {
                    val line = body.readUTF8Line() ?: break
                    parser.feed(line)?.let { send(it) }
                }
                parser.finish()?.let { send(it) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: HttpRequestTimeoutException) {
            send(ChatEvent.Failure(ChatError.Timeout))
        } catch (_: Exception) {
            send(ChatEvent.Failure(ChatError.Unavailable))
        }
    }

    private fun httpError(status: Int, body: String): ChatError {
        val code = try {
            json.decodeFromString<ErrorData>(body).error
        } catch (_: Exception) {
            null
        }
        return ChatError.fromHttpStatus(status, code)
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
