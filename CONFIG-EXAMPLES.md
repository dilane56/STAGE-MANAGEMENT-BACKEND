# 🔧 Exemples de Configuration

## 📝 Variables d'Environnement par Plateforme

### 🚂 Railway

#### Configuration Minimale (Railway fournit DATABASE_URL automatiquement)
```env
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=votre-secret-jwt-securise
MAIL_USERNAME=votre-email@gmail.com
MAIL_PASSWORD=votre-mot-de-passe-app
MINIO_URL=http://votre-minio:9000
MINIO_ACCESS_KEY=votre-access-key
MINIO_SECRET_KEY=votre-secret-key
```

#### Configuration Complète
```env
# Profil Spring
SPRING_PROFILES_ACTIVE=prod

# Base de données (fournie automatiquement par Railway)
# DATABASE_URL est automatiquement injectée

# JWT
JWT_SECRET=1Gmn7N06rjiLK02f0L+o1rj5IfI6z1UQUn6B+Hokt0Lrs2XhXbdcYIw/fyr5HcQGKpXLXe5oyXeDU5Sv/mgZ6w==
JWT_EXPIRATION=86400000

# Email
MAIL_USERNAME=kamgaingfotso@gmail.com
MAIL_PASSWORD=behg oeeu xwbj mdqq

# MinIO
MINIO_URL=http://minio.railway.internal:9000
MINIO_ACCESS_KEY=admin
MINIO_SECRET_KEY=admin123
MINIO_BUCKET_NAME=stage-management

# CORS (ajoutez vos URLs frontend)
CORS_ALLOWED_ORIGINS=https://votre-frontend.vercel.app,https://votre-frontend.netlify.app

# Port (Railway le gère automatiquement)
# PORT est automatiquement injectée
```

---

### 🎨 Render

#### Configuration Minimale
```env
# Base de données (copier depuis Render PostgreSQL)
DATABASE_URL=postgresql://user:password@host:5432/database?sslmode=require

SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=votre-secret-jwt-securise
MAIL_USERNAME=votre-email@gmail.com
MAIL_PASSWORD=votre-mot-de-passe-app
MINIO_URL=http://votre-minio:9000
MINIO_ACCESS_KEY=votre-access-key
MINIO_SECRET_KEY=votre-secret-key
```

#### Configuration Complète
```env
# Base de données (IMPORTANT: ajouter ?sslmode=require)
DATABASE_URL=postgresql://stage_user:password123@dpg-xxxxx.oregon-postgres.render.com/stage_db?sslmode=require

# Profil Spring
SPRING_PROFILES_ACTIVE=prod

# JWT
JWT_SECRET=1Gmn7N06rjiLK02f0L+o1rj5IfI6z1UQUn6B+Hokt0Lrs2XhXbdcYIw/fyr5HcQGKpXLXe5oyXeDU5Sv/mgZ6w==
JWT_EXPIRATION=86400000

# Email
MAIL_USERNAME=kamgaingfotso@gmail.com
MAIL_PASSWORD=behg oeeu xwbj mdqq

# MinIO
MINIO_URL=http://minio-service.onrender.com:9000
MINIO_ACCESS_KEY=admin
MINIO_SECRET_KEY=admin123
MINIO_BUCKET_NAME=stage-management

# CORS
CORS_ALLOWED_ORIGINS=https://votre-frontend.vercel.app,https://votre-frontend.netlify.app

# Port (Render utilise 10000 par défaut)
PORT=10000
```

---

## 🗄️ Configuration MinIO

### Option 1: MinIO sur Railway
```env
# Déployez MinIO comme service séparé sur Railway
MINIO_URL=http://minio.railway.internal:9000
MINIO_ACCESS_KEY=admin
MINIO_SECRET_KEY=admin123
MINIO_BUCKET_NAME=stage-management
```

### Option 2: MinIO Cloud (Recommandé)
```env
# Inscrivez-vous sur https://min.io/cloud
MINIO_URL=https://play.min.io:9000
MINIO_ACCESS_KEY=Q3AM3UQ867SPQQA43P2F
MINIO_SECRET_KEY=zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG
MINIO_BUCKET_NAME=stage-management
```

### Option 3: AWS S3 (Alternative)
```env
# Si vous préférez AWS S3, modifiez le code pour utiliser:
AWS_ACCESS_KEY_ID=votre-access-key
AWS_SECRET_ACCESS_KEY=votre-secret-key
AWS_REGION=eu-west-1
AWS_S3_BUCKET=stage-management
```

---

## 📧 Configuration Email Gmail

### Créer un Mot de Passe d'Application

1. Allez sur https://myaccount.google.com/security
2. Activez la validation en 2 étapes
3. Recherchez "Mots de passe des applications"
4. Créez un nouveau mot de passe pour "Mail"
5. Utilisez ce mot de passe (16 caractères) pour MAIL_PASSWORD

```env
MAIL_USERNAME=votre-email@gmail.com
MAIL_PASSWORD=abcd efgh ijkl mnop  # Mot de passe d'application (sans espaces)
```

---

## 🔐 Génération de JWT Secret Sécurisé

### Méthode 1: OpenSSL (Linux/Mac)
```bash
openssl rand -base64 64
```

### Méthode 2: PowerShell (Windows)
```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
```

### Méthode 3: En ligne
Utilisez: https://generate-secret.vercel.app/64

```env
JWT_SECRET=votre-nouveau-secret-genere-ici
```

---

## 🌐 Configuration CORS

### Développement Local
```env
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:3000,http://localhost:5173
```

### Production (Frontend déployé)
```env
# Vercel
CORS_ALLOWED_ORIGINS=https://votre-app.vercel.app

# Netlify
CORS_ALLOWED_ORIGINS=https://votre-app.netlify.app

# Multiple frontends
CORS_ALLOWED_ORIGINS=https://app1.com,https://app2.com,https://admin.app1.com
```

### Production + Développement
```env
CORS_ALLOWED_ORIGINS=https://votre-app.vercel.app,http://localhost:4200
```

---

## 🔄 Configuration DATABASE_URL

### Format Standard
```
jdbc:postgresql://host:port/database
```

### Railway (automatique)
```env
# Railway injecte automatiquement:
DATABASE_URL=jdbc:postgresql://containers-us-west-xxx.railway.app:5432/railway
```

### Render (manuel avec SSL)
```env
# Copier depuis Render et ajouter ?sslmode=require
DATABASE_URL=postgresql://user:pass@host.oregon-postgres.render.com/db?sslmode=require
```

### Heroku (si utilisé)
```env
# Heroku fournit postgres://, convertir en jdbc:postgresql://
DATABASE_URL=jdbc:postgresql://host:5432/database?sslmode=require
```

### Local
```env
DATABASE_URL=jdbc:postgresql://localhost:5432/stage_management
```

---

## 📊 Configurations par Environnement

### 🧪 Développement Local
```env
# application.properties (déjà configuré)
port=8080
dbName=stage_management
userName=postgres
password=postgres
```

### 🧪 Test/Staging
```env
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://test-db.railway.app:5432/stage_test
JWT_SECRET=test-secret-different-de-prod
MAIL_USERNAME=test@gmail.com
MAIL_PASSWORD=test-app-password
MINIO_URL=http://test-minio:9000
MINIO_ACCESS_KEY=test-access
MINIO_SECRET_KEY=test-secret
CORS_ALLOWED_ORIGINS=https://test-frontend.vercel.app
```

### 🚀 Production
```env
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://prod-db.railway.app:5432/stage_prod
JWT_SECRET=super-secret-production-key-tres-long-et-securise
MAIL_USERNAME=production@gmail.com
MAIL_PASSWORD=prod-app-password
MINIO_URL=https://minio.production.com:9000
MINIO_ACCESS_KEY=prod-access-key
MINIO_SECRET_KEY=prod-secret-key-very-secure
CORS_ALLOWED_ORIGINS=https://app.votredomaine.com
```

---

## 🎯 Checklist de Configuration

### Avant le déploiement:
- [ ] JWT_SECRET changé (différent du développement)
- [ ] Mot de passe d'application Gmail créé
- [ ] MinIO ou S3 configuré et accessible
- [ ] DATABASE_URL correcte (avec ?sslmode=require pour Render)
- [ ] CORS_ALLOWED_ORIGINS contient l'URL du frontend
- [ ] Toutes les variables copiées dans Railway/Render
- [ ] Aucun secret committé dans Git

### Après le déploiement:
- [ ] API répond (test curl)
- [ ] Swagger UI accessible
- [ ] Login fonctionne
- [ ] Upload de fichiers fonctionne
- [ ] Emails envoyés correctement
- [ ] CORS fonctionne avec le frontend

---

## 💡 Conseils de Sécurité

1. **Ne jamais committer** les fichiers .env
2. **Utiliser des secrets différents** pour dev/test/prod
3. **Changer JWT_SECRET** régulièrement en production
4. **Limiter CORS** aux domaines nécessaires uniquement
5. **Utiliser SSL/TLS** pour toutes les connexions
6. **Activer 2FA** sur Gmail pour les mots de passe d'application
7. **Sauvegarder** les variables d'environnement de manière sécurisée

---

## 🆘 Dépannage

### Erreur: "DATABASE_URL not found"
```
→ Vérifier que la variable est bien définie
→ Railway: Vérifier que PostgreSQL est ajouté
→ Render: Copier l'URL depuis le dashboard PostgreSQL
```

### Erreur: "SSL connection required"
```
→ Ajouter ?sslmode=require à DATABASE_URL (Render)
→ Exemple: postgresql://host/db?sslmode=require
```

### Erreur: "CORS policy blocked"
```
→ Ajouter l'URL du frontend dans CORS_ALLOWED_ORIGINS
→ Vérifier le format: https://domain.com (sans slash final)
```

### Erreur: "MinIO connection refused"
```
→ Vérifier que MinIO est accessible
→ Tester l'URL avec curl
→ Vérifier les credentials
```

---

**Besoin d'aide ?** Consultez [DEPLOYMENT.md](DEPLOYMENT.md) pour plus de détails.
