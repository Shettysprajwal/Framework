package com.pqvcf.shared.types;

import com.pqvcf.shared.domain.ValueObject;
import java.util.Objects;

/**
 * Value Object representing a pagination request parameters.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class PageRequest extends ValueObject {

    private final int page;
    private final int size;

    public PageRequest(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }
        this.page = page;
        this.size = size;
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

    public int getPage() { return page; }
    public int getSize() { return size; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageRequest that)) return false;
        return page == that.page && size == that.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size);
    }
}
