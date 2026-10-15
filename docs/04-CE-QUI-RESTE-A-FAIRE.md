# 04 — Ce qui reste à faire

Feuille de route priorisée. Chaque tâche indique **le pourquoi** et **où regarder** dans le code.

---

## Priorité 1 — Finir ce qui est commencé

### 1.1 Retester la publication d'un produit vidéo de bout en bout
- **Quoi** : publier un produit vidéo fraîchement depuis la file et vérifier que la vignette arrive bien dans `product_media` (`media_type = 'poster'`) et s'affiche sur la carte vitrine.
- **Pourquoi** : la propagation a été corrigée mais pas re-testée sur un nouveau produit.
- **Où** : `artifacts/admin/src/server.js`, fonction `attachMedia()` (vers la ligne 400).

### 1.2 Rattraper les vignettes des envois en attente
- **Quoi** : lancer l'outil de rattrapage pour les vidéos `processed = false` qui n'ont pas encore de `poster`.
- **Où** : `bridge/tools/backfill-pending-posters.js` (déjà utilisé). Vérifier qu'il ne reste aucun message vidéo sans vignette.

### 1.3 Corriger les métadonnées de taille vidéo
- **Quoi** : les `raw_payload.media[].size` de certaines vidéos reflètent encore le fichier 4K, pas la version 480p réellement stockée.
- **Pourquoi** : fausse les éventuels calculs de poids/quotas.
- **Où** : `bridge/tools/restage.js`, `bridge/src/agent/agent.js` (`stageMedia`).

### 1.4 Aligner le nom de la variable du numéro du propriétaire
- **Quoi** : le code lit `process.env.OWNER_PHONE`, mais la variable posée sur le serveur s'appelle `OWNER_WA`. `OWNER_PHONE` est donc indéfini et le pont retombe sur une valeur écrite en dur dans `config.js`.
- **Pourquoi** : les escalades WhatsApp (demande d'humain, question de stock) partent vers le bon numéro **par hasard**. Changer la configuration n'a aucun effet, et un numéro personnel resterait figé dans le code.
- **Où** : `.env` du pont (variable `OWNER_PHONE=`), puis `bridge/src/agent/config.js` : remplacer le repli par une valeur vide et un avertissement au démarrage.
- **Note** : le détail est dans le dépôt privé des identifiants, section « Bugs et détects ».

---

## Priorité 2 — Ouvrir la surveillance aux autres canaux

### 2.1 Valider l'extraction sur chaque canal, un par un
- **Quoi** : pour chaque canal, faire un `dry-run` (sans écrire) et regarder un échantillon.
- **Outil** : `bridge/tools/dry-run.js`, `bridge/tools/probe-channel.js`, `bridge/tools/survey-channels.js`.
- **Pourquoi** : chaque grossiste écrit différemment ; il faut vérifier prix/nom/catégorie avant d'ouvrir les vannes.

### 2.2 Passer progressivement les 11 canaux en production
- **Quoi** : modifier `WATCH_CHANNELS` dans `bridge/.env` (vide = tous). Commencer par quelques-uns.
- **Où** : `bridge/src/agent/config.js` (liste `CHANNELS`), `bridge/index.js` (lecture de `WATCH_CHANNELS`).
- **Attention** : le premier chargement peut être long (OCR). Surveiller les logs.

### 2.3 Import automatique depuis le backoffice
- **Quoi** : rendre le bouton « Actualiser » capable de demander au pont un import sur les 7 derniers jours.
- **Comment** : exposer une route sur le pont (ou un job) et l'appeler depuis l'admin, protégé par un secret partagé.
- **Pourquoi** : aujourd'hui l'utilisateur ne peut pas forcer un import.
- **Où** : `bridge/src/server.js` (ajouter une route), `artifacts/admin/src/server.js` + `public/app.js`.

---

## Priorité 3 — Stockage des médias (R2 / PeerTube)

### 3.1 Choisir et provisionner
- **Recommandé** : **Cloudflare R2** (compatible S3, pas d'egress fees, moins cher que Supabase pour de la vidéo).
- **Prérequis manquants** : compte Cloudflare, bucket, clés API, éventuellement une zone DNS (le domaine est actuellement chez Hostinger).
- **Alternative** : PeerTube (nécessite un VPS ; plus lourd).

### 3.2 Migrer
- **Quoi** : téléverser les vidéos vers R2, stocker l'URL R2 dans `product_media.media_url`, garder Supabase comme source de vérité des fiches.
- **Ne pas** : casser les URLs existantes (prévoir une période de coexistence ou une redirection).
- **Où** : `bridge/src/agent/agent.js` (`stageMedia`), `artifacts/admin/src/server.js` (`/products/:id/media/upload`).

---

## Priorité 4 — Confiance & maintenabilité

### 4.1 Vérifier que le RLS est bien actif en production
- **Quoi** : exécuter `SECURITY-RLS.sql` et lire la requête de vérification finale (dernière colonne = 0 partout).
- **Pourquoi** : sans RLS, la clé publique (présente dans le JS de la vitrine) peut écrire dans la base.

### 4.2 Rotation des secrets
- **Quoi** : faire tourner les clés exposées pendant le développement (Supabase, FTP/SSH, GitHub).
- **Détails** : [10-Sécurité-et-secrets](10-SECURITE-SECRETS.md).

### 4.3 Versionner proprement à partir de maintenant
- Le dépôt Git existe désormais. Committer par petits pas, avec des messages clairs.

### 4.4 Remettre les tests dans le dépôt
- Les tests backoffice (upload, visualiseur, filtre) vivaient hors dépôt. Les recréer dans un dossier `tests/` pour éviter les régressions.

---

## Priorité 5 — Améliorations produit

- **Recherche** sur la vitrine (aujourd'hui : catégories + mode détail/gros).
- **Fiche produit** détaillée (description, stock, variantes).
- **Suivi de commande** WhatsApp plus riche (statuts).
- **Statistiques** dans le backoffice (produits publiés, taux de validation, canaux les plus productifs).
- **Nettoyage** du scaffolding Replit inutile (`artifacts/api-server`, `artifacts/mockup-sandbox`, `lib/`) si on veut alléger.

---

## Dettes techniques connues

| Dette | Impact | Piste |
| --- | --- | --- |
| Build Git Hostinger cassé (`EACCES esbuild`) | Déploiement manuel | Revoir les permissions du compte, ou conserver le déploiement par copie |
| Taille des vidéos dans `raw_payload` erronée | Cosmétique / quotas | Recalculer au moment du staging |
| Client Telegram lié à la session du pont | Un seul process possible | Ne pas lancer deux instances du pont |
| Extraction sans LLM | Précision limitée | Assumé : validation humaine |
| CDN qui sert un vieux CSS/JS | Confusion au déploiement | `?v=…` pour forcer, ou purge de cache |
