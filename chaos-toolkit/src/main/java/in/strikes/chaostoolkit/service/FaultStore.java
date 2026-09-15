package in.strikes.chaostoolkit.service;

import in.strikes.chaostoolkit.model.FaultConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FaultStore {

    private final Map<String, Map<String, FaultConfig>> faultsByService = new ConcurrentHashMap<>();
    private volatile boolean globalKillSwitchEngaged = false;

    public void activateFault(FaultConfig config) {
        faultsByService
                .computeIfAbsent(config.getServiceName(), s -> new ConcurrentHashMap<>())
                .put(config.getFaultId(), config);
    }

    public void deactivateFault(String serviceName, String faultId) {
        Map<String, FaultConfig> serviceFaults = faultsByService.get(serviceName);
        if (serviceFaults != null) {
            serviceFaults.remove(faultId);
        }
    }

    public void deactivateAllFor(String serviceName) {
        faultsByService.remove(serviceName);
    }

    public List<FaultConfig> getActiveFaultsFor(String serviceName) {
        if (globalKillSwitchEngaged) {
            return List.of();
        }
        Map<String, FaultConfig> serviceFaults = faultsByService.get(serviceName);
        if (serviceFaults == null || serviceFaults.isEmpty()) {
            return List.of();
        }

        // Purge expired faults
        List<String> expiredFaultIds = new ArrayList<>();
        for (FaultConfig config : serviceFaults.values()) {
            if (config.isExpired()) {
                expiredFaultIds.add(config.getFaultId());
            }
        }
        for (String id : expiredFaultIds) {
            serviceFaults.remove(id);
        }

        return List.copyOf(serviceFaults.values());
    }

    public Map<String, Map<String, FaultConfig>> getAllActiveFaults() {
        // Clean expired faults across all services before returning snapshot
        for (Map<String, FaultConfig> serviceMap : faultsByService.values()) {
            serviceMap.values().removeIf(FaultConfig::isExpired);
        }
        return Map.copyOf(faultsByService);
    }

    public void engageGlobalKillSwitch() {
        globalKillSwitchEngaged = true;
    }

    public void releaseGlobalKillSwitch() {
        globalKillSwitchEngaged = false;
    }

    public boolean isGlobalKillSwitchEngaged() {
        return globalKillSwitchEngaged;
    }
}
