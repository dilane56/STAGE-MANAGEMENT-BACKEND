@echo off
echo ========================================
echo Building STAGE-MANAGEMENT-BACKEND
echo ========================================
call mvnw clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ========================================
echo Build successful!
echo ========================================
echo.
echo To run with production profile locally:
echo java -jar -Dspring.profiles.active=prod target\STAGE-MANAGEMENT-BACKEND-0.0.1-SNAPSHOT.jar
echo.
pause
