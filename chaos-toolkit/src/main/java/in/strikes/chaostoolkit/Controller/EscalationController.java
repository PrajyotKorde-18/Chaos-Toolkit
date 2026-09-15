package in.strikes.chaostoolkit.Controller;

import in.strikes.chaostoolkit.model.escalation.EscalationPlan;
import in.strikes.chaostoolkit.model.escalation.EscalationReport;
import in.strikes.chaostoolkit.service.escalation.EscalationEngine;
import in.strikes.chaostoolkit.service.history.ExperimentHistoryStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/escalation")
public class EscalationController {

    private final EscalationEngine escalationEngine;
    private final ExperimentHistoryStore historyStore;

    public EscalationController(EscalationEngine escalationEngine, ExperimentHistoryStore historyStore) {
        this.escalationEngine = escalationEngine;
        this.historyStore = historyStore;
    }

    @PostMapping("/run")
    public ResponseEntity<EscalationReport> runPlan(@RequestBody EscalationPlan plan) {
        EscalationReport report = escalationEngine.executePlan(plan);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/history")
    public ResponseEntity<List<EscalationReport>> getHistory() {
        return ResponseEntity.ok(historyStore.getEscalations());
    }
}
