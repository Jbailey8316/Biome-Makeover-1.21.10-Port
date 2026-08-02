@echo off
setlocal enabledelayedexpansion
where java >nul 2>nul || (echo Java was not found in PATH.& exit /b 1)
java -version
call gradlew.bat --no-daemon clean build
if errorlevel 1 exit /b %errorlevel%

echo.
echo Verifying playable JAR contents...
set "PLAYABLE_JAR="
for %%F in (build\libs\*.jar) do (
    echo %%~nxF | findstr /I /C:"-sources.jar" >nul
    if errorlevel 1 set "PLAYABLE_JAR=%%F"
)
if not defined PLAYABLE_JAR (
    echo ERROR: No playable JAR found in build\libs\
    exit /b 2
)

jar tf "!PLAYABLE_JAR!" | findstr /C:"party/lemons/biomemakeover/BiomeMakeover.class" >nul
if errorlevel 1 (
    echo ERROR: Main entrypoint class is missing from !PLAYABLE_JAR!
    exit /b 3
)
jar tf "!PLAYABLE_JAR!" | findstr /C:"party/lemons/biomemakeover/client/BiomeMakeoverClient.class" >nul
if errorlevel 1 (
    echo ERROR: Client entrypoint class is missing from !PLAYABLE_JAR!
    exit /b 4
)
jar tf "!PLAYABLE_JAR!" | findstr /C:"fabric.mod.json" >nul
if errorlevel 1 (
    echo ERROR: fabric.mod.json is missing from !PLAYABLE_JAR!
    exit /b 5
)

echo VERIFIED PLAYABLE JAR: !PLAYABLE_JAR!
echo Build complete. Install exactly that file in Prism Launcher.
