package com.pqvcf.pip.domain.model;

import com.pqvcf.shared.domain.ValueObject;
import java.util.Objects;

/**
 * Value Object representing a contextual attribute in the Policy Information Point.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class ContextAttribute extends ValueObject {

    private final AttributeCategory category;
    private final String key;
    private final String value;
    private final String dataType;

    public ContextAttribute(AttributeCategory category, String key, String value, String dataType) {
        this.category = Objects.requireNonNull(category, "Category must not be null");
        this.key = requireNonBlank(key, "Attribute key");
        this.value = value != null ? value.trim() : "";
        this.dataType = dataType != null ? dataType.trim() : "String";
    }

    private static String requireNonBlank(String val, String fieldName) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        return val.trim();
    }

    public AttributeCategory getCategory() { return category; }
    public String getKey() { return key; }
    public String getValue() { return value; }
    public String getDataType() { return dataType; }

    public boolean asBoolean() {
        return Boolean.parseBoolean(value);
    }

    public int asInteger() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContextAttribute that)) return false;
        return category == that.category &&
                Objects.equals(key, that.key) &&
                Objects.equals(value, that.value) &&
                Objects.equals(dataType, that.dataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, key, value, dataType);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s = %s (%s)", category, key, value, dataType);
    }
}
