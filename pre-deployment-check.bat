@echo off
echo ========================================
echo Pre-Deployment Checklist
echo ========================================
echo.

echo [1/7] Checking Java version...
java -version 2>&1 | findstr "17"
if %ERRORLEVEL% EQU 0 (
    echo [OK] Java 17 detected
) else (
    echo [WARNING] Java 17 not detected. Please install Java 17.
)
echo.

echo [2/7] Checking Maven...
call mvnw -version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Maven wrapper found
) else (
    echo [ERROR] Maven wrapper not found
)
echo.

echo [3/7] Checking required files...
if exist "Dockerfile" (
    echo [OK] Dockerfile found
) else (
    echo [ERROR] Dockerfile not found
)

if exist "railway.json" (
    echo [OK] railway.json found
) else (
    echo [ERROR] railway.json not found
)

if exist "render.yaml" (
    echo [OK] render.yaml found
) else (
    echo [ERROR] render.yaml not found
)

if exist "src\main\resources\application-prod.properties" (
    echo [OK] application-prod.properties found
) else (
    echo [ERROR] application-prod.properties not found
)
echo.

echo [4/7] Checking .gitignore...
findstr /C:".env" .gitignore >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] .env in .gitignore
) else (
    echo [WARNING] .env not in .gitignore
)
echo.

echo [5/7] Testing build...
echo This may take a few minutes...
call mvnw clean package -DskipTests >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Build successful
) else (
    echo [ERROR] Build failed. Run 'mvnw clean package' to see errors.
)
echo.

echo [6/7] Checking if JAR was created...
if exist "target\STAGE-MANAGEMENT-BACKEND-0.0.1-SNAPSHOT.jar" (
    echo [OK] JAR file created
) else (
    echo [ERROR] JAR file not found
)
echo.

echo [7/7] Checking Git status...
git status >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Git repository detected
    echo.
    echo Uncommitted changes:
    git status --short
) else (
    echo [WARNING] Not a Git repository
)
echo.

echo ========================================
echo Checklist Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Review any [ERROR] or [WARNING] messages above
echo 2. Commit and push your code to GitHub
echo 3. Follow QUICK-START.md for deployment
echo.
echo Environment variables to configure:
echo - DATABASE_URL (provided by Railway/Render)
echo - JWT_SECRET
echo - MAIL_USERNAME
echo - MAIL_PASSWORD
echo - MINIO_URL
echo - MINIO_ACCESS_KEY
echo - MINIO_SECRET_KEY
echo - CORS_ALLOWED_ORIGINS
echo.
echo See .env.example for details
echo.
pause
