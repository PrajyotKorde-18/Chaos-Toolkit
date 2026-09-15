$root = $PSScriptRoot
if (-not $root) { $root = Get-Location }
$logsDir = Join-Path $root "logs"
if (-not (Test-Path $logsDir)) { New-Item -ItemType Directory -Path $logsDir | Out-Null }

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  Starting Chaos Engineering Ecosystem (4 Services)" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

# Resolve base chaos-toolkit directory
$tkDir = $root
if (Test-Path (Join-Path $root "chaos-toolkit")) {
    $tkDir = Join-Path $root "chaos-toolkit"
}

# 1. Chaos Toolkit
Write-Host "[1/4] Starting Chaos Toolkit Control Plane (Port 9000)..." -ForegroundColor Yellow
$tkJar = Join-Path $tkDir "target\chaos-toolkit-0.0.1-SNAPSHOT.jar"
$tkOut = Join-Path $logsDir "chaos-toolkit.log"
$tkErr = Join-Path $logsDir "chaos-toolkit-error.log"
Start-Process -FilePath "java" -ArgumentList "-jar", "`"$tkJar`"" -WorkingDirectory $tkDir -RedirectStandardOutput $tkOut -RedirectStandardError $tkErr

Start-Sleep -Seconds 5

# 2. Payment Service
Write-Host "[2/4] Starting Payment Service (Port 8081)..." -ForegroundColor Yellow
$payDir = Join-Path $tkDir "demo-apps\payment-service"
$payJar = Join-Path $payDir "target\payment-service-0.0.1-SNAPSHOT.jar"
$payOut = Join-Path $logsDir "payment-service.log"
$payErr = Join-Path $logsDir "payment-service-error.log"
Start-Process -FilePath "java" -ArgumentList "-jar", "`"$payJar`"" -WorkingDirectory $payDir -RedirectStandardOutput $payOut -RedirectStandardError $payErr

# 3. Inventory Service
Write-Host "[3/4] Starting Inventory Service (Port 8082)..." -ForegroundColor Yellow
$invDir = Join-Path $tkDir "demo-apps\inventory-service"
$invJar = Join-Path $invDir "target\inventory-service-0.0.1-SNAPSHOT.jar"
$invOut = Join-Path $logsDir "inventory-service.log"
$invErr = Join-Path $logsDir "inventory-service-error.log"
Start-Process -FilePath "java" -ArgumentList "-jar", "`"$invJar`"" -WorkingDirectory $invDir -RedirectStandardOutput $invOut -RedirectStandardError $invErr

Start-Sleep -Seconds 3

# 4. Order Service
Write-Host "[4/4] Starting Order Service Gateway (Port 8080)..." -ForegroundColor Yellow
$ordDir = Join-Path $tkDir "demo-apps\order-service"
$ordJar = Join-Path $ordDir "target\order-service-0.0.1-SNAPSHOT.jar"
$ordOut = Join-Path $logsDir "order-service.log"
$ordErr = Join-Path $logsDir "order-service-error.log"
Start-Process -FilePath "java" -ArgumentList "-jar", "`"$ordJar`"" -WorkingDirectory $ordDir -RedirectStandardOutput $ordOut -RedirectStandardError $ordErr

Start-Sleep -Seconds 6

Write-Host ""
Write-Host "===================================================" -ForegroundColor Green
Write-Host "  All 4 services launched!" -ForegroundColor Green
Write-Host "  Logs: $logsDir" -ForegroundColor Green
Write-Host "  Control Center Dashboard: http://localhost:9000" -ForegroundColor Green
Write-Host "  Order API Gateway:        http://localhost:8080/orders/create" -ForegroundColor Green
Write-Host "===================================================" -ForegroundColor Green
Write-Host "Run .\stop-all.ps1 to terminate all background services."
