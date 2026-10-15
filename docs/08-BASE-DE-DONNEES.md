# 08 — La base de données

Supabase (PostgreSQL + Storage). Tout le monde y accède en REST/PostgREST ; aucun n'utilise `supabase-js` ni Drizzle (le schéma Drizzle du monorepo est un échafaudage vide).

## Pourquoi PostgREST brut
Le backoffice (`artifacts/admin/src/supabase.js`) et le pont (`bridge/src/agent/supabase.js`) parlent directement à PostgREST et au Storage avec `fetch`. Raison : pas de `DATABASE_URL` disponible et le schéma Drizzle du monorepo ne reflète pas le schéma réel.

## Les tables utilisées

### `products`
Le catalogue. Colonnes manipulées par le code :
`id`, `name`, `description`, `category_id`, `category_name`, `category`,
`wholesale_price`, `suggested_sale_price`,
`is_lot`, `lot_quantity`, `lot_unit_price`, `lot_total_price`, `lot_label`, `stock`,
`source_message_id`, `source_channel_id`,
`status`, `active`, `is_published_to_website`,
`image_url`, `images`, `supplier_id`, `created_at`, `updated_at`.

- `is_published_to_website` + `active` = conditions d'affichage vitrine.
- `image_url` = image principale (résumée depuis `product_media` par `syncProductImages`).
- `source_message_id` / `source_channel_id` = traçabilité Telegram (sert à l'idempotence de publication).

### `product_media`
Les médias d'un produit. Colonnes : `id`, `product_id`, `media_url`, `media_type`, `alt_text`, `sort_order`.

`media_type` vaut :
| Valeur | Sens |
| --- | --- |
| `image` | Photo (carrousel) |
| `video` | Fichier vidéo hébergé (compressed 480p) |
| `poster` | **Vignette** d'une vidéo — pas une image du carrousel |
| `vimeo` | Lien de diffusion Vimeo |

> `syncProductImages()` ne résume que les `image` : un `poster` ne pollue pas `products.images`.

### `categories`
`id`, `name`, `display_order`, `is_active`. 13 catégories + `non-categorise`. Les slugs sont utilisés par `CATEGORY_RULES` du pont.

### `telegram_messages`
La file d'attente. Colonnes : `id`, `channel_id`, `channel_name`, `telegram_message_id`, `raw_payload` (jsonb), `processed`, `created_at`.

- `processed = false` → dans la file du backoffice.
- `raw_payload` contient : `extraction` (nom, prix, lot, catégorie…), `media[]` (dont `kind`: `photo`/`video`/`poster`), et après validation `product_id` + `published_at`.
- **`product_id` n'est pas une colonne** : il vit dans `raw_payload` (un PATCH qui l'envoyait comme colonne échouait en 400).
- Idempotence : clé logique `(channel_id, telegram_message_id)`.

### `suppliers`
`id`, `name`, … Un fournisseur par canal Telegram (`resolveSupplier`).

### Vue `products_with_category`
Vue de lecture jointe produit + catégorie, utilisée par le backoffice et la vitrine. Passée en `security_invoker = on` par le script RLS (PostgreSQL ≥ 15) pour ne pas contourner les politiques.

### Tables historiques
Le commentaire du script RLS mentionne : `users`, `orders`, `withdrawals`, `affiliates`, `profiles`. Elles appartiennent à l'origine du projet et **ne sont pas utilisées** par le flux actuel. Elles restent verrouillées par RLS.

## Storage
- Bucket : **`product-media`** (public en lecture).
- Chemins : `products/<id>/<NN>.<ext>` (produit publié), `staging/<message_id>/…` (en attente), `products/<id>/poster.jpg` (vignette).

## Sécurité (RLS)
Voir `SECURITY-RLS.sql` à la racine et [10-Sécurité](10-SECURITE-SECRETS.md).

En résumé :
1. Active RLS sur **toutes** les tables.
2. Supprime **toutes** les politiques d'écriture accessibles à `anon`/`public` (c'était le trou : la clé publique pouvait modifier le catalogue).
3. N'autorise que trois lectures publiques : produits publiés, médias, catégories actives.
4. Retire les politiques d'écriture du Storage.
5. `security_invoker` sur la vue.

> Le rôle `service_role` (clé `sb_secret_…`) **contourne** RLS : le backoffice et le pont continuent de fonctionner.

## Clés Supabase : laquelle où ?
| Usage | Type de clé |
| --- | --- |
| Vitrine (navigateur public) | **anon / publishable** (`sb_publishable_…`) — publique par nature |
| Backoffice (serveur) | **secrète** (`sb_secret_…`, ex-service_role) — écrit et contourne RLS |
| Pont (serveur) | **secrète** (écrit) ou anon selon les besoins |
