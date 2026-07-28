# Maths Trainer

Application Android d'entrainement aux additions et soustractions, concue pour des enfants.

## Fonctionnalites

- Profils multiples, chacun avec ses reglages, sa progression et son historique
- Quatre modes : Mix, Addition, Soustraction, Tables
- Progression par paliers de 10 jusqu'a 200, debloques a 80 % de reussite sur au moins 10 questions
- Chronometre et moyenne de secondes par question
- Corrige detaille en fin de partie
- Historique des 12 dernieres parties par profil
- Sons de retour et confettis
- Banniere publicitaire AdMob en bas d'ecran

## Ergonomie pensee pour les enfants

Le clavier systeme est remplace par un **pave numerique integre** : cibles tactiles
larges, uniquement des chiffres, aucun risque de sortir de l'application. Les
boutons font au moins 56 dp de haut, la police est large et la palette tres
contrastee. Les reglages n'utilisent que des boutons + / − et des interrupteurs,
sans aucune saisie libre de nombre.

## Technique

Application **native Kotlin + Jetpack Compose**, Material 3.

Elle remplace une version Cordova qui plantait au demarrage. L'empilement
WebView + plugin AdMob tiers + substitution de variables dans le manifeste
etait la source du probleme : le SDK publicitaire recevait un identifiant
invalide et interrompait le lancement. En natif, l'App ID est ecrit en dur
dans `AndroidManifest.xml` et le SDK AdMob officiel est une simple dependance
Gradle.

| Element | Choix |
|---|---|
| Langage | Kotlin 2.0.21 |
| Interface | Jetpack Compose, Material 3 |
| Persistance | DataStore Preferences |
| Publicite | `play-services-ads` 23.5.0 (SDK officiel) |
| minSdk / targetSdk | 24 / 35 |

## Structure

```
app/src/main/java/com/standesv/mathtrainer/
├── MainActivity.kt        navigation, assemblage des ecrans
├── GameViewModel.kt       etat de la partie, chronometre, progression
├── data/
│   ├── Model.kt           modes, reglages, generation des questions
│   └── Store.kt           persistance par profil
└── ui/
    ├── Theme.kt           palette et typographie
    ├── Components.kt      pave numerique, boutons, cartes
    ├── Screens.kt         accueil, jeu, resultat, scores
    ├── SettingsSheet.kt   reglages
    ├── Effects.kt         sons et confettis
    └── AdBanner.kt        banniere AdMob
```

## Compilation

```bash
gradle assembleRelease bundleRelease
```

Prerequis : Java 17, Android SDK 35, Gradle 8.9.

Le nom affiche sous l'icone inclut la version, injectee depuis le tag git
par la CI (`Maths Trainer 1.1.0`).

## Publication

Voir `DEPLOYMENT.md`.

## Auteur

Stan Desvoye (@standesv)
