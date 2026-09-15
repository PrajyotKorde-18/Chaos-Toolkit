package in.strikes.chaostoolkit.model.escalation;

import in.strikes.chaostoolkit.model.FaultType;

public record EscalationStage(
        Integer stageNumber,
        String name,
        FaultType faultType,
        Integer blastRadiusPercent,
        Long minMs,
        Long maxMs,
        String exceptionClassName,
        String exceptionMessage,
        Integer durationSeconds,
        Long maxPermissibleLatencyMs,
        Double maxPermissibleErrorRate
) {
    public int getStageNum() { return stageNumber != null ? stageNumber : 1; }
    public int getBlastRadius() { return blastRadiusPercent != null ? blastRadiusPercent : 100; }
    public long getMinMsSafe() { return minMs != null ? minMs : 0; }
    public long getMaxMsSafe() { return maxMs != null ? maxMs : 0; }
    public int getDurationSafe() { return durationSeconds != null ? durationSeconds : 30; }
    public long getMaxLatencySafe() { return maxPermissibleLatencyMs != null ? maxPermissibleLatencyMs : 2000; }
    public double getMaxErrorSafe() { return maxPermissibleErrorRate != null ? maxPermissibleErrorRate : 0.2; }
}
