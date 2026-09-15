@echo off
echo ===================================================
echo   Starting Chaos Engineering Ecosystem (4 Services)
echo ===================================================

set BASE_DIR=%~dp0
if exist "%BASE_DIR%chaos-toolkit\target" (
    set TK_DIR=%BASE_DIR%chaos-toolkit\
) else (
    set TK_DIR=%BASE_DIR%
)

echo [1/4] Starting Chaos Toolkit Control Plane (Port 9000)...
start "Chaos Toolkit [9000]" cmd /k "cd /d %TK_DIR% && title Chaos Toolkit (9000) && java -jar target\chaos-toolkit-0.0.1-SNAPSHOT.jar"

timeout /t 4 /nobreak >nul

echo [2/4] Starting Payment Service (Port 8081)...
start "Payment Service [8081]" cmd /k "cd /d %TK_DIR%demo-apps\payment-service && title Payment Service (8081) && java -jar target\payment-service-0.0.1-SNAPSHOT.jar"

echo [3/4] Starting Inventory Service (Port 8082)...
start "Inventory Service [8082]" cmd /k "cd /d %TK_DIR%demo-apps\inventory-service && title Inventory Service (8082) && java -jar target\inventory-service-0.0.1-SNAPSHOT.jar"

timeout /t 2 /nobreak >nul

echo [4/4] Starting Order Service Gateway (Port 8080)...
start "Order Service [8080]" cmd /k "cd /d %TK_DIR%demo-apps\order-service && title Order Service (8080) && java -jar target\order-service-0.0.1-SNAPSHOT.jar"

echo.
echo ===================================================
echo   All 4 services are starting in dedicated windows!
echo   Control Center UI: http://localhost:9000
echo   Order API Gateway: http://localhost:8080/orders/create
echo ===================================================
echo To stop all services, run stop-all.bat
pause
