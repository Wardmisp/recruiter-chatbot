"""Backend du chatbot recruteur : /health et /chat (streaming SSE)."""

import asyncio
import json
import logging
import time
from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from typing import Literal

import httpx
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse, StreamingResponse
from pydantic import BaseModel, Field
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded
from slowapi.util import get_remote_address

from .config import settings
from .prompt import FINAL_REMINDERS, build_system_prompt, conversation_language, load_knowledge

# Journalisation minimale : ni IP, ni contenu des messages.
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger("chatbot")
# slowapi journalise l'IP à chaque dépassement, httpx chaque requête vers Ollama.
logging.getLogger("slowapi").setLevel(logging.ERROR)
logging.getLogger("httpx").setLevel(logging.WARNING)

# L'IP vient de X-Forwarded-For (posé par Caddy, lu par uvicorn --proxy-headers).
limiter = Limiter(key_func=get_remote_address)

# Un seul GPU : au-delà de N générations simultanées, on refuse plutôt que d'empiler.
generation_slots = asyncio.Semaphore(settings.max_concurrent_generations)

# Les réponses précédentes du bot sont renvoyées par le client ; on les borne
# à une taille compatible avec max_output_tokens (~4 caractères par token, avec marge).
MAX_ASSISTANT_CHARS = settings.max_output_tokens * 6


@asynccontextmanager
async def lifespan(app: FastAPI):
    # read = délai max entre deux morceaux reçus d'Ollama (le délai total est géré à part).
    timeout = httpx.Timeout(10.0, read=settings.request_timeout_seconds)
    async with httpx.AsyncClient(base_url=settings.ollama_url, timeout=timeout) as client:
        app.state.ollama = client
        yield


app = FastAPI(title="Chatbot recruteur", docs_url=None, redoc_url=None, openapi_url=None, lifespan=lifespan)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)
app.add_middleware(
    CORSMiddleware,
    # ALLOWED_ORIGIN peut lister plusieurs origines séparées par des virgules
    # (ex. le site + http://localhost:8080 pendant le développement du client web).
    allow_origins=[o.strip() for o in settings.allowed_origin.split(",") if o.strip()],
    allow_methods=["GET", "POST"],
    allow_headers=["Content-Type"],
)


class Message(BaseModel):
    role: Literal["user", "assistant"]
    content: str = Field(min_length=1)


class ChatRequest(BaseModel):
    messages: list[Message] = Field(min_length=1, max_length=50)


def error(status: int, code: str) -> JSONResponse:
    return JSONResponse({"error": code}, status_code=status)


def sse(event: str, data: dict) -> str:
    return f"event: {event}\ndata: {json.dumps(data, ensure_ascii=False)}\n\n"


@app.get("/health")
async def health(request: Request):
    try:
        response = await request.app.state.ollama.get("/api/tags", timeout=3.0)
        response.raise_for_status()
        models = {m.get("name") for m in response.json().get("models", [])}
    except (httpx.HTTPError, ValueError):
        return JSONResponse({"status": "unavailable"}, status_code=503)
    if settings.ollama_model not in models:
        return JSONResponse({"status": "unavailable"}, status_code=503)
    return {"status": "ok"}


@app.post("/chat")
@limiter.limit(settings.rate_limit)
async def chat(request: Request, body: ChatRequest):
    history = body.messages[-settings.max_history_messages :]
    if history[-1].role != "user":
        return error(422, "last_message_must_be_user")
    for message in history:
        limit = settings.max_message_chars if message.role == "user" else MAX_ASSISTANT_CHARS
        if len(message.content) > limit:
            return error(413, "message_too_long")
    if generation_slots.locked():
        return error(503, "busy")

    # Le parcours est envoyé dans la langue de la question : le modèle répond alors
    # dans cette langue bien plus sûrement qu'en traduisant lui-même.
    language = conversation_language([m.content for m in history if m.role == "user"])
    knowledge_file = settings.knowledge_file_en if language == "en" else settings.knowledge_file
    system_prompt = build_system_prompt(load_knowledge(knowledge_file), language)
    payload = {
        "model": settings.ollama_model,
        "messages": [{"role": "system", "content": system_prompt}]
        + [m.model_dump() for m in history]
        + [{"role": "system", "content": FINAL_REMINDERS[language]}],
        "stream": True,
        # La réflexion retarde le premier token de quelques secondes, mais évite que le
        # modèle invente des liens entre compétences et expériences.
        "think": True,
        "options": {
            "num_ctx": settings.ollama_num_ctx,
            "num_predict": settings.max_output_tokens,
            "temperature": 0.2,
        },
    }
    return StreamingResponse(
        stream_answer(request.app.state.ollama, payload),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"},
    )


async def stream_answer(client: httpx.AsyncClient, payload: dict) -> AsyncIterator[str]:
    """Relaie les tokens d'Ollama en événements SSE : thinking (au plus une fois), token*,
    puis done ou error."""
    start = time.monotonic()
    deadline = start + settings.request_timeout_seconds
    outcome = "ok"
    thinking_announced = False
    try:
        async with generation_slots:
            async with client.stream("POST", "/api/chat", json=payload) as response:
                response.raise_for_status()
                async for line in response.aiter_lines():
                    if time.monotonic() > deadline:
                        raise TimeoutError
                    if not line:
                        continue
                    chunk = json.loads(line)
                    if "error" in chunk:
                        raise ValueError("erreur Ollama")
                    message = chunk.get("message", {})
                    # Le contenu de la réflexion n'est jamais transmis, seulement son début.
                    if message.get("thinking") and not thinking_announced:
                        thinking_announced = True
                        yield sse("thinking", {})
                    token = message.get("content", "")
                    if token:
                        yield sse("token", {"text": token})
                    if chunk.get("done"):
                        break
        yield sse("done", {})
    except (TimeoutError, httpx.TimeoutException):
        outcome = "timeout"
        yield sse("error", {"code": "timeout"})
    except (httpx.HTTPError, ValueError) as exc:
        outcome = f"erreur ({type(exc).__name__})"
        yield sse("error", {"code": "unavailable"})
    except (asyncio.CancelledError, GeneratorExit):
        # Le client a fermé la connexion : la sortie du "async with" arrête la génération.
        outcome = "interrompu par le client"
        raise
    finally:
        logger.info("chat : %s en %.1f s", outcome, time.monotonic() - start)
