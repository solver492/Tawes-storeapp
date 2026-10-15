# 01 — Architecture

## Vue d'ensemble

```
                          ┌─────────────────────────────────────────┐
   Canaux Telegram        │              SUPABASE                   │
   de grossistes          │  (PostgreSQL + Storage)                 │
   (11 configurés)        │                                         │
        │                 │  tables : products, product_media,      │
        │  MTProto        │           categories, telegram_messages│
        ▼                 │  bucket : product-media                 │
 ┌──────────────┐         │                                         │
 │   LE PONT    │───────▶ │  telegram_messages (processed=false)    │
 │  bridge/     │  écrit  │                    │                     │
 │              │         └────────────────────┼─────────────────────┘
 │  • watcher   │                              │ lecture
 │  • agent     │                              ▼
 │  • OCR       │                    ┌──────────────────────┐
 │  • compression│                   │   LE BACKOFFICE      │
 │  • poster    │                    │   artifacts/admin/   │
 │              │                    │                      │
 │  WhatsApp ◀──┼── répond           │  • file d'attente    │
 │  (Baileys)   │   aux clients      │  • validation        │
 └──────────────┘                    │  • médias / upload   │
        ▲                            │  • catégories        │
        │ messages clients           │  • Vimeo             │
        │                            └──────────┬───────────┘
        │                                       │ publie
        │                                       ▼
        │                            ┌──────────────────────┐
        │                            │   LA VITRINE         │
        └────────────────────────────│  artifacts/          │
             (bouton WhatsApp)       │  tawes-store/        │
                                     │  React + Vite        │
                                     │  lit Supabase direct │
                                     └──────────────────────┘
```

## Les quatre briques

### 1. La base Supabase
Le pivot. Tout le monde parle à la même base.
- La **vitrine** la lit avec la clé **anon/publishable** (donc protégée par RLS).
- Le **backoffice** et **le pont** l'écrivent avec la clé **secrète** (`service_role`, contourne RLS).

Détails : [08-Base-de-donnees](08-BASE-DE-DONNEES.md).

### 2. Le pont (`bridge/`)
Un seul processus Node qui porte deux identités et une boucle de travail :
- **WhatsApp** via Baileys (`src/whatsapp.js`) — sert de canal de vente et de support.
- **Telegram** via teleproto (`src/telegram.js`) — sert à *lire* les canaux de grossistes.
- **Le watcher** (`src/agent/watcher.js`) — toutes les 15 min, relit les derniers messages de chaque canal et les passe à l'agent.
- **L'agent commercial** (`src/agent/agent.js`) — analyse un message, en extrait un produit, l'écrit en attente (jamais en public directement).
- **Le répondeur** (`src/agent/responder.js`) — répond aux clients WhatsApp (prix, disponibilité, catalogue) et escalade au propriétaire.
- Un petit **serveur HTTP de contrôle** (`src/server.js`) : `/status`, `/qr/:platform`, `/messages`, `/wa/send`, `/tg/send`…

Détails : [05-Le-pont](05-PONT-WA-BRIDGE.md).

### 3. Le backoffice (`artifacts/admin/`)
Un serveur Express + une page unique en JS vanilla.
- Authentification par mot de passe (`ADMIN_PASSWORD`) et cookie signé.
- **Onglet En attente** : les produits extraits de Telegram, à valider/corriger.
- **Onglet Produits** : le catalogue publié, avec gestion des médias (dont l'**upload multiple** d'images et vidéos).
- **Onglet Catégories** et **carousel/Vimeo**.

Détails : [06-Boutique-et-admin](06-BOUTIQUE-ET-ADMIN.md).

### 4. La vitrine (`artifacts/tawes-store/`)
Site statique React + Vite. **Il n'appelle pas le backoffice** : il interroge Supabase directement avec la clé publique. Affiche produits, catégories, carrousels d'images et vidéos (avec vignette).

## Flux de données typique

1. Un grossiste publie sur Telegram : une photo + une légende « 130dh ».
2. Le watcher relit le message (fenêtre glissante de 6 h) et le passe à l'agent.
3. L'agent OCRise la photo, lit le prix dans la légende, devine la catégorie, et écrit une ligne `telegram_messages` avec `processed = false`.
4. Le commerçant ouvre le backoffice, corrige le nom/prix si besoin, valide.
5. La validation crée/met à jour un `products` publié + ses `product_media` (images, vidéo, vignette).
6. La vitrine lit Supabase et affiche le produit.
7. Un client clique « WhatsApp » : le pont peut répondre automatiquement (prix, dispo) ou transmettre au propriétaire.

## Choix techniques structurants

| Choix | Pourquoi |
| --- | --- |
| Supabase comme pivot (pas d'API maison) | Vitrine statique + RLS, aucun serveur à maintenir côté catalogue |
| Validation manuelle obligatoire | L'OCR ne lit bien que ~3 % des annonces ; l'arabe dialectal est trop variable |
| Extraction **sans LLM** | Coût nul, pas de dépendance externe ; la précision est compensée par la validation humaine |
| Vidéos compressées en 480p + vignette | Ménager le stockage et le débit de la vitrine |
| Le pont porte WhatsApp **et** Telegram dans un seul process | Deux clients Telegram sur la même session se marchent dessus |
| Backoffice en JS vanilla | Léger, sans build, déployable par simple copie de fichiers |
