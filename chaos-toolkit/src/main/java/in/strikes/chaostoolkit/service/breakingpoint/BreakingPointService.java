package in.strikes.chaostoolkit.service.breakingpoint;

import in.strikes.chaostoolkit.model.FaultConfig;
import in.strikes.chaostoolkit.model.FaultType;
import in.strikes.chaostoolkit.model.breakingpoint.BreakingPointReport;
import in.strikes.chaostoolkit.model.breakingpoint.BreakingPointRequest;
import in.strikes.chaostoolkit.model.breakingpoint.BreakingPointStep;
import in.strikes.chaostoolkit.service.FaultStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.*;

@Service
public class BreakingPointService {

    private static final Logger log = LoggerFactory.getLogger(BreakingPointService.class);

    private final FaultStore faultStore;
    private final RestClient restClient;

    public BreakingPointService(FaultStore faultStore) {
        this.faultStore = faultStore;
        this.restClient = RestClient.builder().build();
    }

    public BreakingPointReport findBreakingPoint(BreakingPointRequest req) {
        String runId = "bp_" + UUID.randomUUID().toString().substring(0, 8);
        Instant start = Instant.now();
        String targetService = req.targetService() != null ? req.targetService() : "payment-service";
        String faultId = req.targetFaultId() != null ? req.targetFaultId() : targetService + ".chargeCard";

        log.info("[breaking-point] Starting breaking point discovery {} targeting '{}'", runId, targetService);

        List<BreakingPointStep> steps = new ArrayList<>();
        boolean broken = false;
        int breakingStepNumber = 0;
        int breakingLatency = 0;
        int breakingBlast = 0;
        String breakingReason = "System remained resilient across all tested stress levels.";

        int currentLatency = req.getStartLatencyMs();
        int stepNum = 1;

        try {
            while (currentLatency <= req.getMaxLatencyMs()) {
                int blast = Math.min(100, 40 + (stepNum * 15)); // progressive blast radius ramp

                // 1. Activate progressive fault
                FaultConfig fault = new FaultConfig(
                        targetService,
                        faultId,
                        FaultType.LATENCY,
                        blast,
                        currentLatency,
                        currentLatency + 200,
                        null,
                        null,
                        30
                );
                faultStore.activateFault(fault);

                // Small delay for agent sync
                Thread.sleep(200);

                // 2. Issue probe requests
                List<Long> latencies = new ArrayList<>();
                int successCalls = 0;
                int fallbackCalls = 0;
                int errorCalls = 0;

                for (int i = 0; i < req.getRequestsPerStep(); i++) {
                    long probeStart = System.currentTimeMillis();
                    try {
                        Map<String, Object> body = Map.of(
                                "orderId", "BP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                                "amount", 100.0 + (stepNum * 10),
                                "itemCode", "STRESS-ITEM"
                        );

                        Map<?, ?> response = restClient.post()
                                .uri(req.getCallerEndpointUrl())
                                .header("Content-Type", "application/json")
                                .body(body)
                                .retrieve()
                                .body(Map.class);

                        long elapsed = System.currentTimeMillis() - probeStart;
                        latencies.add(elapsed);

                        if (response != null) {
                            String pStatus = String.valueOf(response.get("paymentStatus"));
                            if ("SUCCESS".equalsIgnoreCase(pStatus)) {
                                successCalls++;
                            } else if ("FALLBACK_TRIGGERED".equalsIgnoreCase(pStatus)) {
                                fallbackCalls++;
                            } else {
                                errorCalls++;
                            }
                        } else {
                            errorCalls++;
                        }
                    } catch (Exception ex) {
                        long elapsed = System.currentTimeMillis() - probeStart;
                        latencies.add(elapsed);
                        errorCalls++;
                    }

                    Thread.sleep(80);
                }

                // 3. Compute metrics
                long avgLatency = latencies.isEmpty() ? 0 : (long) latencies.stream().mapToLong(Long::longValue).average().orElse(0);
                Collections.sort(latencies);
                int p95Index = Math.min(latencies.size() - 1, (int) Math.ceil(0.95 * latencies.size()) - 1);
                long p95Latency = latencies.isEmpty() ? 0 : latencies.get(Math.max(0, p95Index));

                int totalCalls = req.getRequestsPerStep();
                double errorRate = (double) errorCalls / totalCalls;
                double fallbackRate = (double) fallbackCalls / totalCalls;

                // 4. Query circuit breaker state
                String cbState = queryCircuitBreakerState();

                // 5. Evaluate if step breaches breaking criteria
                boolean stepBroken = false;
                String reason = null;

                if (avgLatency > req.getMaxPermissibleLatencyMs()) {
                    stepBroken = true;
                    reason = String.format("SLO Latency breached: observed %dms > permissible %dms", avgLatency, req.getMaxPermissibleLatencyMs());
                } else if (errorRate > req.getMaxPermissibleErrorRate()) {
                    stepBroken = true;
                    reason = String.format("Error rate threshold breached: %.1f%% errors", errorRate * 100);
                } else if ("OPEN".equalsIgnoreCase(cbState)) {
                    stepBroken = true;
                    reason = "Resilience4j Circuit Breaker tripped OPEN under latency stress";
                }

                BreakingPointStep step = new BreakingPointStep(
                        stepNum,
                        currentLatency,
                        blast,
                        avgLatency,
                        p95Latency,
                        errorRate,
                        fallbackRate,
                        cbState,
                        stepBroken,
                        reason != null ? reason : "Within acceptable SLO performance envelope."
                );
                steps.add(step);

                if (stepBroken && !broken) {
                    broken = true;
                    breakingStepNumber = stepNum;
                    breakingLatency = currentLatency;
                    breakingBlast = blast;
                    breakingReason = reason;
                }

                currentLatency += req.getStepLatencyMs();
                stepNum++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Restore clean state
            faultStore.deactivateFault(targetService, faultId);
        }

        Instant end = Instant.now();
        String evaluation = broken
                ? String.format("System breaking point discovered at %dms latency (%d%% blast radius). Circuit Breaker / Fallback protected downstream users with reason: %s",
                breakingLatency, breakingBlast, breakingReason)
                : "System demonstrated exceptional resilience across all stress levels without breaching SLOs.";

        return new BreakingPointReport(
                runId,
                targetService,
                start,
                end,
                broken,
                breakingStepNumber,
                breakingLatency,
                breakingBlast,
                breakingReason,
                steps,
                evaluation
        );
    }

    private String queryCircuitBreakerState() {
        try {
            List<?> list = restClient.get()
                    .uri("http://localhost:8080/orders/circuit-breakers")
                    .retrieve()
                    .body(List.class);
            if (list != null && !list.isEmpty()) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> map) {
                        if ("paymentService".equalsIgnoreCase(String.valueOf(map.get("name")))) {
                            return String.valueOf(map.get("state"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[breaking-point] Could not query circuit breaker state: {}", e.getMessage());
        }
        return "CLOSED";
    }
}
