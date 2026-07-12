package com.pqvcf.regulation.domain.model;

/**
 * Classification of deontic types for clauses and articles in the PQVCF system.
 *
 * <p>Deontic logic is the formal logic of obligations, permissions, and prohibitions.
 * It is the mathematical foundation for representing legal norms as machine-readable rules.
 *
 * <p>Every regulatory clause maps to one of these deontic types, enabling the
 * compliance engine to reason about what is allowed, required, or forbidden.
 *
 * <p><b>Formal Semantics (Deontic Logic):</b>
 * <ul>
 *   <li>{@code PERMISSION} → {@code P(A, C)} — Agent A is permitted to do action in context C</li>
 *   <li>{@code OBLIGATION} → {@code O(A, C)} — Agent A is obliged to do action in context C</li>
 *   <li>{@code PROHIBITION} → {@code F(A, C)} — Agent A is forbidden to do action in context C</li>
 *   <li>{@code EXEMPTION} → {@code ¬O(A, C)} under special condition — obligation waived</li>
 * </ul>
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public enum ClauseType {

    /** Grants permission to perform an action under specified conditions */
    PERMISSION("Permission", "P", "#22c55e"),

    /** Imposes an obligation to perform an action */
    OBLIGATION("Obligation", "O", "#f59e0b"),

    /** Prohibits an action absolutely */
    PROHIBITION("Prohibition", "F", "#ef4444"),

    /** Provides an exemption from an obligation or prohibition */
    EXEMPTION("Exemption", "E", "#8b5cf6"),

    /** Defines a term used in other clauses (definitional) */
    DEFINITION("Definition", "D", "#6b7280"),

    /** General provision not strictly deontic */
    PROVISION("Provision", "-", "#94a3b8");

    private final String displayName;
    /** Short formal deontic operator symbol */
    private final String symbol;
    /** Hex color for UI badge rendering */
    private final String color;

    ClauseType(String displayName, String symbol, String color) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public String getSymbol() { return symbol; }
    public String getColor() { return color; }
    public boolean isDeontic() { return this != DEFINITION && this != PROVISION; }
}
