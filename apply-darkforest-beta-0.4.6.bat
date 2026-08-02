@echo off
xcopy /E /I /Y src src >nul
if errorlevel 1 exit /b 1
echo Applied DarkForest Beta 0.4.6
