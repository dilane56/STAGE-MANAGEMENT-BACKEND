# STAGE-MANAGEMENT-BACKEND

## Description
API REST pour la gestion des stages étudiants. Cette application permet la gestion complète du processus de stages : création d'offres, candidatures, conventions, et communication entre les différents acteurs.

## Technologies
- **Backend:** Spring Boot 3.x, Spring Security, JPA/Hibernate
- **Base de données:** PostgreSQL
- **Stockage fichiers:** MinIO
- **Documentation:** Swagger/OpenAPI
- **Authentification:** JWT

## Rôles utilisateurs
- **ADMIN:** Administrateur système
- **ENTREPRISE:** Entreprises proposant des stages
- **ETUDIANT:** Étudiants cherchant des stages
- **ENSEIGNANT:** Enseignants validant les conventions

---

## API Documentation

### Authentication Endpoints

#### 1. Login
```
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "email": "user@example.com",
      "fullName": "John Doe",
      "telephone": "+1234567890",
      "avatar": "avatar_url",
      "role": "ETUDIANT",
      "createAt": "2024-01-01",
      "updateAt": "2024-01-01",
      "profile": {
        "filiere": "Informatique",
        "anneeScolaire": "2023-2024",
        "niveau": "Master 2",
        "universite": "Université XYZ"
      }
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

#### 2. Refresh Token
```
POST /api/auth/refresh
```

#### 3. Logout
```
POST /api/auth/logout
```

---

### User Management Endpoints

#### 1. Get All Users (Admin only)
```
GET /api/users
```
**Query Parameters:**
- `page`: number (default: 0)
- `size`: number (default: 10)
- `role`: string (ETUDIANT, ENTREPRISE, ENSEIGNANT, ADMIN)
- `search`: string (search by name or email)

#### 2. Get User by ID
```
GET /api/users/{id}
```

#### 3. Create Student
```
POST /api/etudiants
```

#### 4. Create Company
```
POST /api/entreprises
```

#### 5. Create Teacher
```
POST /api/enseignants
```

#### 6. Create Administrator
```
POST /api/administrateurs
```

#### 7. Update Users
```
PUT /api/etudiants/{id}
PUT /api/entreprises/{id}
PUT /api/enseignants/{id}
PUT /api/administrateurs/{id}
```

#### 8. Delete User
```
DELETE /api/users/{id}
```

---

### Contacts Endpoints

#### 1. Get All Contacts
```
GET /api/utilisateurs/contacts
```

#### 2. Get Enterprise Contacts
```
GET /api/utilisateurs/contacts/entreprise
```
**Autorisation:** ENTREPRISE
**Description:** Retourne les étudiants ayant postulé + les admins

#### 3. Get Student Contacts
```
GET /api/utilisateurs/contacts/candidat
```
**Autorisation:** ETUDIANT
**Description:** Retourne les entreprises auxquelles l'étudiant a postulé

---

### Internship Offers Endpoints

#### 1. Get All Offers
```
GET /api/offres-stage
```
**Autorisation:** ADMIN, ETUDIANT, ENSEIGNANT

#### 2. Get Offer by ID
```
GET /api/offres-stage/{id}
```
**Autorisation:** ENTREPRISE, ADMIN, ETUDIANT

#### 3. Create Offer
```
POST /api/offres-stage
```
**Autorisation:** ENTREPRISE, ADMIN

**Request Body:**
```json
{
  "intitule": "Développeur Full Stack",
  "description": "Développement d'applications web",
  "localisation": "Paris",
  "dureeStage": 6,
  "competences": ["Java", "React", "Spring Boot"],
  "datePublication": "2024-01-01",
  "dateLimiteCandidature": "2024-02-01T23:59:59",
  "nombrePlaces": 2,
  "secteurId": 1,
  "dateDebutStage": "2024-03-01"
}
```

#### 4. Update Offer
```
PUT /api/offres-stage/{id}
```
**Autorisation:** ENTREPRISE, ADMIN

#### 5. Delete Offer
```
DELETE /api/offres-stage/{id}
```
**Autorisation:** ADMIN

#### 6. Filter Offers
```
GET /api/offres-stage/filtrer
```
**Query Parameters:**
- `localisation`: string (optional)
- `duree`: integer (optional)
- `secteurNom`: string (optional)

#### 7. Get Offers by Company
```
GET /api/offres-stage/entreprise/{entrepriseId}
```
**Autorisation:** ENTREPRISE, ADMIN

#### 8. Add Competence to Offer
```
POST /api/offres-stage/{id}/competences
```

---

### Applications Endpoints

#### 1. Get All Applications
```
GET /api/candidatures
```
**Autorisation:** ADMIN

#### 2. Get Application by ID
```
GET /api/candidatures/{id}
```
**Autorisation:** ETUDIANT, ADMIN

#### 3. Create Application
```
POST /api/candidatures/ajouter
```
**Autorisation:** ETUDIANT, ADMIN
**Content-Type:** multipart/form-data

**Form Data:**
- `idEtudiant`: Long
- `idOffre`: Long
- `cv`: MultipartFile (PDF)
- `lettreMotivation`: String

#### 4. Update Application
```
PUT /api/candidatures/{id}
```
**Autorisation:** ETUDIANT, ADMIN
**Content-Type:** multipart/form-data

#### 5. Delete Application
```
DELETE /api/candidatures/{id}
```
**Autorisation:** ETUDIANT, ADMIN

#### 6. Get Applications by Student
```
GET /api/candidatures/etudiant/{etudiantId}
```
**Autorisation:** ETUDIANT, ADMIN

#### 7. Get Applications by Company
```
GET /api/candidatures/entreprise/{entrepriseId}
```
**Autorisation:** ENTREPRISE, ADMIN

#### 8. Update Application Status
```
PUT /api/candidatures/{id}/statut
```
**Autorisation:** ENTREPRISE, ADMIN
**Parameters:**
- `statut`: String (EN_ATTENTE, ACCEPTE, REFUSE)
- `message`: String (optional)

#### 9. Download CV
```
GET /api/candidatures/downloadCV/{id}/cv
```

---

### Conventions Endpoints

#### 1. Get All Conventions
```
GET /api/conventions
```
**Autorisation:** ENTREPRISE, ADMIN, ENSEIGNANT

#### 2. Get Convention by ID
```
GET /api/conventions/{id}
```
**Autorisation:** ENTREPRISE, ADMIN, ENSEIGNANT

#### 3. Create Convention
```
POST /api/conventions
```
**Autorisation:** ENTREPRISE, ADMIN

#### 4. Update Convention
```
PUT /api/conventions/{id}
```
**Autorisation:** ENTREPRISE, ADMIN, ENSEIGNANT

#### 5. Delete Convention
```
DELETE /api/conventions/{id}
```
**Autorisation:** ADMIN

#### 6. Get Conventions by Company
```
GET /api/conventions/entreprise/{entrepriseId}
```
**Autorisation:** ENTREPRISE, ADMIN

#### 7. Get Conventions by Teacher
```
GET /api/conventions/enseignant/{enseignantId}
```
**Autorisation:** ENSEIGNANT, ADMIN

#### 8. Validate Convention (Teacher)
```
PUT /api/conventions/{id}/valider-enseignant/{enseignantId}
```
**Autorisation:** ENSEIGNANT

#### 9. Approve Convention (Admin)
```
PUT /api/conventions/{id}/approuver-administrateur/{adminId}
```
**Autorisation:** ADMIN

#### 10. Generate Convention PDF
```
GET /api/conventions/{id}/generate-pdf
```
**Autorisation:** ADMIN, ENSEIGNANT, ENTREPRISE

---

### Sectors Endpoints

#### 1. Get All Sectors
```
GET /api/v1/secteurs
```

#### 2. Get Sector by ID
```
GET /api/v1/secteurs/{id}
```

#### 3. Create Sector
```
POST /api/v1/secteurs
```

#### 4. Update Sector
```
PUT /api/v1/secteurs/{id}
```

#### 5. Delete Sector
```
DELETE /api/v1/secteurs/{id}
```

---

### Statistics Endpoints

#### 1. Dashboard Statistics
```
GET /api/stats/dashboard
```
**Description:** Retourne des statistiques selon le rôle de l'utilisateur connecté

**Response for Student:**
```json
{
  "success": true,
  "data": {
    "availableInternships": 25,
    "myApplications": 5,
    "acceptedApplications": 1,
    "pendingApplications": 3
  }
}
```

**Response for Company:**
```json
{
  "success": true,
  "data": {
    "publishedOffers": 10,
    "receivedApplications": 45,
    "acceptedApplications": 8,
    "pendingApplications": 20
  }
}
```

**Response for Teacher:**
```json
{
  "success": true,
  "data": {
    "totalConventions": 15,
    "pendingConventions": 3,
    "studentsFollowed": 12,
    "completedInternships": 10
  }
}
```

**Response for Admin:**
```json
{
  "success": true,
  "data": {
    "totalUsers": 150,
    "totalCompanies": 25,
    "totalInternships": 50,
    "totalConventions": 30,
    "activeInternships": 35,
    "pendingConventions": 5
  }
}
```

#### 2. Monthly Evolution Statistics
```
GET /api/stats/monthly-evolution
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "month": "Jan",
      "internships": 12,
      "applications": 45
    },
    {
      "month": "Fév", 
      "internships": 18,
      "applications": 67
    }
  ]
}
```

#### 3. Internships by Sector Statistics
```
GET /api/stats/internships-by-sector
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "name": "Informatique",
      "value": 65,
      "color": "#3b82f6"
    },
    {
      "name": "Marketing", 
      "value": 28,
      "color": "#10b981"
    }
  ]
}
```

---

## Token Management

- **Access Token:** 24 heures d'expiration
- **Refresh Token:** 7 jours d'expiration
- **Usage:** Inclure le token d'accès dans l'en-tête Authorization: `Bearer <token>`

---

## Error Responses

### Standard Error Format
```json
{
  "success": false,
  "error": "ERROR_CODE",
  "message": "Description de l'erreur"
}
```

### Common HTTP Status Codes
- **200:** Success
- **201:** Created
- **400:** Bad Request
- **401:** Unauthorized
- **403:** Forbidden
- **404:** Not Found
- **500:** Internal Server Error

---

## Installation et Configuration

### Prérequis
- Java 17+
- PostgreSQL
- MinIO Server

### Variables d'environnement
```properties
port=8080
dbName=stage_management
userName=your_db_username
password=your_db_password
```

### Démarrage
```bash
./mvnw spring-boot:run
```

### Documentation Swagger
Accès à la documentation interactive : `http://localhost:8080/swagger-ui.html`

---

## Contact
Pour toute question ou support, contactez l'équipe de développement.