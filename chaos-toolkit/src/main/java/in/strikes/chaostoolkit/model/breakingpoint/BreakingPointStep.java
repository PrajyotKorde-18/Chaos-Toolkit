package in.strikes.chaostoolkit.model.breakingpoint;

public record BreakingPointStep(
        int stepNumber,
        int latencyInjectedMs,
        int blastRadiusPercent,
        long observedAverageLatencyMs,
        long observedP95LatencyMs,
        double errorRate,
        double fallbackRate,
        String circuitBreakerState,
        boolean isBroken,
        String breachReason
) {}
