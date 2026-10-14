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
- Banniere publicitaire AdMob en bas d'ecran, soumise au consentement RGPD

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
| Consentement | `user-messaging-platform` 4.0.0 (UMP) |
| minSdk / targetSdk | 24 / 35 |

## Publicite, consentement et enfants

La banniere ne se charge **jamais** avant que le SDK UMP n'ait etabli que le
consentement le permet (`ConsentManager.canRequestAds`). Sans cela, aucune
`AdView` n'est construite et aucune requete ne part.

L'application visant des enfants, la constante `ConsentManager.CHILD_DIRECTED`
(a `true`) applique trois regles :

- annonces marquees *child-directed* (COPPA) ;
- contenu limite a la categorie **G**, tout public ;
- utilisateur declare sous l'age du consentement, ce qui **desactive les
  annonces personnalisees**.

Consequence directe : les revenus sont nettement inferieurs a ceux d'annonces
personnalisees. C'est le reglage prudent pour une application destinee a des
enfants ; le modifier releve d'une decision qui appartient a l'editeur.

Deux points restent a faire **hors du code**, sans quoi la conformite n'est pas
acquise :

1. Creer un message de consentement dans AdMob, onglet **Confidentialite et
   messagerie**. Le SDK n'affiche que des messages configures la-bas ; sans
   message, aucun formulaire ne s'affichera.
2. Publier une politique de confidentialite et la declarer dans la fiche Play,
   ainsi que la section **Securite des donnees**, obligatoire des qu'une regie
   publicitaire est integree.

## Structure

```
app/src/main/java/com/standesv/mathtrainer/
├── MainActivity.kt        navigation, assemblage des ecrans
├── GameViewModel.kt       etat de la partie, chronometre, progression
├── ads/
│   └── ConsentManager.kt  consentement RGPD (UMP) et regime enfants
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
