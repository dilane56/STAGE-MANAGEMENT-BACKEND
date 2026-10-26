# Modifications appliquées — STAGE-MANAGEMENT-BACKEND

Audit du 21/10/2026. Ce document recense toutes les corrections et améliorations apportées au backend ; la présentation du dépôt et la documentation de l'API restent dans `README.md`.

## ⚠️ Actions requises de votre part

1. **Révoquer les secrets exposés.** Le fichier `.env.example` et les documents `CONFIG-EXAMPLES.md`, `DEPLOYMENT.md`, `QUICK-START.md` contenaient un **vrai mot de passe d'application Gmail**, le **secret JWT** et une **clé secrète MinIO**. Ils ont été remplacés par des valeurs fictives, mais ils restent dans l'historique Git : il faut **révoquer le mot de passe d'application Gmail**, **générer un nouveau `JWT_SECRET`** (`openssl rand -base64 64`) et **changer la clé MinIO**.
2. **Définir `ADMIN_EMAIL` / `ADMIN_PASSWORD`** en production (le compte `admin@gmail.com / password` n'est plus qu'un repli de développement, signalé par un avertissement au démarrage).
3. **Reconnexion des utilisateurs** : les jetons émis avant cette version ne portent pas le nouveau claim `type` et sont refusés ; chaque utilisateur devra se reconnecter une fois.
4. **Base de données** : `ddl-auto=update` ajoutera automatiquement les colonnes `grade` et `departement` (profil enseignant). Aucune autre migration n'est nécessaire.
5. **Déployer le frontend en même temps** (voir son `MODIFICATIONS-AUDIT.md`) : l'authentification par cookies, le chat temps réel et plusieurs écrans dépendent des nouveaux contrats.
6. **Variables Render** pour les cookies et le chat : voir la section « Authentification par cookies HttpOnly » ci-dessous (déjà déclarées dans `render.yaml`).

## Sécurité

| # | Problème constaté | Correction | Fichiers |
|---|---|---|---|
| B1 | Secrets réels (Gmail, JWT, MinIO) versionnés | Remplacés par des placeholders, `.env.example` documenté | `.env.example`, `CONFIG-EXAMPLES.md`, `DEPLOYMENT.md`, `QUICK-START.md` |
| B2 | Le **refresh token (7 jours) était accepté comme access token** : rien ne distinguait les deux | Claim `type` (`access`/`refresh`) vérifié partout ; logique JWT centralisée ; secret de moins de 256 bits refusé au démarrage | `config/JwtService.java` (nouveau), `config/JwtRequestFillter.java`, `service/auth/AuthService.java` |
| B3 | **IDOR** : n'importe quel utilisateur pouvait lire/modifier les données d'un autre en changeant l'ID dans l'URL (profils, candidatures, offres, conventions, notifications, messages) | Contrôles d'appartenance : `@PreAuthorize("... #id == authentication.principal.id")` dans les contrôleurs et vérifications dans les services via le nouveau `CurrentUserService` | `model/auth/CustomUserDetails.java` (expose `id`/`role`), `service/auth/CurrentUserService.java` (nouveau), contrôleurs et services métier |
| B4 | **WebSocket sans authentification** : connexion anonyme et envoi de messages au nom d'un autre (`expediteurId` fourni par le client) ; l'intercepteur existant n'était même pas enregistré | JWT exigé sur la trame STOMP `CONNECT` ; l'expéditeur est toujours l'utilisateur authentifié ; origines autorisées = `cors.allowed.origins` (au lieu de `*`) | `config/WebSocketAuthInterceptor.java`, `config/WebSocketConfig.java`, `chat/WebSocketController.java`, `chat/MessageController.java` |
| B5 | `POST /api/notifications/send` permettait à tout utilisateur connecté d'envoyer un email arbitraire (relais de spam) | Réservé à `ADMIN` | `controlleur/notification/NotificationRestController.java` |
| B6 | Secteurs (`/api/v1/secteurs`) et ajout de compétence modifiables par n'importe quel rôle | Écriture réservée à `ADMIN` / à l'entreprise propriétaire | `controlleur/SecteurController.java`, `controlleur/OffreStageController.java` |
| B7 | Un étudiant ou une entreprise pouvait créer d'autres comptes (`POST /api/etudiants`, `/api/entreprises`) | Création de comptes réservée à `ADMIN` | `controlleur/EtudiantController.java`, `controlleur/EntrepriseController.java` |
| B8 | La liste des offres exposait à tous les étudiants le nom des autres candidats | Candidatures renvoyées seulement à l'entreprise propriétaire et à l'admin | `mapper/OffreStageMapper.java`, `service/impl/OffreStageServiceImpl.java` |
| B9 | Téléchargement du CV ouvert à tout utilisateur connecté, via une URL lue en base (`new URL(...).openStream()`) | Accès vérifié (étudiant, entreprise concernée, enseignant, admin), lecture directe dans MinIO | `controlleur/CandidatureController.java`, `minio/MinIOService.java` |
| B10 | Upload de CV sans contrôle (type, nom de fichier brut utilisé comme nom d'objet) | PDF uniquement (extension + signature `%PDF`), nom d'objet UUID | `minio/MinIOService.java` |
| B11 | Requête sans jeton → **403** (le frontend ne pouvait pas distinguer « session expirée » de « accès interdit ») | 401 JSON via `authenticationEntryPoint`, 403 JSON pour un accès refusé | `config/SecurityConfig.java` |
| B12 | Filtre JWT (`@Component`) aussi enregistré comme filtre de servlet global ; double configuration CORS (`WebConfig` sans `PATCH`) ; `@EnableGlobalMethodSecurity` déprécié | `FilterRegistrationBean` désactivé, source CORS unique (motifs acceptés, ex. `https://*.vercel.app`), `@EnableMethodSecurity` | `config/SecurityConfig.java`, `config/WebConfig.java` (supprimé) |
| B13 | Compte admin `admin@gmail.com / password` codé en dur | Paramétrable (`ADMIN_EMAIL`, `ADMIN_PASSWORD`), créé seulement si aucun admin n'existe, avertissement si mot de passe par défaut | `config/DefaultUserInitializer.java` |
| B14 | Un admin pouvait supprimer son propre compte ou le dernier admin | Interdit (409) | `service/impl/UtilisateurServiceImpl.java`, `service/impl/AdministrateurServiceImpl.java` |
| B15 | Ancien `ChatController` qui rediffusait n'importe quel message à tous les abonnés de `/topic/messages` | Supprimé (non utilisé par le frontend) | `chat/ChatController.java` (supprimé) |
| B16 | Image Docker exécutée en root | Utilisateur dédié non root, cache des dépendances Maven | `Dockerfile` |

## Bugs fonctionnels corrigés

| # | Problème constaté | Correction | Fichiers |
|---|---|---|---|
| B20 | **Modifier un profil sans saisir de mot de passe provoquait une erreur 500** (BCrypt sur `null`) ; chaque mise à jour remplaçait par `null` les champs non envoyés (ex. filière de l'enseignant perdue) | Mise à jour partielle : seuls les champs fournis sont modifiés, le mot de passe seulement s'il est renseigné | `service/impl/UserAccountHelper.java` (nouveau), `Etudiant/Enseignant/Entreprise/AdministrateurServiceImpl.java` |
| B21 | Email déjà utilisé → 404 ou 500 selon le service | 409 (`ResourceAlreadyExistException`) partout | idem |
| B22 | **Messages privés jamais livrés en temps réel** : sans Principal STOMP, `convertAndSendToUser` ne trouvait aucune session | Principal = ID utilisateur (posé à la connexion) ; `/user` retiré des préfixes du broker | `config/WebSocketAuthInterceptor.java`, `config/WebSocketConfig.java` |
| B23 | `WebSocketEventListener` : statut EN_LIGNE jamais posé à la connexion (attribut de session absent à ce moment) et `NullPointerException` possible à la déconnexion | Basé sur le Principal de la session | `config/WebSocketEventListener.java` |
| B24 | Les **URL de CV stockées en base expiraient au bout de 7 jours** (URL pré-signée) | Stockage du nom d'objet ; URL pré-signée (60 min) régénérée à chaque lecture ; anciennes valeurs compatibles | `minio/MinIOService.java`, `mapper/CandidatureMapper.java` |
| B25 | Erreurs en texte brut alors que le frontend lit `error.message` en JSON ; exceptions non gérées (`IllegalStateException`, `IllegalArgumentException`, intégrité référentielle, taille de fichier…) → 500 | Erreurs au format `{status, error, message, timestamp}` avec le bon code (400/403/404/409/413) ; plus de stack trace renvoyée | `exception/GlobalExceptionHandler.java` |
| B26 | `PUT /api/conventions/{id}` exigeait `?candidature-id=` alors que le frontend envoie `{"idCandidature"}` → 400 | Les deux formats acceptés | `controlleur/ConventionController.java` |
| B27 | Workflow des conventions non contrôlé : validation d'une convention déjà approuvée, approbation sans validation enseignant, validation par un enseignant d'une autre filière | Transitions imposées `EN_ATTENTE → VALIDE → APPROUVE`, filière vérifiée | `service/impl/ConventionServiceImpl.java` |
| B28 | Notifications envoyées à l'utilisateur d'ID `1` supposé admin ; à la création d'une convention, aucun enseignant n'était prévenu | Notification de **tous** les admins ; enseignants de la filière et étudiant prévenus à la création | `service/notification/NotificationService.java`, `ConventionServiceImpl.java`, `OffreStageServiceImpl.java` |
| B29 | Dates de convention basées sur le jour de création | Début = date de début de l'offre (à défaut aujourd'hui), fin = début + durée | `ConventionServiceImpl.java` |
| B30 | Candidature : possible après la date limite, modifiable/supprimable après acceptation, suppression impossible si convention (erreur SQL), statut inconnu → 500 | Date limite vérifiée, modification/retrait seulement si `EN_ATTENTE`, suppression bloquée si convention, statut validé (400) | `service/impl/CandidatureServiceImpl.java` |
| B31 | Offre : `secteurId` absent → 500 ; **la modification d'une offre échouait systématiquement** (secteur non renvoyé au frontend) ; la modification changeait la date de publication | `secteurId` et `entrepriseId` ajoutés à la réponse ; secteur conservé s'il n'est pas fourni ; validations (intitulé, durée, places) ; une entreprise ne publie qu'en son nom | `dto/offreStage/OffreStageResponseDTO.java`, `mapper/OffreStageMapper.java`, `OffreStageServiceImpl.java` |
| B32 | **Export Excel « stages par filière » plantait** (`stagesTermines` jamais renseigné → `NullPointerException`) | Calcul En cours / Terminés / En attente en une seule requête | `service/impl/ReportsServiceImpl.java` |
| B33 | Export « stages par année » : ne trouvait que les conventions créées **le 1er janvier** | Toute l'année ; accepte `/stages/2025/export` et l'ancien `/stages/2025-01-01/export` | `ReportsServiceImpl.java`, `ReportsController.java`, `repository/ConventionStageRepository.java` |
| B34 | Export partenaires : `NullPointerException` si le profil embarqué est vide | Lecture directe sur l'entité | `ReportsServiceImpl.java` |
| B35 | Statistiques : « conventions en attente » de l'enseignant toujours à 0, « offres actives » = toutes les offres | Calculs corrigés (filière de l'enseignant, date limite non dépassée) | `service/impl/StatsServiceImpl.java`, `repository/OffreStageRepository.java` |
| B36 | La messagerie enseignant appelait `/api/utilisateurs/contacts/enseignant/{id}` qui n'existait pas | Endpoint ajouté (étudiants de la filière + admins) ; l'utilisateur courant est exclu de ses contacts | `controlleur/UtilisateurController.java`, `UtilisateurServiceImpl.java`, `UtilisateurRepository.java` |
| B37 | `DELETE` secteur/convention renvoyaient une chaîne brute que le frontend lisait en JSON | Réponse `{"message": ...}` ; suppression d'un secteur utilisé → 409 explicite ; nom de secteur unique | `SecteurController.java`, `SecteurServiceImpl.java`, `ConventionController.java` |
| B38 | Profil enseignant : filière, grade et département absents du `profile` renvoyé | Ajoutés à `Profile` et synchronisés | `model/embeded/Profile.java`, `EnseignantServiceImpl.java` |
| B39 | Login : `derniereConnexion` jamais mise à jour, `profile` toujours vide dans la réponse | Corrigé | `AuthService.java` |
| B40 | `/api/auth/logout` derrière l'authentification (échouait avec un jeton expiré) | Endpoint public (jetons sans état) | `SecurityConfig.java`, `JwtRequestFillter.java` |
| B41 | Chaque ouverture du chat créait une notification « Votre statut a été mis à jour » | Supprimé | `chat/WebSocketController.java` |

## Qualité / maintenabilité

- **B50** — Les services appelaient un **contrôleur REST** (`NotificationController.sendNotification`) : l'envoi (base + push STOMP sur `/user/queue/notifications`) est déplacé dans `NotificationService`. Un échec SMTP ne fait plus échouer l'action métier.
- **B51** — `@ToString.Exclude` / `@EqualsAndHashCode.Exclude` sur les collections bidirectionnelles (`Entreprise.offres`, `OffreStage.candidatures`) : évite une récursion infinie avec Lombok `@Data`.
- **B52** — `PasswordEncoder` injecté (au lieu de `new BCryptPasswordEncoder()` à chaque appel) ; code mort supprimé (`AuthService.authenticateUser`, `getUserRole`, `uploadImage`, `FileUploadConfig` qui fixait 10 Mo en conflit avec les 5 Mo des properties).
- **B53** — `System.out/err.println` remplacés par SLF4J ; comptage des notifications non lues en base (`countByDestinataireIdAndLuFalse`) au lieu d'un chargement en mémoire.
- **B54** — Configuration : `server.port` par défaut `9001` (port attendu par le frontend) ; hôte/port PostgreSQL, MinIO, CORS, durées des jetons et compte admin paramétrables par variables d'environnement ; `jwt.expiration.milliseconds` (inutilisé) remplacé par `jwt.access-token.expiration-ms` / `jwt.refresh-token.expiration-ms` ; Swagger désactivable en prod (`SWAGGER_ENABLED=false`).

## Authentification par cookies HttpOnly (seconde passe)

Objectif : les jetons ne sont plus lisibles par le JavaScript du frontend (protection contre leur vol en cas de faille XSS). Le frontend passe par un proxy Vercel (voir son `MODIFICATIONS-AUDIT.md`), ce qui rend les cookies « même site » quelle que soit l'URL Vercel et évite le blocage des cookies tiers par Safari / iOS.

| # | Changement | Fichiers |
|---|---|---|
| B60 | **Cookies `HttpOnly`** posés au login : `access_token` (15 min, chemin `/api`) et `refresh_token` (7 jours, chemin `/api/auth` uniquement). Pas d'attribut `Domain` : le cookie appartient à l'hôte qui répond (le domaine du frontend derrière le proxy), aucune configuration de domaine n'est donc nécessaire. `Secure` et `SameSite` configurables (`COOKIE_SECURE`, `COOKIE_SAME_SITE`) | `config/AuthCookieService.java` (nouveau) |
| B61 | Le login ne renvoie plus les jetons dans le JSON (seulement le profil). Le refresh lit le cookie (le champ JSON `refreshToken` reste accepté pour les outils) et renouvelle le cookie d'accès ; en cas d'échec, les cookies sont supprimés. Le logout supprime les cookies. `app.auth.tokens-in-body=true` (défaut en local uniquement) renvoie aussi les jetons pour tester avec Swagger/Postman | `controlleur/auth/AuthController.java`, `service/auth/AuthService.java` |
| B62 | Le filtre JWT lit le jeton dans l'en-tête `Authorization` (Swagger, outils) **ou** dans le cookie `access_token` | `config/JwtRequestFillter.java` |
| B63 | **Protection CSRF** : toute requête authentifiée par cookie qui modifie des données (POST, PUT, PATCH, DELETE) doit porter l'en-tête `X-Requested-With`, sinon 403 `CSRF_HEADER_MISSING`. Un site tiers ne peut pas ajouter cet en-tête sans autorisation CORS ; combiné à `SameSite=Lax`, cela bloque les requêtes forgées | `config/JwtRequestFillter.java` |
| B64 | **Ticket WebSocket** : `POST /api/auth/ws-ticket` (authentifié) renvoie un jeton de type `ws` valable 30 s et **à usage unique** (identifiant `jti` mémorisé). Le chat se connecte directement à Render (le proxy Vercel ne relaie pas les WebSockets) et envoie ce ticket dans la trame STOMP `CONNECT`. Un access token reste accepté pour les outils de test | `config/JwtService.java`, `config/WebSocketAuthInterceptor.java`, `AuthController.java` |
| B65 | `render.yaml` : `COOKIE_SECURE=true`, `COOKIE_SAME_SITE=Lax`, `CORS_ALLOWED_ORIGINS=https://*.vercel.app` (connexion directe du chat), et déclaration des secrets `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` à saisir dans le tableau de bord | `render.yaml` |

Configuration Render (variables) :

| Variable | Valeur |
|---|---|
| `COOKIE_SECURE` | `true` (HTTPS) |
| `COOKIE_SAME_SITE` | `Lax` |
| `CORS_ALLOWED_ORIGINS` | `https://*.vercel.app`, ou plus restrictif : `https://nom-de-votre-projet*.vercel.app` |
| `AUTH_TOKENS_IN_BODY` | ne pas définir (vaut `false` en production) |

> Avec le proxy, les appels REST n'ont plus besoin de CORS (pour le navigateur, ils sont sur le domaine du frontend) ; CORS ne sert plus qu'à la connexion directe du chat. Autoriser `*.vercel.app` ne permet pas à un autre site Vercel d'utiliser le chat : il lui faudrait un ticket, qui n'est délivré qu'avec le cookie de session de votre propre domaine.

## Tests ajoutés

| Test | Ce qui est vérifié |
|---|---|
| `config/JwtServiceTest` (6 tests) | access/refresh non interchangeables, signature, expiration, longueur minimale du secret |
| `minio/MinIOServiceTest` (4 tests) | compatibilité des anciennes URL stockées, refus des fichiers non PDF ou vides |
| `controlleur/EtudiantControllerSecurityTest` (5 tests) | 401 sans jeton, un étudiant ne lit que son profil, l'admin lit tout, une entreprise ne liste pas les étudiants |
| `config/CookieAuthenticationTest` (5 tests) | authentification par cookie avec de vrais jetons signés, refresh token refusé dans le cookie d'accès, écriture sans `X-Requested-With` refusée (403), acceptée avec l'en-tête, en-tête `Authorization` toujours accepté |
| `controlleur/auth/AuthControllerCookieTest` (3 tests) | le login pose deux cookies `HttpOnly` (chemins `/api` et `/api/auth`, `SameSite=Lax`) sans jeton dans le corps JSON, le logout les supprime, refresh sans cookie → 401 |

```bash
mvn test -Dtest='JwtServiceTest,MinIOServiceTest,EtudiantControllerSecurityTest,CookieAuthenticationTest,AuthControllerCookieTest'
```

Résultat : **23 tests, 0 échec**. Le test existant `StageManagementBackendApplicationTests.contextLoads` nécessite toujours une base PostgreSQL et MinIO accessibles.
L'application complète n'a pas pu être démarrée pendant l'audit (pas de PostgreSQL disponible) : prévoir un test manuel des parcours avant mise en production.

## Changements de contrat d'API

| Endpoint | Avant | Après |
|---|---|---|
| Toute erreur | texte brut | JSON `{status, error, message, timestamp}` |
| Requête sans jeton | 403 | 401 |
| WebSocket `/ws-chat` | connexion anonyme, abonnement `/user/{id}/queue/messages` | en-tête STOMP `Authorization: Bearer <ticket ws>` obligatoire (ticket via `POST /api/auth/ws-ticket`), abonnements `/user/queue/messages` et `/user/queue/errors` |
| `POST /api/auth/login` | jetons dans `data.token` / `data.refreshToken` | jetons dans des cookies `HttpOnly` ; corps : `data.user` uniquement (en production) |
| `POST /api/auth/refresh` | `{"refreshToken"}` dans le corps → `{"token"}` | cookie `refresh_token` → nouveau cookie `access_token`, corps `{"success": true}` |
| Requêtes authentifiées par cookie (POST/PUT/PATCH/DELETE) | — | en-tête `X-Requested-With` obligatoire |
| `POST /api/auth/ws-ticket` | inexistant | nouveau |
| `/app/chat.send` | `expediteurId` pris dans le message | ignoré : l'expéditeur est l'utilisateur connecté |
| `POST /api/etudiants`, `POST /api/entreprises` | ETUDIANT/ENTREPRISE ou ADMIN | ADMIN uniquement |
| `DELETE /api/conventions/{id}` | ADMIN, réponse texte | ADMIN ou entreprise propriétaire (convention en attente), réponse `{"message"}` |
| `DELETE /api/v1/secteurs/{id}` | tout rôle, réponse texte | ADMIN, réponse `{"message"}` |
| `GET /api/utilisateurs/contacts` | tout utilisateur connecté | ADMIN |
| `GET /api/utilisateurs/contacts/enseignant/{id}` | inexistant | nouveau |
| `POST /api/notifications/send` | tout utilisateur, paramètres de formulaire | ADMIN, corps JSON |
| `GET /api/reports/stages/{year}/export` | date exacte (`LocalDate`) | année (`2025`) ou ancien format `2025-01-01` |
| `OffreStageResponseDTO` | — | + `secteurId`, `entrepriseId` ; `candidatures` vide pour les non-propriétaires |
| `CandidatureResponseDTO.cheminFichier` | URL stockée (expirait après 7 jours) | URL pré-signée régénérée (60 min) |

## Recommandations non appliquées (à planifier)

- `ConventionStage.statutConvention` et `Utilisateur.status` sont stockés en **ordinal** (pas de `@Enumerated(EnumType.STRING)`) : réordonner l'enum corromprait les données. À migrer avec un script SQL dédié.
- Remplacer `ddl-auto=update` par des migrations versionnées (Flyway/Liquibase) en production.
- Ajouter la pagination sur les listes (utilisateurs, offres, candidatures) et une limitation de débit sur `/api/auth/login`.
- Ajouter un test d'intégration avec Testcontainers (PostgreSQL + MinIO) pour remplacer `contextLoads`.
