$JAVA_HOME = 'C:\Users\andre\.jdk\jdk-25.0.2'
$env:JAVA_HOME = $JAVA_HOME
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "  Verificando conexión a Base de Datos" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# Compilar si es necesario
if (-not (Test-Path "target\ppark-1.0.0.jar")) {
    Write-Host "Compilando proyecto..." -ForegroundColor Yellow
    & "C:\Users\andre\.maven\maven-3.9.15\bin\mvn.cmd" clean package -q
}

Write-Host "Ejecutando verificación de conexión..." -ForegroundColor Green
Write-Host ""

$javaArgs = @(
    '-cp', 'target\ppark-1.0.0.jar;target\libs\*',
    'com.estacionamiento.Main'
)

& "$JAVA_HOME\bin\java.exe" $javaArgs

Write-Host ""
if ($LASTEXITCODE -eq 0) {
    Write-Host "Conexión exitosa! La base de datos está disponible." -ForegroundColor Green
    Write-Host ""
    Write-Host "Ahora puedes ejecutar la interfaz gráfica con:" -ForegroundColor Yellow
    Write-Host "  .\ejecutar-ui.ps1" -ForegroundColor Cyan
} else {
    Write-Host "Error de conexión a la base de datos" -ForegroundColor Red
    Write-Host ""
    Write-Host "Verifica que:" -ForegroundColor Yellow
    Write-Host "  1. MySQL esté corriendo en localhost:3306" -ForegroundColor Gray
    Write-Host "  2. La base de datos 'estacionamiento' exista" -ForegroundColor Gray
    Write-Host "  3. Las credenciales en ConexionDB.java sean correctas" -ForegroundColor Gray
}
