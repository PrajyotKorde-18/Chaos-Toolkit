Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  Stopping Chaos Ecosystem Services..." -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

$ports = @(9000, 8080, 8081, 8082)
$pidsToKill = Get-NetTCPConnection -LocalPort $ports -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique

if ($pidsToKill) {
    foreach ($p in $pidsToKill) {
        try {
            Stop-Process -Id $p -Force -ErrorAction SilentlyContinue
            Write-Host "Killed process with PID: $p" -ForegroundColor Yellow
        } catch {
            Write-Host "Could not kill PID $p : $_" -ForegroundColor Red
        }
    }
} else {
    Write-Host "No active processes found on ports 9000, 8080, 8081, 8082." -ForegroundColor Gray
}

Write-Host "===================================================" -ForegroundColor Green
Write-Host "  All services stopped." -ForegroundColor Green
Write-Host "===================================================" -ForegroundColor Green
