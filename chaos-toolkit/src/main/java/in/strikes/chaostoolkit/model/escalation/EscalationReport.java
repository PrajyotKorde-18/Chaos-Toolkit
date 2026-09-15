package in.strikes.chaostoolkit.model.escalation;

import java.time.Instant;
import java.util.List;

public record EscalationReport(
        String runId,
        String planName,
        String targetService,
        Instant startedAt,
        Instant endedAt,
        boolean sloBreached,
        int breakingStageNumber,
        String breakingReason,
        List<StageExecutionSummary> stageSummaries
) {
    public record StageExecutionSummary(
            int stageNumber,
            String stageName,
            long averageLatencyMs,
            double errorRate,
            double fallbackRate,
            boolean sloPassed,
            String note
    ) {}
}
