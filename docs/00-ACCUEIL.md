# Documentation Tawes Platform

Bienvenue. Cette documentation est écrite pour qu'une personne **ou une IA** puisse reprendre le projet sans avoir à deviner.

## Par où commencer

| # | Document | Contenu |
| --- | --- | --- |
| 01 | [Architecture](01-ARCHITECTURE.md) | Les briques, comment elles communiquent |
| 02 | [Objectif final](02-OBJECTIF-FINAL.md) | À quoi doit ressembler le produit fini |
| 03 | [État des lieux](03-ETAT-DES-LIEUX.md) | Ce qui marche aujourd'hui, vérifié |
| 04 | [Ce qui reste à faire](04-CE-QUI-RESTE-A-FAIRE.md) | La feuille de route priorisée |
| 05 | [Le pont wa-bridge](05-PONT-WA-BRIDGE.md) | WhatsApp + Telegram, en détail |
| 06 | [La boutique et l'admin](06-BOUTIQUE-ET-ADMIN.md) | Vitrine + backoffice |
| 07 | [Le pipeline Telegram](07-PIPELINE-TELEGRAM.md) | De la publication grossiste au produit publié |
| 08 | [La base de données](08-BASE-DE-DONNEES.md) | Tables, colonnes, RLS |
| 09 | [Déploiement](09-DEPLOIEMENT.md) | Hostinger + pm2, sans secret |
| 10 | [Sécurité et secrets](10-SECURITE-SECRETS.md) | Ce qui a été exposé, ce qu'il faut faire |
| 11 | [Historique du travail](11-HISTORIQUE-TRAVAIL.md) | Ce qui a été fait, et pourquoi |
| 12 | [Guide de reprise IA](12-GUIDE-REPRISE-IA.md) | Marche à suivre pour un agent |

## Le contexte en un paragraphe

Le propriétaire achète des articles en gros auprès de fournisseurs marocains qui publient leurs stocks sur **Telegram** (photos + légendes en arabe, prix inclus). Recopier ces annonces à la main dans une boutique est lent et source d'erreurs. Le projet automatise la **collecte et l'extraction**, mais garde une **validation humaine** car l'OCR et l'arabe dialectal restent trop imprécis. Le commerçant valide dans un backoffice, la vitrine publique affiche les produits, et un second canal WhatsApp répond automatiquement aux questions simples des clients.

## Vocabulaire

- **Le pont** (`bridge/`) : le programme Node qui parle à WhatsApp et Telegram.
- **Le canal** : un canal Telegram de grossiste surveillé.
- **L'agent** (commercial) : la partie du pont qui analyse un message et crée une fiche produit candidate.
- **La file / le staging** : les produits `processed = false` en attente de validation humaine.
- **Le backoffice / l'admin** : l'interface de gestion Express.
- **La vitrine / le store** : le site public.
- **Canal surveillé** : un canal fournisseur activé (identifiants anonymisés dans ce dépôt ; voir `bridge/src/agent/config.js`).
