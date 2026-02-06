# 📦 Commandes Git pour le Déploiement

## 🔍 Vérifier l'état actuel

```bash
# Voir les fichiers modifiés
git status

# Voir les différences
git diff
```

---

## ✅ Commiter les changements

### Option 1: Tout commiter en une fois
```bash
# Ajouter tous les nouveaux fichiers
git add .

# Commiter avec un message
git commit -m "Configure deployment for Railway and Render"

# Pousser vers GitHub
git push origin main
```

### Option 2: Commiter par étapes
```bash
# Ajouter les fichiers de configuration
git add Dockerfile railway.json render.yaml system.properties .dockerignore

# Ajouter les fichiers de documentation
git add DEPLOYMENT.md DEPLOYMENT-SUMMARY.md QUICK-START.md CONFIG-EXAMPLES.md

# Ajouter les fichiers de configuration Spring
git add src/main/resources/application-prod.properties
git add src/main/java/org/kfokam48/stagemanagementbackend/config/WebConfig.java

# Ajouter les autres fichiers
git add .env.example .gitignore README.md build.bat pre-deployment-check.bat

# Commiter
git commit -m "Configure deployment for Railway and Render

- Add Dockerfile for containerized deployment
- Add Railway and Render configuration files
- Add production profile with environment variables
- Update CORS configuration for production
- Add comprehensive deployment documentation
- Add pre-deployment checklist script"

# Pousser vers GitHub
git push origin main
```

---

## 🌿 Si vous travaillez sur une branche

```bash
# Créer une nouvelle branche
git checkout -b deployment-config

# Ajouter et commiter les changements
git add .
git commit -m "Configure deployment for Railway and Render"

# Pousser la branche
git push origin deployment-config

# Ensuite, créez une Pull Request sur GitHub
# Puis mergez dans main
```

---

## 🔄 Mettre à jour après modifications

```bash
# Après avoir modifié des fichiers
git add .
git commit -m "Update deployment configuration"
git push origin main
```

---

## 📋 Vérifications avant de pousser

### 1. Vérifier que les secrets ne sont pas committé
```bash
# Vérifier qu'aucun fichier .env n'est tracké
git status | findstr ".env"

# Si .env apparaît, l'enlever:
git rm --cached .env
git commit -m "Remove .env from tracking"
```

### 2. Vérifier les fichiers à commiter
```bash
# Lister tous les fichiers qui seront committé
git diff --cached --name-only
```

### 3. Vérifier le .gitignore
```bash
# S'assurer que .env est ignoré
type .gitignore | findstr ".env"
```

---

## 🚀 Workflow complet de déploiement

```bash
# 1. Vérifier l'état
git status

# 2. Tester le build localement
mvnw clean package -DskipTests

# 3. Ajouter tous les changements
git add .

# 4. Vérifier ce qui sera committé
git status

# 5. Commiter
git commit -m "Configure deployment for Railway and Render"

# 6. Pousser vers GitHub
git push origin main

# 7. Aller sur Railway ou Render
# 8. Connecter le repository
# 9. Configurer les variables d'environnement
# 10. Déployer !
```

---

## 🔧 Commandes utiles

### Voir l'historique des commits
```bash
git log --oneline
```

### Annuler le dernier commit (avant push)
```bash
git reset --soft HEAD~1
```

### Voir les fichiers dans le dernier commit
```bash
git show --name-only
```

### Créer un tag pour la version
```bash
git tag -a v1.0.0 -m "Version 1.0.0 - Ready for deployment"
git push origin v1.0.0
```

---

## 📝 Messages de commit suggérés

### Pour le commit initial de déploiement:
```
Configure deployment for Railway and Render

- Add Docker support with multi-stage build
- Add Railway and Render configuration files
- Create production profile with environment variables
- Update CORS configuration for production
- Add comprehensive deployment documentation
- Add configuration examples and quick start guide
- Add pre-deployment checklist
```

### Pour des mises à jour:
```
Update deployment configuration

- Fix DATABASE_URL format for Render
- Update CORS origins
- Improve documentation
```

```
Add MinIO configuration examples

- Add MinIO Cloud setup instructions
- Add AWS S3 alternative configuration
```

---

## ⚠️ Erreurs courantes et solutions

### Erreur: "fatal: not a git repository"
```bash
# Initialiser Git
git init
git add .
git commit -m "Initial commit"

# Ajouter le remote
git remote add origin https://github.com/votre-username/votre-repo.git
git push -u origin main
```

### Erreur: "rejected - non-fast-forward"
```bash
# Récupérer les changements distants
git pull origin main --rebase

# Puis pousser
git push origin main
```

### Erreur: "large files"
```bash
# Vérifier la taille des fichiers
git ls-files -z | xargs -0 du -h | sort -h

# Supprimer les gros fichiers du tracking
git rm --cached chemin/vers/gros-fichier
```

---

## 🎯 Checklist finale avant push

- [ ] Build réussi localement
- [ ] Aucun fichier .env committé
- [ ] .gitignore à jour
- [ ] Documentation à jour
- [ ] Pas de secrets dans le code
- [ ] Tests passent (si applicable)
- [ ] Message de commit descriptif

---

## 📚 Ressources

- Git Documentation: https://git-scm.com/doc
- GitHub Guides: https://guides.github.com
- Railway Docs: https://docs.railway.app
- Render Docs: https://render.com/docs

---

**Prêt à déployer ?** Suivez [QUICK-START.md](QUICK-START.md) ! 🚀
