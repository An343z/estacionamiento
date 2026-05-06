@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"
set JAVA_HOME=C:\Users\andre\.jdk\jdk-25.0.2
set PATH=C:\Users\andre\.maven\maven-3.9.15\bin;%PATH%

REM Iniciar servicio MySQL
echo Verificando servicio MySQL...
net start MySQL80 >nul 2>&1
if errorlevel 1 (
    echo Intentando iniciar MySQL con nombre alternativo...
    net start MySQL >nul 2>&1
)
echo Servicio MySQL verificado
echo.

REM Compilar si es necesario
if not exist target\ppark-1.0.0.jar (
    echo Compilando proyecto...
    mvn clean package -q
)

REM Ejecutar con logs de error
echo.
echo ====================================
echo   Iniciando P.PARK - Sistema UI
echo ====================================
echo.

"%JAVA_HOME%\bin\java.exe" ^
  --module-path target\libs ^
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base ^
  --add-opens java.base/java.lang=ALL-UNNAMED ^
  -cp "target\ppark-1.0.0.jar;target\libs\*" ^
  com.estacionamiento.App

if errorlevel 1 (
    echo.
    echo ERROR: La aplicacion genero un error
    echo.
)
pause
