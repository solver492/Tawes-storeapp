# Tawes Store — Application Android

Application e-commerce et gestion commerciale pour **Tawes Store**, développée en Kotlin natif avec **Jetpack Compose** et **Material Design 3**.

## Fonctionnalités

### Vitrine E-commerce & Commandes
- **Double Mode d'Achat** :
  - **Achat au détail** : pour les particuliers, affichage des prix de vente, remises et promotions.
  - **Achat en gros** : pour les revendeurs, tarifs grossiste préférentiels, seuils de commande minimum par produit.
- **Navigation & Filtres** :
  - Filtrage par section : Femme, Homme, Enfant, Tout.
  - Filtrage par catégories : Téléphones, Électronique, Mode, Maison, Beauté, Électroménager, Accessoires.
  - Recherche instantanée par nom, catégorie et mots-clés.
- **Fiche Produit Détaillée** :
  - Galerie visuelle et caractéristiques.
  - Sélecteur de quantité avec respect des minimums de commande grossiste.
  - Bouton **"Commander sur WhatsApp"** avec message pré-formaté (produit, mode, quantité, total).
  - Ajout rapide au panier / devis.
- **Panier & Devis** :
  - Gestion des quantités, récapitulatif clair en Dirhams (DH).
  - Finalisation de la commande directement via WhatsApp.
- **Support Client WhatsApp** :
  - Bouton d'assistance flottant pour échanger directement avec l'équipe commerciale.

### Espace Backoffice (Gestion)
- **File d'attente (Telegram)** :
  - Réception des arrivages détectés depuis les canaux de grossistes Telegram.
  - Calculateur automatique de marge commerciale en Dirhams (Prix vente = Prix gros + marge).
  - Validation et publication directe dans la vitrine, ou rejet.
- **Catalogue & Stocks** :
  - Visualisation de tous les articles, statut, prix de gros vs prix de vente, et suppression.
- **Import / Ajout de Produit** :
  - Formulaire complet pour ajouter de nouvelles références manuellement.

## Stack Technique
- **Langage** : Kotlin (JVM 21)
- **UI** : Jetpack Compose + Material Design 3
- **Images** : Coil Compose
- **Build System** : Gradle Kotlin DSL + Android Gradle Plugin 9.1.1
- **Icône** : Icône adaptative personnalisée Tawes Store (Monogramme T doré sur vert émeraude `#164D3A`)
