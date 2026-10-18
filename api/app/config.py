"""Configuration lue depuis les variables d'environnement (fournies par docker-compose / .env).

Aucune valeur par défaut : une variable manquante fait échouer le démarrage
plutôt que de tourner avec un réglage inattendu.
"""

from pathlib import Path

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    ollama_url: str
    ollama_model: str
    ollama_num_ctx: int
    allowed_origin: str
    rate_limit: str
    max_message_chars: int
    max_history_messages: int
    max_output_tokens: int
    max_concurrent_generations: int
    request_timeout_seconds: float
    knowledge_file: Path
    knowledge_file_en: Path


settings = Settings()
