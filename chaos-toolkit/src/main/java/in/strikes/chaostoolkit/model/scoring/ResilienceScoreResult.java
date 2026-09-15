package in.strikes.chaostoolkit.model.scoring;

import in.strikes.chaostoolkit.model.FaultType;

public record ResilienceScoreResult(
        String experimentId,
        String serviceName,
        FaultType faultType,
        int blastRadiusPercent,
        double detectScore,
        double recoverScore,
        double fallbackScore,
        double rawScore,
        double severityWeight,
        double severityAdjustedScore,
        String grade,
        String evaluation
) {}
