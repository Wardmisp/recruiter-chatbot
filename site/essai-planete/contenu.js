// Textes du monde 3D, tirés de knowledge/parcours.md et parcours.en.md : à garder synchronisés.
export const UI = {
    fr: {
        classic: "Mode classique", visited: (n, t) => `Lieux visités : ${n}/${t}`, close: "Fermer",
        enter: (name) => `Entrer : ${name}`,
        hintDesktop: "← → pour marcher · Espace pour sauter · Entrée pour entrer", forward: "Avancer", back: "Reculer", jump: "Sauter", steps: "Étapes du parcours",
        introKicker: "Ingénieur mobile · Android / Kotlin · Grenoble",
        introTitle: "Promenez-vous dans mon parcours",
        introText: "Valkea, ma chienne, fait le tour d'une petite planète : sept étapes, de ma présentation au chalet, qui réunit l'assistant, le CV et le contact. Chaque os ramassé et chaque découverte rapportent de l'XP ; certains os demandent de sauter. Tous les os débloquent un bonus.",
        introControls: "Clavier : ← → pour marcher, Espace pour sauter, Entrée pour entrer. Souris ou écran tactile : boutons en bas de l'écran.",
        start: "Commencer", done: (xp) => `Tour complet, ${xp} XP : merci de votre visite ! Le CV et mes coordonnées sont au chalet.`, bones: (n, t) => `Os : ${n}/${t}`, bonusButton: "★ Bonus",
        unavailable: "Le monde 3D ne peut pas s'afficher sur cet appareil.", fallbackLink: "Voir le site classique",
    },
    en: {
        classic: "Classic mode", visited: (n, t) => `Places visited: ${n}/${t}`, close: "Close",
        enter: (name) => `Enter: ${name}`,
        hintDesktop: "← → to walk · Space to jump · Enter to go in", forward: "Walk forward", back: "Walk back", jump: "Jump", steps: "Career stops",
        introKicker: "Mobile Engineer · Android / Kotlin · Grenoble",
        introTitle: "Take a walk through my career",
        introText: "Valkea, my dog, walks around a tiny planet: seven stops, from my introduction to the chalet, which brings together the assistant, the resume and my contact details. Every bone and discovery earns XP; some bones take a jump. Finding them all unlocks a bonus.",
        introControls: "Keyboard: ← → to walk, Space to jump, Enter to go in. Mouse or touch: buttons at the bottom of the screen.",
        start: "Start", done: (xp) => `Full tour, ${xp} XP: thanks for visiting! My resume and contact details are at the chalet.`, bones: (n, t) => `Bones: ${n}/${t}`, bonusButton: "★ Bonus",
        unavailable: "The 3D world can't be displayed on this device.", fallbackLink: "See the classic site",
    },
};

const LINKS = `<a href="mailto:remi.mialonevan@gmail.com">remi.mialonevan@gmail.com</a> · <a href="https://www.linkedin.com/in/r%C3%A9mi-mialonevan-b0a388174" target="_blank" rel="noopener">LinkedIn</a> · <a href="https://github.com/Wardmisp" target="_blank" rel="noopener">GitHub</a>`;

export const BONUS = {
    fr: { html: (xp) => `
        <h2>Bonus débloqué</h2><p class="meta">Les 14 os sont trouvés · score final : ${xp} XP</p>
        <p>Votre récompense : mon profil en 30 secondes.</p>
        <ul><li><b>Plus de 4 ans</b> de développement Android/Kotlin et iOS, aujourd'hui référent technique Android.</li>
        <li><b>Plus de 1 000 lieux</b> équipés de la navigation intérieure sur laquelle j'ai travaillé, dont les aéroports de Miami et de Doha.</li>
        <li><b>Plus de 1 000 appareils</b> dans la flotte Android de mon poste actuel.</li>
        <li><b>Disponible</b> au plus tard fin novembre 2026 · français natif, anglais courant.</li></ul>
        <a class="action" data-cv href="../cv/Remi_MialonEvan_CV_FR.pdf" target="_blank" rel="noopener">Ouvrir le CV</a>
        <h3>Une question ? Elle est déjà prête :</h3>
        <div class="questions">
            <button type="button">Sur quels projets à grande échelle a-t-il travaillé ?</button>
            <button type="button">Quelle est son expérience en IA ?</button>
            <button type="button">Peut-il travailler en Suisse ?</button>
        </div>` },
    en: { html: (xp) => `
        <h2>Bonus unlocked</h2><p class="meta">All 14 bones found · final score: ${xp} XP</p>
        <p>Your reward: my profile in 30 seconds.</p>
        <ul><li><b>Over 4 years</b> of Android/Kotlin and iOS development, now Android technical lead.</li>
        <li><b>Over 1,000 venues</b> using the indoor navigation I worked on, including Miami and Doha airports.</li>
        <li><b>Over 1,000 devices</b> in the Android fleet of my current role.</li>
        <li><b>Available</b> by the end of November 2026 at the latest · native French, fluent English.</li></ul>
        <a class="action" data-cv href="../cv/Remi_MialonEvan_CV_EN.pdf" target="_blank" rel="noopener">Open the resume</a>
        <h3>Got a question? It's ready to go:</h3>
        <div class="questions">
            <button type="button">What large-scale projects has he worked on?</button>
            <button type="button">What is his experience with AI?</button>
            <button type="button">Can he work in Switzerland?</button>
        </div>` },
};

export const PLACES = {
    fr: {
        presentation: { name: "Présentation", html: `
            <h2>Présentation</h2><p class="meta">Rémi Mialon-Evan · Grenoble</p>
            <p>Ingénieur mobile, actuellement référent technique Android / Kotlin, avec plus de 4 ans d'expérience en développement Android/Kotlin et iOS. J'ai conçu des solutions utilisées par plusieurs centaines de milliers d'utilisateurs.</p>
            <ul><li>Expertise en Clean Architecture, principes SOLID et Kotlin Multiplatform.</li>
            <li>Double compétence en Machine Learning / Data : spécialisation IBM, agent vocal basé sur l'OpenAI Realtime API, chatbot interne Llama + RAG.</li></ul>` },
        alpes: { name: "Alpes Contrôles", html: `
            <h2>Alpes Contrôles</h2><p class="meta">Référent technique Android · depuis mars 2026</p>
            <p>Un ensemble d'applications et de bibliothèques Android déployé sur une flotte de plus de 1 000 appareils, dans une équipe de 8 experts.</p>
            <ul><li>Garant des choix d'architecture et des bonnes pratiques (Clean Architecture, SOLID).</li>
            <li>Agent vocal conversationnel basé sur l'API Realtime d'OpenAI.</li>
            <li>Diagnostic et correction de fuites mémoire en production.</li>
            <li>Migration de bibliothèques internes vers Jetpack Compose.</li>
            <li>Pipelines de livraison CI/CD avec GitLab CI.</li></ul>` },
        visioglobe: { name: "Visioglobe", html: `
            <h2>Visioglobe</h2><p class="meta">Ingénieur logiciel · octobre 2022 – décembre 2025</p>
            <p>La navigation intérieure de plus de 1 000 lieux, dont les aéroports de Miami et de Doha.</p>
            <ul><li>Maintenance et évolution de la bibliothèque Android, maintenance de la bibliothèque iOS.</li>
            <li>Bibliothèque React Native complète, intégrée à plusieurs applications.</li>
            <li>Suivi technique et correction de bugs sur des applications Android/iOS en production.</li>
            <li>Chatbot interne basé sur Llama et une architecture RAG, avec un backend Python / FastAPI.</li>
            <li>Mise en place de Kanban et de Sprints avec les équipes R&amp;D.</li></ul>` },
        debuts: { name: "Débuts et formation", html: `
            <h2>Débuts et formation</h2><p class="meta">ALTEN · avril 2021 – août 2022</p>
            <ul><li>Développeur VR : authentification adaptée à la réalité virtuelle sur Oculus Quest 2, avec Unreal Engine (C++, Blueprint).</li>
            <li>Test logiciel et bases de données SQL chez MBDA.</li></ul>
            <h3>Formation</h3>
            <ul><li>Diplôme d'ingénieur (grade de Master, CTI), spécialisation Big Data — ECE Paris (2018 – 2021).</li>
            <li>CPGE MP — Lycée Pothier (2016 – 2018).</li>
            <li>Spécialisation IBM Advanced Data Science (Coursera).</li></ul>
            <h3>Langues</h3>
            <p>Français natif · anglais courant (TOEFL 98/120) · allemand A2.</p>` },
        projets: { name: "Projets et compétences", html: `
            <h2>Projets et compétences</h2>
            <h3>Glorious Five</h3>
            <p>Application Android en bêta fermée sur le Play Store : les 300 meilleures saisons NBA, multijoueur. Room, Supabase, Hilt, données issues d'un scraping Python.</p>
            <h3>Le chatbot de ce site</h3>
            <p>Assistant auto-hébergé : Python / FastAPI en streaming, modèle open-source Qwen3 exécuté localement sur GPU, Docker Compose et Caddy. Développé avec l'assistance de Claude Code.</p>
            <h3>Compétences</h3>
            <ul><li><b>Mobile :</b> Kotlin, Jetpack Compose, Coroutines / Flow, Clean Architecture, MVVM, Room, Hilt, Ktor, Kotlin Multiplatform, React Native, iOS/Swift.</li>
            <li><b>IA et data :</b> LLM, RAG, OpenAI Realtime API, Machine Learning, TensorFlow Lite.</li>
            <li><b>Backend et outils :</b> Python, FastAPI, Supabase, GitLab CI/CD, Scrum, Kanban, tests JUnit et MockK.</li></ul>` },
        loisirs: { name: "Hors du code", html: `
            <h2>Hors du code</h2>
            <p>Grenoble, et quelques passions :</p>
            <ul><li>La fauconnerie.</li><li>La course à pied et le basket-ball.</li><li>La batterie.</li>
            <li>Les voyages en semi-autonomie.</li><li>Valkea, ma chienne, qui vous guide ici.</li></ul>` },
        chalet: { name: "Le chalet", html: `
            <h2>Le chalet</h2><p class="meta">Discuter, ouvrir le CV, me contacter</p>
            <p><b>Poste recherché :</b> Développeur Android/Kotlin ou Ingénieur Mobile, en France, en Suisse, au Luxembourg, au Canada, en Norvège ou au Danemark.</p>
            <p><b>Disponibilité :</b> au plus tard fin novembre 2026.</p>
            <p>${LINKS}</p>
            <a class="action" data-cv href="../cv/Remi_MialonEvan_CV_FR.pdf" target="_blank" rel="noopener">Ouvrir le CV</a>
            <button type="button" class="primary" data-open-chat>Poser une question à l'assistant</button>` },
    },
    en: {
        presentation: { name: "Introduction", html: `
            <h2>Introduction</h2><p class="meta">Rémi Mialon-Evan · Grenoble</p>
            <p>Mobile engineer, currently Android / Kotlin technical lead, with over 4 years of experience in Android/Kotlin and iOS development. I have built solutions used by several hundred thousand users.</p>
            <ul><li>Expertise in Clean Architecture, SOLID principles and Kotlin Multiplatform.</li>
            <li>Dual skills in Machine Learning / Data: IBM specialization, voice agent built on the OpenAI Realtime API, internal Llama + RAG chatbot.</li></ul>` },
        alpes: { name: "Alpes Contrôles", html: `
            <h2>Alpes Contrôles</h2><p class="meta">Android Technical Lead · since March 2026</p>
            <p>A suite of Android apps and libraries deployed on a fleet of over 1,000 devices, in a team of 8 experts.</p>
            <ul><li>Owner of architecture choices and best practices (Clean Architecture, SOLID).</li>
            <li>Conversational voice agent built on the OpenAI Realtime API.</li>
            <li>Diagnosed and fixed memory leaks in production.</li>
            <li>Migrated internal libraries to Jetpack Compose.</li>
            <li>CI/CD delivery pipelines with GitLab CI.</li></ul>` },
        visioglobe: { name: "Visioglobe", html: `
            <h2>Visioglobe</h2><p class="meta">Software Engineer · October 2022 – December 2025</p>
            <p>Indoor navigation for over 1,000 venues, including Miami and Doha airports.</p>
            <ul><li>Maintained and evolved the Android library, maintained the iOS library.</li>
            <li>Full React Native library, integrated into several apps.</li>
            <li>Technical follow-up and bug fixing on Android/iOS apps in production.</li>
            <li>Internal chatbot based on Llama and a RAG architecture, with a Python / FastAPI backend.</li>
            <li>Introduced Kanban and Sprints with the R&amp;D teams.</li></ul>` },
        debuts: { name: "Early career & education", html: `
            <h2>Early career &amp; education</h2><p class="meta">ALTEN · April 2021 – August 2022</p>
            <ul><li>VR developer: authentication suited to virtual reality on Oculus Quest 2, with Unreal Engine (C++, Blueprint).</li>
            <li>Software testing and SQL databases at MBDA.</li></ul>
            <h3>Education</h3>
            <ul><li>Engineering degree (Master's level, CTI), Big Data specialization — ECE Paris (2018 – 2021).</li>
            <li>CPGE MP preparatory classes — Lycée Pothier (2016 – 2018).</li>
            <li>IBM Advanced Data Science Specialization (Coursera).</li></ul>
            <h3>Languages</h3>
            <p>Native French · fluent English (TOEFL 98/120) · German A2.</p>` },
        projets: { name: "Projects & skills", html: `
            <h2>Projects &amp; skills</h2>
            <h3>Glorious Five</h3>
            <p>Android app in closed beta on the Play Store: the 300 best NBA seasons, multiplayer. Room, Supabase, Hilt, data scraped with Python.</p>
            <h3>This site's chatbot</h3>
            <p>Self-hosted assistant: streaming Python / FastAPI, open-source Qwen3 model running locally on GPU, Docker Compose and Caddy. Developed with the assistance of Claude Code.</p>
            <h3>Skills</h3>
            <ul><li><b>Mobile:</b> Kotlin, Jetpack Compose, Coroutines / Flow, Clean Architecture, MVVM, Room, Hilt, Ktor, Kotlin Multiplatform, React Native, iOS/Swift.</li>
            <li><b>AI &amp; data:</b> LLM, RAG, OpenAI Realtime API, Machine Learning, TensorFlow Lite.</li>
            <li><b>Backend &amp; tools:</b> Python, FastAPI, Supabase, GitLab CI/CD, Scrum, Kanban, JUnit and MockK tests.</li></ul>` },
        loisirs: { name: "Away from code", html: `
            <h2>Away from code</h2>
            <p>Grenoble, and a few passions:</p>
            <ul><li>Falconry.</li><li>Running and basketball.</li><li>Drums.</li>
            <li>Semi-self-supported travel.</li><li>Valkea, my dog, your guide here.</li></ul>` },
        chalet: { name: "The chalet", html: `
            <h2>The chalet</h2><p class="meta">Chat, open the resume, contact me</p>
            <p><b>Looking for:</b> Android/Kotlin Developer or Mobile Engineer role, in France, Switzerland, Luxembourg, Canada, Norway or Denmark.</p>
            <p><b>Availability:</b> by the end of November 2026 at the latest.</p>
            <p>${LINKS}</p>
            <a class="action" data-cv href="../cv/Remi_MialonEvan_CV_EN.pdf" target="_blank" rel="noopener">Open the resume</a>
            <button type="button" class="primary" data-open-chat>Ask the assistant a question</button>` },
    },
};
