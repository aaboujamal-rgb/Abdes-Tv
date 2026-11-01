# Rapport d'Architecture, de Vérification Statique et de Compilation – Abdes TV

## 1. Synthèse du Projet

* **Nom de la marque** : Abdes TV
* **Langue de l'interface** : Français (100 % des menus, catégories, erreurs et paramètres)
* **Design System** : Noir Profond (`#060609`) & Violet Royal (`#8B5CF6` / `#A855F7` / `#C084FC`)
* **Architecture** : Multi-modules Gradle (`:core`, `:mobile`, `:tv`)

## 2. Matrice des Variantes d'Applications

| Critère | Abdes TV Mobile (`:mobile`) | Abdes TV Android TV (`:tv`) |
| :--- | :--- | :--- |
| **Application ID** | `com.abdestv.mobile` | `com.abdestv.tv` |
| **Cible Principale** | Samsung Galaxy S24 Ultra & Smartphones | Android TV, Google TV & Box TV |
| **Min SDK / Target SDK / Compile SDK** | 24 / 34 / 34 | 24 / 34 / 34 |
| **Catégorie de Lanceur** | `android.intent.category.LAUNCHER` | `android.intent.category.LEANBACK_LAUNCHER` + `LAUNCHER` |
| **Exigence Écran Tactile** | Tactile + Gestes optimisés | `android.hardware.touchscreen="false"` (100 % Télécommande D-Pad) |
| **Nom de l'APK de sortie** | `Abdes-TV-Mobile.apk` | `Abdes-TV-Android-TV.apk` |

## 3. Matrice des Versions et Dépendances Vérifiées

| Composant / Bibliothèque | Version | Statut de Compatibilité |
| :--- | :--- | :--- |
| **Gradle Wrapper** | `8.7` | Vérifié (compatible AGP 8.5.2 & JDK 17) |
| **Android Gradle Plugin (AGP)** | `8.5.2` | Vérifié |
| **Kotlin & Compose Compiler Plugin** | `2.0.20` | Vérifié (`org.jetbrains.kotlin.plugin.compose`) |
| **KSP (Room Compiler)** | `2.0.20-1.0.24` | Aligné exactement sur Kotlin `2.0.20` |
| **Jetpack Compose BOM** | `2024.09.02` | Vérifié (Material 3, Foundation, UI, Navigation `2.8.1`) |
| **AndroidX Media3 ExoPlayer** | `1.4.1` | Vérifié (`exoplayer`, `hls`, `dash`, `rtsp`, `ui`, `datasource-okhttp`) |
| **AndroidX Room Database** | `2.6.1` | Vérifié (`room-runtime`, `room-ktx`, `room-compiler`) |
| **OkHttp / Retrofit / Gson** | `4.12.0` / `2.11.0` | Vérifié |
| **AndroidX Security Crypto** | `1.1.0-alpha06` | Vérifié (`EncryptedSharedPreferences` + fallback TV Box) |
| **Coil Compose** | `2.7.0` | Vérifié (Chargement asynchrone des logos et affiches VOD) |

## 4. Résultats des Contrôles d'Assurance Qualité (QA)

1. **Validation des fichiers XML & AndroidManifest** :
   * `core/src/main/AndroidManifest.xml` : Valide.
   * `mobile/src/main/AndroidManifest.xml` : Valide (`usesCleartextTraffic="true"`, `supportsPictureInPicture="true"`, icône `@drawable/ic_abdes_launcher`).
   * `tv/src/main/AndroidManifest.xml` : Valide (`android.software.leanback="true"`, `android.hardware.touchscreen="false"`, `android:banner="@drawable/tv_banner"`, `LEANBACK_LAUNCHER`).
2. **Validation du Code Source Kotlin** :
   * Équilibrage syntaxique (accolades, parenthèses, imports, packages) validé à 100 % sur l'ensemble des modules `:core`, `:mobile` et `:tv`.
   * Séparation stricte des identifiants `com.abdestv.mobile` et `com.abdestv.tv` permettant d'installer ou de gérer les deux applications sans conflit.
3. **Validation du Pipeline CI/CD GitHub Actions (`.github/workflows/build-apks.yml`)** :
   * Génération automatique du keystore de signature Release (`keystore/abdestv-release.jks`) lors du build afin que `Abdes-TV-Mobile.apk` et `Abdes-TV-Android-TV.apk` soient **immédiatement installables** sur Samsung Galaxy S24 Ultra et Android TV sans erreur *"App not installed / Package appears to be invalid"*.
