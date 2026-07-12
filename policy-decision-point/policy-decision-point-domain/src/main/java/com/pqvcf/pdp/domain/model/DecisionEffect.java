package com.pqvcf.pdp.domain.model;

/**
 * Compliance decision effect returned by the PDP.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public enum DecisionEffect {
    
    /** The request satisfies compliance specifications and is permitted */
    PERMIT("Permit"),

    /** The request violates compliance specifications and is denied */
    DENY("Deny"),

    /** The request context cannot be resolved or maps to no active rules */
    INDETERMINATE("Indeterminate");

    private final String displayName;

    DecisionEffect(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
