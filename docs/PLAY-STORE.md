# Publier Tawes Store sur Google Play

Checklist pratique. Cocher au fur et a mesure.

## 0. Prerequis

- [ ] **Compte developpeur Google Play** (25 $ une seule fois) : https://play.google.com/console
- L'`applicationId` de l'app est **`com.tawesstore.app`** (`app/build.gradle.kts`).
      **Il est définitif** : une fois le premier AAB publie, il ne peut plus changer.

## 1. Signature (obligatoire pour publier)

Google Play exige un **AAB signé**. On ne code jamais le keystore dans le depot.

1. Créer une clé d'upload (une seule fois, uniquement en local) :
   ```bash
   keytool -genkeypair -v -keystore upload.jks -alias tawes-upload \
     -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Créer a la racine du projet un fichier **`keystore.properties`** (non versionne,
   deja dans `.gitignore`) :
   ```
   storeFile=../keystore/upload.jks
   storePassword=********
   keyAlias=upload
   keyPassword=********
   ```
   **Conserver ce fichier et le `.jks` en lieu sûr** (dans le dépôt privé des secrets) :
   sans eux, impossible de publier des mises à jour.
- **Play App Signing** : Google re-signe l'app ; la clé ci-dessus est la clé
  d'**upload**. (Active a la 1re publication.)

## 2. Version

A chaque mise a jour : incrementer `versionCode` et ajuster `versionName`
dans `app/build.gradle.kts`.

## 3. Construire l'AAB

```bash
./gradlew bundleRelease
```
Sortie : `app/build/outputs/bundle/release/app-release.aab`.

## 4. Fiche Play Store (a preparer)

- Nom (max 30), description courte (80), description complete (4000).
- **Icône** 512×512 PNG (l'app a déjà `ic_launcher`).
- **Feature graphic** 1024×500 PNG (obligatoire).
- **Captures d'écran** téléphone (min. 2).
- Catégorie, coordonnées support, e-mail.

## 5. Conformite (obligatoire)

- **Politique de confidentialité** : une URL publique est exigee. A heberger
  (ex. `https://pubstack.space/privacy`). L'app **ne collecte aucune donnee
  personnelle** : la page peut le preciser (produits charges depuis Supabase,
  ouverture de WhatsApp).
- **Formulaire Data safety** : declarer « aucune donnee collectee/partagee ».
- **Classification du contenu** : remplir le questionnaire.
- **Public cible** et **pays de diffusion**.
- Verifier que l'app respecte l'API cible requise (ici `targetSdk = 36`).

## 6. Deploiement

1. Play Console → creer l'application.
2. **Tests internes** d'abord (AAB), valider sur un appareil reel.
3. Puis **Production** (validation Google ~quelques jours).

## Notes

- **Paiement** : la commande se fait **via WhatsApp** (produits physiques) :
  **pas besoin de Google Play Billing**.
- Si l'app proposait un jour un contenu **numerique** paye, la facturation
  Google serait obligatoire (ce n'est pas le cas ici).
