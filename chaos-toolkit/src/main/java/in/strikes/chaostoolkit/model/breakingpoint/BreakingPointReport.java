package in.strikes.chaostoolkit.model.breakingpoint;

import java.time.Instant;
import java.util.List;

public record BreakingPointReport(
        String runId,
        String targetService,
        Instant startedAt,
        Instant endedAt,
        boolean systemBroken,
        int breakingStepNumber,
        int breakingLatencyMs,
        int breakingBlastRadiusPercent,
        String breakingReason,
        List<BreakingPointStep> steps,
        String resilienceEvaluation
) {}
