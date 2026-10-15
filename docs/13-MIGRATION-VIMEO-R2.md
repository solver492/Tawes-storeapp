# Migration des vidéos Vimeo vers Cloudflare R2

## Contexte

Les anciennes vidéos sont stockées sur Vimeo et référencées dans `product_media` avec
`media_type = 'vimeo'` et une `media_url` de la forme `https://player.vimeo.com/video/ID`.

La migration conserve les liens Vimeo existants (pas de rupture) et ajoute les nouvelles
vidéos dans R2. La bascule complète est optionnelle et se fait vidéo par vidéo.

## Stratégie : cohabitation, pas coupure

1. Les enregistrements Vimeo existants **restent intacts** dans `product_media`.
2. La vitrine lit `media_type` et affiche l'embed correspondant :
   - `vimeo`   → `<iframe src="https://player.vimeo.com/video/ID">`
   - `youtube` → `<iframe src="https://www.youtube.com/embed/ID">`
   - `video`   → `<video src="URL_R2">`
3. Les nouvelles vidéos passent directement par R2 via la route `/api/media/presign`.

## Procédure de migration (une vidéo à la fois)

### Étape 1 — Identifier les vidéos Vimeo

```sql
SELECT id, product_id, media_url, alt_text
FROM product_media
WHERE media_type = 'vimeo'
ORDER BY product_id;
```

### Étape 2 — Télécharger la vidéo depuis Vimeo

L'API Vimeo (token dans `.env` → `VIMEO_ACCESS_TOKEN`) permet de récupérer
le lien de téléchargement :

```bash
VIDEO_ID=123456789
curl -H "Authorization: bearer $VIMEO_ACCESS_TOKEN" \
  "https://api.vimeo.com/videos/$VIDEO_ID?fields=download" \
  | jq '.download[] | select(.quality=="hd") | .link'
```

### Étape 3 — Uploader vers R2

Obtenir une URL pré-signée depuis l'api-server (authentifié) :

```bash
curl -X POST http://localhost:3001/api/media/presign \
  -H "Content-Type: application/json" \
  -d '{"mimeType":"video/mp4","fileSize":52428800,"prefix":"products/PRODUCT_ID/"}'
```

Réponse : `{ "uploadUrl": "https://…r2…", "publicUrl": "https://media.tawes.ma/…", "key": "…" }`

Uploader le fichier :

```bash
curl -X PUT "$UPLOAD_URL" \
  -H "Content-Type: video/mp4" \
  --data-binary @video.mp4
```

### Étape 4 — Mettre à jour Supabase

```sql
UPDATE product_media
SET media_url   = 'https://media.tawes.ma/products/PRODUCT_ID/UUID.mp4',
    media_type  = 'video'
WHERE id = MEDIA_ROW_ID;
```

### Étape 5 — Vérifier sur la vitrine puis supprimer Vimeo

Ouvrir la fiche produit et vérifier que la vidéo se lit depuis R2.
Si OK, supprimer la vidéo de Vimeo (dashboard ou `DELETE /videos/ID`).

## Risques

- **Vimeo supprime les vidéos avant la migration** : les liens existants
  fonctionnent tant que le compte Vimeo est actif — migrer en priorité.
- **Taille** : une vidéo HD peut dépasser 500 Mo ; ajuster `ALLOWED_TYPES`
  dans `api-server/src/lib/r2.ts` si besoin.
- **Pas de rollback** : vérifier la lecture R2 **avant** de supprimer sur Vimeo.
