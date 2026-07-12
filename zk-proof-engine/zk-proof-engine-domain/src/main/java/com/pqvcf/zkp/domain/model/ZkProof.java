package com.pqvcf.zkp.domain.model;

import com.pqvcf.shared.domain.ValueObject;
import java.util.Arrays;
import java.util.Objects;

/**
 * Value Object representing a generated Sigma protocol Zero-Knowledge proof.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class ZkProof extends ValueObject {

    private final String proofId;
    private final ZkProofType proofType;
    private final PedersenCommitment commitment;
    private final byte[] challengeBytes;
    private final byte[] responseBytes;
    private final String publicInputs;
    private final boolean verified;

    public ZkProof(
            String proofId,
            ZkProofType proofType,
            PedersenCommitment commitment,
            byte[] challengeBytes,
            byte[] responseBytes,
            String publicInputs,
            boolean verified) {
        this.proofId = Objects.requireNonNull(proofId, "Proof ID required").trim();
        this.proofType = Objects.requireNonNull(proofType, "Proof Type required");
        this.commitment = Objects.requireNonNull(commitment, "Pedersen Commitment required");
        this.challengeBytes = Objects.requireNonNull(challengeBytes, "Challenge bytes required");
        this.responseBytes = Objects.requireNonNull(responseBytes, "Response bytes required");
        this.publicInputs = publicInputs != null ? publicInputs.trim() : "";
        this.verified = verified;
    }

    public String getProofId() { return proofId; }
    public ZkProofType getProofType() { return proofType; }
    public PedersenCommitment getCommitment() { return commitment; }
    public byte[] getChallengeBytes() { return challengeBytes.clone(); }
    public byte[] getResponseBytes() { return responseBytes.clone(); }
    public String getPublicInputs() { return publicInputs; }
    public boolean isVerified() { return verified; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ZkProof that)) return false;
        return verified == that.verified &&
                Objects.equals(proofId, that.proofId) &&
                proofType == that.proofType &&
                Objects.equals(commitment, that.commitment) &&
                Arrays.equals(challengeBytes, that.challengeBytes) &&
                Arrays.equals(responseBytes, that.responseBytes) &&
                Objects.equals(publicInputs, that.publicInputs);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(proofId, proofType, commitment, publicInputs, verified);
        result = 31 * result + Arrays.hashCode(challengeBytes);
        result = 31 * result + Arrays.hashCode(responseBytes);
        return result;
    }
}
