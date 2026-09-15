package in.strikes.chaostoolkit.service.escalation;

import in.strikes.chaostoolkit.model.FaultConfig;
import in.strikes.chaostoolkit.model.FaultType;
import in.strikes.chaostoolkit.model.escalation.EscalationPlan;
import in.strikes.chaostoolkit.model.escalation.EscalationReport;
import in.strikes.chaostoolkit.model.escalation.EscalationStage;
import in.strikes.chaostoolkit.service.FaultStore;
import in.strikes.chaostoolkit.service.history.ExperimentHistoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EscalationEngine {

    private static final Logger log = LoggerFactory.getLogger(EscalationEngine.class);

    private final FaultStore faultStore;
    private final ExperimentHistoryStore historyStore;
    private final RestClient restClient;

    public EscalationEngine(FaultStore faultStore, ExperimentHistoryStore historyStore) {
        this.faultStore = faultStore;
        this.historyStore = historyStore;
        this.restClient = RestClient.builder().build();
    }

    public EscalationReport executePlan(EscalationPlan plan) {
        String runId = "esc_" + UUID.randomUUID().toString().substring(0, 8);
        Instant startedAt = Instant.now();
        log.info("[escalation] Starting escalation run {} for plan '{}' (target: {})",
                runId, plan.planName(), plan.targetService());

        List<EscalationReport.StageExecutionSummary> stageSummaries = new ArrayList<>();
        boolean sloBreached = false;
        int breakingStageNumber = 0;
        String breakingReason = "All stages completed within SLO bounds.";

        String endpointUrl = (plan.callerEndpointUrl() != null && !plan.callerEndpointUrl().isBlank())
                ? plan.callerEndpointUrl()
                : "http://localhost:8080/orders/create";

        int requestsPerStage = Math.max(3, plan.requestsPerStage());

        for (EscalationStage stage : plan.stages()) {
            log.info("[escalation] Executing Stage {}: '{}' (Fault: {}, Blast: {}%)",
                    stage.getStageNum(), stage.name(), stage.faultType(), stage.getBlastRadius());

            // 1. Activate Stage Fault
            FaultConfig faultConfig = new FaultConfig(
                    plan.targetService(),
                    plan.faultId(),
                    stage.faultType() != null ? stage.faultType() : FaultType.LATENCY,
                    stage.getBlastRadius(),
                    stage.getMinMsSafe(),
                    stage.getMaxMsSafe(),
                    stage.exceptionClassName(),
                    stage.exceptionMessage(),
                    stage.getDurationSafe()
            );
            faultStore.activateFault(faultConfig);
            sleepSilently(2200);

            long totalLatency = 0;
            int errors = 0;
            int fallbacks = 0;
            int successes = 0;

            for (int i = 0; i < requestsPerStage; i++) {
                long start = System.currentTimeMillis();
                try {
                    Map<?, ?> response = restClient.post()
                            .uri(endpointUrl)
                            .retrieve()
                            .body(Map.class);

                    long duration = System.currentTimeMillis() - start;
                    totalLatency += duration;

                    if (response != null) {
                        String status = String.valueOf(response.get("status"));
                        if ("COMPLETED".equals(status)) {
                            successes++;
                        } else {
                            fallbacks++;
                        }
                    } else {
                        successes++;
                    }
                } catch (Exception ex) {
                    long duration = System.currentTimeMillis() - start;
                    totalLatency += duration;
                    errors++;
                }
            }

            long avgLatency = (requestsPerStage > 0) ? totalLatency / requestsPerStage : 0;
            double errorRate = (requestsPerStage > 0) ? (double) errors / requestsPerStage : 0;
            double fallbackRate = (requestsPerStage > 0) ? (double) fallbacks / requestsPerStage : 0;

            boolean stagePassed = true;
            String note = "Stage passed within SLO constraints.";

            if (stage.getMaxLatencySafe() > 0 && avgLatency > stage.getMaxLatencySafe()) {
                stagePassed = false;
                note = String.format("SLO Latency Breach: Avg %dms exceeded max %dms", avgLatency, stage.getMaxLatencySafe());
            } else if (stage.getMaxErrorSafe() >= 0 && errorRate > stage.getMaxErrorSafe()) {
                stagePassed = false;
                note = String.format("SLO Error Rate Breach: %.1f%% exceeded max %.1f%%", errorRate * 100, stage.getMaxErrorSafe() * 100);
            }

            stageSummaries.add(new EscalationReport.StageExecutionSummary(
                    stage.getStageNum(),
                    stage.name(),
                    avgLatency,
                    errorRate,
                    fallbackRate,
                    stagePassed,
                    note
            ));

            if (!stagePassed) {
                sloBreached = true;
                breakingStageNumber = stage.getStageNum();
                breakingReason = String.format("Breaking point reached at Stage %d ('%s'): %s",
                        stage.getStageNum(), stage.name(), note);
                log.warn("[escalation] {}", breakingReason);
                break; // Stop immediately upon breaking point
            }
        }

        // Clean up fault store
        faultStore.deactivateFault(plan.targetService(), plan.faultId());
        Instant endedAt = Instant.now();

        EscalationReport report = new EscalationReport(
                runId,
                plan.planName(),
                plan.targetService(),
                startedAt,
                endedAt,
                sloBreached,
                breakingStageNumber,
                breakingReason,
                stageSummaries
        );

        historyStore.saveEscalationReport(report);
        return report;
    }

    private void sleepSilently(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
