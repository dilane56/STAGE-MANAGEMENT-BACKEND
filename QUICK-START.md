# 🚀 Déploiement Rapide - Aide-Mémoire

## 📋 Checklist Avant Déploiement

- [ ] Code poussé sur GitHub
- [ ] Toutes les dépendances dans pom.xml
- [ ] Variables d'environnement préparées
- [ ] Service MinIO ou S3 configuré
- [ ] Mot de passe d'application Gmail créé

---

## ⚡ RAILWAY - Déploiement en 5 minutes

### 1️⃣ Créer le projet
```
railway.app → New Project → Deploy from GitHub → Sélectionner repo
```

### 2️⃣ Ajouter PostgreSQL
```
+ New → Database → PostgreSQL
```

### 3️⃣ Variables d'environnement (copier-coller)
```
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=1Gmn7N06rjiLK02f0L+o1rj5IfI6z1UQUn6B+Hokt0Lrs2XhXbdcYIw/fyr5HcQGKpXLXe5oyXeDU5Sv/mgZ6w==
MAIL_USERNAME=kamgaingfotso@gmail.com
MAIL_PASSWORD=behg oeeu xwbj mdqq
MINIO_URL=http://your-minio:9000
MINIO_ACCESS_KEY=admin
MINIO_SECRET_KEY=admin123
MINIO_BUCKET_NAME=stage-management
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

### 4️⃣ Déployer
```
Railway détecte automatiquement → Build démarre → Attendez 5-10 min
```

### 5️⃣ Obtenir l'URL
```
Settings → Networking → Generate Domain
```

---

## ⚡ RENDER - Déploiement en 5 minutes

### 1️⃣ Créer Web Service
```
render.com → New + → Web Service → Connect GitHub repo
```

### 2️⃣ Configuration
```
Name: stage-management-backend
Runtime: Docker
Instance Type: Free
```

### 3️⃣ Ajouter PostgreSQL
```
New + → PostgreSQL → Free → Copier Internal Database URL
```

### 4️⃣ Variables d'environnement
```
DATABASE_URL=<URL PostgreSQL copiée>
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=1Gmn7N06rjiLK02f0L+o1rj5IfI6z1UQUn6B+Hokt0Lrs2XhXbdcYIw/fyr5HcQGKpXLXe5oyXeDU5Sv/mgZ6w==
MAIL_USERNAME=kamgaingfotso@gmail.com
MAIL_PASSWORD=behg oeeu xwbj mdqq
MINIO_URL=http://your-minio:9000
MINIO_ACCESS_KEY=admin
MINIO_SECRET_KEY=admin123
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

### 5️⃣ Déployer
```
Create Web Service → Build démarre → Attendez 10-15 min
```

---

## 🧪 Tests Rapides

### Vérifier que l'API fonctionne:
```bash
# Remplacez YOUR-URL par votre URL Railway/Render

# Test 1: API répond
curl https://YOUR-URL.com/api/auth/login

# Test 2: Swagger UI
Ouvrir: https://YOUR-URL.com/swagger-ui.html

# Test 3: Login
curl -X POST https://YOUR-URL.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}'
```

---

## 🔧 Commandes Utiles

### Build local:
```bash
mvnw clean package -DskipTests
```

### Test avec profil prod en local:
```bash
java -jar -Dspring.profiles.active=prod target/STAGE-MANAGEMENT-BACKEND-0.0.1-SNAPSHOT.jar
```

### Build Docker local:
```bash
docker build -t stage-backend .
docker run -p 8080:8080 stage-backend
```

---

## 🐛 Dépannage Express

### ❌ Build échoue
```
→ Vérifier les logs
→ Vérifier Java 17
→ Vérifier pom.xml
```

### ❌ App ne démarre pas
```
→ Vérifier toutes les variables d'environnement
→ Vérifier DATABASE_URL
→ Vérifier les logs de démarrage
```

### ❌ Erreur base de données
```
→ Ajouter ?sslmode=require à DATABASE_URL (Render)
→ Vérifier que PostgreSQL est démarré
→ Tester la connexion
```

### ❌ Erreur CORS
```
→ Ajouter URL frontend dans CORS_ALLOWED_ORIGINS
→ Format: https://frontend.com,https://app.com
```

---

## 📱 URLs Importantes

### Railway:
- Dashboard: https://railway.app/dashboard
- Docs: https://docs.railway.app

### Render:
- Dashboard: https://dashboard.render.com
- Docs: https://render.com/docs

### Votre API:
- Railway: `https://[nom-projet].up.railway.app`
- Render: `https://[nom-service].onrender.com`
- Swagger: `[URL-API]/swagger-ui.html`

---

## 💡 Conseils Pro

1. **Utilisez Railway pour débuter** (plus simple)
2. **Surveillez les logs** pendant le premier déploiement
3. **Testez localement** avec le profil prod avant de déployer
4. **Changez JWT_SECRET** en production
5. **Configurez CORS** avec l'URL de votre frontend
6. **Sauvegardez** vos variables d'environnement

---

## 📚 Documentation Complète

Pour plus de détails, consultez:
- **DEPLOYMENT.md** - Guide complet étape par étape
- **DEPLOYMENT-SUMMARY.md** - Résumé des modifications
- **.env.example** - Template des variables

---

**Besoin d'aide ?** Consultez DEPLOYMENT.md section "Dépannage" 🆘
