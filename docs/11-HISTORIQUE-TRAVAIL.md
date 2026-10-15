# 11 — Historique du travail

> Historique **reconstitué** au 30 septembre 2026 à partir du code, de la base et des échanges. Le dépôt Git n'a été créé qu'à la fin : il n'existe donc pas de `git log` antérieur à cette date. Les dates sont approximatives sauf mention.

## 1. Origine — la boutique
Le projet naît d'une boutique en ligne (**Digital Ecom Land**), hébergée en PHP. Elle a été **archivée hors ligne** avant la refonte. Une partie des tables historiques de la base (`orders`, `users`, `withdrawals`, `affiliates`, `profiles`) date de cette époque.

## 2. Pivot — la plateforme d'import
L'idée nouvelle : **Tawes**, une plateforme qui importe automatiquement des produits depuis des **canaux Telegram de grossistes** et les revend au détail ou en gros, avec :
- une **vitrine** publique ;
- un **backoffice** de validation ;
- un **pont** connecté à la fois à Telegram (source) et WhatsApp (clients).

## 3. Le monorepo
Un monorepo est monté avec plusieurs « artifacts » :
- `artifacts/tawes-store` — la vitrine (React/Vite) ;
- `artifacts/admin` — le backoffice (Express) ;
- `artifacts/api-server`, `artifacts/mockup-sandbox`, `lib/` — échafaudage initial, resté vide/inutilisé.

**Constat clé** : le schéma Drizzle de ce monorepo **ne reflète pas** le schéma réel de la base Supabase. Décision : abandonner l'ORM et parler **directement à PostgREST** en `fetch`, sans `supabase-js`. C'est l'origine du client maison (`artifacts/admin/src/supabase.js`).

## 4. Le backoffice
Construit en Express + page JavaScript vanilla **sans framework et sans build**, parce que l'hébergement ne permet pas de builder de façon fiable. Fonctions ajoutées progressivement :
- file d'attente (messages `processed = false`) ;
- produits, catégories ;
- validation / rejet / publication ;
- gestion des médias (URL, upload, Vimeo).

## 5. Le pont wa-bridge
Un processus Node unique tient **WhatsApp (Baileys)** et **Telegram (teleproto)**. Difficultés rencontrées :

- **WhatsApp + LID** : les envois vers `xxx@s.whatsapp.net` étaient acceptés par Baileys puis **silencieusement abandonnés** par le serveur. Solution : passer par `onWhatsApp()` et envoyer vers le `lid`.
- **Port** : le port 3003 était occupé sur la machine de dev → passage à `BRIDGE_PORT=3013`.
- **Escalade dans le vide** : le numéro du propriétaire n'était pas vérifié ; des escalades disparaissaient sans erreur → `checkOwner()` au démarrage.

## 6. Le pipeline Telegram
- **Watcher** : fenêtre glissante de 6 h, passe toutes les 15 min. Choix du glissant plutôt que du temps réel pour rattraper les messages perdus (la dédup rend l'opération idempotente).
- **Extraction 100 % à règles** (jamais de LLM) : prix dans la **légende**, nom dans la **photo** (OCR Tesseract).
- **Albums** : regroupement par `groupedId`. Sans cela, un album de 4 photos créait 4 produits.
- **Déduplication** `(channel_id, telegram_message_id)` en mémoire et en base, avec pré-chargement des IDs connus pour ne pas rejouer l'historique.

## 7. Les incidents du 30 septembre 2026

Trois bugs corrigés le même jour :

1. **Import mort pendant 13 h** — le watcher échouait à son démarrage et ne reprenait pas ; `status().actif` restait faux. Corrigé par `startWatcherWithRetry()` (relance à délai croissant) et un statut `actif` posé dès le **premier tour** (l'OCR initial peut prendre des minutes).
2. **Albums éclatés** en plusieurs produits — corrigé par le regroupement `groupedId` + `markAlbumSeen()`.
3. **Vignettes absentes** sur les cartes vidéo — ajout de `extractPoster()` et d'un `media_type = 'poster'`, plus un **outil de rattrapage** (`backfill-posters.js`, `backfill-pending-posters.js`) pour les vidéos déjà en base.

## 8. Les vidéos
Les vidéos importées (souvent du 4K) étaient trop lourdes. Mise en place d'une **compression 480p** (H.264, `+faststart`) avant stockage — jusqu'à ~45 Mo → ~0,6 Mo — avec repli sur l'original si la compression échoue. Une **vignette** est extraite et servie sur la carte vitrine.

## 9. Déploiement
- **Hostinger** pour la vitrine (statique) et le backoffice (Node/pm2).
- **Build Git Hostinger cassé** (`EACCES esbuild`) → on construit en local et on **copie** les fichiers.
- La vitrine lit Supabase **en direct** (clé anon dans le bundle).
- Piège récurrent : **cache CDN** servant un ancien `app.js`/`style.css`.

## 10. Le trou de sécurité
La base n'avait pas de RLS. La clé anon étant publique (dans le JS), **n'importe qui pouvait écrire** dans le catalogue. Rédaction de `SECURITY-RLS.sql` : activation de RLS, suppression des politiques d'écriture publiques, trois lectures autorisées, Storage verrouillé, `security_invoker` sur la vue.

## 11. La refonte de l'upload des médias
Deux itérations :
1. L'upload et la vignette vidéo sont ajoutés, mais le panneau s'ouvre **en bas de page** (`<details>` replié) → **l'utilisateur ne le trouve pas**.
2. Refonte : le panneau devient une **ligne insérée juste sous le produit**, avec bouton visible, glisser-déposer, un seul panneau à la fois, bouton Fermer, rapport d'échec fichier par fichier.

> Leçon retenue et mémorisée : *une action doit s'ouvrir sous la ligne cliquée, jamais repliée ailleurs.*

## 12. Ce dépôt
Créé le **30 septembre 2026** pour tout rassembler au propre, anonymisé, et permettre à un autre agent IA (ou un humain) de reprendre le projet. Voir [12-Guide de reprise](12-GUIDE-REPRISE-IA.md).
