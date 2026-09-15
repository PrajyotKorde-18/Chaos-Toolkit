package in.strikes.chaostoolkit.model.scoring;

public record ScoringThresholds(
        String serviceName,
        long tDetectThresholdMs,
        long tRecoverThresholdMs
) {
    public static ScoringThresholds defaultFor(String serviceName) {
        return new ScoringThresholds(serviceName, 2000L, 5000L);
    }
}
