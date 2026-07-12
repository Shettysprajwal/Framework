package com.pqvcf.pqc.domain.model;

/**
 * Supported Post-Quantum Cryptographic (PQC) algorithm types.
 * Aligns with NIST FIPS 203, 204, and 205 specifications.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public enum PqcKeyType {
    
    /** ML-KEM-768 (FIPS 203) — Post-Quantum Key Encapsulation Mechanism */
    ML_KEM_768("ML-KEM-768"),

    /** ML-DSA-65 (FIPS 204) — Post-Quantum Digital Signature Algorithm */
    ML_DSA_65("ML-DSA-65"),

    /** SLH-DSA-SHA2-256f (FIPS 205) — Stateless Hash-Based Signature Scheme */
    SLH_DSA_256("SLH-DSA-SHA2-256f");

    private final String algorithmName;

    PqcKeyType(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }
}
