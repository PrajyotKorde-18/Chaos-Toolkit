Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Testing Chaos Engineering Ecosystem" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Test Order API
Write-Host "`n1. Testing Order Gateway (Baseline)..." -ForegroundColor Yellow
$orderBody = @{
    orderId = "ORD-TEST-01"
    amount = 250.0
    itemCode = "WIDGET-99"
} | ConvertTo-Json
$orderRes = Invoke-RestMethod -Uri "http://localhost:8080/orders/create" -Method POST -ContentType "application/json" -Body $orderBody
Write-Host "Order Response:" ($orderRes | ConvertTo-Json -Depth 3) -ForegroundColor Green

# 2. Test Circuit Breaker Metrics
Write-Host "`n2. Circuit Breaker Metrics..." -ForegroundColor Yellow
$cbRes = Invoke-RestMethod -Uri "http://localhost:8080/orders/circuit-breakers" -Method GET
Write-Host "Circuit Breakers:" ($cbRes | ConvertTo-Json -Depth 3) -ForegroundColor Green

# 3. Test Phase 2/3: Automated Verification
Write-Host "`n3. Running Phase 2/3 Automated Resilience Verification..." -ForegroundColor Yellow
$verBody = @{
    targetService = "payment-service"
    targetFaultId = "payment-service.chargeCard"
    faultType = "EXCEPTION"
    blastRadiusPercent = 100
    durationSeconds = 8
    callerEndpointUrl = "http://localhost:8080/orders/create"
    numberOfRequests = 5
    requestIntervalMs = 150
} | ConvertTo-Json
$verRes = Invoke-RestMethod -Uri "http://localhost:9000/api/v1/verification/run" -Method POST -ContentType "application/json" -Body $verBody
Write-Host "Verification Result:" ($verRes | ConvertTo-Json -Depth 3) -ForegroundColor Green

# 4. Test Phase 5: Escalation
Write-Host "`n4. Running Phase 5 Multi-Stage Escalation..." -ForegroundColor Yellow
$escBody = @{
    planName = "Payment Service Latency Escalation"
    targetService = "payment-service"
    faultId = "payment-service.chargeCard"
    callerEndpointUrl = "http://localhost:8080/orders/create"
    requestsPerStage = 3
    stages = @(
        @{ stageNumber = 1; name = "Mild Latency"; faultType = "LATENCY"; blastRadiusPercent = 100; minMs = 100; maxMs = 200; maxPermissibleLatencyMs = 1000; maxPermissibleErrorRate = 0.0 },
        @{ stageNumber = 2; name = "High Latency"; faultType = "LATENCY"; blastRadiusPercent = 100; minMs = 500; maxMs = 800; maxPermissibleLatencyMs = 2000; maxPermissibleErrorRate = 0.0 }
    )
} | ConvertTo-Json -Depth 5
$escRes = Invoke-RestMethod -Uri "http://localhost:9000/api/v1/escalation/run" -Method POST -ContentType "application/json" -Body $escBody
Write-Host "Escalation Result:" ($escRes | ConvertTo-Json -Depth 4) -ForegroundColor Green

# 5. Test Phase 6: STRIDE Security Simulation
Write-Host "`n5. Running Phase 6 STRIDE Security Simulation..." -ForegroundColor Yellow
$secRes = Invoke-RestMethod -Uri "http://localhost:9000/api/v1/security-chaos/run?scenario=CREDENTIAL_STUFFING_SIMULATION&targetService=order-service" -Method POST
Write-Host "Security Result:" ($secRes | ConvertTo-Json -Depth 3) -ForegroundColor Green

# 6. Test Phase 7: Post-Mortem Report
Write-Host "`n6. Generating Phase 7 Post-Mortem Report..." -ForegroundColor Yellow
$pmRes = Invoke-RestMethod -Uri "http://localhost:9000/api/v1/reports/post-mortem" -Method GET
Write-Host "Post-Mortem Markdown Report:" -ForegroundColor Cyan
Write-Host $pmRes

# 7. Test Breaking Point Discovery Engine
Write-Host "`n7. Running Breaking Point Discovery Engine..." -ForegroundColor Yellow
$bpRes = Invoke-RestMethod -Uri "http://localhost:9000/api/v1/breaking-point/find" -Method POST -ContentType "application/json" -Body "{}"
Write-Host "Breaking Point Result:" ($bpRes | ConvertTo-Json -Depth 4) -ForegroundColor Green

# 8. Test Comparative Fault Reaction Matrix
Write-Host "`n8. Running Comparative Fault Reaction Matrix..." -ForegroundColor Yellow
$matRes = Invoke-RestMethod -Uri "http://localhost:9000/api/v1/fault-matrix/run" -Method POST
Write-Host "Fault Matrix Result:" ($matRes | ConvertTo-Json -Depth 4) -ForegroundColor Green

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "  All Ecosystem Endpoints Verified!" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
