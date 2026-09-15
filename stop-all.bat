@echo off
echo ===================================================
echo   Stopping Chaos Ecosystem Services...
echo ===================================================

powershell -Command "Get-NetTCPConnection -LocalPort 9000,8080,8081,8082 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue; Write-Host 'Stopped process with PID:' $_ }"

echo ===================================================
echo   All Chaos Ecosystem services stopped.
echo ===================================================
pause
