# 05 — Le pont wa-bridge

Le pont est le composant le plus original du projet : **un seul processus Node** qui tient à la fois une connexion WhatsApp et une connexion Telegram, et fait tourner une boucle d'import.

## Rôle

| Capacité | Fichier | Détail |
| --- | --- | --- |
| Connexion WhatsApp | `src/whatsapp.js` | Baileys, session sur disque, reconnexion |
| Connexion Telegram | `src/telegram.js` | teleproto (MTProto), session StringSession |
| Serveur de contrôle | `src/server.js` | HTTP local (`/status`, `/qr`, `/send`…) |
| Journal mémoire | `src/store.js` | tampon des derniers messages |
| Surveillance des canaux | `src/agent/watcher.js` | boucle de collecte |
| Agent commercial | `src/agent/agent.js` | analyse → fiche produit |
| Extraction | `src/agent/extract.js` | prix, lot, nom, catégorie (règles) |
| OCR & médias | `src/agent/media.js` | Tesseract, compression vidéo, vignette |
| Réponse WhatsApp | `src/agent/responder.js` | réponse auto + escalade |
| Client Supabase | `src/agent/supabase.js` | REST + Storage |
| Configuration | `src/agent/config.js` | marges, règles de catégorie, canaux de repli |

## Démarrage

```bash
cd bridge
cp .env.example .env      # renseigner Supabase + Telegram
npm install
node index.js             # ou via pm2
```

> **Ne jamais lancer `node index.js` à la main si pm2 fait déjà tourner le pont** : deux instances se disputent la session WhatsApp (« session remplacée »).

Scripts utiles :
```bash
./bridge.sh status        # état pm2 + /status
./bridge.sh log 100       # dernières lignes
./bridge.sh msgs 30       # derniers messages en mémoire
./bridge.sh send 2126… "texte"   # envoyer un WhatsApp
./restart.sh              # redémarrage propre (supervisé pm2)
./restart.sh --reset      # efface l'état WhatsApp (ré-appairage)
./stop.sh
```

## Le watcher (`src/agent/watcher.js`)

- Tour toutes les **15 min** (`WATCH_INTERVAL_MIN`), fenêtre glissante de **6 h** (`WATCH_LOOKBACK_H`), 30 messages par canal (`WATCH_PER_CHANNEL`).

### D'où viennent les canaux
Trois niveaux se superposent :
1. **`CHANNELS_FILE`** — un fichier JSON (`[{ id, label, hint, margin }]`) qui porte la **liste réelle**. Ce fichier n'est pas versionné ici : il est fourni avec les identifiants. Les commentaires de pleine ligne `//` sont acceptés.
2. **`config.js`** — si `CHANNELS_FILE` est absent ou illisible, le pont retombe sur la liste **anonymisée** du dépôt public (11 placeholders) et **affiche un avertissement**. L'import ne trouve alors rien : c'est un avertissement bruyant, pas un silence.
3. **`WATCH_CHANNELS`** — liste blanche : ne filtre que ce qui est réellement importé. Vide = tous.
- **Fenêtre glissante plutôt que temps réel** : un message raté (réseau, redémarrage) est rattrapé au tour suivant, et la déduplication rend le passage idempotent.
- **Un seul tour à la fois** ; un tour long (OCR) n'est pas chevauché.
- **Reprise au démarrage** (`startWatcherWithRetry` dans `index.js`) : si le démarrage échoue (erreur Supabase ponctuelle, par ex.), il réessaie indéfiniment avec un délai croissant. *Incident fondateur : le 30/09, le watcher est mort au démarrage et l'import est resté silencieusement arrêté 13 h.*
- `status().actif` est **vrai dès le premier tour**, pas seulement quand le timer est posé (sinon l'interface affichait « inactif » alors que le premier tour OCR durait plusieurs minutes).

### Dédoublonnage
- Clé : `(channel_id, telegram_message_id)`.
- Deux niveaux : mémoire (`primedChannels`, pré-charge les IDs déjà connus) **et** base.
- Les **albums** sont marqués vus en bloc (`markAlbumSeen`) pour éviter qu'un même album soit retraité message par message.

## L'agent (`src/agent/agent.js`)

`handleMessage(message, entity)` renvoie une action :
- `publie` — écrit directement un produit (seulement hors mode file, rare).
- `confirmation` — écrit une ligne `telegram_messages` `processed = false` (mode `queue`, le défaut).
- `doublon` — déjà vu.
- `ignore` — canal non surveillé, message non pertinent.
- `erreur`.

Points importants :
- **Les albums** : `albumOf()` regroupe les messages partageant le même `groupedId`. On passe `offsetId: message.id + 1` (**exclusif** dans teleproto) et une limite de 25 pour récupérer les frères.
- **Catégorie** : `resolveCategory()` teste les règles de `CATEGORY_RULES` (13 catégories + `non-categorise`). Le `hint` du canal force une catégorie.
- **Marge** : `DEFAULT_MARGIN = 10` DH ajoutée au prix de gros pour proposer un prix de vente.
- **Fournisseur** : `supplierFor()` associe le canal à une ligne `suppliers`.

## L'extraction (`src/agent/extract.js`) — 100 % à règles

- `findPrice(text)` : lit le prix dans la **légende** (pas dans la photo).
- `findLotQuantity(text)` : détecte les mots de lot (كرطونة، لوت، carton, pack…).
- `buildName(text)` / `nameFromOcr(ocrText)` : le **nom** est souvent écrit dans l'image.
- `looksLikeProductName()` : filtre le bruit (معلومات التوصيل, numéros, liens…).
- `analyzeMessage()` : assemble le tout.

> Pas de LLM. La précision est volontairement compensée par la validation humaine.

## Médias (`src/agent/media.js`)

- `ocrFor(message)` : Tesseract sur la photo.
- `ensureFile(message)` : télécharge le média localement.
- `extractPoster(videoPath, outPath)` : extrait une vignette JPEG d'une vidéo (à ~1 s).
- Compression vidéo : **480p**, H.264, `+faststart`, repli silencieux sur l'original en cas d'échec.

## Le répondeur WhatsApp (`src/agent/responder.js`)

Traitement d'un message entrant → `{ action: 'repondu' | 'escalade' | 'ignore' }` :
- Ignore : agent désactivé, groupe, message vide, politesse, cooldown, plafond d'escalades.
- Escalade : message multimédia sans texte, demande d'humain, question de stock, question non reconnue, aucun/plusieurs produits correspondants, escalade échouée.
- Répond : question de **prix** sur un produit identifiable (recherche par tokens, un seul résultat sinon escalade).

L'escalade envoie un message au **propriétaire** (`OWNER_PHONE`). `checkOwner()` vérifie au démarrage que le numéro existe sur WhatsApp — *sinon les escalades partent dans le vide sans erreur*.

## Pièges connus

### Baileys : ne livre que la forme LID
`xxxx@s.whatsapp.net` est **accepté** par Baileys puis **abandonné** par le serveur, **sans aucune erreur**. La seule cible fiable est le `lid` renvoyé par `onWhatsApp()`. Voir `resolveJid()` (`src/whatsapp.js`, ~ligne 300) et la résolution dans `send()`.

### Config Telegram
`TELEGRAM_API_ID` / `TELEGRAM_API_HASH` sont nécessaires à la première connexion. La session est persistée (`sessions/`, hors dépôt). `BRIDGE_PORT` (par défaut 3013) : un autre process occupait 3003 sur la machine de dev, d'où un port dédié.

## Outils (`bridge/tools/`)

| Outil | But |
| --- | --- |
| `dry-run.js` | Simuler un import sans écrire |
| `probe-channel.js` | Examiner un canal (derniers messages) |
| `inspect-channels.js` | Lister les canaux accessibles |
| `survey-channels.js` | Panorama de plusieurs canaux |
| `backfill.js` | Import manuel sur une fenêtre de jours |
| `restage.js` | Re-téléverser des vidéos compressées |
| `backfill-posters.js` | Vignettes des produits publiés |
| `backfill-pending-posters.js` | Vignettes des messages en attente |
| `dump-raw.js` | Afficher le `raw_payload` d'un message |
| `test-extract.js`, `test-ocr.js`, `test-responder.js` | Tests ciblés |
