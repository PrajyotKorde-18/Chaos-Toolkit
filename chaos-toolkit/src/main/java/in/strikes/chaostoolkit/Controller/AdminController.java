package in.strikes.chaostoolkit.Controller;

import in.strikes.chaostoolkit.model.FaultConfig;
import in.strikes.chaostoolkit.service.FaultStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final FaultStore faultStore;

    public AdminController(FaultStore faultStore) {
        this.faultStore = faultStore;
    }

    @PostMapping("/faults/activate")
    public FaultConfig activateFault(@Valid @RequestBody ActivateFaultRequest request) {
        FaultConfig config = new FaultConfig();
        config.setServiceName(request.serviceName);
        config.setFaultId(request.faultId);
        config.setType(request.type);
        config.setBlastRadiusPercent(request.blastRadiusPercent);
        config.setMinMs(request.minMs);
        config.setMaxMs(request.maxMs);
        config.setExceptionClassName(request.exceptionClassName);
        config.setExceptionMessage(request.exceptionMessage);
        if (request.durationSeconds > 0) {
            config.setExpiresAt(Instant.now().plusSeconds(request.durationSeconds));
        }
        faultStore.activateFault(config);
        return config;
    }

    @DeleteMapping("/faults/{serviceName}/{faultId}")
    public void deactivateFault(@PathVariable String serviceName, @PathVariable String faultId) {
        faultStore.deactivateFault(serviceName, faultId);
    }

    @DeleteMapping("/faults/{serviceName}")
    public void deactivateAllForService(@PathVariable String serviceName) {
        faultStore.deactivateAllFor(serviceName);
    }

    @GetMapping("/faults")
    public Map<String, Map<String, FaultConfig>> listAllActiveFaults() {
        return faultStore.getAllActiveFaults();
    }

    @PostMapping("/kill-switch/engage")
    public void engageKillSwitch() {
        faultStore.engageGlobalKillSwitch();
    }

    @PostMapping("/kill-switch/release")
    public void releaseKillSwitch() {
        faultStore.releaseGlobalKillSwitch();
    }

    @GetMapping("/kill-switch/status")
    public Map<String, Boolean> killSwitchStatus() {
        return Map.of("engaged", faultStore.isGlobalKillSwitchEngaged());
    }
}
