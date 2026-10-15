# 12 — Guide de reprise pour un agent IA

Tu reprends ce projet. Lis ce document en entier avant d'agir.

## En 30 secondes

**Tawes** importe automatiquement des produits depuis des **canaux Telegram de grossistes** et les vend via une **vitrine** (`pubstack.space`) et un **backoffice** (`gestion-tawes.pubstack.space`). Un **pont** (`bridge/`) tient Telegram + WhatsApp, extrait les produits **sans LLM**, et les place dans une **file de validation humaine**. L'humain valide, le produit est publié.

## Ordre de lecture conseillé

1. `README.md` — vue d'ensemble.
2. `docs/01-ARCHITECTURE.md` — qui parle à qui.
3. `docs/02-OBJECTIF-FINAL.md` — où on veut aller.
4. `docs/03-ETAT-DES-LIEUX.md` — ce qui marche, ce qui est partiel.
5. `docs/04-CE-QUI-RESTE-A-FAIRE.md` — la feuille de route priorisée.
6. `docs/05` … `docs/08` — le détail par composant.
7. `docs/10-SECURITE-SECRETS.md` — **avant de toucher à la base**.

## Règles non négociables

| Règle | Pourquoi |
| --- | --- |
| **Écrire en français** | L'utilisateur communique en français |
| **Jamais de LLM dans le code** | Choix assumé : extraction et réponses WhatsApp sont à règles pures |
| **Jamais d'écriture de test en production** | La base est une prod vivante. Une écriture de test peut corrompre des données réelles |
| **Vérification humaine avant push** | L'utilisateur veut voir le rendu (desktop + mobile) lui-même avant tout déploiement |
| **Secrets hors dépôt** | Ce dépôt est public. Voir `docs/10` |
| **Ne pas casser l'existant** | Les URLs médias et les fiches publiées doivent rester valides |

## Comment inspecter le système vivant (lecture seule)

Base Supabase (remplacer par les vraies valeurs, lues dans un `.env` local) :

```bash
# Compter les produits publiés
curl -s -H "apikey: $ANON" -H "Authorization: Bearer $ANON" \
  "$SUPABASE_URL/rest/v1/products?select=id&is_published_to_website=eq.true" | jq length

# Voir la file d'attente
curl -s -H "apikey: $ANON" -H "Authorization: Bearer $ANON" \
  "$SUPABASE_URL/rest/v1/telegram_messages?select=id,channel_name,telegram_message_id,created_at&processed=eq.false&order=created_at.desc" | jq

# Médias d'un produit
curl -s -H "apikey: $ANON" -H "Authorization: Bearer $ANON" \
  "$SUPABASE_URL/rest/v1/product_media?select=media_type,media_url,sort_order&product_id=eq.<ID>&order=sort_order" | jq
```

Pont :
```bash
cd bridge
./bridge.sh status        # doit renvoyer connected + actif: true
./bridge.sh msgs 20
node tools/dry-run.js 1   # analyse sans écrire
```

> ⚠️ `PATCH`/`DELETE` PostgREST **sans filtre** échoue en `400 21000 "UPDATE requires a WHERE clause"`. C'est une protection : toujours cibler par `id=eq.…`.

## La question à se poser avant chaque action

> **Est-ce une écriture ?** Si oui : est-elle explicitement demandée et métier ? Sinon, utiliser une lecture, un `dry-run`, ou une transaction. Ne jamais « tester » une écriture sur des données réelles.

## Par où commencer (premiers pas concrets)

1. **Vérifier le RLS** : exécuter la requête de contrôle de `SECURITY-RLS.sql`. C'est le risque le plus élevé.
2. **Retester la publication d'un produit vidéo** (voir `docs/04`, tâche 1.1) : la vignette doit arriver en `product_media`.
3. **Ouvrir un second canal Telegram** en `dry-run`, vérifier l'extraction, puis l'activer.
4. **Rotation des secrets** avant toute mise en production (voir `docs/10`).

## Pièges à ne pas re-découvrir

- **Baileys/LID** : n'envoyer que vers le `lid` (`onWhatsApp()`), jamais vers `@s.whatsapp.net`.
- **Une seule instance du pont** : deux sessions WhatsApp se remplacent mutuellement.
- **`product_id`** n'est pas une colonne de `telegram_messages` ; il est dans `raw_payload`.
- **Cache CDN** : vérifier les assets avec `?v=<timestamp>`.
- **Build Hostinger cassé** : déployer par copie, et synchroniser `last-source/`.
- **OCR ~3 %** : ne pas s'attendre à de l'automatique parfait ; c'est la raison de la validation humaine.
- **Prix vs nom** : prix dans la légende, nom dans la photo.

## Où creuser pour aller plus loin

- `docs/04-CE-QUI-RESTE-A-FAIRE.md` — la feuille de route détaillée.
- `docs/05-PONT-WA-BRIDGE.md` — mécanique du pont, outils de diagnostic.
- `docs/07-PIPELINE-TELEGRAM.md` — le flux Telegram pas à pas.
- `docs/11-HISTORIQUE-TRAVAIL.md` — pourquoi les choses sont ainsi.

Bonne reprise.
