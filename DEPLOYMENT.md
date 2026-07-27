# Guide de Déploiement

## 1. Créer un Repository sur GitHub

Si vous n'avez pas déjà créé le repository:

1. Allez sur https://github.com/new
2. Nommez-le `math_trainer_app`
3. **Ne pas** initialiser avec README (nous en avons déjà un)
4. Créez le repository

## 2. Pousser le code local vers GitHub

```bash
cd ~/Downloads/math_trainer_app

# Ajouter le remote GitHub (remplacer YOUR_USERNAME par votre username)
git remote add origin https://github.com/YOUR_USERNAME/math_trainer_app.git

# Renommer la branche master en main (optionnel)
git branch -M main

# Pousser le code
git push -u origin main
```

## 3. Configurer les GitHub Actions

Le workflow est déjà configuré dans `.github/workflows/build.yml`.

Lors du prochain push, GitHub Actions:
1. Installera les dépendances
2. Compilera la plateforme Android
3. Générera l'APK et l'AAB
4. Les mettra à disposition en téléchargement

## 4. Télécharger APK & AAB

Après le build (quelques minutes):

1. Allez sur votre repository
2. Cliquez sur **Actions**
3. Sélectionnez le dernier workflow
4. Sous **Artifacts**, téléchargez:
   - `APK` - fichier .apk pour installer directement
   - `AAB` - fichier .aab pour Google Play Store

## 5. Installer sur votre téléphone

### Via APK:
1. Transférer le fichier APK sur votre téléphone
2. Ouvrir le gestionnaire de fichiers
3. Taper sur le fichier APK
4. Accepter l'installation

### Via Google Play Store:
1. Utiliser l'AAB pour mettre en place une version de test interne dans la Google Play Console
2. Inviter des testeurs
3. Ou publier directement sur le Play Store

## 6. Mise à jour

Pour mettre à jour l'application:

```bash
# Modifier les fichiers (www/app.v20.js, www/styles.v20.css, etc.)

# Commit et push
git add .
git commit -m "Mise à jour v1.x.x"
git push
```

GitHub Actions recompilera automatiquement l'APK et l'AAB.

## 7. Configuration des Secrets (Optionnel)

Pour un déploiement plus sécurisé, vous pouvez stocker le keystore en tant que secret:

1. Générer un keystore sécurisé
2. Encoder en base64
3. Ajouter en secret GitHub
4. Mettre à jour le workflow pour l'utiliser

Voir `.github/workflows/build.yml` pour les détails.

## Dépannage

### Le build échoue?
- Vérifier les logs dans **Actions** → **Workflow** → Output
- Vérifier que Node.js, Java, et Android SDK sont disponibles
- Vérifier le format du keystore

### L'app ne s'installe pas?
- Vérifier les permissions AndroidManifest.xml
- Vérifier la version minimale d'Android (API 31+)

## Support

Pour plus d'aide:
- Cordova: https://cordova.apache.org/docs/en/latest/
- AdMob: https://admob.google.com/
- GitHub Actions: https://docs.github.com/en/actions
