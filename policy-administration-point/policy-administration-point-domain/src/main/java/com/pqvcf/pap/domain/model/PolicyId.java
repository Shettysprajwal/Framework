package com.pqvcf.pap.domain.model;

import com.pqvcf.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

/**
 * Type-safe identifier for the Policy aggregate.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class PolicyId extends ValueObject {

    private final UUID value;

    private PolicyId(UUID value) {
        this.value = requireNonNull(value, "PolicyId value");
    }

    public static PolicyId generate() {
        return new PolicyId(UUID.randomUUID());
    }

    public static PolicyId of(UUID value) {
        return new PolicyId(value);
    }

    public static PolicyId fromString(String str) {
        try {
            return new PolicyId(UUID.fromString(str));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid PolicyId: " + str, e);
        }
    }

    public UUID getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PolicyId that)) return false;
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
