package in.strikes.chaosagent.core;

import in.strikes.chaosagent.config.ChaosAgentProperties;
import in.strikes.chaosagent.model.FaultConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChaosFaultRegistry {

    private static final Logger log = LoggerFactory.getLogger(ChaosFaultRegistry.class);

    private final ChaosAgentProperties properties;
    private final RestClient restClient;
    private final Map<String, FaultConfig> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private volatile boolean killSwitchEngaged = false;
    private volatile boolean started = false;

    public ChaosFaultRegistry(ChaosAgentProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getControlPlaneUrl())
                .build();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "chaos-agent-poller");
            t.setDaemon(true);
            return t;
        });
    }

    public synchronized void start() {
        if (started) {
            return;
        }
        started = true;
        log.info("[chaos-agent] Starting fault registry poller for service '{}' (polling {} every {}ms)",
                properties.getServiceName(), properties.getControlPlaneUrl(), properties.getPollIntervalMs());
        scheduler.scheduleWithFixedDelay(
                this::pollOnce,
                0,
                properties.getPollIntervalMs(),
                TimeUnit.MILLISECONDS
        );
    }

    public synchronized void stop() {
        log.info("[chaos-agent] Stopping fault registry poller for service '{}'", properties.getServiceName());
        started = false;
        scheduler.shutdownNow();
    }

    private void pollOnce() {
        try {
            List<FaultConfig> activeFaults = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/faults/active")
                            .queryParam("service", properties.getServiceName())
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<FaultConfig>>() {});

            Map<String, FaultConfig> freshMap = new ConcurrentHashMap<>();
            if (activeFaults != null) {
                for (FaultConfig fault : activeFaults) {
                    if (fault.getFaultId() != null) {
                        freshMap.put(fault.getFaultId(), fault);
                    }
                }
            }

            // Sync cache with active faults
            cache.keySet().removeIf(key -> !freshMap.containsKey(key));
            cache.putAll(freshMap);

            log.debug("[chaos-agent] Polled active faults for '{}': {} fault(s) active",
                    properties.getServiceName(), cache.size());
        } catch (Exception e) {
            log.debug("[chaos-agent] Failed to poll control plane at '{}': {} (safe fallback: keeping existing cache)",
                    properties.getControlPlaneUrl(), e.getMessage());
        }
    }

    public FaultConfig getActiveFault(String faultId) {
        if (!properties.isEnabled() || killSwitchEngaged || faultId == null) {
            return FaultConfig.none(faultId);
        }
        FaultConfig fault = cache.get(faultId);
        if (fault == null) {
            return FaultConfig.none(faultId);
        }
        return fault;
    }

    public void engageKillSwitch() {
        log.warn("[chaos-agent] Local kill-switch engaged for service '{}'. All chaos injection disabled.",
                properties.getServiceName());
        this.killSwitchEngaged = true;
    }

    public void releaseKillSwitch() {
        log.info("[chaos-agent] Local kill-switch released for service '{}'. Chaos injection resumed.",
                properties.getServiceName());
        this.killSwitchEngaged = false;
    }

    public boolean isKillSwitchEngaged() {
        return killSwitchEngaged;
    }

    public Map<String, FaultConfig> getCachedFaults() {
        return Map.copyOf(cache);
    }
}
