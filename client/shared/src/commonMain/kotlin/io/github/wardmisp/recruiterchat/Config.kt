package io.github.wardmisp.recruiterchat

/** Réglages du client, alignés sur la configuration de l'API (fichier .env du serveur). */
object Config {
    const val API_BASE_URL = "https://remi-chat.duckdns.org:38443"

    const val CONTACT_EMAIL = "remi.mialonevan@gmail.com"
    const val LINKEDIN_URL = "https://www.linkedin.com/in/r%C3%A9mi-mialonevan-b0a388174"

    /** Site GitHub Pages (page d'accueil, appli web et CV). */
    const val SITE_URL = "https://wardmisp.github.io/recruiter-chatbot"

    /**
     * CV publiés avec le site depuis le dossier cv/ du dépôt : téléchargeables même quand le
     * serveur maison est éteint, et remplaçables sans recompiler.
     */
    const val CV_FR_URL = "$SITE_URL/cv/Remi_MialonEvan_CV_FR.pdf"
    const val CV_EN_URL = "$SITE_URL/cv/Remi_MialonEvan_CV_EN.pdf"

    /** MAX_MESSAGE_CHARS côté API : au-delà, le serveur répond 413. */
    const val MAX_MESSAGE_CHARS = 1000

    /** MAX_HISTORY_MESSAGES côté API : inutile d'envoyer plus, le serveur tronque. */
    const val MAX_HISTORY_MESSAGES = 10

    /** Conversation conservée sur l'appareil du visiteur : au plus ce nombre de messages… */
    const val SAVED_MESSAGES = 50

    /** … effacée si le dernier message date de plus de 30 jours. */
    const val SAVED_HISTORY_MAX_AGE_MILLIS = 30L * 24 * 60 * 60 * 1000
}
