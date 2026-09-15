package in.strikes.chaostoolkit.model.matrix;

import java.time.Instant;
import java.util.List;

public record FaultMatrixReport(
        String matrixRunId,
        Instant executedAt,
        String targetService,
        List<FaultScenarioResult> scenarios,
        String overallVerdict,
        String summaryRecommendations
) {}
