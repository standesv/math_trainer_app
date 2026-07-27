# Maths Trainer - Application Mobile

Application d'entraînement aux additions et soustractions pour enfants, disponible sur Android et web.

## Fonctionnalités

- 📱 Application mobile Android (APK/AAB)
- ➕ Additions et soustractions
- 🎯 Mode tables de multiplication
- 📊 Suivi des scores et progression
- 👥 Profils utilisateur multiples
- 🎨 Interface simple et colorée
- 🔊 Effets sonores
- ✨ Confettis sur les bonnes réponses

## Installation

### Via APK (Android)
1. Télécharger le fichier APK depuis les [Releases](https://github.com/standesv/math_trainer/releases)
2. Installer sur votre appareil Android
3. Ouvrir l'application

### Via AAB (Google Play)
Fichier de déploiement Google Play disponible dans les releases.

## Structure du Projet

```
math_trainer_app/
├── www/                    # Fichiers web (HTML/CSS/JS)
├── platforms/              # Plateformes de build (Android, iOS)
├── plugins/                # Plugins Cordova
├── config.xml              # Configuration Cordova
├── package.json            # Dépendances Node
└── .github/workflows/      # GitHub Actions pour le build
```

## Développement

### Prérequis
- Node.js 18+
- Cordova 12+
- Android SDK (pour le build local)
- Java 11+

### Setup
```bash
npm install
cordova platform add android
```

### Build Local
```bash
# Build APK
cordova build android --release

# Build AAB
cd platforms/android
./gradlew bundleRelease
```

## Déploiement Automatique

Le projet utilise GitHub Actions pour compiler automatiquement les APK et AAB à chaque push.

Voir `.github/workflows/build.yml` pour les détails.

## Configuration AdMob

Les IDs AdMob sont configurés dans `www/js/admob.js`:
- **App ID**: ca-app-pub-3209259150498249~7379927993
- **Banner ID**: ca-app-pub-3209259150498249/5742724335

Pour modifier, éditer le fichier `www/js/admob.js`.

## License

MIT

## Auteur

Stan Desvoye (@standesv)
