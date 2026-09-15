package in.strikes.chaostoolkit.model.breakingpoint;

public record BreakingPointRequest(
        String targetService,
        String targetFaultId,
        Integer startLatencyMs,
        Integer stepLatencyMs,
        Integer maxLatencyMs,
        Integer maxPermissibleLatencyMs,
        Double maxPermissibleErrorRate,
        String callerEndpointUrl,
        Integer requestsPerStep
) {
    public int getStartLatencyMs() { return startLatencyMs != null ? startLatencyMs : 200; }
    public int getStepLatencyMs() { return stepLatencyMs != null ? stepLatencyMs : 400; }
    public int getMaxLatencyMs() { return maxLatencyMs != null ? maxLatencyMs : 2600; }
    public int getMaxPermissibleLatencyMs() { return maxPermissibleLatencyMs != null ? maxPermissibleLatencyMs : 1200; }
    public double getMaxPermissibleErrorRate() { return maxPermissibleErrorRate != null ? maxPermissibleErrorRate : 0.05; }
    public String getCallerEndpointUrl() { return callerEndpointUrl != null ? callerEndpointUrl : "http://localhost:8080/orders/create"; }
    public int getRequestsPerStep() { return requestsPerStep != null ? requestsPerStep : 4; }
}
