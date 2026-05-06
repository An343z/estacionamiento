param(
    [switch]$Debug = $false
)

$JAVA_HOME = 'C:\Users\andre\.jdk\jdk-25.0.2'
$env:JAVA_HOME = $JAVA_HOME
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "  P PARK - Sistema de Gestion de Estacionamiento" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# Iniciar servicio MySQL
Write-Host "Verificando servicio MySQL..." -ForegroundColor Yellow
$mysqlService = Get-Service -Name "MySQL80" -ErrorAction SilentlyContinue
if ($null -eq $mysqlService) {
    $mysqlService = Get-Service -Name "MySQL" -ErrorAction SilentlyContinue
}

if ($null -ne $mysqlService) {
    if ($mysqlService.Status -ne "Running") {
        Write-Host "Iniciando servicio MySQL..." -ForegroundColor Yellow
        Start-Service -Name $mysqlService.Name -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
    }
    Write-Host "Servicio MySQL verificado OK" -ForegroundColor Green
}
else {
    Write-Host "Advertencia: No se encontro servicio MySQL instalado" -ForegroundColor Yellow
}
Write-Host ""

# Compilar si no existe el JAR
if (-not (Test-Path "target\ppark-1.0.0.jar")) {
    Write-Host "Compilando proyecto..." -ForegroundColor Yellow
    & "C:\Users\andre\.maven\maven-3.9.15\bin\mvn.cmd" clean package -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error en compilacion" -ForegroundColor Red
        exit 1
    }
}

Write-Host "Iniciando aplicacion..." -ForegroundColor Green
Write-Host ""

$javaArgs = @(
    '--module-path', 'target\libs',
    '--add-modules', 'javafx.controls,javafx.fxml,javafx.graphics,javafx.base',
    '--add-opens', 'java.base/java.lang=ALL-UNNAMED',
    '--add-opens', 'java.base/java.util=ALL-UNNAMED',
    '-cp', 'target\ppark-1.0.0.jar;target\libs\*',
    'com.estacionamiento.App'
)

if ($Debug) {
    Write-Host "Argumentos: $($javaArgs -join ' ')" -ForegroundColor Gray
}

& "$JAVA_HOME\bin\java.exe" $javaArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "La aplicacion finalizo con error (Exit Code: $LASTEXITCODE)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Prueba estos comandos para mas informacion:" -ForegroundColor Yellow
    Write-Host "  1. .\ejecutar-logs.ps1  (Ver logs detallados)" -ForegroundColor Gray
    Write-Host "  2. .\verificar-bd.ps1   (Verificar conexion a BD)" -ForegroundColor Gray
}
