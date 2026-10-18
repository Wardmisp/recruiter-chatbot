package io.github.wardmisp.recruiterchat

/** Réglages du client, alignés sur la configuration de l'API (fichier .env du serveur). */
object Config {
    const val API_BASE_URL = "https://remi-chat.duckdns.org:38443"

    const val CONTACT_EMAIL = "remi.mialonevan@gmail.com"
    const val LINKEDIN_URL = "https://www.linkedin.com/in/r%C3%A9mi-mialonevan-b0a388174"

    /** CV servis par Caddy depuis le dossier cv/ du serveur : les remplacer ne demande pas de recompiler. */
    const val CV_FR_URL = "$API_BASE_URL/cv/Remi_MialonEvan_CV_FR.pdf"
    const val CV_EN_URL = "$API_BASE_URL/cv/Remi_MialonEvan_CV_EN.pdf"

    /** MAX_MESSAGE_CHARS côté API : au-delà, le serveur répond 413. */
    const val MAX_MESSAGE_CHARS = 1000

    /** MAX_HISTORY_MESSAGES côté API : inutile d'envoyer plus, le serveur tronque. */
    const val MAX_HISTORY_MESSAGES = 10
}
