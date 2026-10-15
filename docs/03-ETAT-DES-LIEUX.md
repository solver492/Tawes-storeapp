# 03 — État des lieux

> État constaté au **30 septembre 2026**. C'est le point de départ réel pour reprendre le travail.

## Ce qui fonctionne aujourd'hui (vérifié)

### La base
- Schéma en place : `products`, `product_media`, `categories`, `telegram_messages`, `suppliers`, et d'autres tables historiques.
- La vitrine lit `products_with_category`, `product_media`, `categories` en direct.
- `SECURITY-RLS.sql` fourni à la racine durcit la base : RLS activé, écriture publique retirée, seules trois lectures `anon` autorisées (produits publiés, médias, catégories actives), vue passée en `security_invoker`.
- **À confirmer** : que le script RLS a bien été exécuté sur le projet de production (voir `docs/10`).

### La vitrine (`artifacts/tawes-store/`)
- Site public en ligne sur **pubstack.space**.
- Affiche les produits publiés, catégories, prix détail / gros, bannière revendeur.
- **Carrousel** d'images par produit.
- **Lecteur vidéo** : `<video>` natif pour un fichier direct (avec repli Vimeo), vignette (`poster`) sur la carte produit.
- Bouton **WhatsApp** fonctionnel.
- Build Vite OK ; déploiement statique vérifié.

### Le backoffice (`artifacts/admin/`)
- En ligne sur **gestion-tawes.pubstack.space**.
- **Onglet En attente** : file des produits extraits, filtre par canal, édition, validation, rejet.
- **Onglet Produits** : catalogue publié, activation, suppression, **gestion des médias**.
- **Upload multiple** de photos **et** vidéos depuis l'appareil (bouton + glisser-déposer), sous la ligne du produit, avec progression et rapport d'échec.
- **Vignettes vidéo** affichées en `<video>` dans la liste ; la vignette extraite (`poster`) n'est pas proposée à la suppression (elle est liée à sa vidéo).
- Diffusion d'une vidéo sur **Vimeo**, réordonnancement des médias.
- **Onglet Catégories**.
- Authentification par mot de passe + cookie.
- Tests : 23 assertions visualiseur, 24 filtre, 25 upload — tous verts (fichiers de test hors dépôt, à recréer).

### Le pont (`bridge/`)
- **WhatsApp** connecté (Baileys), session persistée.
- **Telegram** connecté (teleproto), session persistée.
- **Watcher** : tour toutes les **15 min**, fenêtre glissante de **6 h**, 30 messages/canal.
  - Gère les **albums** (regroupés par `groupedId`, 4 médias par album typiquement).
  - **Déduplication** par `telegram_message_id` (mémoire + base), idempotent.
  - Reprise automatique au démarrage (`startWatcherWithRetry`) : un échec ne tue plus le watcher.
  - `status().actif` passe à vrai dès le **premier tour** (correction d'un bug qui masquait un import mort).
- **Extraction à règles** : prix, quantité de lot, nettoyage du texte, nom (texte ou OCR), catégorie.
- **OCR** Tesseract sur les photos.
- **Vidéos** compressées en **480p** (H.264 + faststart) avant upload ; repli sur l'original si la compression échoue.
- **Vignettes** extraites des vidéos (`extractPoster`) et écrites en `media_type = 'poster'`.
- **Répondeur WhatsApp** : réponse aux cas simples, escalade au propriétaire.
- Supervisé par **pm2**, scripts `bridge.sh`, `restart.sh`, `stop.sh`.

### Données à date
- Import de test sur un canal fournisseur : 19 messages vus, 8 mis en file, 11 doublons, 0 erreur (sur 100 jours).
- Produits publiés vérifiés manuellement, dont :
  - un **album** (plusieurs photos) ;
  - un **produit vidéo** avec sa vignette.
- 12 messages vidéo en attente ont reçu une vignette rétroactive.

## Ce qui est partiel

| Sujet | État |
| --- | --- |
| Canaux Telegram surveillés | **1 sur 11**. La liste réelle est chargée par `CHANNELS_FILE` ; `config.js` ne contient que des placeholders. `WATCH_CHANNELS` n'en active qu'un. |
| Identifiants réels | Déposés dans le dépôt **privé** `tawes-platform-secrets` ; absents d'ici. |
| Bouton « Actualiser » du backoffice | Recharge la liste, **ne déclenche pas** d'import Telegram. |
| Postérité des vignettes | Les **nouveaux** imports en reçoivent une ; d'anciens messages peuvent encore en manquer (outil de rattrapage fourni). |
| Publication d'un produit vidéo | La vignette est bien propagée en `product_media.media_type='poster'` (corrigé), mais à **retester** sur un produit fraîchement publié. |
| Métadonnées de taille vidéo | Les tailles stockées dans `raw_payload` pour d'anciennes vidéos reflètent la version **4K d'origine**, pas la version compressée. |
| R2 / stockage externe | **Non commencé**. |
| RLS en production | Script écrit ; exécution **à confirmer**. |

## Ce qui ne fonctionne pas / n'existe pas

- Import automatique déclenché depuis le backoffice.
- Surveillance des 10 autres canaux.
- Hébergement vidéo hors Supabase (R2/PeerTube).
- Traçabilité propre (le dépôt Git n'a été créé qu'à la toute fin ; historique reconstitué dans `docs/11`).
- Tests automatisés **dans** le dépôt (ils existaient sur la machine de dev, hors dépôt).

## Environnement technique actuel

| Élément | Valeur (à adapter) |
| --- | --- |
| Hébergement vitrine + admin | **Hostinger** (hébergement mutualisé), sous-domaines `pubstack.space` et `gestion-tawes.pubstack.space` |
| Build Hostinger | **cassé** (`EACCES esbuild`) → déploiement par copie de fichiers |
| Le pont | tourne sur une machine séparée, sous **pm2** |
| Node | version récente (ESM + `--env-file-if-exists`) |
| Vidéo | `ffmpeg` présent sur la machine du pont |

Voir [09-Déploiement](09-DEPLOIEMENT.md).
