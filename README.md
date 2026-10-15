# Tawes Store — Application Android (vitrine)

Application **Kotlin + Jetpack Compose** (Material 3) : une **vitrine** pour les
clients de Tawes Store. Elle affiche les produits **publies** de la boutique
(meme base **Supabase** que le site [pubstack.space](https://pubstack.space)) et
permet de **commander via WhatsApp**.

## Ce que fait l'application

- Catalogue des produits publies (grille, recherche, filtres Femme / Homme / Enfant, categories).
- Fiche produit (image, description, prix, remise).
- Panier local + finalisation de la commande via **WhatsApp**.
- Deux modes : **détail** (particuliers) et **gros** (revendeurs).
- Support client via WhatsApp.

## Ce que l'application NE fait PAS (volontairement)

- Aucune administration (pas de backoffice, pas de gestion Telegram).
- Aucun secret, aucune ecriture en base : **lecture seule** des produits publies.
- Aucune donnee personnelle stockee.

## Configuration

Trois valeurs **publiques** sont injectees au build (voir `.env.example`) :

| Variable | Description |
|---|---|
| `SUPABASE_URL` | URL du projet Supabase |
| `SUPABASE_ANON_KEY` | Clé **publishable** (lecture seule) |
| `WHATSAPP_NUMBER` | Numéro WhatsApp de la boutique (format international sans `+`) |

A renseigner dans `local.properties` (non versionne) ou en variables
d'environnement avant de builder. Sans ces valeurs, l'app compile mais affiche
« boutique non configuree ».

`local.properties` (exemple) :
```
SUPABASE_URL=https://xxxxxxxx.supabase.co
SUPABASE_ANON_KEY=sb_publishable_xxxxxxxx
WHATSAPP_NUMBER=2126XXXXXXXX
```

## Build

```bash
./gradlew assembleDebug     # APK de test
./gradlew bundleRelease     # AAB a publier (voir docs/PLAY-STORE.md pour la signature)
```

## Publication

Voir **[docs/PLAY-STORE.md](docs/PLAY-STORE.md)** pour la checklist complete
(signature, fiche Play, confidentialite, Data safety…).

## Stack

- Kotlin 2.2, Jetpack Compose (BOM 2024.09), Material 3, AGP 9.1.1, Gradle 9.3.1
- Coil (images)
- Reseau : `HttpURLConnection` + `org.json` (aucune dependance supplementaire)
