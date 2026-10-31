// Petite planète 3D : elle tourne sous Valkea, qui avance, recule et saute d'étape en étape.
import * as THREE from "./vendor/three.module.min.js";
import { UI, PLACES, BONUS } from "./contenu.js";

const LANG_KEY = "site.lang";
const R = 26;               // Rayon de la planète.
const SPEED = 8.5;          // Vitesse de marche, en unités par seconde.
const ENTER_DISTANCE = 5;   // Distance le long du chemin à laquelle on peut entrer.
const JUMP_SPEED = 13;
const GRAVITY = 34;
const projected = new THREE.Vector3();
const motionOk = !matchMedia("(prefers-reduced-motion: reduce)").matches;

const $ = (id) => document.getElementById(id);
const read = (key) => { try { return localStorage.getItem(key); } catch (e) { return null; } };
const write = (key, value) => { try { localStorage.setItem(key, value); } catch (e) { /* stockage bloqué : rien de mémorisé */ } };

let lang = read(LANG_KEY) || ((navigator.language || "fr").toLowerCase().startsWith("fr") ? "fr" : "en");
if (lang !== "fr" && lang !== "en") lang = "fr";

// --- Rendu ---
let renderer;
try {
    renderer = new THREE.WebGLRenderer({ canvas: $("scene"), antialias: true });
} catch (e) {
    $("fallback").hidden = false;
    $("fallback-title").textContent = UI[lang].unavailable;
    $("fallback-link").textContent = UI[lang].fallbackLink;
    throw e;
}
renderer.setPixelRatio(Math.min(devicePixelRatio, 2));
renderer.shadowMap.enabled = true;
renderer.shadowMap.type = THREE.PCFSoftShadowMap;

const COLORS = {
    sky: 0xf2f1ed, grass: 0xc9cdb0, grassDark: 0xb7bd9e, path: 0xf7f5ee, ink: 0x161616, wall: 0xe9e6dc, accent: 0xb23a1c,
    pine: 0x3f5147, pineDark: 0x2f3e36, rock: 0x777a76, rockLight: 0x9a9d98, snow: 0xfbfaf6, wood: 0x6b4a33, glass: 0x8fb0bd,
};
const scene = new THREE.Scene();
scene.background = new THREE.Color(COLORS.sky);
const camera = new THREE.PerspectiveCamera(38, 1, 0.1, 400);
scene.add(new THREE.HemisphereLight(0xffffff, 0xb9b6a8, 2.1));
const sunLight = new THREE.DirectionalLight(0xfff1e0, 2.2);
sunLight.position.set(-20, R + 34, 26);
sunLight.target.position.set(0, R - 10, 0);
scene.add(sunLight.target);
sunLight.castShadow = true;
sunLight.shadow.mapSize.set(1536, 1536);
Object.assign(sunLight.shadow.camera, { left: -38, right: 38, top: 38, bottom: -38, near: 1, far: 120 });
scene.add(sunLight);

// Géométrie : matériaux mats à facettes, comme le papier découpé des crêtes de la page classique.
const materials = new Map();
function material(color) {
    if (!materials.has(color)) materials.set(color, new THREE.MeshStandardMaterial({ color, flatShading: true, roughness: 0.95 }));
    return materials.get(color);
}
function mesh(geometry, color, x = 0, y = 0, z = 0, parent = scene) {
    const m = new THREE.Mesh(geometry, typeof color === "number" ? material(color) : color);
    m.position.set(x, y, z);
    m.castShadow = true;
    m.receiveShadow = true;
    parent.add(m);
    return m;
}
const box = (w, h, d, color, x, y, z, parent) => mesh(new THREE.BoxGeometry(w, h, d), color, x, y + h / 2, z, parent);
const cyl = (rt, rb, h, color, x, y, z, parent, seg = 8) => mesh(new THREE.CylinderGeometry(rt, rb, h, seg), color, x, y + h / 2, z, parent);
const cone = (r, h, color, x, y, z, parent, seg = 6) => mesh(new THREE.ConeGeometry(r, h, seg), color, x, y + h / 2, z, parent);

// Générateur à graine : le décor est identique à chaque visite.
let seed = 20261031;
const rand = () => (seed = (seed * 1664525 + 1013904223) >>> 0) / 4294967296;

// --- Planète ---
// Le chemin suit l'équateur du plan z = 0. Elle tourne autour de z : l'angle `position` passe au sommet.
const planet = new THREE.Group();
scene.add(planet);

// Relief : nul autour du chemin, puis collines de plus en plus marquées vers les côtés.
function relief(n) {
    const away = Math.min(1, Math.max(0, (Math.abs(n.z) - 0.42) / 0.3));
    const wave = Math.sin(n.x * 5.1 + 1) * Math.sin(n.y * 6.3 + 2) * Math.sin(n.z * 4.7) + 0.5 * Math.sin(n.x * 11 + n.z * 9);
    return away * wave * 1.5;
}
const surface = (n) => R + relief(n);
{
    const geometry = new THREE.IcosahedronGeometry(R, 12);
    const position = geometry.attributes.position;
    const colors = new Float32Array(position.count * 3);
    const v = new THREE.Vector3();
    const tones = [COLORS.grass, COLORS.grassDark, COLORS.rockLight, COLORS.snow].map((c) => new THREE.Color(c));
    for (let i = 0; i < position.count; i++) {
        v.fromBufferAttribute(position, i).normalize();
        v.multiplyScalar(surface(v.clone()));
        position.setXYZ(i, v.x, v.y, v.z);
    }
    for (let i = 0; i < position.count; i += 3) {
        const cz = Math.abs(position.getZ(i) + position.getZ(i + 1) + position.getZ(i + 2)) / (3 * R);
        const tone = cz > 0.86 ? tones[3] : cz > 0.72 ? tones[2] : tones[rand() < 0.5 ? 0 : 1];
        for (let j = 0; j < 3; j++) tone.toArray(colors, (i + j) * 3);
    }
    geometry.setAttribute("color", new THREE.BufferAttribute(colors, 3));
    geometry.computeVertexNormals();
    const ground = new THREE.Mesh(geometry, new THREE.MeshStandardMaterial({ vertexColors: true, flatShading: true, roughness: 1 }));
    ground.receiveShadow = true;
    planet.add(ground);
}
const pathRing = mesh(new THREE.CylinderGeometry(R + 0.12, R + 0.12, 2.6, 96, 1, true), COLORS.path, 0, 0, 0, planet);
pathRing.rotation.x = Math.PI / 2;
pathRing.castShadow = false;

// Pose un objet sur la planète : `angle` le long du chemin, `side` en radians vers l'arrière (-) ou l'avant (+).
const UP = new THREE.Vector3(0, 1, 0);
function normalAt(angle, side) {
    return new THREE.Vector3(Math.sin(angle) * Math.cos(side), Math.cos(angle) * Math.cos(side), Math.sin(side));
}
function onPlanet(angle, side) {
    const n = normalAt(angle, side);
    const g = new THREE.Group();
    g.position.copy(n).multiplyScalar(surface(n) - 0.12);
    g.quaternion.setFromUnitVectors(UP, n);
    planet.add(g);
    return g;
}

// Soleil vermillon derrière la planète.
mesh(new THREE.CircleGeometry(13, 48), new THREE.MeshBasicMaterial({ color: COLORS.accent }), 30, R + 2, -120).castShadow = false;

// --- Étapes, dans l'ordre du tour ---
const placeDefs = [
    { id: "presentation", top: 5.4, build: buildSignpost },
    { id: "alpes", top: 5.4, build: buildAlpes },
    { id: "visioglobe", top: 9.2, build: buildAirport },
    { id: "debuts", top: 4.2, build: buildVr },
    { id: "projets", top: 5.8, build: buildCourt },
    { id: "loisirs", top: 4.2, build: buildFalconry },
    { id: "chalet", top: 8, build: buildChalet },
];
const SIDE_OFFSET = -0.23; // Bâtiments en retrait, derrière le chemin vu de la caméra.
placeDefs.forEach((p, i) => {
    p.angle = (i / placeDefs.length) * Math.PI * 2;
    p.group = onPlanet(p.angle, SIDE_OFFSET);
    p.build(p.group);
    // Losange au-dessus du lieu tant qu'il n'est pas visité.
    p.marker = mesh(new THREE.OctahedronGeometry(0.6), new THREE.MeshStandardMaterial({ color: COLORS.accent, emissive: COLORS.accent, emissiveIntensity: 0.35, flatShading: true }), 0, p.top, 0, p.group);
    p.marker.castShadow = false;
});

function tree(angle, side, s) {
    const g = onPlanet(angle, side);
    cyl(0.18 * s, 0.25 * s, 0.8 * s, COLORS.wood, 0, 0, 0, g, 5);
    cone(1.15 * s, 1.9 * s, COLORS.pine, 0, 0.7 * s, 0, g);
    cone(0.82 * s, 1.6 * s, COLORS.pineDark, 0, 1.8 * s, 0, g);
}
function mountain(angle, side, r, h) {
    const g = onPlanet(angle, side);
    g.rotateY(rand() * Math.PI);
    cone(r, h, COLORS.rock, 0, -0.4, 0, g, 5);
    // Un peu plus large que la roche à cette hauteur : évite que les deux surfaces se confondent.
    cone(r * 0.38, h * 0.36, COLORS.snow, 0, h * 0.64 - 0.4, 0, g, 5);
}
const nearPlace = (angle, side, gap) => placeDefs.some((p) => {
    const d = Math.atan2(Math.sin(angle - p.angle), Math.cos(angle - p.angle));
    return Math.abs(d) < gap && Math.abs(side - SIDE_OFFSET) < 0.3;
});
for (let i = 0; i < 34; i++) {
    const angle = rand() * Math.PI * 2;
    const side = -(0.55 + rand() * 0.55);
    mountain(angle, side, 2.6 + rand() * 2.6, 5 + rand() * 6.5);
}
for (let placed = 0, tries = 0; placed < 90 && tries < 800; tries++) {
    const angle = rand() * Math.PI * 2;
    // Arbres de chaque côté, jamais sur le chemin ni entre la caméra et Valkea.
    const side = rand() < 0.5 ? -(0.16 + rand() * 0.34) : 0.3 + rand() * 0.6;
    if (nearPlace(angle, side, 0.34)) continue;
    tree(angle, side, 0.6 + rand() * 0.6);
    placed++;
}

// --- Os à ramasser : au sol sur le chemin, ou en l'air (il faut sauter) ---
const BONE_COUNT = 14;
const BONE_HIGH = 3.7;
const boneMaterial = new THREE.MeshStandardMaterial({ color: 0xe6b44c, emissive: 0xe6b44c, emissiveIntensity: 0.3, flatShading: true });
function buildBone() {
    const bone = new THREE.Group();
    const shaft = mesh(new THREE.CylinderGeometry(0.13, 0.13, 0.9, 6), boneMaterial, 0, 0, 0, bone);
    shaft.rotation.z = Math.PI / 2;
    [[-0.45, 0.13], [-0.45, -0.13], [0.45, 0.13], [0.45, -0.13]].forEach(([x, y]) => mesh(new THREE.IcosahedronGeometry(0.19), boneMaterial, x, y, 0, bone));
    return bone;
}
const bones = [];
for (let i = 0; i < BONE_COUNT; i++) {
    // Répartis entre les étapes, un sur deux en hauteur.
    const angle = ((i + 0.5) / BONE_COUNT) * Math.PI * 2;
    const high = i % 2 === 1;
    const holder = onPlanet(angle, 0);
    const model = buildBone();
    model.position.y = high ? BONE_HIGH : 1.3;
    holder.add(model);
    bones.push({ index: i, angle, high, holder, model });
}

function buildSignpost(g) {
    // Panneau de départ : la présentation.
    cyl(0.14, 0.18, 3.6, COLORS.wood, 0, 0, 0, g, 6);
    box(3.2, 1, 0.14, COLORS.snow, 0.4, 2.5, 0.12, g);
    box(2.4, 0.14, 0.05, COLORS.ink, 0.4, 3.2, 0.2, g);
    box(1.8, 0.14, 0.05, COLORS.ink, 0.1, 2.85, 0.2, g);
    const flag = mesh(new THREE.ConeGeometry(0.42, 1, 3), COLORS.accent, 0.5, 3.75, 0, g);
    flag.rotation.z = -Math.PI / 2;
}
function buildChalet(g) {
    box(7, 3.4, 5.4, COLORS.wood, 0, 0, 0, g);
    const roof = cone(5.7, 3, COLORS.ink, 0, 3.4, 0, g, 4);
    roof.rotation.y = Math.PI / 4;
    roof.scale.z = 0.8;
    box(1.4, 2.2, 0.2, COLORS.ink, 0, 0, 2.72, g);
    box(1.2, 1, 0.2, 0xf4d58d, -2.2, 1.3, 2.72, g);
    box(1.2, 1, 0.2, 0xf4d58d, 2.2, 1.3, 2.72, g);
    box(0.7, 1.8, 0.7, COLORS.wall, 2, 4, 0, g);
}
function buildAirport(g) {
    // Tour de contrôle et avion : la navigation intérieure dans les aéroports.
    cyl(0.9, 1.3, 6, COLORS.wall, -1.8, 0, -0.6, g);
    cyl(1.8, 1.25, 1.4, COLORS.glass, -1.8, 6, -0.6, g, 8);
    cyl(1.9, 1.9, 0.25, COLORS.ink, -1.8, 7.4, -0.6, g, 8);
    box(4.5, 1.8, 3.2, COLORS.wall, 1.2, 0, -0.6, g);
    const plane = new THREE.Group();
    plane.position.set(1.5, 1, 2);
    plane.rotation.y = 1.1;
    g.add(plane);
    const body = cyl(0.42, 0.42, 4.6, COLORS.snow, 0, -2.3, 0, plane);
    body.rotation.x = Math.PI / 2;
    body.position.set(0, 0, 0);
    box(4.4, 0.1, 0.9, COLORS.accent, 0, -0.05, 0, plane);
    box(0.1, 0.9, 0.6, COLORS.accent, 0, 0.2, -2, plane);
}
function buildAlpes(g) {
    // Bureau et flotte de téléphones, tous pareils : plus de 1 000 appareils.
    box(6, 3.5, 3.2, COLORS.wall, 0, 0, -1, g);
    box(6.3, 0.3, 3.5, COLORS.ink, 0, 3.5, -1, g);
    box(1.2, 2, 0.15, COLORS.ink, 0, 0, 0.65, g);
    for (let i = 0; i < 2; i++) box(0.9, 0.9, 0.12, COLORS.glass, -2.1 + i * 4.2, 1.5, 0.62, g);
    for (let i = 0; i < 5; i++) {
        const phone = box(0.65, 1.2, 0.1, COLORS.ink, -2.4 + i * 1.2, 0, 1.8, g);
        phone.rotation.x = -0.2;
        box(0.5, 0.92, 0.04, COLORS.accent, 0, -0.46, 0.06, phone);
    }
}
function buildCourt(g) {
    // Panier de basket : Glorious Five. Serveur : le chatbot auto-hébergé.
    const court = mesh(new THREE.PlaneGeometry(6.5, 4.8), 0xc9a57b, 0, 0.3, 0, g);
    court.rotation.x = -Math.PI / 2;
    court.castShadow = false;
    cyl(0.12, 0.12, 4, COLORS.ink, 0, 0, -2.1, g);
    box(1.9, 1.2, 0.1, COLORS.snow, 0, 3.4, -1.85, g);
    const hoop = mesh(new THREE.TorusGeometry(0.42, 0.05, 6, 14), COLORS.accent, 0, 3.55, -1.35, g);
    hoop.rotation.x = Math.PI / 2;
    mesh(new THREE.IcosahedronGeometry(0.42), 0xd8702e, 1.2, 0.72, 0.8, g);
    box(1.3, 2.3, 1.1, COLORS.ink, -2.5, 0, 0.4, g);
    for (let i = 0; i < 3; i++) box(0.9, 0.12, 0.05, 0x7fd18c, -2.5, 0.6 + i * 0.5, 0.97, g);
}
function buildVr(g) {
    // Socle et casque VR, puis pile de livres : les débuts et la formation.
    cyl(1.2, 1.5, 1.4, COLORS.wall, -0.7, 0, 0, g, 8);
    box(1.7, 0.9, 0.9, COLORS.ink, -0.7, 1.7, 0, g);
    box(1.35, 0.5, 0.15, COLORS.accent, -0.7, 1.9, 0.48, g);
    const strap = mesh(new THREE.TorusGeometry(0.72, 0.07, 6, 14), COLORS.ink, -0.7, 2.15, -0.15, g);
    strap.rotation.x = Math.PI / 2;
    [COLORS.accent, COLORS.ink, COLORS.glass].forEach((c, i) => box(1.5 - i * 0.15, 0.35, 1, c, 1.8, i * 0.35, 0.8, g));
}
function buildFalconry(g) {
    // Perchoir et faucon, batterie à côté.
    cyl(0.1, 0.12, 2.3, COLORS.wood, -1.6, 0, 0, g, 6);
    box(1.1, 0.12, 0.12, COLORS.wood, -1.6, 2.3, 0, g);
    const bird = mesh(new THREE.IcosahedronGeometry(0.42), 0x6b5a48, -1.6, 2.82, 0, g);
    bird.scale.set(0.8, 1.15, 0.8);
    cone(0.13, 0.28, COLORS.ink, -1.6, 3.05, 0.25, g, 4).rotation.x = Math.PI / 2;
    cyl(0.75, 0.75, 0.75, COLORS.accent, 1.4, 0.15, 0, g, 12);
    cyl(0.4, 0.4, 0.35, COLORS.snow, 0.8, 1.05, 0.25, g, 12);
    cyl(0.4, 0.4, 0.35, COLORS.snow, 2, 1.05, 0.25, g, 12);
    cyl(0.03, 0.03, 1.7, COLORS.ink, 2.7, 0, 0, g, 5);
    cyl(0.5, 0.5, 0.03, 0xd9b75a, 2.7, 1.7, 0, g, 12);
}

// --- Valkea : reste au sommet, c'est la planète qui tourne sous elle ---
// Akita d'après photos : robe fauve au dos sombre, plastron et pattes blancs, masque noir, queue enroulée.
const dog = new THREE.Group();
const DOG = { fawn: 0xb8804a, saddle: 0x3d3733, white: 0xf6f3ea, mask: 0x191716, face: 0x8c7a65, ear: 0x2a2523, tail: 0xefe6d4, tongue: 0xd9707a };
box(0.58, 0.54, 1.3, DOG.fawn, 0, 0.5, 0, dog);
box(0.6, 0.16, 1.02, DOG.saddle, 0, 0.92, -0.08, dog);
box(0.5, 0.14, 1.1, DOG.white, 0, 0.42, 0, dog);
box(0.6, 0.6, 0.3, DOG.white, 0, 0.46, 0.56, dog);                // Plastron.
box(0.54, 0.16, 0.34, DOG.fawn, 0, 0.92, 0.5, dog);
box(0.62, 0.56, 0.1, DOG.mask, 0, 0.5, 0.28, dog);                // Harnais noir et médaille.
box(0.12, 0.12, 0.05, COLORS.accent, 0.25, 0.62, 0.72, dog);
const head = box(0.56, 0.5, 0.5, DOG.face, 0, 0.98, 0.72, dog);
box(0.58, 0.24, 0.4, DOG.mask, 0, -0.25, 0.08, head);             // Masque.
box(0.14, 0.3, 0.06, DOG.mask, 0, 0, 0.26, head);                 // Liste noire du front.
box(0.34, 0.24, 0.32, DOG.mask, 0, -0.25, 0.36, head);            // Museau.
box(0.14, 0.09, 0.05, 0x050505, 0, -0.06, 0.53, head);            // Truffe.
box(0.16, 0.04, 0.2, DOG.tongue, 0, -0.27, 0.42, head);
[-0.2, 0.2].forEach((x) => {
    box(0.08, 0.08, 0.04, 0x120c08, x, 0.01, 0.25, head);         // Yeux.
    const ear = cone(0.17, 0.42, DOG.ear, x, 0.2, -0.06, head, 4);
    ear.rotation.z = x > 0 ? -0.12 : 0.12;
});
// Queue touffue enroulée sur le dos, base sombre.
const tail = new THREE.Group();
tail.position.set(0, 1.26, -0.56);
dog.add(tail);
const curl = mesh(new THREE.TorusGeometry(0.2, 0.13, 6, 10, Math.PI * 1.55), DOG.tail, 0, 0, 0, tail);
curl.rotation.set(0, Math.PI / 2, -0.5);
box(0.2, 0.26, 0.2, DOG.saddle, 0, -0.38, -0.2, tail);
const legs = [[-0.2, 0.46], [0.2, 0.46], [-0.2, -0.46], [0.2, -0.46]].map(([x, z]) => box(0.19, 0.56, 0.2, DOG.white, x, 0, z, dog));
legs.slice(2).forEach((leg) => box(0.195, 0.1, 0.205, DOG.saddle, 0, -0.02, 0, leg)); // Marque sombre aux pattes arrière.
dog.scale.setScalar(1.75);
dog.position.set(0, R + 0.12, 0);
scene.add(dog);

// --- Jeu ---
// Partie gardée en mémoire seulement : chaque nouvelle connexion repart de zéro.
const visited = new Set();

// Chaque action ne rapporte qu'une fois, et le score se fige avec le dernier os.
const XP = { bone: 10, jump: 5, place: 50, chat: 30, cv: 30, tour: 100 };
const score = { xp: 0, bones: 0, actions: [] };
const allBones = () => score.bones === BONE_COUNT;

function gain(points) {
    score.xp += points;
    renderScore();
    if (!motionOk || dialogOpen()) return;
    // Petit « +10 » qui s'élève au-dessus de Valkea.
    const pop = document.createElement("span");
    pop.className = "pop";
    pop.textContent = `+${points}`;
    projected.set(0, R + 3.2 + height, 0).project(camera);
    pop.style.left = `${(projected.x + 1) / 2 * innerWidth}px`;
    pop.style.top = `${(1 - projected.y) / 2 * innerHeight}px`;
    pop.addEventListener("animationend", () => pop.remove());
    $("labels").append(pop);
}
function firstTime(action, points) {
    if (allBones() || score.actions.includes(action)) return;
    score.actions.push(action);
    gain(points);
}
function renderScore() {
    $("xp").textContent = `${score.xp} XP`;
    $("bones").textContent = UI[lang].bones(score.bones, BONE_COUNT);
    $("bonus").hidden = !allBones();
    $("bonus").textContent = UI[lang].bonusButton;
}
function collectBones() {
    if (dialogOpen()) return;
    bones.forEach((b) => {
        if (!b.holder.visible || Math.abs(shortest(b.angle - position)) * R > 1.6) return;
        if (b.high && height < 1.1) return;
        b.holder.visible = false;
        score.bones++;
        gain(XP.bone);
        if (allBones()) openBonus();
    });
}

const FORWARD_KEYS = new Set(["ArrowRight", "KeyD", "ArrowUp", "KeyW"]);   // KeyW / KeyD = Z / D en AZERTY.
const BACK_KEYS = new Set(["ArrowLeft", "KeyA", "ArrowDown", "KeyS"]);     // KeyA / KeyS = Q / S en AZERTY.
const keys = new Set();
let held = 0;             // Bouton ou écran maintenu : +1 avance, -1 recule.
let position = -0.2;      // Angle de la planète : Valkea démarre juste avant la première étape.
let velocity = 0;         // Vitesse lissée, en radians par seconde.
let targetPlace = null;   // Étape à rejoindre seule (choisie dans la liste), ouverte à l'arrivée.
let current = null;       // Étape assez proche pour y entrer.
let height = 0;
let verticalSpeed = 0;
let heading = 1;          // Sens du regard, lissé entre -1 et 1.
let walkTime = 0;

const placeDialog = $("place");
const introDialog = $("intro");
const dialogOpen = () => placeDialog.open || introDialog.open;
const shortest = (a) => Math.atan2(Math.sin(a), Math.cos(a));

function jump() {
    if (height !== 0 || verticalSpeed > 0 || dialogOpen()) return;
    verticalSpeed = JUMP_SPEED;
    firstTime("jump", XP.jump);
}

function openPlace(place) {
    targetPlace = null;
    held = 0;
    keys.clear();
    const firstCompletion = !visited.has(place.id) && visited.size === placeDefs.length - 1;
    const firstVisit = !visited.has(place.id);
    visited.add(place.id);
    if (firstVisit && !allBones()) gain(XP.place);
    if (firstCompletion) firstTime("tour", XP.tour);
    place.marker.visible = false;
    renderProgress();
    const body = $("place-body");
    body.innerHTML = PLACES[lang][place.id].html;
    if (firstCompletion) {
        const done = document.createElement("p");
        done.className = "reward";
        done.textContent = UI[lang].done(score.xp);
        body.append(done);
    }
    wirePanel(body);
    placeDialog.showModal();
    placeDialog.scrollTop = 0;
    renderEnter();
}

// Résumé express débloqué par le dernier os : le CV et des questions prêtes pour l'assistant.
function openBonus() {
    targetPlace = null;
    held = 0;
    keys.clear();
    const body = $("place-body");
    body.innerHTML = BONUS[lang].html(score.xp);
    wirePanel(body);
    placeDialog.showModal();
    placeDialog.scrollTop = 0;
    renderEnter();
}

function wirePanel(body) {
    const openChat = (anchor, title, question) => {
        const frame = document.createElement("iframe");
        frame.className = "chat-frame";
        frame.title = title;
        frame.src = `../chat/?lang=${lang}` + (question ? `&q=${encodeURIComponent(question)}` : "");
        anchor.replaceWith(frame);
        firstTime("chat", XP.chat);
        frame.scrollIntoView({ block: "nearest" });
    };
    const chatButton = body.querySelector("[data-open-chat]");
    if (chatButton) chatButton.addEventListener("click", () => openChat(chatButton, chatButton.textContent));
    const questions = body.querySelector(".questions");
    if (questions) questions.querySelectorAll("button").forEach((button) => {
        button.addEventListener("click", () => openChat(questions, button.textContent, button.textContent));
    });
    const cvLink = body.querySelector("[data-cv]");
    if (cvLink) cvLink.addEventListener("click", () => firstTime("cv", XP.cv));
}

function renderProgress() {
    $("progress").textContent = UI[lang].visited(visited.size, placeDefs.length);
    placeDefs.forEach((p) => {
        p.label.classList.toggle("is-visited", visited.has(p.id));
        p.step.classList.toggle("is-visited", visited.has(p.id));
        p.step.classList.toggle("is-current", p === current);
    });
}
function renderEnter() {
    const button = $("enter");
    const show = current && !dialogOpen();
    button.hidden = !show;
    if (show) button.textContent = UI[lang].enter(PLACES[lang][current.id].name);
}
function render() {
    const t = UI[lang];
    document.documentElement.lang = lang;
    $("classic").textContent = t.classic;
    $("hint").textContent = t.hintDesktop;
    $("place-close").textContent = t.close;
    $("intro-kicker").textContent = t.introKicker;
    $("intro-title").textContent = t.introTitle;
    $("intro-text").textContent = t.introText;
    $("intro-controls").textContent = t.introControls;
    $("intro-start").textContent = t.start;
    $("back").setAttribute("aria-label", t.back);
    $("forward").setAttribute("aria-label", t.forward);
    $("jump").setAttribute("aria-label", t.jump);
    $("jump").title = t.jump;
    $("steps").setAttribute("aria-label", t.steps);
    document.querySelectorAll(".lang button").forEach((b) => b.setAttribute("aria-pressed", String(b.dataset.lang === lang)));
    placeDefs.forEach((p, i) => {
        const name = PLACES[lang][p.id].name;
        p.label.textContent = name;
        p.step.title = name;
        p.step.setAttribute("aria-label", `${i + 1}. ${name}`);
    });
    renderProgress();
    renderEnter();
    renderScore();
}

placeDefs.forEach((p, i) => {
    p.label = document.createElement("button");
    p.label.type = "button";
    p.label.className = "label";
    p.label.tabIndex = -1; // Au clavier, c'est la liste d'étapes qui sert de navigation.
    p.label.addEventListener("click", () => { targetPlace = p; });
    $("labels").append(p.label);
    p.step = document.createElement("button");
    p.step.type = "button";
    p.step.className = "step";
    p.step.textContent = i + 1;
    p.step.addEventListener("click", () => { targetPlace = p; });
    $("steps").append(p.step);
});

document.querySelectorAll(".lang button").forEach((b) => b.addEventListener("click", () => {
    lang = b.dataset.lang;
    write(LANG_KEY, lang);
    render();
}));
$("enter").addEventListener("click", () => { if (current) openPlace(current); });
$("bonus").addEventListener("click", openBonus);
$("place-close").addEventListener("click", () => placeDialog.close());
placeDialog.addEventListener("click", (e) => { if (e.target === placeDialog) placeDialog.close(); });
placeDialog.addEventListener("close", renderEnter);
introDialog.addEventListener("close", renderEnter);

addEventListener("keydown", (e) => {
    if (dialogOpen() || e.ctrlKey || e.metaKey || e.altKey) return;
    const onControl = e.target.closest?.("a, button");
    if (FORWARD_KEYS.has(e.code) || BACK_KEYS.has(e.code)) {
        keys.add(e.code);
        targetPlace = null;
        e.preventDefault();
    } else if (e.code === "Space" && !onControl) {
        jump();
        e.preventDefault();
    } else if ((e.code === "Enter" || e.code === "KeyE") && current && !onControl) {
        // Sans cela, le même appui activerait « Fermer », qui reçoit le focus à l'ouverture.
        e.preventDefault();
        openPlace(current);
    }
});
addEventListener("keyup", (e) => keys.delete(e.code));
addEventListener("blur", () => { keys.clear(); held = 0; });

// Boutons maintenus. preventDefault : le focus reste sur la page, Espace continue de faire sauter.
function holdButton(button, direction) {
    button.addEventListener("pointerdown", (e) => {
        e.preventDefault();
        button.setPointerCapture(e.pointerId);
        held = direction;
        targetPlace = null;
    });
    ["pointerup", "pointercancel", "lostpointercapture"].forEach((type) => button.addEventListener(type, () => { held = 0; }));
    // Activation au clavier (Tab puis Entrée) : va directement à l'étape suivante dans ce sens.
    button.addEventListener("click", (e) => {
        if (e.detail !== 0) return;
        const n = placeDefs.length;
        const from = (position / (Math.PI * 2)) * n;
        const index = direction > 0 ? Math.floor(from + 0.01) + 1 : Math.ceil(from - 0.01) - 1;
        targetPlace = placeDefs[((index % n) + n) % n];
    });
}
holdButton($("back"), -1);
holdButton($("forward"), 1);
$("jump").addEventListener("pointerdown", (e) => { e.preventDefault(); jump(); });
$("jump").addEventListener("click", (e) => { if (e.detail === 0) jump(); });

// Maintenir sur la scène : à droite on avance, à gauche on recule.
const canvas = $("scene");
canvas.addEventListener("pointerdown", (e) => {
    canvas.setPointerCapture(e.pointerId);
    held = e.clientX >= innerWidth / 2 ? 1 : -1;
    targetPlace = null;
});
["pointerup", "pointercancel"].forEach((type) => canvas.addEventListener(type, () => { held = 0; }));

// Caméra fixe, reculée sur écran étroit pour garder la courbure de la planète à l'écran.
function resize() {
    renderer.setSize(innerWidth, innerHeight, false);
    camera.aspect = innerWidth / innerHeight;
    const k = Math.min(2.1, Math.max(1, 1.2 / camera.aspect));
    camera.position.set(0, R + 7 * k, 44 * k);
    camera.lookAt(0, R - 4.5 * k, 0);
    camera.updateProjectionMatrix();
}
addEventListener("resize", resize);
resize();

function placeLabels() {
    planet.updateMatrixWorld();
    placeDefs.forEach((p) => {
        // Seules les étapes de la face visible, proches du sommet, gardent leur nom à l'écran.
        const visible = Math.abs(shortest(p.angle - position)) < 1.15;
        p.label.hidden = !visible;
        if (!visible) return;
        projected.set(0, p.top + 1.7, 0);
        p.group.localToWorld(projected).project(camera);
        const w = p.label.offsetWidth;
        const x = Math.max(8, Math.min(innerWidth - w - 8, (projected.x + 1) / 2 * innerWidth - w / 2));
        const y = Math.max(76, (1 - projected.y) / 2 * innerHeight - p.label.offsetHeight / 2);
        p.label.style.transform = `translate(${Math.round(x)}px, ${Math.round(y)}px)`;
    });
}

function step(dt, time) {
    let wanted = 0;
    if (!dialogOpen()) {
        keys.forEach((code) => { wanted += FORWARD_KEYS.has(code) ? 1 : -1; });
        wanted = Math.sign(wanted) || held;
        if (!wanted && targetPlace) {
            const gap = shortest(targetPlace.angle - position);
            if (Math.abs(gap) * R < 0.25) openPlace(targetPlace);
            else wanted = Math.sign(gap) * Math.min(1, Math.abs(gap) * R / 1.5 + 0.2);
        }
    }
    const maxSpeed = SPEED / R;
    velocity += (wanted * maxSpeed - velocity) * Math.min(1, dt * 10);
    if (Math.abs(velocity) < 0.001 && !wanted) velocity = 0;
    position += velocity * dt;
    planet.rotation.z = position;
    const moving = velocity !== 0;
    if (moving) walkTime += dt * Math.abs(velocity) / maxSpeed;

    // Saut.
    if (height > 0 || verticalSpeed > 0) {
        height += verticalSpeed * dt;
        verticalSpeed -= GRAVITY * dt;
        if (height <= 0) { height = 0; verticalSpeed = 0; }
    }
    dog.position.y = R + 0.12 + height;
    collectBones();

    // Valkea regarde dans le sens de la marche (de profil, vers la droite ou la gauche).
    if (wanted) heading += (Math.sign(wanted) - heading) * Math.min(1, dt * 12);
    dog.rotation.y = (Math.PI / 2) * heading;

    const closest = placeDefs.find((p) => Math.abs(shortest(p.angle - position)) * R < ENTER_DISTANCE) || null;
    if (closest !== current) {
        current = closest;
        renderEnter();
        renderProgress();
    }

    if (motionOk) {
        const swing = moving && height === 0 ? Math.sin(walkTime * 15) * 0.65 : height > 0 ? 0.5 : 0;
        legs.forEach((leg, i) => { leg.rotation.x = height > 0 ? (i < 2 ? -swing : swing) : (i === 0 || i === 3) ? swing : -swing; });
        tail.rotation.y = Math.sin(time * (moving ? 12 : 5)) * 0.3;
        bones.forEach((b, i) => {
            b.model.rotation.y = time * 2 + i;
            b.model.position.y = (b.high ? BONE_HIGH : 1.3) + Math.sin(time * 3 + i) * 0.12;
        });
        placeDefs.forEach((p, i) => {
            p.marker.rotation.y = time * 1.2;
            p.marker.position.y = p.top + Math.sin(time * 2 + i) * 0.2;
        });
    }
}

let last = performance.now();
function frame(now) {
    const dt = Math.min(0.1, (now - last) / 1000);
    last = now;
    step(dt, now / 1000);
    placeLabels();
    renderer.render(scene, camera);
    requestAnimationFrame(frame);
}

render();
introDialog.showModal();
requestAnimationFrame(frame);
