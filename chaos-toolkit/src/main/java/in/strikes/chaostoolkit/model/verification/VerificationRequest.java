package in.strikes.chaostoolkit.model.verification;

import in.strikes.chaostoolkit.model.FaultType;

public record VerificationRequest(
        String targetService,
        String targetFaultId,
        FaultType faultType,
        Integer blastRadiusPercent,
        Long minMs,
        Long maxMs,
        String exceptionClassName,
        String exceptionMessage,
        Integer durationSeconds,
        String callerEndpointUrl,
        Integer numberOfRequests,
        Long requestIntervalMs
) {
    public int getBlastRadius() {
        return blastRadiusPercent != null ? blastRadiusPercent : 100;
    }
    public long getMinMsSafe() {
        return minMs != null ? minMs : 0;
    }
    public long getMaxMsSafe() {
        return maxMs != null ? maxMs : 0;
    }
    public int getDurationSafe() {
        return durationSeconds != null ? durationSeconds : 30;
    }
    public int getNumRequestsSafe() {
        return numberOfRequests != null ? numberOfRequests : 5;
    }
    public long getIntervalSafe() {
        return requestIntervalMs != null ? requestIntervalMs : 200;
    }
}
