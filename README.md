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
npm run build:apk

# Build AAB
npm run build:aab
```

Prérequis local : Node 22, Java 21, Android SDK 36.

## Déploiement Automatique

GitHub Actions compile l'APK et l'AAB à chaque push sur `main`.
Un push de tag `v*` publie en plus une Release avec les deux fichiers.

Voir `.github/workflows/build.yml` et `DEPLOYMENT.md`.

## Configuration AdMob

Plugin utilisé : [`admob-plus-cordova`](https://github.com/admob-plus/admob-plus) (activement maintenu).

| Élément | Valeur | Emplacement |
|---|---|---|
| App ID | `ca-app-pub-3209259150498249~7379927993` | `config.xml` (variable `APP_ID_ANDROID`) |
| Banner ID | `ca-app-pub-3209259150498249/5742724335` | `www/js/admob.js` |

La bannière s'affiche en bas de l'écran, au-dessus de la barre de navigation.
En développement (`http://localhost`), l'ID de test officiel Google est utilisé
automatiquement afin de ne pas fausser les statistiques ni risquer une
suspension du compte AdMob.

## License

MIT

## Auteur

Stan Desvoye (@standesv)
