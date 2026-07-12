package com.pqvcf.pqc.domain.model;

import com.pqvcf.shared.domain.ValueObject;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * Value Object representing a PQC public-private key pair inside the KMS context.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class PqcKeyPair extends ValueObject {

    private final String keyId;
    private final PqcKeyType keyType;
    private final byte[] publicKeyBytes;
    private final byte[] privateKeyBytes;
    private final Instant createdAt;
    private final Instant expiresAt;

    public PqcKeyPair(
            String keyId,
            PqcKeyType keyType,
            byte[] publicKeyBytes,
            byte[] privateKeyBytes,
            Instant createdAt,
            Instant expiresAt) {
        this.keyId = Objects.requireNonNull(keyId, "Key ID required").trim();
        this.keyType = Objects.requireNonNull(keyType, "Key Type required");
        this.publicKeyBytes = Objects.requireNonNull(publicKeyBytes, "Public key bytes required");
        this.privateKeyBytes = Objects.requireNonNull(privateKeyBytes, "Private key bytes required");
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresAt = Objects.requireNonNull(expiresAt);
    }

    public String getKeyId() { return keyId; }
    public PqcKeyType getKeyType() { return keyType; }
    public byte[] getPublicKeyBytes() { return publicKeyBytes.clone(); }
    public byte[] getPrivateKeyBytes() { return privateKeyBytes.clone(); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PqcKeyPair that)) return false;
        return Objects.equals(keyId, that.keyId) &&
                keyType == that.keyType &&
                Arrays.equals(publicKeyBytes, that.publicKeyBytes) &&
                Arrays.equals(privateKeyBytes, that.privateKeyBytes) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(expiresAt, that.expiresAt);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(keyId, keyType, createdAt, expiresAt);
        result = 31 * result + Arrays.hashCode(publicKeyBytes);
        result = 31 * result + Arrays.hashCode(privateKeyBytes);
        return result;
    }
}
