# 07 — Le pipeline Telegram

De la publication d'un grossiste jusqu'au produit visible sur le site.

## Le schéma

```
 Grossiste publie (Telegram)
   photo(s) + légende arabe "130dh"
            │
            ▼
 ┌─────────────────────────┐
 │ WATCHER (toutes 15 min) │  fenêtre glissante 6 h, 30 msg/canal
 └───────────┬─────────────┘
             ▼
 ┌─────────────────────────┐
 │ AGENT.handleMessage()   │
 │  • canal surveillé ?    │
 │  • album ? → regroupé   │
 │  • doublon ? → stop     │
 │  • extraction (règles)  │
 │  • OCR de la photo      │
 └───────────┬─────────────┘
             ▼
   telegram_messages  (processed = false)   ← la FILE
             │
             ▼  validation humaine dans le backoffice
   products + product_media
             │
             ▼
        LA VITRINE
```

## Étape 1 — Collecte (watcher)

- Relit les derniers messages de chaque canal, filtre `!m.out && m.date >= since`.
- `since = maintenant − lookbackH` (6 h par défaut).
- Pré-charge les IDs déjà vus (`primeSeen`) pour ne pas rejouer l'historique.

## Étape 2 — Analyse (agent)

Pour chaque message :
1. **Canal surveillé ?** Sinon `ignore`.
2. **Album ?** `albumOf()` regroupe les messages de même `groupedId` (souvent 4 médias).
3. **Doublon ?** clé `(channel_id, telegram_message_id)` en mémoire et en base.
4. **Extraction** (`extract.js`) :
   - `findPrice()` → prix (dans la **légende**).
   - `findLotQuantity()` → nombre d'unités du lot.
   - `nameFromOcr()` / `buildName()` → nom (souvent dans la **photo**).
   - `resolveCategory()` → catégorie (règles + hint du canal).
5. **OCR** (`media.js`) sur la photo.
6. **Médias** : les images sont préparées ; la vidéo est **compressée en 480p** et une **vignette** est extraite (`extractPoster`).
7. Écrit une ligne `telegram_messages` avec `processed = false` et tout le détail dans `raw_payload`.

> En mode `queue` (le défaut), l'agent **ne publie jamais seul**. Il prépare un brouillon.

## Étape 3 — Validation (backoffice)

Le commerçant ouvre l'onglet **En attente** :
- voit la photo, le prix détecté, le nom proposé, la catégorie ;
- corrige ce qui est faux ;
- **valide** (`POST /api/pending/:id/publish`) ou **rejette** (`DELETE`).

La validation :
- crée/met à jour le `products` (nom, prix de gros, prix de vente = gros + marge, catégorie, fournisseur) ;
- recopie les médias via `attachMedia()` : images, **vidéo**, et **vignette** (`media_type = 'poster'`) ;
- marque le message `processed = true`.

## Étape 4 — Publication (vitrine)

La vitrine lit `products_with_category` (produits publiés et actifs) + `product_media`, et affiche la carte, le carrousel et la vidéo.

---

## Réalités de l'extraction (à ne pas oublier)

### L'OCR plafonne à ~3 %
Tesseract ne lit proprement qu'une **petite fraction** des annonces : photos floues, texte incrusté dans l'image, arabe dialectal, prix mélangés au décor. **C'est la raison d'être de la validation manuelle.** Ne pas chercher à « tout automatiser » sans accepter ce taux.

### Prix vs nom : qui est où
Dans ces canaux :
- le **prix est dans la légende** (« 130dh », « 90 ») ;
- le **nom du produit est dans la photo** (texte incrusté).
Ne pas inverser.

### Les albums
Un produit = souvent plusieurs photos. Le regroupement par `groupedId` est essentiel : sans lui, un album de 4 photos devient 4 produits. Détail teleproto : `offsetId` est **exclusif**, d'où `message.id + 1` et une limite de 25 pour attraper les frères.

### Les boîtes à prix et le bruit
Les légendes contiennent beaucoup de bruit (numéros, « للطلب », liens, mentions de livraison). `extract.js` filtre via `BOILERPLATE`, `NOISE`, `DIMENSIONS`.

### Les vidéos
- Compressées **480p** avant stockage (ex. 45,7 Mo → 0,6 Mo).
- Une **vignette** est extraite et stockée (`poster`), réutilisée par la carte vitrine.
- Repli : si `ffmpeg` échoue, l'original est conservé.

---

## Outils de diagnostic

```bash
cd bridge

# Voir ce qu'un canal contient, sans rien écrire
node tools/probe-channel.js -100000000001

# Simuler l'analyse sur les N derniers jours
node tools/dry-run.js 7

# Inspecter le détail brut d'un message
node tools/dump-raw.js <telegram_message_id>

# Rattraper les vignettes manquantes
node tools/backfill-pending-posters.js
```

## Incidents passés (pour mémoire)

| Date | Incident | Corrigé par |
| --- | --- | --- |
| 30/09 | Watcher mort au démarrage, import arrêté 13 h en silence | `startWatcherWithRetry()` + `status().actif` fiable |
| 30/09 | Albums éclatés en plusieurs produits | Regroupement par `groupedId` + `markAlbumSeen` |
| 30/09 | Doublons après redémarrage | Pré-chargement des IDs vus (`primedChannels`) |
| 30/09 | Vignettes absentes sur les cartes vidéo | `extractPoster` + propagation `poster` |
