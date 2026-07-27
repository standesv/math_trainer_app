# Guide de Déploiement

## 1. Pousser le code

```bash
cd ~/Downloads/math_trainer_app
rm -f package-lock.json
git add -A
git commit -m "Modernise le build Android et remplace le plugin AdMob"
git push
```

GitHub Actions démarre automatiquement et produit :

- `MathsTrainer-APK` — installation directe sur un téléphone
- `MathsTrainer-AAB` — dépôt sur le Google Play Store

Onglet **Actions** → dernier workflow → section **Artifacts**.

## 2. Keystore de signature

Sans configuration, le workflow génère un keystore **éphémère** à chaque build.
C'est suffisant pour tester un APK, mais :

- deux builds successifs ne peuvent pas se mettre à jour l'un l'autre ;
- le Play Store refusera un AAB signé par une clé différente de la précédente.

### Créer un keystore permanent

```bash
keytool -genkeypair -v \
  -keystore mathtrainer-release.keystore \
  -alias mathtrainer \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Stan Desvoye, O=Personal, C=FR"
```

Conservez ce fichier et son mot de passe hors du dépôt : **il est irremplaçable**.
Le perdre signifie ne plus jamais pouvoir mettre à jour l'application publiée.

### Le déclarer dans GitHub

```bash
base64 -w0 mathtrainer-release.keystore > keystore.b64
```

Puis dans **Settings → Secrets and variables → Actions → New repository secret** :

| Secret | Valeur |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | contenu de `keystore.b64` |
| `ANDROID_KEYSTORE_PASSWORD` | mot de passe du keystore |
| `ANDROID_KEY_ALIAS` | `mathtrainer` |
| `ANDROID_KEY_PASSWORD` | mot de passe de la clé |

Le workflow les détecte automatiquement au build suivant.

## 3. Publier une version

```bash
git tag v1.0.0
git push origin v1.0.0
```

Une Release GitHub est créée avec l'APK et l'AAB attachés.

## 4. Installer l'APK sur Android

1. Transférer le `.apk` sur le téléphone
2. Autoriser « Installer des applications inconnues » pour le gestionnaire de fichiers
3. Ouvrir le fichier et confirmer

## 5. Publier sur le Play Store

1. Google Play Console → créer l'application
2. Téléverser `MathsTrainer.aab` en test interne
3. Renseigner la fiche Play, la politique de confidentialité et le questionnaire
   « Sécurité des données » — obligatoire dès qu'une régie publicitaire est intégrée
4. L'application cible les enfants : vérifier la section **Familles** et régler
   le traitement des annonces en conséquence dans AdMob

## Dépannage

**Le build échoue** — Actions → workflow → dérouler l'étape en rouge.
Les causes fréquentes sont une version de plugin incompatible ou un secret mal encodé
(`base64 -w0` est requis, sans retour à la ligne).

**La bannière ne s'affiche pas** — une nouvelle unité AdMob met généralement
quelques heures avant de diffuser. Vérifier les logs via `adb logcat | grep Ads`.

## Références

- Cordova Android : https://cordova.apache.org/docs/en/latest/guide/platforms/android/
- AdMob Plus : https://admob-plus.github.io/
- Politique AdMob : https://support.google.com/admob/answer/6128543
