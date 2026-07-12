package com.pqvcf.governance.domain.model;

import com.pqvcf.shared.domain.ValueObject;
import java.util.Objects;

/**
 * Value Object representing the cross-border data flow parameters.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class DataFlow extends ValueObject {

    private final String sourceCountry;
    private final String targetCountry;
    private final String dataCategory;
    private final String processingPurpose;

    public DataFlow(
            String sourceCountry,
            String targetCountry,
            String dataCategory,
            String processingPurpose) {
        this.sourceCountry = requireNonBlank(sourceCountry, "Source Country");
        this.targetCountry = requireNonBlank(targetCountry, "Target Country");
        this.dataCategory = requireNonBlank(dataCategory, "Data Category");
        this.processingPurpose = requireNonBlank(processingPurpose, "Processing Purpose");
    }

    private static String requireNonBlank(String val, String field) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
        return val.trim();
    }

    public String getSourceCountry() { return sourceCountry; }
    public String getTargetCountry() { return targetCountry; }
    public String getDataCategory() { return dataCategory; }
    public String getProcessingPurpose() { return processingPurpose; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataFlow that)) return false;
        return Objects.equals(sourceCountry, that.sourceCountry) &&
                Objects.equals(targetCountry, that.targetCountry) &&
                Objects.equals(dataCategory, that.dataCategory) &&
                Objects.equals(processingPurpose, that.processingPurpose);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceCountry, targetCountry, dataCategory, processingPurpose);
    }
}
