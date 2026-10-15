# 02 — Objectif final

## La cible

Une machine à vendre simple pour un commerçant marocain :

> **Je ne recopie plus jamais une annonce à la main. Les produits arrivent tout seuls depuis mes fournisseurs Telegram, je valide en quelques secondes dans une interface, et ils sont en ligne. Les clients qui m'écrivent sur WhatsApp obtiennent une réponse immédiate, et je suis prévenu quand une question mérite mon attention.**

## Ce que « fini » veut dire, concrètement

### 1. Collecte automatique, fiable
- Le pont surveille **les 11 canaux** de grossistes (aujourd'hui, un seul est activé).
- Il rattrape les messages manqués (redémarrage, coupure réseau) sans doublons.
- Il tourne en permanence, redémarre seul, et **signale** s'il ne travaille plus (le plus grand risque est un import mort en silence).

### 2. Extraction utile
- Détecter le **prix** (dans la légende), le **nom** (souvent dans la photo), la **quantité du lot**, la **catégorie**.
- Gérer les **albums** (plusieurs photos = un seul produit).
- Récupérer **photos + vidéos**, compresser les vidéos, en extraire une **vignette**.
- Classer ce qui est incertain en « à confirmer » plutôt que de le jeter.

### 3. Validation rapide
- Le backoffice montre la file d'attente, filtrable par canal.
- Corriger un prix/nom, choisir une catégorie, publier : en quelques clics.
- Pouvoir **ajouter soi-même** des photos/vidéos à un produit (upload multiple).
- Supprimer, réordonner, diffuser une vidéo sur Vimeo.

### 4. Vitrine crédible
- Un catalogue public propre, rapide, responsive, en français.
- Produits avec images/vidéos, prix détail **et** prix revendeur (gros).
- Bouton WhatsApp qui ouvre la conversation avec le bon numéro.

### 5. Support WhatsApp assisté
- Répondre automatiquement aux questions simples (prix d'un produit, lien du site, horaires).
- Escalader au propriétaire tout ce qui dépasse (négociation, réclamation, gros volume).

### 6. Stockage des médias
- À terme, **sortir les vidéos de Supabase** et les héberger sur un stockage moins cher (Cloudflare R2 / PeerTube), tout en gardant Supabase comme source de vérité des fiches.

## Ce qui n'est **pas** l'objectif

- Pas de paiement en ligne dans un premier temps : la vente se conclut sur WhatsApp.
- Pas de compte client, pas de panier transactionnel côté serveur (le « panier » de la vitrine est local).
- Pas d'IA générative dans le produit (pas de LLM) : les règles suffisent, la validation humaine rattrape.
- Pas d'application mobile native.

## Indicateurs de réussite

| Indicateur | Aujourd'hui | Cible |
| --- | --- | --- |
| Canaux surveillés en production | 1 | 11 |
| Temps pour mettre un produit en ligne | quasi nul | idem |
| Doublons en base | 0 | 0 |
| Import qui meurt sans alerte | incident corrigé (30/09) | impossible |
| Coût de stockage vidéo | Supabase | R2 (moins cher) |
| Questions WhatsApp sans réponse | manuel | réponses auto + escalade |
