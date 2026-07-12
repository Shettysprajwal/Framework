package com.pqvcf.translation.domain.model;

import com.pqvcf.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

/**
 * Type-safe identifier for a LegalRule aggregate.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class LegalRuleId extends ValueObject {

    private final UUID value;

    private LegalRuleId(UUID value) {
        this.value = requireNonNull(value, "LegalRuleId value");
    }

    public static LegalRuleId generate() {
        return new LegalRuleId(UUID.randomUUID());
    }

    public static LegalRuleId of(UUID value) {
        return new LegalRuleId(value);
    }

    public static LegalRuleId fromString(String str) {
        try {
            return new LegalRuleId(UUID.fromString(str));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid LegalRuleId: " + str, e);
        }
    }

    public UUID getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LegalRuleId that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
