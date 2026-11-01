# Abdes TV – Application IPTV Android Complète (Mobile & Android TV)

Bienvenue dans le projet officiel **Abdes TV** (`v2.0.0`), conçu selon une architecture modulaire **Kotlin + Jetpack Compose + Media3 ExoPlayer + Room Database**. Ce dépôt contient deux applications Android distinctes partageant le même moteur IPTV haute performance :

| Application | Identifiant de Package (`applicationId`) | Appareil Cible | Orientation & Navigation | Fichier APK Généré |
| :--- | :--- | :--- | :--- | :--- |
| **Abdes TV Mobile** | `com.abdestv.mobile` | **Samsung Galaxy S24 Ultra** & Smartphones Android | Portrait / Paysage adaptatif, tactile & gestes, mode Plein Écran & PiP | `Abdes-TV-Mobile.apk` |
| **Abdes TV Android TV** | `com.abdestv.tv` | **Android TV, Google TV** & Box TV | Paysage 10 pieds (16:9), navigation **100 % télécommande D-Pad**, barre latérale gauche, bannière Leanback | `Abdes-TV-Android-TV.apk` |

---

## 1. Quel fichier APK installer sur quel appareil ?

* **Sur votre Samsung Galaxy S24 Ultra** : Installez **`Abdes-TV-Mobile.apk`** (`com.abdestv.mobile`). Cette version est optimisée pour l'écran Dynamic AMOLED 2X du S24 Ultra, la navigation tactile à une main avec barre inférieure Material 3, la bascule Plein Écran instantanée et le contrôle précis du volume et des pistes.
* **Sur votre téléviseur Android TV / Google TV ou Box TV** : Installez **`Abdes-TV-Android-TV.apk`** (`com.abdestv.tv`). Cette version déclare le lanceur officiel `LEANBACK_LAUNCHER`, ne nécessite aucun écran tactile (`android.hardware.touchscreen="false"`), affiche une bordure lumineuse violette sur chaque élément sélectionné à la télécommande (D-Pad) et permet de zapper avec les touches `CH+` / `CH-` et les flèches directionnelles.

---

## 2. Obtenir vos 2 fichiers APK compilés et signés via GitHub Actions (Sans installer Android Studio)

Le projet inclut un workflow d'intégration continue clé en main (`.github/workflows/build-apks.yml`) qui configure automatiquement **JDK 17**, le **SDK Android (API 34)**, **Gradle 8.7**, génère une clé de signature Release (`abdestv-release.jks`) et compile les deux fichiers APK prêts à installer.

### Étape 1 : Créer un dépôt sur GitHub et importer le code
1. Décompressez l'archive **`Complete-Android-Source.zip`** sur votre ordinateur ou téléphone.
2. Connectez-vous sur [github.com](https://github.com) et cliquez sur **New repository** (Nouveau dépôt).
3. Nommez le dépôt `abdes-tv` (vous pouvez le mettre en **Public** ou **Private**).
4. Envoyez les fichiers sur GitHub :
   * **Méthode rapide via navigateur web** : Cliquez sur **uploading an existing file**, glissez-déposez l'ensemble du contenu extrait (dossiers `.github`, `core`, `mobile`, `tv`, `gradle`, fichiers `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradlew`) puis cliquez sur **Commit changes**.
   * **Méthode via Git (Terminal)** :
     ```bash
     cd abdes-tv-project
     git init
     git add .
     git commit -m "Initial commit: Abdes TV Mobile & Android TV"
     git branch -M main
     git remote add origin https://github.com/VOTRE_COMPTE/abdes-tv.git
     git push -u origin main
     ```

### Étape 2 : Lancer la compilation automatique des APK
1. Dès que les fichiers sont envoyés, ouvrez l'onglet **Actions** de votre dépôt GitHub.
2. Le workflow **Build Abdes TV APKs (Mobile & Android TV)** démarre automatiquement (vous pouvez aussi cliquer sur **Run workflow** manuellement).
3. Patientez environ **3 à 5 minutes** pendant que GitHub compile et signe les deux applications.

### Étape 3 : Télécharger vos 2 fichiers APK
1. Cliquez sur l'exécution terminée (icône verte ✔) dans l'onglet **Actions**.
2. Faites défiler vers le bas jusqu'à la section **Artifacts** :
   * Cliquez sur **`Abdes-TV-Mobile`** pour récupérer **`Abdes-TV-Mobile.apk`** (pour votre Samsung Galaxy S24 Ultra).
   * Cliquez sur **`Abdes-TV-Android-TV`** pour récupérer **`Abdes-TV-Android-TV.apk`** (pour votre Android TV / Google TV).
   * Ou cliquez sur **`ABDES-TV-COMPLETE-APKS`** pour télécharger les deux APK simultanément.

---

## 3. Compiler localement avec Android Studio ou en ligne de commande

Si vous souhaitez compiler les APK sur votre propre machine (Windows, macOS ou Linux) :

1. Assurez-vous d'avoir **JDK 17** et le **SDK Android (API 34)** installés (ou ouvrez simplement le dossier dans **Android Studio Ladybug / Koala**).
2. Exécutez la commande suivante à la racine du projet :
   ```bash
   ./gradlew :mobile:assembleRelease :tv:assembleRelease
   ```
3. Les fichiers APK compilés seront disponibles dans :
   * `mobile/build/outputs/apk/release/mobile-release.apk`
   * `tv/build/outputs/apk/release/tv-release.apk`

---

## 4. Architecture & Fonctionnalités Implémentées

* **Identité visuelle Netflix Noir Profond & Violet Électrique** : Palette `#060609` (Fond Noir Profond), `#0E0E15` (Surface Obsidienne), `#8B5CF6` / `#A855F7` / `#C084FC` (Accents Violets) et interface **100 % en français**.
* **Moteur IPTV Réel (`:core`)** :
  * **Xtream Codes API** : Authentification `player_api.php`, statut du compte et date d'expiration, récupération des catégories et flux TV en direct (`.ts` avec basculement automatique `.m3u8`), catalogue VOD Films et Séries complètes avec saisons et épisodes.
  * **M3U / M3U8** : Analyseur haute performance compatible URL distante et fichier `.m3u` local (`#EXTINF`, `tvg-id`, `tvg-name`, `tvg-logo`, `group-title`, `#EXTVLCOPT:http-user-agent`, détection automatique `url-tvg`).
  * **Guide TV EPG XMLTV** : Analyseur streaming `XmlPullParser` affichant le programme en cours et la barre de progression en temps réel.
  * **Sécurité** : Stockage chiffré des mots de passe IPTV via `EncryptedSharedPreferences` (`AES256_SIV` / `AES256_GCM`).
  * **Base de données Room** : Gestion multi-profils, Favoris, Masquage des chaînes indésirables, Recherche instantanée et reprise de lecture (*Continuer à regarder*).
* **Lecteur Vidéo Android Media3 ExoPlayer (`1.4.1`)** :
  * Support natif HLS, MPEG-TS, DASH, RTSP et MP4/MKV.
  * Sélection des pistes Audio et Sous-titres, 4 formats d'image (`Ajuster 16:9`, `Plein Écran`, `Zoom Cinéma`, `Format Original`), reconnexion automatique avec basculement de secours et zapping rapide.
