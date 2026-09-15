package in.strikes.chaostoolkit.model.security;

import java.time.Instant;

public record SecuritySimulationResult(
        String simulationId,
        SecurityScenarioType scenarioType,
        String targetService,
        String strideCategory,
        Instant executedAt,
        int totalProbesSent,
        int blockedProbes,
        int allowedProbes,
        boolean detectedBySecurityLayer,
        String observation,
        String remediationGuidance
) {}
