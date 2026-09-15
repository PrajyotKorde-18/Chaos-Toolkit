package in.strikes.chaostoolkit.service.reporting;

import in.strikes.chaostoolkit.model.escalation.EscalationReport;
import in.strikes.chaostoolkit.model.scoring.ResilienceScoreResult;
import in.strikes.chaostoolkit.model.verification.VerificationResult;
import in.strikes.chaostoolkit.service.history.ExperimentHistoryStore;
import in.strikes.chaostoolkit.service.scoring.ResilienceScoreCalculator;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PostMortemReportGenerator {

    private final ExperimentHistoryStore historyStore;
    private final ResilienceScoreCalculator scoreCalculator;

    public PostMortemReportGenerator(ExperimentHistoryStore historyStore, ResilienceScoreCalculator scoreCalculator) {
        this.historyStore = historyStore;
        this.scoreCalculator = scoreCalculator;
    }

    public String generateMarkdownReport(String experimentId) {
        VerificationResult vr = historyStore.getVerificationById(experimentId);
        if (vr == null) {
            List<VerificationResult> all = historyStore.getVerifications();
            if (!all.isEmpty()) {
                vr = all.getLast();
            }
        }

        if (vr == null) {
            return "# Chaos Engineering Post-Mortem Report\n\nNo verification experiments found in history store.";
        }

        ResilienceScoreResult sr = scoreCalculator.calculate(vr);
        DateTimeFormatter dtf = DateTimeFormatter.ISO_INSTANT;

        StringBuilder sb = new StringBuilder();
        sb.append("# 🛡️ Chaos Engineering Post-Mortem Report\n\n");
        sb.append("**Experiment ID:** `").append(vr.experimentId()).append("`  \n");
        sb.append("**Target Service:** `").append(vr.targetService()).append("`  \n");
        sb.append("**Fault Injected:** `").append(vr.faultType()).append("` (Blast Radius: ").append(vr.blastRadiusPercent()).append("%)  \n");
        sb.append("**Time Window:** ").append(dtf.format(vr.startTime())).append(" -> ").append(dtf.format(vr.endTime())).append("  \n\n");

        sb.append("## 1. Executive Resilience Summary\n\n");
        sb.append("| Metric | Value | Threshold / Target | Result |\n");
        sb.append("|---|---|---|---|\n");
        sb.append(String.format("| **Detection Latency ($t_{detect}$)** | %d ms | < 2000 ms | %s |\n",
                vr.tDetectMs(), vr.tDetectMs() <= 2000 ? "✅ PASS" : "⚠️ SLOW"));
        sb.append(String.format("| **Recovery Latency ($t_{recover}$)** | %d ms | < 5000 ms | %s |\n",
                vr.tRecoverMs(), vr.tRecoverMs() <= 5000 ? "✅ PASS" : "⚠️ SLOW"));
        sb.append(String.format("| **Fallback Handling Rate** | %.1f%% | > 90%% | %s |\n",
                vr.fallbackSuccessRate() * 100, vr.fallbackSuccessRate() >= 0.90 ? "✅ PASS" : "❌ DEGRADED"));
        sb.append(String.format("| **Resilience Grade** | **%s** | Grade A / B | Score: %.2f |\n\n",
                sr.grade(), sr.severityAdjustedScore() * 100));

        sb.append("## 2. Telemetry & Request Distribution\n\n");
        sb.append("- **Total Requests Issued:** ").append(vr.totalCalls()).append("\n");
        sb.append("- **Direct Successes:** ").append(vr.successfulCalls()).append("\n");
        sb.append("- **Gracefully Handled by CircuitBreaker Fallback:** ").append(vr.fallbackCalls()).append("\n");
        sb.append("- **Hard System Failures (5xx):** ").append(vr.failedCalls()).append("\n");
        sb.append("- **Observed Circuit State:** `").append(vr.circuitBreakerObservedState()).append("`\n\n");

        sb.append("## 3. Resilience Mathematical Scoring Breakdown\n\n");
        sb.append("```\n");
        sb.append("detect_score  = clamp(1 - (").append(vr.tDetectMs()).append(" / 2000)) = ").append(sr.detectScore()).append("\n");
        sb.append("recover_score = clamp(1 - (").append(vr.tRecoverMs()).append(" / 5000)) = ").append(sr.recoverScore()).append("\n");
        sb.append("fallback_score = ").append(sr.fallbackScore()).append("\n");
        sb.append("raw_score = (0.35 * ").append(sr.detectScore()).append(") + (0.35 * ").append(sr.recoverScore()).append(") + (0.30 * ").append(sr.fallbackScore()).append(") = ").append(sr.rawScore()).append("\n");
        sb.append("severity_weight = ").append(sr.severityWeight()).append("\n");
        sb.append("final_score = ").append(sr.severityAdjustedScore()).append(" (Grade: ").append(sr.grade()).append(")\n");
        sb.append("```\n\n");

        sb.append("## 4. Root Cause Analysis & Recommendations\n\n");
        if (vr.failedCalls() == 0 && vr.fallbackCalls() > 0) {
            sb.append("> **Finding:** The service demonstrated robust resilience. When faults were injected, the caller's Resilience4j Circuit Breaker isolated the faulty dependency and smoothly routed requests to fallback handlers without user-facing outages.\n\n");
            sb.append("- **Action Item:** Ensure cached fallback data expiration TTL is monitored.\n");
        } else if (vr.failedCalls() > 0) {
            sb.append("> **Finding:** Unhandled exceptions escaped the circuit breaker boundary, resulting in ").append(vr.failedCalls()).append(" unhandled errors.\n\n");
            sb.append("- **Action Item:** Review `CircuitBreaker` sliding window thresholds and add comprehensive fallback handlers to client callers.\n");
        } else {
            sb.append("> **Finding:** Baseline conditions remained stable throughout testing.\n\n");
        }

        return sb.toString();
    }
}
