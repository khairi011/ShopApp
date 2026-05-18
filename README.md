# 🛍️ BoutiqueApp — Mini Projet Android

Application Android de gestion de boutique avec Firebase.

## 📋 Critères du projet

| Critère | Réalisé |
|---|---|
| Au moins 2 acteurs | ✅ Admin + Client |
| Au moins 3 activités | ✅ 6 activités |
| RecyclerView | ✅ MainActivity + AdminActivity |
| Base de données Firebase | ✅ Auth + Firestore |
| Code commenté | ✅ Tous les fichiers |

## 🏗️ Structure du projet

```
app/
├── java/com/example/boutiqueapp/
│   ├── models/
│   │   └── Product.java           ← Modèle de données
│   ├── adapters/
│   │   └── ProductAdapter.java    ← Adaptateur RecyclerView
│   └── activities/
│       ├── LoginActivity.java     ← Connexion (partagée)
│       ├── RegisterActivity.java  ← Inscription (client)
│       ├── MainActivity.java      ← Liste produits (client)
│       ├── ProductDetailActivity  ← Détail produit (client)
│       ├── AdminActivity.java     ← Gestion produits (admin)
│       └── AddEditProductActivity ← Ajout/Modification (admin)
└── res/
    └── layout/
        ├── activity_login.xml
        ├── activity_register.xml
        ├── activity_main.xml
        ├── activity_product_detail.xml
        ├── activity_admin.xml
        ├── activity_add_edit_product.xml
        └── item_product.xml
```

## 🚀 Mise en place

### 1. Créer le projet Firebase

1. Aller sur [console.firebase.google.com](https://console.firebase.google.com)
2. Créer un nouveau projet : **BoutiqueApp**
3. Ajouter une application Android :
   - Package : `com.example.boutiqueapp`
   - Télécharger `google-services.json` → le placer dans `app/`
4. Activer **Authentication** → Email/Mot de passe
5. Activer **Cloud Firestore** → Mode test
6. Créer manuellement le compte admin :
   - Email : `admin@boutique.com`
   - Mot de passe : `admin123`

### 2. Créer le projet Android Studio

1. New Project → Empty Activity
2. Package name : `com.example.boutiqueapp`
3. Min SDK : API 24
4. Copier tous les fichiers Java et XML
5. Copier `google-services.json` dans `app/`
6. Modifier `build.gradle` (voir section ci-dessous)

### 3. Dépendances build.gradle (app)

```gradle
plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services'   // Plugin Firebase
}

android {
    compileSdk 34
    defaultConfig {
        applicationId "com.example.boutiqueapp"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }
    buildFeatures { viewBinding true }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.7.0'
    implementation 'com.google.android.material:material:1.12.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.cardview:cardview:1.0.0'

    // Firebase BOM — gère toutes les versions automatiquement
    implementation platform('com.google.firebase:firebase-bom:33.0.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
}
```

### 4. build.gradle (projet, niveau racine)

```gradle
plugins {
    id 'com.android.application' version '8.2.0' apply false
    id 'com.google.gms.google-services' version '4.4.1' apply false
}
```

### 5. Règles Firestore (mode test)

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;  // Mode test uniquement !
    }
  }
}
```

## 👤 Comptes de test

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Admin | admin@boutique.com | admin123 |
| Client | client@test.com | client123 |

> L'email `admin@boutique.com` est hardcodé dans `LoginActivity.java` pour la redirection.

## 📱 Fonctionnalités

### Acteur 1 — Admin
- Connexion → **AdminActivity** (RecyclerView de tous les produits)
- Ajouter un produit → **AddEditProductActivity**
- Modifier un produit → **AddEditProductActivity** (pré-rempli)
- Supprimer un produit (appui long sur la carte)

### Acteur 2 — Client
- Inscription → **RegisterActivity**
- Connexion → **MainActivity** (RecyclerView avec grille)
- Recherche en temps réel par nom ou catégorie
- Détail produit → **ProductDetailActivity**

## 🔗 GitHub

```bash
git init
git add .
git commit -m "Initial commit — BoutiqueApp Android"
git branch -M main
git remote add origin https://github.com/TON_USERNAME/BoutiqueApp.git
git push -u origin main
```

## Structure Firestore

```
produits/  (collection)
  └── {documentId}/  (document)
        ├── nom        : String
        ├── description: String
        ├── prix       : Number
        ├── quantite   : Number
        └── categorie  : String
```
