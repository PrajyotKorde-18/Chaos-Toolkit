package in.strikes.chaosagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chaos.agent")
public class ChaosAgentProperties {

    /**
     * Whether the chaos agent is enabled. Defaults to true.
     */
    private boolean enabled = true;

    /**
     * The name of the service running this agent. Defaults to "unnamed-service".
     */
    private String serviceName = "unnamed-service";

    /**
     * Base URL of the chaos control plane. Defaults to "http://localhost:9000".
     */
    private String controlPlaneUrl = "http://localhost:9000";

    /**
     * Polling interval in milliseconds. Defaults to 2000 ms.
     */
    private long pollIntervalMs = 2000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getControlPlaneUrl() {
        return controlPlaneUrl;
    }

    public void setControlPlaneUrl(String controlPlaneUrl) {
        this.controlPlaneUrl = controlPlaneUrl;
    }

    public long getPollIntervalMs() {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }
}
