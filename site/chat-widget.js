/*
 * Bulle de chat : ouvre l'assistant (appli Compose Multiplatform) dans un panneau.
 * L'appli n'est chargée qu'au premier clic.
 *
 * Intégration sur n'importe quelle page :
 *   <script src="https://wardmisp.github.io/recruiter-chatbot/chat-widget.js" defer></script>
 * Option : data-chat-url="..." pour pointer vers une autre copie de l'appli.
 */
(() => {
  const script = document.currentScript;
  const chatUrl = (script && script.dataset.chatUrl) || new URL("chat/", script ? script.src : location.href).href;
  const french = (navigator.language || "").toLowerCase().startsWith("fr");
  const t = french
    ? { open: "Discuter avec mon assistant", close: "Fermer le chat", title: "Assistant de Rémi" }
    : { open: "Chat with my assistant", close: "Close the chat", title: "Rémi's assistant" };

  const style = document.createElement("style");
  style.textContent = `
    .rc-bubble, .rc-close {
      position: fixed; z-index: 2147483000; border: none; cursor: pointer;
      background: #2e5a88; color: #fff; font: 600 15px/1 system-ui, sans-serif;
      box-shadow: 0 6px 20px rgba(0, 0, 0, .25);
    }
    .rc-bubble {
      right: 20px; bottom: 20px; height: 56px; padding: 0 20px 0 16px;
      border-radius: 28px; display: flex; align-items: center; gap: 10px;
    }
    .rc-bubble svg { width: 24px; height: 24px; flex: none; }
    .rc-bubble:hover, .rc-close:hover { background: #244a72; }
    .rc-bubble:focus-visible, .rc-close:focus-visible { outline: 3px solid #9cc3f0; outline-offset: 2px; }
    .rc-panel {
      position: fixed; z-index: 2147482999; right: 20px; bottom: 20px;
      width: 420px; height: min(680px, calc(100vh - 40px));
      border-radius: 16px; overflow: hidden; background: #fff;
      box-shadow: 0 12px 40px rgba(0, 0, 0, .3);
    }
    .rc-panel iframe { width: 100%; height: 100%; border: 0; display: block; }
    .rc-close {
      right: 28px; bottom: calc(min(680px, 100vh - 40px) + 20px - 40px - 8px);
      width: 32px; height: 32px; border-radius: 16px; font-size: 18px;
    }
    .rc-hidden { display: none !important; }
    @media (max-width: 600px) {
      .rc-panel { inset: 0; width: 100%; height: 100%; border-radius: 0; }
      .rc-close { top: 12px; right: 12px; bottom: auto; }
      .rc-bubble span { display: none; }
      .rc-bubble { padding: 0 16px; }
    }
  `;
  document.head.appendChild(style);

  const bubble = document.createElement("button");
  bubble.type = "button";
  bubble.className = "rc-bubble";
  bubble.setAttribute("aria-label", t.open);
  bubble.setAttribute("aria-expanded", "false");
  bubble.innerHTML =
    '<svg viewBox="0 0 24 24" aria-hidden="true" fill="currentColor">' +
    '<path d="M4 4h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H9l-5 4v-4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z"/></svg>' +
    "<span></span>";
  bubble.querySelector("span").textContent = t.open;

  const panel = document.createElement("div");
  panel.className = "rc-panel rc-hidden";
  panel.setAttribute("role", "dialog");
  panel.setAttribute("aria-label", t.title);

  const close = document.createElement("button");
  close.type = "button";
  close.className = "rc-close rc-hidden";
  close.setAttribute("aria-label", t.close);
  close.textContent = "✕";

  function open() {
    // Chargement différé : l'appli (plusieurs Mo) n'est téléchargée qu'au premier clic.
    if (!panel.firstChild) {
      const frame = document.createElement("iframe");
      frame.src = chatUrl;
      frame.title = t.title;
      frame.allow = "clipboard-write";
      panel.appendChild(frame);
    }
    panel.classList.remove("rc-hidden");
    close.classList.remove("rc-hidden");
    bubble.classList.add("rc-hidden");
    bubble.setAttribute("aria-expanded", "true");
    panel.firstChild.focus();
  }

  function hide() {
    panel.classList.add("rc-hidden");
    close.classList.add("rc-hidden");
    bubble.classList.remove("rc-hidden");
    bubble.setAttribute("aria-expanded", "false");
    bubble.focus();
  }

  bubble.addEventListener("click", open);
  close.addEventListener("click", hide);
  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && !panel.classList.contains("rc-hidden")) hide();
  });

  document.body.append(panel, close, bubble);

  // Permet à la page hôte d'ouvrir le chat depuis ses propres boutons.
  window.RecruiterChat = { open, close: hide };
})();
