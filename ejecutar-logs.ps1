$JAVA_HOME = 'C:\Users\andre\.jdk\jdk-25.0.2'
$env:JAVA_HOME = $JAVA_HOME
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "  Ejecutando con Logs Detallados" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# Compilar si es necesario
if (-not (Test-Path "target\ppark-1.0.0.jar")) {
    Write-Host "Compilando proyecto..." -ForegroundColor Yellow
    & "C:\Users\andre\.maven\maven-3.9.15\bin\mvn.cmd" clean package -q
}

$logFile = "app-error-$(Get-Date -Format 'yyyyMMdd-HHmmss').log"
Write-Host "Los logs se guardarán en: $logFile" -ForegroundColor Yellow
Write-Host ""

$javaArgs = @(
    '--module-path', 'target\libs',
    '--add-modules', 'javafx.controls,javafx.fxml,javafx.graphics,javafx.base',
    '--add-opens', 'java.base/java.lang=ALL-UNNAMED',
    '--add-opens', 'java.base/java.util=ALL-UNNAMED',
    '-cp', 'target\ppark-1.0.0.jar;target\libs\*',
    'com.estacionamiento.App'
)

& "$JAVA_HOME\bin\java.exe" $javaArgs 2>&1 | Tee-Object -FilePath $logFile

Write-Host ""
Write-Host "Contenido del log:" -ForegroundColor Yellow
Get-Content $logFile
