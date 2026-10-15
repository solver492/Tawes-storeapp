# AGENTS.md — consignes pour les agents IA

Ce fichier s'adresse a tout agent (Kiro, Claude Code, Cursor, Copilot…) qui reprend ce depot. Lis-le en entier avant d'agir.

## Le projet en 10 secondes

Application **Android** (Kotlin + Jetpack Compose) de **Tawes Store** : une **vitrine** pour les clients. Elle affiche les produits **publies** de la boutique (meme base **Supabase** que le site `pubstack.space`) et permet de **commander via WhatsApp**. Elle ne contient **aucune** logique d'administration.

## Regles absolues

1. **NE JAMAIS committer de secret.** Pas de `.env`, pas de clé `sb_secret_…`, pas de mot de passe, pas de token, pas de numéro de téléphone réel, pas d'identifiant de canal. Les seules valeurs de configuration admises sont **publiques** : l'URL Supabase, la clé **publishable/anon** (lecture seule) et le numéro WhatsApp (renseignés au build, cf. `.env.example`).
2. **L'app est en LECTURE SEULE sur Supabase.** Utiliser la clé **publishable**, jamais la clé secrète/service, et uniquement des `GET` sur les produits publies.
3. **Vitrine uniquement.** Pas de Telegram, pas de pont WhatsApp admin, pas de backoffice, pas de génération d'images.
4. **NE JAMAIS tester une écriture sur la base de production.**
5. **L'utilisateur communique en francais.** Repondre en francais.
6. **Vérification manuelle avant déploiement.** Préparer, montrer, attendre le feu vert.
7. **Pas de push Git sans demande explicite.**
8. **Pas de LLM dans le code.**

## Structure

```
Tawes-storeapp/
├── app/
│   └── src/main/
│       ├── java/com/tawesstore/app/
│       │   ├── MainActivity.kt              ← navigation (HOME / DETAIL / CART)
│       │   ├── data/
│       │   │   ├── AppConfig.kt             ← config injectée au build (BuildConfig)
│       │   │   ├── model/                   ← Product, CartItem, ShoppingMode
│       │   │   ├── remote/SupabaseClient.kt ← lecture Supabase (HttpURLConnection + org.json)
│       │   │   └── repository/ProductRepository.kt
│       │   └── ui/                          ← theme, components, screens (Compose)
│       └── res/
├── .github/workflows/android-ci.yml
├── docs/PLAY-STORE.md                       ← checklist de publication
└── gradle/ (wrapper)
```

## Commandes clés

```bash
./gradlew assembleDebug        # APK de test
./gradlew bundleRelease        # AAB a publier (signer d'abord, cf. docs/PLAY-STORE.md)
./gradlew lint
```

## Configuration (au build, non versionnee)

| Variable | Rôle | Sensible ? |
|---|---|---|
| `SUPABASE_URL` | URL du projet Supabase | non (publique) |
| `SUPABASE_ANON_KEY` | Clé **publishable** (lecture seule) | non (publique, déjà dans le site) |
| `WHATSAPP_NUMBER` | Numéro WhatsApp de la boutique | non |

Renseigner dans `local.properties` (Gradle) ou en variables d'environnement. **Ne jamais** y mettre la clé `sb_secret_…`.

## Pieges connus

- **Ne pas confondre** la clé publishable (OK, lecture) avec la clé **secrete** du backoffice (`sb_secret_…`, nom historique `SUPABASE_ANON_KEY` cote admin). Cette derniere ne doit jamais entrer dans l'app.
- La table `products` est lue via la **vue `products_with_category`** (le site fait pareil) ; les images/videos vivent dans **`product_media`**.
- L'app suit le **meme reseau** que le site : si le site affiche des produits, l'app aussi (meme requete `is_published_to_website=eq.true&active=eq.true`).
