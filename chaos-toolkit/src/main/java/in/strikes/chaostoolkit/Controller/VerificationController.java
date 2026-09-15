package in.strikes.chaostoolkit.Controller;

import in.strikes.chaostoolkit.model.scoring.ResilienceScoreResult;
import in.strikes.chaostoolkit.model.verification.VerificationRequest;
import in.strikes.chaostoolkit.model.verification.VerificationResult;
import in.strikes.chaostoolkit.service.history.ExperimentHistoryStore;
import in.strikes.chaostoolkit.service.verification.VerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/verification")
public class VerificationController {

    private final VerificationService verificationService;
    private final ExperimentHistoryStore historyStore;

    public VerificationController(VerificationService verificationService, ExperimentHistoryStore historyStore) {
        this.verificationService = verificationService;
        this.historyStore = historyStore;
    }

    @PostMapping("/run")
    public ResponseEntity<VerificationResult> runVerification(@RequestBody VerificationRequest request) {
        VerificationResult result = verificationService.runVerification(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<VerificationResult>> getHistory() {
        return ResponseEntity.ok(historyStore.getVerifications());
    }

    @GetMapping("/scores")
    public ResponseEntity<List<ResilienceScoreResult>> getScores() {
        return ResponseEntity.ok(historyStore.getScores());
    }
}
