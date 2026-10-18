# Projet : chatbot recruteur auto-hébergé (100 % open-source)

## Objectif
Chatbot intégré à mon site perso, qui répond aux questions de recruteurs sur mon parcours
(développeur Android, Kotlin Multiplatform, Python/FastAPI, ML). Le serveur est mon PC
Windows à la maison. Tous les outils doivent être open-source.

## Infrastructure déjà en place (ne pas la refaire)
- PC Windows, GPU NVIDIA RTX 5080 (16 Go de VRAM), IP locale fixe
- Box Internet avec IPv4 publique ; redirection du port externe 38443 vers le port 443 du PC
  (pas de port 80 joignable)
- Détails réseau (IP, box, DHCP, pare-feu) : fichier local non versionné @CLAUDE.local.md
- Domaine : remi-chat.duckdns.org (DuckDNS), résolu vers mon IPv4 publique (vérifié avec nslookup)
- URL publique de l'API : https://remi-chat.duckdns.org:38443
- Le token DuckDNS est dans un fichier .env local (variable DUCKDNS_TOKEN),
  jamais dans Git, jamais affiché dans les logs ni dans la conversation

## Architecture cible (Docker Compose)
- `ollama` : image ollama/ollama, accès GPU, volume persistant, AUCUN port publié,
  OLLAMA_KEEP_ALIVE=10m, accessible seulement depuis le réseau interne Docker
- `api` : backend FastAPI (Python), aucun port publié, accessible seulement via Caddy
- `caddy` : image personnalisée compilée avec le plugin caddy-dns/duckdns (xcaddy),
  seul service exposé (port 443 publié)
- Certificat HTTPS par challenge DNS-01 (DuckDNS), car les ports 80/443 ne sont pas
  joignables depuis Internet. Dans le Caddyfile : bloc `tls { dns duckdns {env.DUCKDNS_TOKEN} }`
- Le site `remi-chat.duckdns.org` est servi par Caddy en reverse proxy vers `api:8000`
  (Caddy écoute sur 443 dans le conteneur, la box traduit 38443 -> 443)

## Backend FastAPI : exigences
- Endpoint de chat en streaming SSE (réponse qui s'écrit au fil de l'eau)
- Endpoint `/health` (utilisé par le front pour afficher un message de repli si le serveur est indisponible)
- Prompt système strict : répondre UNIQUEMENT à partir du fichier de connaissances,
  sinon dire que l'information n'est pas disponible et renvoyer vers mon contact
  (pas d'invention sur mon parcours, jamais)
- Réponses en français ou en anglais selon la langue de la question
- Rate limiting par IP (slowapi ou équivalent), limite de taille des messages,
  limite de l'historique envoyé au modèle, timeout sur les requêtes
- CORS limité à l'origine de mon site (variable d'environnement ALLOWED_ORIGIN)
- Aucune donnée personnelle dans les logs au-delà du strict nécessaire
- Configuration par variables d'environnement (.env), pas de valeur codée en dur

## Modèle et connaissances
- Modèle de départ : Qwen 14B quantifié Q4 via Ollama (doit tenir dans 16 Go de VRAM avec du contexte).
  Vérifier le tag exact disponible dans la bibliothèque Ollama ; alternatives : Gemma 12B, Mistral Small.
- Pas de RAG pour l'instant : tout mon parcours est injecté dans le prompt système
  à partir d'un fichier Markdown `knowledge/parcours.md`
  (expériences, projets, compétences, formation, localisation, disponibilité, contact).
  Prévoir de passer au RAG (base vectorielle) seulement si la base grossit.
- Le fichier `knowledge/parcours.md` sera rédigé avec moi : créer un modèle avec
  des sections vides à remplir, sans inventer de contenu.
- Tout ce qui est dans ce fichier est PUBLIC : aucune donnée sensible (pas d'adresse, de téléphone, de salaire, etc.)

## Front (étape ultérieure)
- D'abord un widget léger fonctionnel (HTML/JS) qui appelle l'API en SSE
- Ensuite, éventuellement, version Kotlin Multiplatform (Compose HTML ou Kobweb), avec client Ktor,
  modèles partagés (kotlinx.serialization) et logique de conversation en module commun
- Le widget appelle `https://remi-chat.duckdns.org:38443` ; si `/health` échoue, afficher
  « Chat indisponible, contactez-moi par mail ou LinkedIn »

## Étapes à suivre, dans l'ordre, avec validation de ma part à chaque étape
1. Vérifier Docker Desktop (WSL2) et le GPU : `docker run --rm --gpus all nvidia/cuda:<tag récent>-base-ubuntu22.04 nvidia-smi`
   doit afficher la RTX 5080. Les cartes Blackwell exigent un pilote et des versions CUDA/Ollama récents.
2. Ouvrir le port TCP 443 entrant dans le pare-feu Windows (commande netsh, terminal administrateur) après m'avoir demandé confirmation.
3. Créer le projet : `docker-compose.yml`, `caddy/Dockerfile`, `caddy/Caddyfile`, `.env.example`, `.gitignore` (qui exclut `.env`), `api/`.
4. Démarrer Ollama et télécharger le modèle, tester une génération simple en local.
5. Écrire et tester le backend FastAPI (chat SSE, /health, rate limiting, CORS) en local.
6. Démarrer Caddy et vérifier l'obtention du certificat par challenge DNS (lire les logs).
7. Mettre en place la mise à jour automatique de l'IP chez DuckDNS (tâche planifiée Windows
   ou conteneur dédié) au cas où mon IPv4 Free change.
8. Configurer le démarrage automatique (`restart: unless-stopped`, Docker Desktop au démarrage de session)
   et désactiver la mise en veille du PC.
9. Test final depuis l'extérieur : téléphone en 4G (Wi-Fi coupé) sur
   `https://remi-chat.duckdns.org:38443/health`. Depuis mon PC, l'accès via l'IP publique
   peut échouer à cause de la box (hairpin NAT), ce n'est pas forcément une erreur.
10. Monitoring optionnel : Uptime Kuma avec alerte Discord.

## Règles de travail pour Claude Code
- Expliquer chaque commande système avant de l'exécuter ; demander confirmation avant toute
  commande qui modifie le pare-feu, le réseau ou des réglages Windows
- Ne jamais afficher ni écrire le token DuckDNS en clair ailleurs que dans `.env`
- Ne jamais exposer Ollama (port 11434) ni FastAPI directement : seul Caddy publie un port
- Ne pas commiter de secrets ; vérifier `.gitignore` avant le premier commit
- En cas d'erreur, montrer le message exact et proposer une correction avant de la lancer
- Ne pas modifier la configuration de ma box (déjà faite) ; si un changement est nécessaire,
  me dire précisément quoi faire
- Langue de travail : français