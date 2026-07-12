package com.pqvcf.shared.domain;

import java.util.Objects;

/**
 * Base class for all Value Objects in the PQVCF domain model.
 *
 * <p>A Value Object is immutable and has no identity of its own — two Value Objects
 * with the same attributes are considered equal. Examples: {@code JurisdictionCode},
 * {@code RegulationId}, {@code DataCategory}.
 *
 * <p>Value Objects must override {@code equals()} and {@code hashCode()} to provide
 * structural equality semantics. Subclasses should be declared {@code final} where
 * possible, and all fields should be {@code final} to guarantee immutability.
 *
 * <p>This base class provides a fail-fast null check helper to enforce the invariant
 * that a Value Object is always valid upon construction.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public abstract class ValueObject {

    /**
     * Asserts that a value is not null, throwing {@link IllegalArgumentException}
     * with a descriptive message if it is.
     *
     * @param value   the value to check
     * @param message the description of the field (for error messages)
     * @param <T>     the type of the value
     * @return the value, guaranteed non-null
     * @throws IllegalArgumentException if the value is null
     */
    protected static <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message + " must not be null");
        }
        return value;
    }

    /**
     * Asserts that a string is non-null and non-blank.
     *
     * @param value   the string to check
     * @param message the description of the field (for error messages)
     * @return the value, guaranteed non-null and non-blank
     * @throws IllegalArgumentException if the value is null or blank
     */
    protected static String requireNonBlank(String value, String message) {
        requireNonNull(value, message);
        if (value.isBlank()) {
            throw new IllegalArgumentException(message + " must not be blank");
        }
        return value;
    }

    /**
     * Subclasses must implement equality based on their structural attributes.
     */
    @Override
    public abstract boolean equals(Object o);

    /**
     * Subclasses must implement {@code hashCode()} consistent with {@code equals()}.
     */
    @Override
    public abstract int hashCode();
}
