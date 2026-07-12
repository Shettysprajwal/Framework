package com.pqvcf.ledger.domain.model;

import com.pqvcf.shared.domain.ValueObject;
import java.util.Objects;

/**
 * Value Object compiling blockchain-style integrity verifications outcomes.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class HashChain extends ValueObject {

    private final boolean valid;
    private final int tamperedIndex;
    private final String details;

    public HashChain(boolean valid, int tamperedIndex, String details) {
        this.valid = valid;
        this.tamperedIndex = tamperedIndex;
        this.details = details != null ? details.trim() : "";
    }

    public boolean isValid() { return valid; }
    public int getTamperedIndex() { return tamperedIndex; }
    public String getDetails() { return details; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashChain that)) return false;
        return valid == that.valid &&
                tamperedIndex == that.tamperedIndex &&
                Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, tamperedIndex, details);
    }
}
