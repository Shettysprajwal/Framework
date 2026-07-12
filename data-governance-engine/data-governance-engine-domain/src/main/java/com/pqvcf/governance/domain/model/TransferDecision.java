package com.pqvcf.governance.domain.model;

/**
 * Compliance transfer evaluation results.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public enum TransferDecision {
    
    /** Legally approved cross-border transfer pathway */
    APPROVED("Approved"),

    /** Blocked transfer path (e.g. localization restrictions or explicit legal prohibitions) */
    BLOCKED("Blocked"),

    /** Conditional pathway (requires standard contractual clauses, encryption, or ZK/PQC validation) */
    CONDITIONAL("Conditional");

    private final String displayName;

    TransferDecision(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
