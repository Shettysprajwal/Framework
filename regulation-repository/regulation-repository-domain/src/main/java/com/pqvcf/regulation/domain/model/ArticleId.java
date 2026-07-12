package com.pqvcf.regulation.domain.model;

import com.pqvcf.shared.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing the unique identity of an {@link Article} entity.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class ArticleId extends ValueObject {

    private final UUID value;

    private ArticleId(UUID value) {
        this.value = requireNonNull(value, "ArticleId value");
    }

    public static ArticleId generate() { return new ArticleId(UUID.randomUUID()); }
    public static ArticleId of(UUID uuid) { return new ArticleId(uuid); }
    public static ArticleId fromString(String s) {
        try { return new ArticleId(UUID.fromString(s)); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid ArticleId: " + s, e); }
    }

    public UUID getValue() { return value; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticleId that)) return false;
        return Objects.equals(value, that.value);
    }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value.toString(); }
}
