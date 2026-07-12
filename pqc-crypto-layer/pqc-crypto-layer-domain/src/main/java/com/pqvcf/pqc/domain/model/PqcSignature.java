package com.pqvcf.pqc.domain.model;

import com.pqvcf.shared.domain.ValueObject;
import java.util.Arrays;
import java.util.Objects;

/**
 * Value Object representing a post-quantum digital signature.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class PqcSignature extends ValueObject {

    private final byte[] signatureBytes;
    private final String keyId;
    private final String algorithm;
    private final int signatureLength;

    public PqcSignature(byte[] signatureBytes, String keyId, String algorithm) {
        this.signatureBytes = Objects.requireNonNull(signatureBytes, "Signature bytes must not be null");
        this.keyId = Objects.requireNonNull(keyId, "Key ID must not be null").trim();
        this.algorithm = Objects.requireNonNull(algorithm, "Algorithm name must not be null").trim();
        this.signatureLength = signatureBytes.length;
    }

    public byte[] getSignatureBytes() { return signatureBytes.clone(); }
    public String getKeyId() { return keyId; }
    public String getAlgorithm() { return algorithm; }
    public int getSignatureLength() { return signatureLength; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PqcSignature that)) return false;
        return signatureLength == that.signatureLength &&
                Arrays.equals(signatureBytes, that.signatureBytes) &&
                Objects.equals(keyId, that.keyId) &&
                Objects.equals(algorithm, that.algorithm);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(keyId, algorithm, signatureLength);
        result = 31 * result + Arrays.hashCode(signatureBytes);
        return result;
    }
}
