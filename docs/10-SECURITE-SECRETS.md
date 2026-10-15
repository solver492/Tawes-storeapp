# 10 — Sécurité et secrets

> Ce dépôt est **public**. Aucune clé, aucun mot de passe, aucun identifiant réel ne doit s'y trouver. Ce document décrit *quoi* protéger et *comment*, jamais les valeurs.
>
> Les **valeurs réelles** sont dans le dépôt privé `solver492/tawes-platform-secrets`, qui contient un inventaire détaillé de chaque identifiant : ce qu'il permet, où il est utilisé, et comment le faire tourner. On y trouve aussi `bridge/channels.json`, la liste réelle des canaux.

## Règle d'or

**Les secrets vivent dans des `.env` sur les serveurs, jamais dans le dépôt.** Les fichiers `.env.example` ne contiennent que des **placeholders**.

Si un secret a fuité (commit, dépôt public, capture, historique Git), il est considéré **compromis** : il faut le **révoquer puis le régénérer**, pas seulement le retirer.

## Secrets à faire tourner (rotation)

Des identifiants ont circulé en clair pendant le développement. À révoquer/régénérer **avant la mise en production** :

| Secret | Où le régénérer | Impact d'une fuite |
| --- | --- | --- |
| Clé **service_role** Supabase (`sb_secret_…`) | Dashboard Supabase → Project Settings → API → *rotate/reset* | **Total** : lit et écrit toute la base, contourne RLS |
| Clé **anon/publishable** Supabase | Idem | Faible *si RLS actif*, mais migrer quand même |
| Mot de passe **admin** du backoffice | `.env` du serveur (`ADMIN_PASSWORD`) | Publication/suppression de produits |
| Identifiants **FTP/SSH** Hostinger | hPanel Hostinger | Déploiement, accès serveur |
| **Token GitHub** | GitHub → Settings → Developer settings → Tokens | Accès aux dépôts |
| **API hash** Telegram (`TELEGRAM_API_HASH`) | my.telegram.org | Accès au compte Telegram du pont |
| Mot de passe **session WhatsApp** | Re-appairage | Accès au WhatsApp du pont |

### Ordre recommandé
1. **Supabase** d'abord (service_role puis anon). Mettre à jour les `.env` de la vitrine, du backoffice et du pont.
2. **GitHub** (créer le dépôt public avec un jeton neuf, révoquer l'ancien).
3. **Hostinger** (FTP/SSH).
4. **Telegram** et **WhatsApp** (re-connexion du pont).
5. Vérifier que l'ancien `ssh-auto.sh` (exclu du dépôt) n'existe plus nulle part en clair, et le supprimer de la machine.

> La clé **anon** reste *par nature* publique (elle est dans le JS de la vitrine). Sa sécurité ne vient pas du secret mais du **RLS** : sans RLS, elle permet toutes les écritures. C'est pourquoi l'exécution de `SECURITY-RLS.sql` est **prioritaire**.

## Le trou de sécurité d'origine

À l'origine, la base n'avait pas de RLS et des politiques autorisaient `anon` à **insérer/modifier/supprimer** dans `products`, `product_media`, `categories`, etc. La clé publique étant visible dans le JS de la vitrine, n'importe qui pouvait modifier le catalogue.

`SECURITY-RLS.sql` corrige cela :

1. `ENABLE ROW LEVEL SECURITY` sur toutes les tables.
2. `DROP` de **toutes** les politiques d'écriture pour `anon`/`public`.
3. Trois politiques de **lecture** seulement : produits publiés, médias liés à un produit publié, catégories actives.
4. RLS + suppression des politiques d'écriture sur `storage.objects`.
5. `security_invoker = on` sur `products_with_category` (le rôle de l'appelant s'applique, la vue ne contourne pas RLS).

### Vérifier après exécution
La requête de contrôle en fin de script liste, par table, le nombre de politiques d'écriture publiques. **La colonne « écriture_publique » doit valoir 0 partout.**

## Bonnes pratiques pour la suite

- **Ne jamais** mettre un mot de passe dans un script (`ssh-auto.sh`, `remote-fix.*`, `fix-commands.sh`). Utiliser `ssh` interactif ou un agent SSH.
- Faire tourner les clés **encore une fois** au moment du passage en production.
- Le backoffice doit utiliser la clé **secrète** (côté serveur uniquement) ; la vitrine n'utilise que **l'anon**.
- Stockage : le bucket `product-media` est **public en lecture** (normal, ce sont des images de catalogue) mais **fermé en écriture**.
- Activer l'authentification à deux facteurs sur Supabase, GitHub et Hostinger.

## Ce qui a été retiré du dépôt avant publication

- `.env` et variantes, `sessions/`, `data/`, `logs/`, `node_modules/`, `dist/`.
- `ssh-auto.sh` (contenait un mot de passe SSH en clair).
- `SSH_COMMANDS.txt`, `ssh-connect.bat`, `remote-fix.*`, `fix-commands.sh`, `.hostin gerrc`, `DEPLOY.md`, `FIX_RAPIDE.md`, `HOSTINGER_FIX.md`.
- `attached_assets/` (captures internes).
- Valeurs remplacées par des placeholders : référence de projet Supabase, IDs de canaux/numéros, clé secrète.

> **Rappel** : si vous voyez une valeur qui ressemble à un vrai secret dans ce dépôt, considérez-la comme fuitée et faites-la tourner.
