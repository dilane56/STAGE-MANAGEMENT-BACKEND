# Guide de Déploiement - Railway & Render

## 📋 Prérequis

- Compte GitHub avec votre code poussé
- Compte Railway (https://railway.app)
- Compte Render (https://render.com)

---

## 🚂 Déploiement sur RAILWAY

### Étape 1: Créer un nouveau projet
1. Connectez-vous à Railway
2. Cliquez sur "New Project"
3. Sélectionnez "Deploy from GitHub repo"
4. Choisissez votre repository `STAGE-MANAGEMENT-BACKEND`

### Étape 2: Ajouter PostgreSQL
1. Dans votre projet, cliquez sur "+ New"
2. Sélectionnez "Database" > "Add PostgreSQL"
3. Railway créera automatiquement la variable `DATABASE_URL`

### Étape 3: Configurer les variables d'environnement
1. Cliquez sur votre service backend
2. Allez dans "Variables"
3. Ajoutez les variables suivantes:

```
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=<votre-secret-jwt-base64>
JWT_EXPIRATION=86400000
MAIL_USERNAME=kamgaingfotso@gmail.com
MAIL_PASSWORD=<votre-mot-de-passe-application-gmail>
MINIO_URL=http://your-minio-url:9000
MINIO_ACCESS_KEY=your-access-key
MINIO_SECRET_KEY=your-secret-key
MINIO_BUCKET_NAME=stage-management
```

### Étape 4: Déployer
1. Railway détectera automatiquement le Dockerfile
2. Le déploiement démarrera automatiquement
3. Attendez que le build se termine (5-10 minutes)
4. Votre API sera disponible sur l'URL fournie par Railway

### Étape 5: Configurer le domaine (optionnel)
1. Dans "Settings" > "Networking"
2. Cliquez sur "Generate Domain"
3. Votre API sera accessible via: `https://your-app.up.railway.app`

---

## 🎨 Déploiement sur RENDER

### Étape 1: Créer un nouveau Web Service
1. Connectez-vous à Render
2. Cliquez sur "New +"
3. Sélectionnez "Web Service"
4. Connectez votre repository GitHub

### Étape 2: Configuration du service
Remplissez les informations:
- **Name:** stage-management-backend
- **Region:** Choisissez la région la plus proche
- **Branch:** main (ou votre branche principale)
- **Runtime:** Docker
- **Instance Type:** Free (ou payant selon vos besoins)

### Étape 3: Ajouter PostgreSQL
1. Dans le dashboard Render, cliquez sur "New +"
2. Sélectionnez "PostgreSQL"
3. Choisissez le plan (Free pour commencer)
4. Notez l'URL de connexion fournie

### Étape 4: Configurer les variables d'environnement
1. Dans votre Web Service, allez dans "Environment"
2. Ajoutez les variables suivantes:

```
DATABASE_URL=<URL PostgreSQL de Render>
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=<votre-secret-jwt-base64>
JWT_EXPIRATION=86400000
MAIL_USERNAME=kamgaingfotso@gmail.com
MAIL_PASSWORD=<votre-mot-de-passe-application-gmail>
MINIO_URL=http://your-minio-url:9000
MINIO_ACCESS_KEY=your-access-key
MINIO_SECRET_KEY=your-secret-key
MINIO_BUCKET_NAME=stage-management
```

### Étape 5: Déployer
1. Cliquez sur "Create Web Service"
2. Render commencera le build automatiquement
3. Attendez la fin du déploiement (10-15 minutes)
4. Votre API sera disponible sur: `https://your-app.onrender.com`

---

## 🗄️ Configuration MinIO (Stockage de fichiers)

### Option 1: MinIO sur Railway
1. Déployez MinIO comme service séparé sur Railway
2. Utilisez l'URL interne Railway pour MINIO_URL

### Option 2: MinIO Cloud (Recommandé)
1. Créez un compte sur https://min.io/cloud
2. Créez un bucket "stage-management"
3. Utilisez les credentials fournis

### Option 3: AWS S3 (Alternative)
Vous pouvez remplacer MinIO par AWS S3 en modifiant le code

---

## ✅ Vérification du déploiement

### Tester l'API
```bash
# Health check
curl https://your-app-url.com/actuator/health

# Swagger UI
https://your-app-url.com/swagger-ui.html

# Test login
curl -X POST https://your-app-url.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'
```

---

## 🔧 Dépannage

### Problème: Application ne démarre pas
- Vérifiez les logs dans Railway/Render dashboard
- Assurez-vous que toutes les variables d'environnement sont définies
- Vérifiez que DATABASE_URL est correctement formatée

### Problème: Erreur de connexion à la base de données
- Vérifiez que PostgreSQL est bien démarré
- Vérifiez le format de DATABASE_URL: `jdbc:postgresql://host:port/database`
- Pour Render, ajoutez `?sslmode=require` à la fin de l'URL

### Problème: Erreur MinIO
- Vérifiez que MinIO est accessible depuis votre application
- Testez la connexion avec les credentials
- Assurez-vous que le bucket existe

### Problème: Build échoue
- Vérifiez que Java 17 est utilisé
- Assurez-vous que le pom.xml est valide
- Vérifiez les logs de build pour les erreurs spécifiques

---

## 📊 Monitoring

### Railway
- Logs: Onglet "Deployments" > Cliquez sur le déploiement
- Métriques: Onglet "Metrics"

### Render
- Logs: Onglet "Logs" dans votre service
- Métriques: Onglet "Metrics"

---

## 💰 Coûts

### Railway
- Plan gratuit: $5 de crédit/mois
- Suffisant pour développement/test
- Paiement à l'usage après épuisement du crédit

### Render
- Plan gratuit disponible
- Services gratuits s'endorment après 15 min d'inactivité
- Plan payant: $7/mois pour service actif 24/7

---

## 🔐 Sécurité

### Recommandations importantes:
1. ✅ Changez JWT_SECRET en production
2. ✅ Utilisez des mots de passe forts pour la base de données
3. ✅ Ne commitez JAMAIS les fichiers .env
4. ✅ Activez HTTPS (automatique sur Railway/Render)
5. ✅ Limitez l'accès à la base de données
6. ✅ Utilisez des secrets pour les credentials sensibles

---

## 📝 Prochaines étapes

1. Déployez le backend sur Railway OU Render
2. Notez l'URL de votre API
3. Configurez votre frontend Angular pour utiliser cette URL
4. Testez toutes les fonctionnalités
5. Configurez un domaine personnalisé (optionnel)

---

## 🆘 Support

En cas de problème:
- Railway: https://railway.app/help
- Render: https://render.com/docs
- Documentation Spring Boot: https://spring.io/guides

Bon déploiement ! 🚀
