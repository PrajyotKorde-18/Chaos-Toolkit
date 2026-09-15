package in.strikes.chaostoolkit.model.security;

public enum SecurityScenarioType {
    CREDENTIAL_STUFFING_SIMULATION("Spoofing / Repudiation", "Rapid bursts of synthetic failed credential validations"),
    PRIVILEGE_ESCALATION_SIMULATION("Elevation of Privilege", "Synthetic request with unauthorized role header"),
    MALFORMED_PAYLOAD_SIMULATION("Tampering", "Synthetically mutated request payloads testing input sanitization"),
    LATERAL_MOVEMENT_SIMULATION("Information Disclosure", "Unexpected cross-boundary service-to-service probe"),
    CONFIG_DRIFT_SIMULATION("Security Misconfiguration", "Simulation of missing or permissive security configuration");

    private final String strideCategory;
    private final String description;

    SecurityScenarioType(String strideCategory, String description) {
        this.strideCategory = strideCategory;
        this.description = description;
    }

    public String getStrideCategory() {
        return strideCategory;
    }

    public String getDescription() {
        return description;
    }
}
