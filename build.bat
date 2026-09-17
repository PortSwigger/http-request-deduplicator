@echo off
REM UniReq Build Script for Windows
REM Builds the Burp Suite extension JAR file using Maven

echo 🚀 Building UniReq - HTTP Request Deduplicator Extension
echo ==============================================

REM Check if Maven is installed
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ Error: Maven is not installed or not in PATH
    echo Please install Maven 3.6+ and try again
    echo Visit: https://maven.apache.org/install.html
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ Error: Java is not installed or not in PATH
    echo Please install JDK 17+ and try again
    pause
    exit /b 1
)

where javac >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ Error: A Java compiler was not found in PATH
    echo Please install JDK 17+ and set JAVA_HOME to that JDK
    pause
    exit /b 1
)

for /f "tokens=2 delims==" %%v in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr "java.specification.version"') do set "JAVA_MAJOR=%%v"
set "JAVA_MAJOR=%JAVA_MAJOR: =%"
if "%JAVA_MAJOR:~0,2%"=="1." set "JAVA_MAJOR=%JAVA_MAJOR:~2%"
if not defined JAVA_MAJOR (
    echo ❌ Error: Could not determine the installed Java version
    pause
    exit /b 1
)
if %JAVA_MAJOR% LSS 17 (
    echo ❌ Error: Java 17 or higher is required ^(found Java %JAVA_MAJOR%^)
    pause
    exit /b 1
)

echo ✅ Java %JAVA_MAJOR% detected

for /f "tokens=2" %%v in ('javac -version 2^>^&1') do set "JAVAC_MAJOR=%%v"
if "%JAVAC_MAJOR:~0,2%"=="1." set "JAVAC_MAJOR=%JAVAC_MAJOR:~2%"
for /f "tokens=1 delims=." %%v in ("%JAVAC_MAJOR%") do set "JAVAC_MAJOR=%%v"
if not defined JAVAC_MAJOR (
    echo ❌ Error: Could not determine the installed Java compiler version
    pause
    exit /b 1
)
if %JAVAC_MAJOR% LSS 17 (
    echo ❌ Error: JDK 17 or higher is required ^(found javac %JAVAC_MAJOR%^)
    echo Please set JAVA_HOME and PATH to a JDK 17+ installation
    pause
    exit /b 1
)

echo ✅ javac %JAVAC_MAJOR% detected

REM Display Maven version
for /f "tokens=*" %%i in ('mvn -version ^| findstr "Apache Maven"') do echo ✅ %%i

echo.
echo 🔧 Cleaning previous builds...
call mvn clean -q
if %errorlevel% neq 0 (
    echo ❌ Maven clean failed
    pause
    exit /b 1
)

echo 📦 Compiling, testing, and packaging extension...
call mvn verify -q
if %errorlevel% neq 0 (
    echo ❌ Maven build failed
    pause
    exit /b 1
)

REM Locate the versioned build artifact
set "JAR_PATH="
for %%I in (target\unireq-deduplicator-*.jar) do set "JAR_PATH=%%I"

if defined JAR_PATH (
    echo.
    echo 🎉 Build completed successfully!
    echo 📍 Extension JAR location: %JAR_PATH%
    echo.
    echo 📋 Next steps:
    echo 1. Open Burp Suite
    echo 2. Go to Extensions → Installed
    echo 3. Click 'Add' and select the JAR file
    echo 4. Look for the 'UniReq' tab in Burp's interface
    echo.
    for %%I in ("%JAR_PATH%") do echo 📊 File size: %%~zI bytes
) else (
    echo ❌ Build failed - JAR file not found
    pause
    exit /b 1
)

pause
