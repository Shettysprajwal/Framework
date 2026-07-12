package com.pqvcf.zkp.domain.model;

/**
 * Categorization of Zero-Knowledge proofs for compliance predicates.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public enum ZkProofType {
    
    /** Proves physical data residency zone matches requirements without revealing IP or exact host */
    DATA_RESIDENCY("DataResidency"),

    /** Proves active transfer basis satisfies GDPR Article 46 without exposing exact contracts details */
    TRANSFER_BASIS("TransferBasis"),

    /** Proves data processing matches user-consented targets without revealing identity data */
    PURPOSE_LIMITATION("PurposeLimitation");

    private final String typeName;

    ZkProofType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
