param(
    [switch]$Debug = $false
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# Detectar Java
$javaExe = $null
$javaHomeCandidates = @()
if ($env:JAVA_HOME) { $javaHomeCandidates += $env:JAVA_HOME }
$javaHomeCandidates += @(
    'C:\Users\andre\.jdk\jdk-25.0.2',
    'C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot',
    'C:\Program Files\Java\jdk-21',
    'C:\Program Files\Java\jdk-17',
    'C:\Users\julie\.jdk',
    'C:\Program Files\Java',
    'C:\Program Files (x86)\Java',
    'C:\Program Files\Apache NetBeans\jdk',
    'C:\Program Files\Apache NetBeans\jdk\bin\javac.exe',
    'C:\Program Files\Android\Android Studio\jbr',
    'C:\Users\julie\AppData\Local\Programs\AdoptOpenJDK',
    'C:\Users\julie\AppData\Local\Programs\Zulu'
)
foreach ($candidateHome in $javaHomeCandidates) {
    if ($candidateHome -and (Test-Path (Join-Path $candidateHome 'bin\java.exe'))) {
        $javaExe = Join-Path $candidateHome 'bin\java.exe'
        break
    }
    if ($candidateHome -and (Test-Path (Join-Path $candidateHome 'java.exe'))) {
        $javaExe = Join-Path $candidateHome 'java.exe'
        break
    }
}
if (-not $javaExe) {
    $command = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($command) { $javaExe = $command.Source }
}
if (-not $javaExe) {
    Write-Host "Error: No se encontro java.exe. Configure JAVA_HOME o agregue Java al PATH." -ForegroundColor Red
    Write-Host "Sugerencia: puede usar una ruta Java instalada, por ejemplo: 'C:\Program Files\Apache NetBeans\jdk'" -ForegroundColor Yellow
    exit 1
}

Write-Host "Usando Java: $javaExe" -ForegroundColor Green
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

    $mavenCmd = $null
    $command = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if (-not $command) { $command = Get-Command mvn -ErrorAction SilentlyContinue }
    if ($command) { $mavenCmd = $command.Source }
    elseif (Test-Path 'C:\Users\andre\.maven\maven-3.9.15\bin\mvn.cmd') { $mavenCmd = 'C:\Users\andre\.maven\maven-3.9.15\bin\mvn.cmd' }
    elseif (Test-Path 'C:\apache-maven-3.9.15-bin\apache-maven-3.9.15\bin\mvn.cmd') { $mavenCmd = 'C:\apache-maven-3.9.15-bin\apache-maven-3.9.15\bin\mvn.cmd' }

    if (-not $mavenCmd) {
        Write-Host "Error: No se encontro Maven. Instale Maven o agregue mvn al PATH." -ForegroundColor Red
        exit 1
    }

    & $mavenCmd clean package -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error en compilacion" -ForegroundColor Red
        exit 1
    }
}
Write-Host "Compilacion exitosa!" -ForegroundColor Green
Write-Host ""

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

& $javaExe $javaArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "La aplicacion finalizo con error (Exit Code: $LASTEXITCODE)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Prueba estos comandos para mas informacion:" -ForegroundColor Yellow
    Write-Host "  1. .\ejecutar-logs.ps1  (Ver logs detallados)" -ForegroundColor Gray
    Write-Host "  2. .\verificar-bd.ps1   (Verificar conexion a BD)" -ForegroundColor Gray
}
