# AGENTS.md — consignes pour les agents IA

Ce fichier s'adresse à tout agent (Kiro, Claude Code, Cursor, Copilot…) qui reprend ce dépôt. Lis-le en entier avant d'agir.

## Le projet en 10 secondes

Vitrine e-commerce (**pubstack.space**) + backoffice + un « pont » Node qui importe des produits depuis des canaux Telegram de grossistes et répond aux clients WhatsApp. Backend = **Supabase** (PostgreSQL + Storage). Pas de serveur applicatif complexe.

## Règles absolues

1. **NE JAMAIS committer de secret dans ce dépôt.** Pas de `.env`, pas de clé `sb_secret_…`, pas de mot de passe, pas de token, pas de numéro de téléphone réel, pas d'adresse IP de serveur, pas d'identifiant de canal. Voir `docs/10-SECURITE-SECRETS.md`. Les valeurs réelles vivent dans le dépôt **privé** `solver492/tawes-platform-secrets`.
2. **NE JAMAIS tester une écriture sur la base de production.** Un `INSERT`/`UPDATE`/`DELETE` de test sur les vraies données est interdit.
3. **L'utilisateur communique en français.** Répondre en français.
4. **Vérification manuelle avant déploiement.** Préparer, montrer, attendre le feu vert.
5. **Pas de push Git sans demande explicite.**
6. **Pas de LLM dans le code.** L'extraction produit et les réponses WhatsApp sont 100 % à règles. Ne pas introduire d'appel IA sans que ce soit demandé (sauf les routes FLUX déjà implémentées dans api-server).

## Structure du dépôt

```
tawes-platform/
├── artifacts/
│   ├── admin/          ← Backoffice Express + JS vanilla (port 4000)
│   ├── tawes-store/    ← Vitrine React + Vite (port 5173)
│   ├── api-server/     ← API TypeScript : R2 presign, FLUX jobs (port 3001)
│   └── mockup-sandbox/ ← Scaffold Replit, NON utilisé en production
├── bridge/             ← Pont WhatsApp (Baileys) + Telegram (teleproto)
├── lib/                ← Librairies partagées monorepo (db, api-zod, api-client-react)
├── scripts/            ← Utilitaires + deploy-linux.sh
├── docs/               ← Toute la documentation
├── .github/workflows/  ← CI GitHub Actions (typecheck + build)
└── SECURITY-RLS.sql    ← Politiques RLS Supabase (à appliquer avant mise en prod)
```

## Conventions de code

- **Vitrine** : React 19 + TypeScript + Vite + Tailwind. Lit Supabase directement.
- **Backoffice** : Express 5 + JavaScript vanilla. Un seul `public/app.js`, `public/style.css`, `src/server.js`. Auth par cookie signé (`ADMIN_PASSWORD`).
- **api-server** : TypeScript + Express. Modules : `src/lib/r2.ts` (presign R2), `src/lib/image-generator.ts` (interface FLUX), `src/jobs/queue.ts` (file d'attente).
- **Pont** : Node ESM (Baileys + teleproto), `dotenv`. Supervisé par pm2 ou systemd.
- Commentaires **en français**, expliquant le *pourquoi*.
- Branche de travail : `feat/backoffice-flux-r2`

## Commandes clés

```bash
# Développement local
pnpm install                          # monorepo (vitrine + api-server + libs)
PORT=5173 pnpm dev                    # vitrine sur :5173
cd artifacts/admin && npm install && npm run dev   # backoffice sur :4000
cd bridge && npm install && node index.js           # pont (auth Telegram requise)

# Vérifications
pnpm typecheck                        # typecheck global
node --check artifacts/admin/src/server.js
node --check artifacts/admin/public/app.js

# Déploiement Linux (VPS Hostinger)
./scripts/deploy-linux.sh
```

## Variables d'environnement

| Fichier | Variables clés |
|---|---|
| `artifacts/admin/.env` | `SUPABASE_URL`, `SUPABASE_ANON_KEY` (clé secrète !), `ADMIN_PASSWORD`, `COLAB_FLUX_URL` (vide = bouton ✦ Personnaliser désactivé, cf. `docs/GUIDE-FLUX-PERSONNALISER.md`) |
| `artifacts/tawes-store/.env` | `VITE_SUPABASE_URL`, `VITE_SUPABASE_ANON_KEY` (clé publishable) |
| `artifacts/api-server/.env` | `R2_*`, `IMAGE_GENERATOR`, `COLAB_FLUX_URL` |
| `bridge/.env` | `TELEGRAM_API_ID`, `TELEGRAM_API_HASH`, `SUPABASE_*` |

## Routes api-server

| Route | Méthode | Description |
|---|---|---|
| `/api/healthz` | GET | Healthcheck |
| `/api/media/presign` | POST | URL pré-signée R2 pour upload direct |
| `/api/media/types` | GET | Types MIME acceptés |
| `/api/images/jobs` | POST | Créer un job FLUX |
| `/api/images/jobs` | GET | Lister les jobs |
| `/api/images/jobs/:id` | GET | Statut d'un job |

## Pièges connus (à ne pas redécouvrir)

- **`SUPABASE_ANON_KEY` dans admin contient la clé SECRÈTE**, pas la clé anon. Nom historique trompeur.
- **Baileys ne livre que la forme LID.** Un envoi vers `xxx@s.whatsapp.net` peut être abandonné.
- **Prix vs nom** : dans les canaux Telegram, le prix est dans la légende et le nom dans la photo.
- **Le build Hostinger casse** (`EACCES esbuild`). Déployer par transfert de fichiers, pas par build serveur.
- **Un panneau d'action du backoffice s'ouvre sous la ligne cliquée**, jamais replié en bas de page.
- **Colab s'arrête** après inactivité : `isAvailable()` pinge avant chaque génération, statut `colab_unavailable` si hors ligne.
- **Le binding `@tailwindcss/oxide` arm64** doit être installé manuellement sur les machines non-Replit : `pnpm add -D @tailwindcss/oxide-linux-arm64-gnu@4.3.3`.
- **Node quitte si stdin est fermé** (nohup sans redirection) : lancer avec `node src/server.js < /dev/null &`.

## Où regarder en premier

| Question | Fichier |
|---|---|
| Vue d'ensemble | `docs/01-ARCHITECTURE.md` |
| Ce qui est fait | `docs/03-ETAT-DES-LIEUX.md` |
| Ce qui reste | `docs/04-CE-QUI-RESTE-A-FAIRE.md` |
| Secrets / sécurité | `docs/10-SECURITE-SECRETS.md` |
| Migration Vimeo → R2 | `docs/13-MIGRATION-VIMEO-R2.md` |
| Déploiement | `docs/09-DEPLOIEMENT.md` |
