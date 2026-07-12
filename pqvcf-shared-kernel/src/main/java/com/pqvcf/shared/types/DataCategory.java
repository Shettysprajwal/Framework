package com.pqvcf.shared.types;

import com.pqvcf.shared.domain.ValueObject;

import java.util.Objects;

/**
 * Value Object representing a category of personal or sensitive data.
 *
 * <p>Data categories are used throughout compliance reasoning to determine which
 * regulations apply and what processing rules must be enforced. For example, health
 * data triggers HIPAA requirements in the US; "special category" data under GDPR
 * Art. 9 requires explicit consent.
 *
 * <p>This follows the data taxonomy used in GDPR, DPDP, and HIPAA:
 * <ul>
 *   <li>GDPR Art. 4 — personal data definition</li>
 *   <li>GDPR Art. 9 — special categories of personal data</li>
 *   <li>HIPAA PHI definition</li>
 *   <li>DPDP Act 2023 — personal data definition</li>
 * </ul>
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class DataCategory extends ValueObject {

    // ---- Standard data categories ----

    public static final DataCategory PERSONAL_IDENTIFICATION = new DataCategory("PERSONAL_IDENTIFICATION", SensitivityLevel.STANDARD);
    public static final DataCategory FINANCIAL = new DataCategory("FINANCIAL", SensitivityLevel.STANDARD);
    public static final DataCategory HEALTH = new DataCategory("HEALTH", SensitivityLevel.SPECIAL);
    public static final DataCategory GENETIC = new DataCategory("GENETIC", SensitivityLevel.SPECIAL);
    public static final DataCategory BIOMETRIC = new DataCategory("BIOMETRIC", SensitivityLevel.SPECIAL);
    public static final DataCategory LOCATION = new DataCategory("LOCATION", SensitivityLevel.STANDARD);
    public static final DataCategory COMMUNICATION = new DataCategory("COMMUNICATION", SensitivityLevel.STANDARD);
    public static final DataCategory POLITICAL_OPINION = new DataCategory("POLITICAL_OPINION", SensitivityLevel.SPECIAL);
    public static final DataCategory RELIGIOUS_BELIEF = new DataCategory("RELIGIOUS_BELIEF", SensitivityLevel.SPECIAL);
    public static final DataCategory SEXUAL_ORIENTATION = new DataCategory("SEXUAL_ORIENTATION", SensitivityLevel.SPECIAL);
    public static final DataCategory CRIMINAL_RECORD = new DataCategory("CRIMINAL_RECORD", SensitivityLevel.SPECIAL);
    public static final DataCategory CHILD_DATA = new DataCategory("CHILD_DATA", SensitivityLevel.SPECIAL);
    public static final DataCategory GENERAL = new DataCategory("GENERAL", SensitivityLevel.STANDARD);

    /**
     * Sensitivity classification of data categories.
     * SPECIAL category data triggers heightened compliance requirements under GDPR Art. 9.
     */
    public enum SensitivityLevel {
        STANDARD, // Regular personal data
        SPECIAL,  // GDPR Art. 9 special category / HIPAA PHI
        CRITICAL  // Reserved for future use (highly sensitive aggregated data)
    }

    private final String name;
    private final SensitivityLevel sensitivityLevel;

    public DataCategory(String name, SensitivityLevel sensitivityLevel) {
        this.name = requireNonBlank(name, "DataCategory name").strip().toUpperCase();
        this.sensitivityLevel = requireNonNull(sensitivityLevel, "SensitivityLevel");
    }

    public static DataCategory of(String name, SensitivityLevel level) {
        return new DataCategory(name, level);
    }

    public String getName() { return name; }
    public SensitivityLevel getSensitivityLevel() { return sensitivityLevel; }
    public boolean isSpecialCategory() { return sensitivityLevel == SensitivityLevel.SPECIAL; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataCategory that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() { return Objects.hash(name); }

    @Override
    public String toString() { return name + "(" + sensitivityLevel + ")"; }
}
