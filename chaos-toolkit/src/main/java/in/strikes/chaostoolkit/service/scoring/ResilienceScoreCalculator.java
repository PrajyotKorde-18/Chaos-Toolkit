package in.strikes.chaostoolkit.service.scoring;

import in.strikes.chaostoolkit.model.FaultType;
import in.strikes.chaostoolkit.model.scoring.ResilienceScoreResult;
import in.strikes.chaostoolkit.model.scoring.ScoringThresholds;
import in.strikes.chaostoolkit.model.verification.VerificationResult;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResilienceScoreCalculator {

    private final Map<String, ScoringThresholds> thresholds = new ConcurrentHashMap<>();

    public void setThresholds(ScoringThresholds customThresholds) {
        thresholds.put(customThresholds.serviceName(), customThresholds);
    }

    public ScoringThresholds getThresholds(String serviceName) {
        return thresholds.getOrDefault(serviceName, ScoringThresholds.defaultFor(serviceName));
    }

    public ResilienceScoreResult calculate(VerificationResult result) {
        ScoringThresholds t = getThresholds(result.targetService());

        // 1. detect_score = clamp(1 - (t_detect / T_detect_threshold), 0, 1)
        double detectScore = (t.tDetectThresholdMs() > 0)
                ? clamp(1.0 - ((double) result.tDetectMs() / t.tDetectThresholdMs()), 0.0, 1.0)
                : 1.0;

        // 2. recover_score = clamp(1 - (t_recover / T_recover_threshold), 0, 1)
        double recoverScore = (t.tRecoverThresholdMs() > 0)
                ? clamp(1.0 - ((double) result.tRecoverMs() / t.tRecoverThresholdMs()), 0.0, 1.0)
                : 1.0;

        // 3. fallback_score = fallback_success_rate
        double fallbackScore = clamp(result.fallbackSuccessRate(), 0.0, 1.0);

        // 4. raw_score = (0.35 * detect) + (0.35 * recover) + (0.30 * fallback)
        double rawScore = (0.35 * detectScore) + (0.35 * recoverScore) + (0.30 * fallbackScore);

        // 5. severity_weight(fault_type, blast_radius)
        double severityWeight = computeSeverityWeight(result.faultType(), result.blastRadiusPercent());

        // 6. severity_adjusted_score
        double severityAdjustedScore = rawScore * severityWeight;

        String grade = assignGrade(severityAdjustedScore);
        String evaluation = generateEvaluation(result, detectScore, recoverScore, fallbackScore, severityAdjustedScore);

        return new ResilienceScoreResult(
                result.experimentId(),
                result.targetService(),
                result.faultType(),
                result.blastRadiusPercent(),
                round(detectScore),
                round(recoverScore),
                round(fallbackScore),
                round(rawScore),
                round(severityWeight),
                round(severityAdjustedScore),
                grade,
                evaluation
        );
    }

    private double computeSeverityWeight(FaultType type, int blastRadiusPercent) {
        if (type == FaultType.NONE) {
            return 0.1;
        }
        double baseWeight = (type == FaultType.EXCEPTION) ? 0.9 : 0.75;
        double radiusFactor = Math.max(0.1, blastRadiusPercent / 100.0);
        return clamp(baseWeight * (0.5 + (0.5 * radiusFactor)), 0.1, 1.0);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round(double val) {
        return Math.round(val * 1000.0) / 1000.0;
    }

    private String assignGrade(double score) {
        if (score >= 0.85) return "A (Exemplary Resilience)";
        if (score >= 0.70) return "B (Resilient with Minor Delays)";
        if (score >= 0.50) return "C (Partially Degraded)";
        if (score >= 0.30) return "D (Poor Fault Isolation)";
        return "F (Fragile - Critical Failures)";
    }

    private String generateEvaluation(VerificationResult vr, double dScore, double rScore, double fbScore, double adjScore) {
        return String.format(
                "Service '%s' evaluated under %s fault (Blast Radius: %d%%). Detection: %.1f%%, Recovery: %.1f%%, Fallback Handling: %.1f%%. Final weighted score: %.2f.",
                vr.targetService(), vr.faultType(), vr.blastRadiusPercent(), dScore * 100, rScore * 100, fbScore * 100, adjScore * 100);
    }
}
