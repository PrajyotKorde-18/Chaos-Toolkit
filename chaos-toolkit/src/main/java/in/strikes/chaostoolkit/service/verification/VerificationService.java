package in.strikes.chaostoolkit.service.verification;

import in.strikes.chaostoolkit.model.FaultConfig;
import in.strikes.chaostoolkit.model.FaultType;
import in.strikes.chaostoolkit.model.scoring.ResilienceScoreResult;
import in.strikes.chaostoolkit.model.verification.VerificationRequest;
import in.strikes.chaostoolkit.model.verification.VerificationResult;
import in.strikes.chaostoolkit.service.FaultStore;
import in.strikes.chaostoolkit.service.history.ExperimentHistoryStore;
import in.strikes.chaostoolkit.service.scoring.ResilienceScoreCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class VerificationService {

    private static final Logger log = LoggerFactory.getLogger(VerificationService.class);

    private final FaultStore faultStore;
    private final ResilienceScoreCalculator scoreCalculator;
    private final ExperimentHistoryStore historyStore;
    private final RestClient restClient;

    public VerificationService(
            FaultStore faultStore,
            ResilienceScoreCalculator scoreCalculator,
            ExperimentHistoryStore historyStore) {
        this.faultStore = faultStore;
        this.scoreCalculator = scoreCalculator;
        this.historyStore = historyStore;
        this.restClient = RestClient.builder().build();
    }

    public VerificationResult runVerification(VerificationRequest request) {
        String experimentId = "exp_" + UUID.randomUUID().toString().substring(0, 8);
        Instant startTime = Instant.now();
        log.info("[verification] Starting experiment {} targeting '{}.{}' with fault {}",
                experimentId, request.targetService(), request.targetFaultId(), request.faultType());

        // 1. Activate fault in FaultStore
        FaultConfig faultConfig = new FaultConfig(
                request.targetService(),
                request.targetFaultId(),
                request.faultType() != null ? request.faultType() : FaultType.LATENCY,
                request.getBlastRadius(),
                request.getMinMsSafe(),
                request.getMaxMsSafe(),
                request.exceptionClassName(),
                request.exceptionMessage(),
                request.getDurationSafe()
        );
        faultStore.activateFault(faultConfig);

        // Wait 2.2s for agent poller cycle
        sleepSilently(2200);

        int totalCalls = Math.max(4, request.getNumRequestsSafe());
        int successfulCalls = 0;
        int fallbackCalls = 0;
        int failedCalls = 0;
        long firstFallbackOrTripTimestamp = 0;

        String endpointUrl = (request.callerEndpointUrl() != null && !request.callerEndpointUrl().isBlank())
                ? request.callerEndpointUrl()
                : "http://localhost:8080/orders/create";

        // 2. Issue requests and observe caller behavior
        for (int i = 0; i < totalCalls; i++) {
            long reqStart = System.currentTimeMillis();
            try {
                Map<?, ?> response = restClient.post()
                        .uri(endpointUrl)
                        .retrieve()
                        .body(Map.class);

                long reqDuration = System.currentTimeMillis() - reqStart;
                if (response != null) {
                    String status = String.valueOf(response.get("status"));
                    String payStatus = String.valueOf(response.get("paymentStatus"));
                    String invStatus = String.valueOf(response.get("inventoryStatus"));

                    if ("COMPLETED".equals(status) || "SUCCESS".equals(payStatus)) {
                        successfulCalls++;
                    } else if (status.contains("FALLBACK") || status.contains("PARTIAL") || payStatus.contains("FALLBACK") || invStatus.contains("BACKORDER")) {
                        fallbackCalls++;
                        if (firstFallbackOrTripTimestamp == 0) {
                            firstFallbackOrTripTimestamp = System.currentTimeMillis();
                        }
                    } else {
                        successfulCalls++;
                    }
                } else {
                    successfulCalls++;
                }
            } catch (Exception e) {
                failedCalls++;
                if (firstFallbackOrTripTimestamp == 0) {
                    firstFallbackOrTripTimestamp = System.currentTimeMillis();
                }
            }

            if (request.getIntervalSafe() > 0) {
                sleepSilently(request.getIntervalSafe());
            }
        }

        // Calculate t_detect
        long tDetectMs = (firstFallbackOrTripTimestamp > 0)
                ? Math.max(50, firstFallbackOrTripTimestamp - startTime.toEpochMilli())
                : 100;

        // 3. Deactivate fault and measure t_recover
        faultStore.deactivateFault(request.targetService(), request.targetFaultId());
        long deactivationTime = System.currentTimeMillis();

        // Wait for agent poll deactivation
        sleepSilently(2200);

        long recoverObservedTime = System.currentTimeMillis();
        try {
            restClient.post().uri(endpointUrl).retrieve().toBodilessEntity();
            recoverObservedTime = System.currentTimeMillis();
        } catch (Exception ignored) {}

        long tRecoverMs = Math.max(100, recoverObservedTime - deactivationTime);
        Instant endTime = Instant.now();

        double fallbackSuccessRate = (totalCalls > 0)
                ? (double) (successfulCalls + fallbackCalls) / totalCalls
                : 1.0;

        String summary = String.format("Experiment %s complete: %d total calls, %d successful, %d handled by fallback, %d hard failures. t_detect: %dms, t_recover: %dms.",
                experimentId, totalCalls, successfulCalls, fallbackCalls, failedCalls, tDetectMs, tRecoverMs);

        VerificationResult result = new VerificationResult(
                experimentId,
                request.targetService(),
                request.targetFaultId(),
                request.faultType() != null ? request.faultType() : FaultType.LATENCY,
                request.getBlastRadius(),
                startTime,
                endTime,
                tDetectMs,
                tRecoverMs,
                totalCalls,
                successfulCalls,
                fallbackCalls,
                failedCalls,
                fallbackSuccessRate,
                (fallbackCalls > 0 ? "CIRCUIT_OPEN_HANDLED" : "CIRCUIT_CLOSED"),
                summary
        );

        ResilienceScoreResult score = scoreCalculator.calculate(result);
        historyStore.saveVerification(result, score);

        log.info("[verification] {} Score: {} (Grade: {})", summary, score.severityAdjustedScore(), score.grade());
        return result;
    }

    private void sleepSilently(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
