package in.strikes.chaostoolkit.model.matrix;

public record FaultScenarioResult(
        String scenarioId,
        String scenarioName,
        String faultType,
        int blastRadiusPercent,
        int latencyInjectedMs,
        long avgLatencyMs,
        long p95LatencyMs,
        double fallbackRate,
        double errorRate,
        String circuitState,
        String appReactionType,
        String detailedExplanation
) {}
