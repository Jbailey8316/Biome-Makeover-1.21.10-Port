@echo off
xcopy /E /I /Y src\* .\src\ >nul
if errorlevel 1 exit /b 1
echo Dark Forest Beta 0.4.3 applied.
