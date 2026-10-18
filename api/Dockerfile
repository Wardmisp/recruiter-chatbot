FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app ./app

# Utilisateur non-root
RUN useradd --system --uid 1000 appuser
USER appuser

# --proxy-headers : récupère l'IP réelle du client transmise par Caddy (rate limiting par IP).
# "*" est acceptable car seul Caddy peut joindre ce conteneur.
# --no-access-log : pas d'IP ni d'URL dans les logs (l'appli journalise juste l'issue de chaque chat).
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--proxy-headers", "--forwarded-allow-ips", "*", "--no-access-log"]
