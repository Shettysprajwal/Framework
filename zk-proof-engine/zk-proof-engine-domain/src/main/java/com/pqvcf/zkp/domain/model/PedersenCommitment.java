package com.pqvcf.zkp.domain.model;

import com.pqvcf.shared.domain.ValueObject;
import java.util.Arrays;
import java.util.Objects;

/**
 * Value Object representing a Pedersen Commitment.
 * Formulated as C = g^x * h^r (computationally binding, perfectly hiding).
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class PedersenCommitment extends ValueObject {

    private final byte[] commitmentBytes;
    private final byte[] blindingFactor;
    private final long secretValue;

    public PedersenCommitment(byte[] commitmentBytes, byte[] blindingFactor, long secretValue) {
        this.commitmentBytes = Objects.requireNonNull(commitmentBytes, "Commitment bytes required");
        this.blindingFactor = Objects.requireNonNull(blindingFactor, "Blinding factor required");
        this.secretValue = secretValue;
    }

    public byte[] getCommitmentBytes() { return commitmentBytes.clone(); }
    public byte[] getBlindingFactor() { return blindingFactor.clone(); }
    public long getSecretValue() { return secretValue; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PedersenCommitment that)) return false;
        return secretValue == that.secretValue &&
                Arrays.equals(commitmentBytes, that.commitmentBytes) &&
                Arrays.equals(blindingFactor, that.blindingFactor);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(secretValue);
        result = 31 * result + Arrays.hashCode(commitmentBytes);
        result = 31 * result + Arrays.hashCode(blindingFactor);
        return result;
    }
}
