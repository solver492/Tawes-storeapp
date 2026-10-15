# 06 — La boutique et le backoffice

## A. La vitrine (`artifacts/tawes-store/`)

### Nature
Site **statique** React 19 + TypeScript + Vite + Tailwind. Il n'y a pas de serveur applicatif : le site lit **directement** Supabase avec la clé publique (`anon`/`publishable`).

### Ce qu'elle affiche
- **Hero** de présentation.
- **Sélecteur de mode** : « Achat au détail » ↔ « Achat en gros » (revendeurs).
- **Catalogue** : grille de produits, filtres par catégorie, compteur de résultats.
- **Carte produit** : image **ou** vidéo (avec vignette `poster`, repli sur la première image de la vidéo via `#t=0.5`), badge « vidéo » / « N images », remise.
- **Modale produit** : carrousel d'images, lecteur vidéo, prix détail/gros.
- Bouton **WhatsApp** flottant.

### Fichiers clés
| Fichier | Rôle |
| --- | --- |
| `src/App.tsx` | Toute la page (hero, catalogue, cartes, modale) |
| `src/index.css` | Styles (dont `.card-video`, `.product-video`) |
| `src/lib/supabase.js` | Lecture des tables (`getProducts`, `getCategories`, `getProductMedia`) |
| `vite.config.ts` | Base path, alias, sortie `dist/public` |

### Lecture des données (`src/lib/supabase.js`)
```js
getProducts()      // products_with_category, is_published_to_website=true, active=true
getCategories()    // categories, is_active=true
getProductMedia(ids) // product_media, order by sort_order
```
La normalisation (`normalizeProduct` dans `App.tsx`) associe à chaque produit ses médias : images (carrousel), vidéo, et **vignette** (`media_type = 'poster'`).

### Build & déploiement
```bash
pnpm install
pnpm build          # → artifacts/tawes-store/dist/public
```
Puis copier `dist/public` sur l'hébergement (voir [09-Déploiement](09-DEPLOIEMENT.md)).

---

## B. Le backoffice (`artifacts/admin/`)

### Nature
Serveur **Express 5** + une page unique en **JavaScript vanilla** (pas de framework, pas de build). Servi sur un sous-domaine protégé.

### Authentification
- `POST /api/login` avec `ADMIN_PASSWORD`, pose un **cookie signé**.
- `GET /api/session` indique si la session est valide.
- Toutes les routes `/api/*` (sauf login/session) exigent le cookie.

### API (routes)

| Méthode & chemin | Rôle |
| --- | --- |
| `POST /api/login`, `POST /api/logout`, `GET /api/session` | Authentification |
| `GET /api/stats` | Compteurs (en attente, produits, catégories) |
| `GET /api/pending` | File des messages `processed = false` |
| `GET /api/pending/:id` | Détail d'un message |
| `PATCH /api/pending/:id` | Corriger l'extraction (nom, prix, catégorie…) |
| `DELETE /api/pending/:id` | Rejeter un message |
| `POST /api/pending/:id/publish` | **Valider** : crée/met à jour le produit + médias |
| `GET /api/products` | Catalogue (vue `products_with_category`) |
| `PATCH /api/products/:id` | Modifier (prix, catégorie, activation) |
| `POST /api/products/:id/validate` | Valider et publier |
| `POST /api/products/:id/publish` | Activer/désactiver la publication |
| `DELETE /api/products/:id` | Supprimer produit + médias |
| `POST /api/products/bulk-category` | Déplacer plusieurs produits de catégorie |
| `GET/POST /api/products/:id/media` | Lister / ajouter un média par URL |
| `DELETE /api/media/:id` | Supprimer un média |
| `POST /api/products/:id/media/upload` | **Upload** d'un fichier (corps brut, `x-file-name`) |
| `POST /api/products/:id/video` | Diffuser une vidéo sur Vimeo |
| `GET/PATCH /api/categories`, `POST`, `DELETE /api/categories/:id` | Catégories |

### Fonctions internes utiles
- `attachMedia(productId, staging, overrides)` (~ligne 400) : recopie les médias du staging vers le produit. **C'est ici que la vignette (`kind: 'poster'`) est propagée en `media_type='poster'`** — point à retester.
- `syncProductImages(productId)` : résume les `media_type='image'` dans `products.images` (ignore `poster`).
- `resolveSupplier(row)` : associe/​crée un fournisseur.

### Onglets de l'interface
| Onglet | Contenu |
| --- | --- |
| **En attente** | File d'attente, filtre par canal, édition, validation, rejet |
| **Produits** | Catalogue publié, activation, suppression, **gestion des médias** |
| **Catégories** | CRUD des catégories |

### La gestion des médias (onglet Produits)
C'est un point qui a demandé plusieurs itérations. Le comportement final voulu :
1. Cliquer le nombre de la colonne **Médias** ouvre un panneau **juste sous la ligne du produit** (pas en bas de page — sinon l'utilisateur ne le voit pas).
2. Le panneau liste les médias : **images** (vignette `<img>`), **vidéos** (vignette `<video>`), **poster** (badge « vignette video », sans bouton supprimer car lié à la vidéo), Vimeo (badge « diffusion »).
3. Un bouton **« + Ajouter des photos ou vidéos »** ouvre le sélecteur de fichiers ; on peut aussi **glisser-déposer**.
4. Envoi **séquentiel** : chaque fichier est indépendant, un échec n'annule pas les autres ; un rapport liste les refus.
5. Un seul panneau ouvert à la fois ; bouton **Fermer**.

> Voir `docs/11-HISTORIQUE-TRAVAIL.md` : la première version ouvrait un `<details>` replié en bas de page, ce que l'utilisateur n'a pas trouvé.

### Démarrage
```bash
cd artifacts/admin
cp .env.example .env      # SUPABASE_URL, SUPABASE_ANON_KEY (ou clé secrète), ADMIN_PASSWORD
npm install
npm start                 # http://localhost:4000
```

> Note : le fichier `artifacts/admin/.env` d'origine portait une clé **secrète** Supabase (`sb_secret_…`), car le backoffice écrit en base et doit contourner le RLS. Le `.env.example` livré laisse le choix.
