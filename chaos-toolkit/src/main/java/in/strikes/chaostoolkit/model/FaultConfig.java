package in.strikes.chaostoolkit.model;

import java.time.Instant;

public class FaultConfig {
    private String faultId;
    private String serviceName;
    private FaultType type = FaultType.NONE;
    private int blastRadiusPercent = 0;
    private long minMs;
    private long maxMs;
    private String exceptionClassName;
    private String exceptionMessage;
    private Instant expiresAt;

    public FaultConfig() {
    }

    public FaultConfig(String serviceName, String faultId, FaultType type, int blastRadiusPercent,
                       long minMs, long maxMs, String exceptionClassName, String exceptionMessage, int durationSeconds) {
        this.serviceName = serviceName;
        this.faultId = faultId;
        this.type = type;
        this.blastRadiusPercent = blastRadiusPercent;
        this.minMs = minMs;
        this.maxMs = maxMs;
        this.exceptionClassName = exceptionClassName;
        this.exceptionMessage = exceptionMessage;
        this.expiresAt = (durationSeconds > 0) ? Instant.now().plusSeconds(durationSeconds) : null;
    }

    public String getFaultId() {
        return faultId;
    }

    public void setFaultId(String faultId) {
        this.faultId = faultId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public FaultType getType() {
        return type;
    }

    public void setType(FaultType type) {
        this.type = type;
    }

    public int getBlastRadiusPercent() {
        return blastRadiusPercent;
    }

    public void setBlastRadiusPercent(int blastRadiusPercent) {
        this.blastRadiusPercent = blastRadiusPercent;
    }

    public long getMinMs() {
        return minMs;
    }

    public void setMinMs(long minMs) {
        this.minMs = minMs;
    }

    public long getMaxMs() {
        return maxMs;
    }

    public void setMaxMs(long maxMs) {
        this.maxMs = maxMs;
    }

    public String getExceptionClassName() {
        return exceptionClassName;
    }

    public void setExceptionClassName(String exceptionClassName) {
        this.exceptionClassName = exceptionClassName;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
