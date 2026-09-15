package in.strikes.chaosagent.model;

public class FaultConfig {

    private String faultId;
    private FaultType type = FaultType.NONE;
    private int blastRadiusPercent = 0;
    private long minMs;
    private long maxMs;
    private String exceptionClassName;
    private String exceptionMessage;

    public FaultConfig() {
    }

    public static FaultConfig none(String faultId) {
        FaultConfig cfg = new FaultConfig();
        cfg.faultId = faultId;
        cfg.type = FaultType.NONE;
        cfg.blastRadiusPercent = 0;
        return cfg;
    }

    public String getFaultId() { return faultId; }
    public void setFaultId(String faultId) { this.faultId = faultId; }

    public FaultType getType() { return type; }
    public void setType(FaultType type) { this.type = type; }

    public int getBlastRadiusPercent() { return blastRadiusPercent; }
    public void setBlastRadiusPercent(int blastRadiusPercent) { this.blastRadiusPercent = blastRadiusPercent; }

    public long getMinMs() { return minMs; }
    public void setMinMs(long minMs) { this.minMs = minMs; }

    public long getMaxMs() { return maxMs; }
    public void setMaxMs(long maxMs) { this.maxMs = maxMs; }

    public String getExceptionClassName() { return exceptionClassName; }
    public void setExceptionClassName(String exceptionClassName) { this.exceptionClassName = exceptionClassName; }

    public String getExceptionMessage() { return exceptionMessage; }
    public void setExceptionMessage(String exceptionMessage) { this.exceptionMessage = exceptionMessage; }
}