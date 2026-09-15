package in.strikes.chaostoolkit.service.history;

import in.strikes.chaostoolkit.model.escalation.EscalationReport;
import in.strikes.chaostoolkit.model.scoring.ResilienceScoreResult;
import in.strikes.chaostoolkit.model.security.SecuritySimulationResult;
import in.strikes.chaostoolkit.model.verification.VerificationResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ExperimentHistoryStore {

    private final List<VerificationResult> verificationHistory = new CopyOnWriteArrayList<>();
    private final List<ResilienceScoreResult> scoreHistory = new CopyOnWriteArrayList<>();
    private final List<EscalationReport> escalationHistory = new CopyOnWriteArrayList<>();
    private final List<SecuritySimulationResult> securityHistory = new CopyOnWriteArrayList<>();
    private final Map<String, VerificationResult> verificationById = new ConcurrentHashMap<>();

    public void saveVerification(VerificationResult vr, ResilienceScoreResult sr) {
        verificationHistory.add(vr);
        scoreHistory.add(sr);
        verificationById.put(vr.experimentId(), vr);
    }

    public void saveEscalationReport(EscalationReport report) {
        escalationHistory.add(report);
    }

    public void saveSecurityResult(SecuritySimulationResult result) {
        securityHistory.add(result);
    }

    public List<VerificationResult> getVerifications() {
        return List.copyOf(verificationHistory);
    }

    public List<ResilienceScoreResult> getScores() {
        return List.copyOf(scoreHistory);
    }

    public List<EscalationReport> getEscalations() {
        return List.copyOf(escalationHistory);
    }

    public List<SecuritySimulationResult> getSecurityResults() {
        return List.copyOf(securityHistory);
    }

    public VerificationResult getVerificationById(String id) {
        if (id == null) {
            return null;
        }
        return verificationById.get(id);
    }
}
