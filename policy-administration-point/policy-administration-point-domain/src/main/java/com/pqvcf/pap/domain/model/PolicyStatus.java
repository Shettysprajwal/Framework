package com.pqvcf.pap.domain.model;

/**
 * Lifecycle status of an organizational compliance policy.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public enum PolicyStatus {
    
    /** Draft stage. Policy is under review, bindings are not enforced */
    DRAFT("Draft"),

    /** Active stage. Policy is enforced by the PDP (Module 5) */
    ACTIVE("Active"),

    /** Deprecated stage. Retired policy, kept for historical validation */
    DEPRECATED("Deprecated");

    private final String displayName;

    PolicyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
