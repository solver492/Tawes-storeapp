# 09 — Déploiement

> Aucune valeur sensible (mot de passe, IP, port SSH) ne doit figurer ici. Utiliser `docs/10` pour la gestion des secrets.

## Les deux environnements

| Composant | Où | Mode |
| --- | --- | --- |
| **Vitrine** | Hostinger, `pubstack.space` | Site statique (fichiers) |
| **Backoffice** | Hostinger, `gestion-tawes.pubstack.space` | Node + pm2 |
| **Le pont** | machine séparée | Node + pm2 |
| **Base** | Supabase | Service managé |

## Contrainte majeure : le build Git Hostinger est cassé

Le build automatique côté hébergeur échoue avec **`EACCES esbuild`** (permissions). Conséquence : **on ne s'appuie pas sur un build Git**. On construit en local, puis on **copie les fichiers** construits (ou le code source pour le backoffice, qui n'a pas besoin de build).

## A. Déployer la vitrine

```bash
cd artifacts/tawes-store
pnpm install
pnpm build                 # génère dist/public/index.html + assets/
```

Puis téléverser le contenu de `dist/public/` dans `public_html/` du domaine vitrine (par FTP/SFTP ou via l'API Hostinger).

Vérification :
```bash
curl -s https://pubstack.space/ | grep -oE 'assets/index-[A-Za-z0-9_-]+\.(js|css)'
# puis vérifier que ces fichiers répondent en 200
```

> **Cache CDN** : un ancien `app.js`/`style.css` peut être servi. Pour vérifier la bonne version, ajouter `?v=$(date +%s)` à l'URL.

## B. Déployer le backoffice

Le backoffice n'a **pas besoin de build** (Express + fichiers statiques). Il faut copier :
- `artifacts/admin/src/`
- `artifacts/admin/public/`
- `artifacts/admin/package.json`

Vers le répertoire de l'application Node sur l'hébergeur.

### Structure Hostinger observée
```
~/domains/gestion-tawes.pubstack.space/hbuilds/
├── current -> versions/<id>        # lien symbolique actif
├── last-source/                    # source de référence
└── versions/<id>/nodejs/           # src/, public/, node_modules/
```

`pm2` fait tourner `src/server.js`. Variables d'environnement via `.env` sur le serveur.

**Important** : après un déploiement par copie directe dans `versions/<id>/`, **synchroniser aussi `last-source/`**, sinon un futur build écrase les changements.

### Vérification
```bash
curl -s -o /dev/null -w '%{http_code}\n' https://gestion-tawes.pubstack.space/
# attendu : 200
```

## C. Déployer le pont

```bash
cd bridge
npm install
cp .env.example .env       # renseigner les vraies valeurs (non committées)
pm2 start ecosystem.config.json
pm2 save
```

Adapter `cwd`, `out_file`, `error_file` dans `ecosystem.config.json` au chemin réel. (Le fichier est livré avec `/opt/tawes-platform/bridge` par défaut.)

Gestion courante :
```bash
./bridge.sh status
./restart.sh
./stop.sh
```

## Checklist avant mise en production

- [ ] `.env` renseignés **sur le serveur uniquement** (jamais dans le dépôt).
- [ ] Script `SECURITY-RLS.sql` **exécuté** et requête de vérification = 0 partout en colonne « écriture_publique ».
- [ ] Vitrine : les assets référencés dans `index.html` répondent en 200.
- [ ] Backoffice : HTTP 200, login OK, file d'attente visible.
- [ ] Pont : `/status` renvoie `connected`, watcher `actif: true`.
- [ ] Un import de test (dry-run) sur un canal donne un résultat plausible.
- [ ] Vérification visuelle desktop **et mobile** par le propriétaire.

## Secrets de déploiement
Il n'existe **aucun script d'automatisation versionné** contenant un mot de passe (l'ancien `ssh-auto.sh` a été volontairement exclu). Utiliser vos propres identifiants SSH/FTP. Voir [10-Sécurité](10-SECURITE-SECRETS.md).
