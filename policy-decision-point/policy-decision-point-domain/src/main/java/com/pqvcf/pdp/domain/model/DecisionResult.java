package com.pqvcf.pdp.domain.model;

import com.pqvcf.shared.domain.ValueObject;
import java.time.Instant;
import java.util.Objects;

/**
 * Value Object aggregating compliance verification results.
 * Holds mathematical traces (SMT specs) and solver outcomes.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class DecisionResult extends ValueObject {

    private final DecisionEffect effect;
    private final String proofTrace;
    private final String validationLog;
    private final Instant solvedAt;

    public DecisionResult(DecisionEffect effect, String proofTrace, String validationLog) {
        this.effect = Objects.requireNonNull(effect, "DecisionEffect must not be null");
        this.proofTrace = proofTrace != null ? proofTrace.trim() : "";
        this.validationLog = validationLog != null ? validationLog.trim() : "";
        this.solvedAt = Instant.now();
    }

    public DecisionEffect getEffect() { return effect; }
    public String getProofTrace() { return proofTrace; }
    public String getValidationLog() { return validationLog; }
    public Instant getSolvedAt() { return solvedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DecisionResult that)) return false;
        return effect == that.effect &&
                Objects.equals(proofTrace, that.proofTrace) &&
                Objects.equals(validationLog, that.validationLog) &&
                Objects.equals(solvedAt, that.solvedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(effect, proofTrace, validationLog, solvedAt);
    }
}
