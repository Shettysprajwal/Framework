package com.pqvcf.regulation.domain.model;

import com.pqvcf.shared.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing the unique identity of a {@link Regulation} aggregate.
 *
 * <p>RegulationId wraps a UUID to provide type-safety, preventing accidental
 * confusion between different aggregate identities (e.g., passing an ArticleId
 * where a RegulationId is expected would be caught at compile time).
 *
 * <p>Research Note: Regulation identities are embedded in compliance proof objects
 * and formal verification reports. Using a strongly-typed ID ensures the
 * proof chain remains consistent and machine-verifiable.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class RegulationId extends ValueObject {

    private final UUID value;

    private RegulationId(UUID value) {
        this.value = requireNonNull(value, "RegulationId value");
    }

    /**
     * Generates a new unique RegulationId.
     *
     * @return a new RegulationId backed by a random UUID
     */
    public static RegulationId generate() {
        return new RegulationId(UUID.randomUUID());
    }

    /**
     * Reconstructs a RegulationId from an existing UUID.
     *
     * @param uuid the UUID to wrap
     * @return the RegulationId
     */
    public static RegulationId of(UUID uuid) {
        return new RegulationId(uuid);
    }

    /**
     * Parses a RegulationId from a UUID string.
     *
     * @param uuidString the UUID string representation
     * @return the RegulationId
     * @throws IllegalArgumentException if the string is not a valid UUID
     */
    public static RegulationId fromString(String uuidString) {
        try {
            return new RegulationId(UUID.fromString(uuidString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid RegulationId format: " + uuidString, e);
        }
    }

    public UUID getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegulationId that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value.toString(); }
}
