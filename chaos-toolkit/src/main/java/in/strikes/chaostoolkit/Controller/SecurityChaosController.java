package in.strikes.chaostoolkit.Controller;

import in.strikes.chaostoolkit.model.security.SecurityScenarioType;
import in.strikes.chaostoolkit.model.security.SecuritySimulationResult;
import in.strikes.chaostoolkit.service.history.ExperimentHistoryStore;
import in.strikes.chaostoolkit.service.security.SecurityChaosSimulator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/security-chaos")
public class SecurityChaosController {

    private final SecurityChaosSimulator securitySimulator;
    private final ExperimentHistoryStore historyStore;

    public SecurityChaosController(SecurityChaosSimulator securitySimulator, ExperimentHistoryStore historyStore) {
        this.securitySimulator = securitySimulator;
        this.historyStore = historyStore;
    }

    @PostMapping("/run")
    public ResponseEntity<SecuritySimulationResult> runSecurityScenario(
            @RequestParam SecurityScenarioType scenario,
            @RequestParam(defaultValue = "order-service") String targetService,
            @RequestParam(defaultValue = "http://localhost:8080") String targetBaseUrl) {

        SecuritySimulationResult result = securitySimulator.runSimulation(scenario, targetService, targetBaseUrl);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<SecuritySimulationResult>> getHistory() {
        return ResponseEntity.ok(historyStore.getSecurityResults());
    }
}
