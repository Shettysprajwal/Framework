package com.pqvcf.pip.domain.model;

/**
 * Categorization category for PIP contextual attributes based on XACML standards.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public enum AttributeCategory {
    
    /** Attributes describing the requester (e.g. user role, security clearance) */
    SUBJECT("Subject"),

    /** Attributes describing the target resource (e.g. data categories, classifications) */
    RESOURCE("Resource"),

    /** Attributes describing the requested action (e.g. read, transfer, delete) */
    ACTION("Action"),

    /** Attributes describing the execution environment (e.g. time, geo-location adequacy path) */
    ENVIRONMENT("Environment");

    private final String displayName;

    AttributeCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
