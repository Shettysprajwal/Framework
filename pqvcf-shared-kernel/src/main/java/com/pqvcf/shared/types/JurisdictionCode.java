package com.pqvcf.shared.types;

import com.pqvcf.shared.domain.ValueObject;

import java.util.Objects;
import java.util.Set;

/**
 * Value Object representing a legal jurisdiction code.
 *
 * <p>Jurisdictions are identified by standardized codes based on ISO 3166-1 alpha-2
 * country codes (e.g., "EU", "IN", "US", "DE", "GB") or supranational regional codes.
 * Supranational codes use the prefix format {@code REG_<NAME>} (e.g., "REG_EEA").
 *
 * <p>Jurisdiction codes are fundamental to compliance reasoning: every data movement
 * is classified by source and destination jurisdiction to determine which regulations apply.
 *
 * <p><b>Invariants:</b>
 * <ul>
 *   <li>Code is never null or blank</li>
 *   <li>Code is always stored in UPPER_CASE</li>
 *   <li>Code length is between 2 and 20 characters</li>
 * </ul>
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class JurisdictionCode extends ValueObject {

    // ---- Well-known jurisdiction codes used throughout PQVCF ----

    /** European Union supranational jurisdiction */
    public static final JurisdictionCode EU = new JurisdictionCode("EU");

    /** European Economic Area */
    public static final JurisdictionCode EEA = new JurisdictionCode("EEA");

    /** India (Digital Personal Data Protection Act 2023) */
    public static final JurisdictionCode IN = new JurisdictionCode("IN");

    /** United States */
    public static final JurisdictionCode US = new JurisdictionCode("US");

    /** United Kingdom (post-Brexit) */
    public static final JurisdictionCode GB = new JurisdictionCode("GB");

    /** Germany (within EU, for specific German state laws) */
    public static final JurisdictionCode DE = new JurisdictionCode("DE");

    /** International / no jurisdiction restriction */
    public static final JurisdictionCode INTERNATIONAL = new JurisdictionCode("INTL");

    /** Set of all GDPR-adequate jurisdictions (GDPR Art. 45 adequacy decisions) */
    public static final Set<JurisdictionCode> GDPR_ADEQUATE_COUNTRIES = Set.of(
            new JurisdictionCode("AD"), // Andorra
            new JurisdictionCode("AR"), // Argentina
            new JurisdictionCode("CA"), // Canada
            new JurisdictionCode("CH"), // Switzerland
            new JurisdictionCode("FO"), // Faroe Islands
            new JurisdictionCode("GB"), // United Kingdom
            new JurisdictionCode("GG"), // Guernsey
            new JurisdictionCode("IL"), // Israel
            new JurisdictionCode("IM"), // Isle of Man
            new JurisdictionCode("JP"), // Japan
            new JurisdictionCode("JE"), // Jersey
            new JurisdictionCode("KR"), // South Korea
            new JurisdictionCode("NZ"), // New Zealand
            new JurisdictionCode("UY"), // Uruguay
            new JurisdictionCode("US")  // US (EU-US Data Privacy Framework, 2023)
    );

    private final String code;

    /**
     * Creates a {@code JurisdictionCode} from a raw string.
     *
     * @param code the jurisdiction code (ISO 3166-1 alpha-2 or custom prefix)
     * @throws IllegalArgumentException if code is null, blank, or invalid length
     */
    public JurisdictionCode(String code) {
        String normalized = requireNonBlank(code, "Jurisdiction code").strip().toUpperCase();
        if (normalized.length() < 2 || normalized.length() > 20) {
            throw new IllegalArgumentException(
                    "Jurisdiction code must be 2–20 characters, got: " + normalized.length());
        }
        this.code = normalized;
    }

    /**
     * Factory method for creating a {@code JurisdictionCode} from a string.
     *
     * @param code the raw jurisdiction code
     * @return a {@code JurisdictionCode} instance
     */
    public static JurisdictionCode of(String code) {
        return new JurisdictionCode(code);
    }

    /**
     * Returns the jurisdiction code string.
     *
     * @return the code, always non-null, non-blank, upper-case
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns {@code true} if this jurisdiction is a GDPR-adequate country.
     *
     * @return whether this jurisdiction has an EU adequacy decision
     */
    public boolean isGdprAdequate() {
        return GDPR_ADEQUATE_COUNTRIES.contains(this) || EU.equals(this) || EEA.equals(this);
    }

    /**
     * Returns {@code true} if this jurisdiction is an EU member state or the EU itself.
     *
     * @return whether this jurisdiction falls under GDPR directly
     */
    public boolean isEuJurisdiction() {
        return EU.equals(this) || EEA.equals(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JurisdictionCode that)) return false;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
