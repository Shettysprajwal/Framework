package com.pqvcf.ledger.application.port.out;

public interface LedgerHasher {
    
    /**
     * Compute a cryptographic SHA-256 hash digest of node elements.
     */
    String calculateHash(String prevHash, String timestamp, String action, String actor, String target, String decision);
}
