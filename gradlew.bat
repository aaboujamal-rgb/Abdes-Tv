@rem Abdes TV Gradle Wrapper Script for Windows
@echo off
setlocal
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set WRAPPER_JAR=%DIRNAME%gradle\wrapper\gradle-wrapper.jar

if exist "%WRAPPER_JAR%" (
    java -Xmx64m -Xms64m -jar "%WRAPPER_JAR%" %*
    exit /b %ERRORLEVEL%
)

where gradle >nul 2>nul
if %ERRORLEVEL% equ 0 (
    gradle %*
    exit /b %ERRORLEVEL%
)

echo Looking for or downloading Gradle 8.7...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ver = '8.7'; $dest = Join-Path $env:USERPROFILE '.gradle\abdestv-bootstrap'; $bin = Join-Path $dest "gradle-$ver\bin\gradle.bat"; if (!(Test-Path $bin)) { New-Item -ItemType Directory -Force -Path $dest | Out-Null; $zip = Join-Path $dest "gradle-$ver-bin.zip"; Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-$ver-bin.zip" -OutFile $zip; Expand-Archive -Path $zip -DestinationPath $dest -Force; Remove-Item $zip }; & $bin $args" %*
exit /b %ERRORLEVEL%
