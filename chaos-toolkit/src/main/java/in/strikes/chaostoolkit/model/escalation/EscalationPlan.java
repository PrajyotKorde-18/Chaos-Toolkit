package in.strikes.chaostoolkit.model.escalation;

import java.util.List;

public record EscalationPlan(
        String planName,
        String targetService,
        String faultId,
        String callerEndpointUrl,
        int requestsPerStage,
        List<EscalationStage> stages
) {}
