package in.strikes.chaostoolkit.model.verification;

import in.strikes.chaostoolkit.model.FaultType;

import java.time.Instant;

public record VerificationResult(
        String experimentId,
        String targetService,
        String faultId,
        FaultType faultType,
        int blastRadiusPercent,
        Instant startTime,
        Instant endTime,
        long tDetectMs,
        long tRecoverMs,
        int totalCalls,
        int successfulCalls,
        int fallbackCalls,
        int failedCalls,
        double fallbackSuccessRate,
        String circuitBreakerObservedState,
        String summary
) {}
