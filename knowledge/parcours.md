# Parcours — Rémi Mialon-Evan

<!--
  Ce fichier est PUBLIC : il est envoyé tel quel au modèle et peut être cité aux recruteurs.
  Ne rien mettre de sensible (adresse, téléphone, salaire, date de naissance, etc.).
  Le chatbot ne répond QU'À PARTIR de ce contenu : ce qui n'est pas écrit ici
  sera considéré comme « information non disponible ».
  Source de la première version : CV « Remi_MialonEvan_CV_Suisse.pdf ».
  Version anglaise : parcours.en.md (envoyée pour les questions en anglais).
  Toute modification ici doit être reportée dans parcours.en.md.
-->

## Présentation

Rémi Mialon-Evan est ingénieur mobile, actuellement référent technique Android / Kotlin.
Il a plus de 4 ans d'expérience en développement Android/Kotlin et iOS, et a conçu des
solutions utilisées par plusieurs centaines de milliers d'utilisateurs.
Il a une expertise en Clean Architecture, principes SOLID et Kotlin Multiplatform, avec une
double compétence en Machine Learning / Data (spécialisation IBM, agent vocal basé sur
l'OpenAI Realtime API, chatbot interne Llama + RAG).
Il est l'auteur d'une bibliothèque React Native complète couvrant tout le cycle de vie produit.

## Poste recherché

Il recherche un poste de Développeur Android/Kotlin ou d'Ingénieur Mobile, en France,
en Suisse (notamment Suisse romande), au Luxembourg, au Canada, en Norvège ou au Danemark.
Il est disponible pour un contrat frontalier ou une relocation.

## Expériences professionnelles

### Développeur d'applications mobiles — Référent technique Android — Alpes Contrôles (depuis mars 2026)

- Application mobile déployée sur une flotte de plus de 1 000 appareils.
- Au sein d'une équipe de 8 experts, dans une DSI d'une quarantaine de personnes, pour une
  entreprise de plus de 1 000 collaborateurs.
- Référent technique Android : garant des choix d'architecture et des bonnes pratiques
  (Clean Architecture, principes SOLID) sur le périmètre mobile.
- A implémenté un agent vocal conversationnel basé sur l'API Realtime d'OpenAI
  (Speech-to-Speech), enrichissant l'assistance et l'expérience utilisateur au sein de l'application.
- A diagnostiqué et corrigé des fuites mémoire (memory leaks), améliorant la stabilité et les
  performances de l'application en production.
- A migré plusieurs bibliothèques internes vers Jetpack Compose, en conformité avec la
  Clean Architecture et les principes SOLID.
- Met en place et maintient les pipelines de livraison CI/CD via GitLab CI.

### Ingénieur logiciel — Visioglobe (octobre 2022 – décembre 2025, 3 ans et 3 mois)

- Équipe de 6 développeurs, dans une entreprise de 18 personnes.
- A assuré la maintenance et l'évolution de la bibliothèque Android de navigation intérieure, déployée dans
  plus de 1 000 lieux, dont les aéroports de Miami et de Doha.
- A assuré le suivi technique et la correction de bugs sur des applications Android/iOS en
  production, garantissant leur stabilité et leur fiabilité pour les utilisateurs finaux.
- A assuré la maintenance de la bibliothèque iOS de navigation intérieure.
- A conçu et maintenu une bibliothèque React Native complète (UI/UX, intégration, tests,
  déploiement), intégrée sur plusieurs applications du portefeuille produit, en appliquant la
  Clean Architecture et l'optimisation des performances.
- A contribué à l'amélioration des processus Agile avec les équipes R&D (mise en place de
  Kanban et de Sprints), renforçant la collaboration inter-équipes et la prévisibilité des livraisons.
- A développé un chatbot interne basé sur le modèle Llama et une architecture RAG, avec un
  backend en Python / FastAPI, automatisant l'accès à l'information et réduisant le temps de recherche des équipes internes.

### Ingénieur consultant — ALTEN (avril 2021 – août 2022, 1 an et 5 mois)

- Mission de test logiciel et de base de données en SQL (conception de cas de test,
  validation des données) chez MBDA (leader européen de l’industrie de la défense).
- Développeur VR : système d'authentification adapté à la réalité virtuelle sur Oculus Quest 2,
  avec Unreal Engine (C++, Blueprint), à Sèvres.

## Projets

### Glorious Five — Application Android (https://github.com/Wardmisp)

- Application Android en bêta fermée sur le Play Store.
- Technologies : Room (stockage local), Supabase (multijoueur), Hilt (injection de dépendances).
- Base de données des 300 meilleures saisons NBA, constituée par scraping en Python, avec un
  scoring basé sur le PER et les Win Shares.
- Évolution prévue vers Kotlin Multiplatform.

### Chatbot recruteur auto-hébergé (le chatbot de ce site)

- Assistant qui répond aux questions des recruteurs sur son parcours, uniquement à partir
  d'un fichier de connaissances (pas d'invention).
- Backend Python / FastAPI avec réponses en streaming (SSE), limitation du débit par IP,
  CORS restreint et limites de taille des messages.
- Modèle de langage open-source (Qwen3 14B) exécuté localement via Ollama sur GPU NVIDIA.
- Déploiement avec Docker Compose, reverse proxy Caddy en HTTPS (certificat Let's Encrypt
  obtenu par challenge DNS), auto-hébergé sur son propre serveur, avec une pile technique entièrement open-source.
- Développé avec l'assistance de Claude Code, un outil de développement agentique
  basé sur l'IA.

## Compétences techniques

- Langages : Kotlin, Java, Swift, JavaScript/TypeScript, SQL, C++, Python.
- Mobile : Android SDK, Jetpack Compose, Coroutines / Flow, Clean Architecture, MVVM, Room, Hilt,
  Retrofit, Ktor, Firebase, publication sur le Play Store, accessibilité, sécurité mobile,
  React Native, Kotlin Multiplatform (KMP), iOS/Swift.
- IA et Data : Machine Learning, LLM, RAG, OpenAI Realtime API (Speech-to-Speech), Llama,
  analyse de données (Data Analysis), TensorFlow Lite.
- Backend : Python, FastAPI, Supabase.
- Outils et méthodes : Git/GitLab, GitLab CI/CD, Agile (Scrum, Kanban), Jira,
  tests unitaires et automatisés (JUnit, MockK, tests d'interface Jetpack Compose).
- Autres : Unreal Engine (C++, Blueprint), réalité virtuelle (Oculus Quest 2).

## Formation

- Diplôme d'ingénieur (grade de Master, titre accrédité par la CTI), spécialisation Big Data —
  ECE Paris (2018 – 2021).
- Classe préparatoire aux grandes écoles (CPGE MP) — Lycée Pothier (2016 – 2018).

## Certifications

- TOEFL : score de 98/120.
- Spécialisation IBM Advanced Data Science (Coursera) : Machine Learning & Data Science Capstone.

## Langues

- Français : langue maternelle.
- Anglais : courant, niveau entre B2 et C1 (environ C1) ; TOEFL 98/120.
- Allemand : notions (niveau A2). Peut travailler en Suisse alémanique (Zurich, Bâle) si
  l'environnement de travail est en anglais.

## Centres d'intérêt (hors code)

- Fauconnerie.
- Sports, notamment la course à pied et le basket-ball.
- Musique : batterie.
- Voyages en semi-autonomie.
- Sa chienne, Valkea.

## Localisation et mobilité

- Basé à Grenoble (Auvergne-Rhône-Alpes, France), à environ 1h30 de Genève.
- En France : mobile dans les Alpes, et ouvert au télétravail complet ou quasi complet
  pour un poste ailleurs en France.
- À l'étranger : disponible pour un contrat frontalier ou une relocation.
- Pour un poste en Suisse : il vivrait côté français, près de la frontière, en tant que
  frontalier.
- Permis de travail suisse : il n'en possède pas encore. Un permis G (frontalier) reste
  nécessaire, mais en tant que ressortissant français il bénéficie de l'accord de libre
  circulation des personnes UE/Suisse : le permis est délivré sur demande de l'employeur,
  sans contingent, sans visa et sans parrainage (sponsoring).
- Suisse alémanique (Zurich, Bâle) : possible uniquement si l'environnement de travail est
  en anglais.
- Espace économique européen (EEE, dont la Norvège ; la Suisse n'en fait pas partie) :
  autorisé à travailler sans visa ni permis, en tant que citoyen de l'Union européenne.
- Canada : il ne dispose pas de permis de travail ; un poste au Canada nécessiterait
  l'appui de l'employeur pour l'obtenir.

## Conditions de travail recherchées

- Taux d'activité : 100 %.
- Types de contrat : CDI ou CDD ; freelance envisageable.
- Télétravail : négociable ; idéalement 2 jours par semaine, après une période d'au moins
  un mois sur place.
- Prétentions salariales : à discuter directement avec lui (par e-mail ou via LinkedIn).
- Références : disponibles sur demande.

## Secteurs

- Expérience sectorielle : aéroports et navigation intérieure (Visioglobe), contrôle technique
  et inspection (Alpes Contrôles), défense (MBDA, via ALTEN).
- En Suisse : préférence pour la medtech et les projets à enjeux de souveraineté ; ouvert à
  d'autres propositions.
- Pourquoi la Suisse : la qualité de vie et la montagne.

## Disponibilité

- Disponible pour commencer au plus tard fin novembre 2026.
- Préavis de 3 mois à partir de novembre 2026.

## Contact

- E-mail : remi.mialonevan@gmail.com
- LinkedIn : https://www.linkedin.com/in/rémi-mialonevan-b0a388174
- GitHub : https://github.com/Wardmisp
- Téléphone : communiqué sur demande, par e-mail ou via LinkedIn.
