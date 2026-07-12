package com.pqvcf.translation.domain.model;

/**
 * Enumeration of standard Deontic logic modal operators.
 * Used for representing normative actions in global regulations.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public enum DeonticOperator {

    /** P(a, x) - Actor is permitted to perform action in context */
    PERMISSION("P", "Permitted"),

    /** O(a, x) - Actor is obliged to perform action in context */
    OBLIGATION("O", "Obliged"),

    /** F(a, x) - Actor is forbidden from performing action in context */
    PROHIBITION("F", "Forbidden"),

    /** E(a, x) - Actor is exempt from an obligation or prohibition under conditions */
    EXEMPTION("E", "Exempted");

    private final String symbol;
    private final String description;

    DeonticOperator(String symbol, String description) {
        this.symbol = symbol;
        this.description = description;
    }

    public String getSymbol() { return symbol; }
    public String getDescription() { return description; }
}
