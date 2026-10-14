# Notes de version

Toutes les versions notables de Maths Trainer.
Le format suit [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/)
et la numerotation [SemVer](https://semver.org/lang/fr/).

---

## 1.6.0 — 2026-10-14

### Modifie

- Identifiant de l'application change en `com.mathtrainer.myapp`, pour
  correspondre a la fiche Google Play. Le paquet Kotlin des sources reste
  inchange.

### Attention

Cet identifiant etant different, Android considere qu'il s'agit d'une autre
application : la version precedente doit etre desinstallee avant d'installer
celle-ci.

---

## 1.5.0 — 2026-10-14

### Ajoute

- **Consentement RGPD** au lancement, via le SDK officiel Google User Messaging
  Platform. La banniere ne se charge plus tant que le consentement n'est pas
  etabli : aucune `AdView` n'est construite, aucune requete publicitaire ne part.
- **Regime « application pour enfants »** : annonces marquees *child-directed*,
  contenu limite a la categorie G, utilisateur declare sous l'age du
  consentement. Les annonces personnalisees sont donc desactivees.
- Bouton **Confidentialite et publicite** dans les reglages, affiche uniquement
  quand Google impose un acces permanent aux options de confidentialite.

### Modifie

- Le choix de la table et de son operation ne vit plus qu'**a un seul endroit**,
  l'accueil. Le bloc « Mode Tables » disparait du panneau de reglages.
- La carte « Niveau / questions » de l'accueil devient cliquable et ouvre les
  reglages.
- Repartition clarifiee : l'accueil porte ce qui change a chaque partie (profil,
  mode, table, operation), les reglages ce qu'un parent configure une fois
  (nombre de questions, niveau, negatifs, progression, sons, confettis, profils).

### Corrige

- Les reglages affichaient le niveau demande alors que l'accueil affichait le
  niveau reellement applique, sans expliquer l'ecart. Le plafond impose par la
  progression est desormais annonce explicitement.

### A faire hors du code

La conformite n'est pas acquise par le seul code. Il reste a creer un message
de consentement dans AdMob (onglet **Confidentialite et messagerie**) — sans
lui, aucun formulaire ne s'affichera — et a publier une politique de
confidentialite declaree dans la fiche Play ainsi que dans la section
**Securite des donnees**.

---

## 1.4.0 — 2026-07-28

### Ajoute

- Mode **`×`** sur l'accueil : toutes les tables de multiplication melangees,
  les deux facteurs restant entre 0 et 10.
- Choix de l'operation (`×` `+` `−`) et de la table directement sur l'accueil,
  sous les modes, avec le numero affiche en grand.

### Modifie

- L'historique distingue les types de table : « Table de 7 (×) » plutot que
  « Table de 7 ».

### Note

La multiplication existait depuis la 1.3.0 mais etait enfouie dans les
reglages, donc introuvable a l'usage. Pour une application destinee a des
enfants, la decouvrabilite fait partie de la fonctionnalite.

---

## 1.3.0 — 2026-07-28

### Ajoute

- **Tables de multiplication** dans le mode Tables, aux cotes de l'addition et
  de la soustraction.
- Le multiplicateur est plafonne a 10 quel que soit le niveau : une table se
  recite de 0 a 10, et un niveau a 200 aurait produit des questions du type
  `7 × 173`.

### Corrige

- Un profil regle sur la multiplication retombait silencieusement sur
  l'addition au redemarrage.

---

## 1.2.2 — 2026-07-28

**Premiere version reellement fonctionnelle.**

### Modifie

- **Reecriture complete en Kotlin natif + Jetpack Compose**, en remplacement de
  la version Cordova.
- Pave numerique integre a la place du clavier systeme : cibles plus larges,
  uniquement des chiffres, aucun risque de sortir de l'application.
- Boutons d'au moins 56 dp, typographie large, palette contrastee.
- Reglages sans aucune saisie libre : uniquement des boutons `+` / `−` et des
  interrupteurs.
- Persistance par profil via DataStore.

### Corrige

- **L'application ne se ferme plus au lancement.** La cause etait la chaine
  Cordova : l'App ID AdMob etait injecte dans le manifeste par substitution de
  variable, et lorsque celle-ci echouait, le SDK Google interrompait le
  demarrage avant meme l'execution du JavaScript. En natif, l'App ID est ecrit
  en dur dans `AndroidManifest.xml`.
- L'initialisation d'AdMob est desormais hors du thread principal et protegee :
  une panne cote publicite ne peut plus empecher l'ouverture.

### Technique

- Fonctionnalites transposees a l'identique depuis la version web : generation
  des questions, progression par paliers de 10 jusqu'a 200 (deblocage a 80 % sur
  au moins 10 questions), historique de 12 parties par profil, chronometre et
  moyenne par question, corrige detaille.
- Nom affiche sous l'icone incluant la version, injectee depuis le tag git.
- Fichiers produits nommes `MathsTrainer-<version>.apk` et `.aab`.

---

## 1.2.0 — 2026-07-28

**Version defectueuse, a ignorer.** Le tag avait ete pose sur un commit vide :
la compilation a reconstruit l'ancien code Cordova.

---

## 1.0.0 a 1.1.0 — 2026-07-27 / 28

Versions Cordova. **Toutes se fermaient au lancement.**

Tentatives successives de correction, documentees ici parce qu'elles expliquent
le passage au natif :

- plugin `cordova-plugin-admob-free`, abandonne depuis 2019, incompatible avec
  la chaine de compilation actuelle ;
- migration vers `admob-plus-cordova` 1.x, figee sur le SDK Ads 20.6.0 de
  janvier 2022, anterieur a Android 14 ;
- essais de montee en version du SDK (23.6.0 puis 22.6.0), tous deux refuses a
  la compilation : `MobileAds.getVersionString()` a disparu des la 22.0.0 ;
- migration vers `admob-plus-cordova` 2.x, qui n'a pas resolu le crash.

La 1.1.0 avait de surcroit ete taguee sur un commit Cordova, et ne contenait
donc pas le code attendu.
