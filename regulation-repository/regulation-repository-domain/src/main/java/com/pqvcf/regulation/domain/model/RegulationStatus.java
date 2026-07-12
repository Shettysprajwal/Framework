package com.pqvcf.regulation.domain.model;

/**
 * Enumeration of lifecycle statuses for a {@link Regulation}.
 *
 * <p>The lifecycle of a regulation in the PQVCF system:
 * <pre>
 *   DRAFT ──→ ACTIVE ──→ DEPRECATED
 *     ↑                      │
 *     └──────────────────────┘ (re-activation via new version)
 * </pre>
 *
 * <p>Only ACTIVE regulations are used in compliance evaluation.
 * DRAFT regulations are in preparation and not yet enforced.
 * DEPRECATED regulations are retained for historical proof validation only.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public enum RegulationStatus {

    /**
     * Regulation is being prepared and is not yet enforced.
     * Formal specifications may be incomplete.
     */
    DRAFT("Draft", "Under preparation, not yet enforced"),

    /**
     * Regulation is currently in force and used in compliance evaluation.
     */
    ACTIVE("Active", "Currently in force and enforced"),

    /**
     * Regulation has been superseded by a newer version or repealed.
     * Retained for historical compliance proof validation.
     */
    DEPRECATED("Deprecated", "Superseded or repealed, retained for history");

    private final String displayName;
    private final String description;

    RegulationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public boolean isActive() { return this == ACTIVE; }
    public boolean isEnforced() { return this == ACTIVE; }
}
