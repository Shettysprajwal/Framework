package com.pqvcf.governance.application.port.out;

public interface AdequacyResolver {
    
    /**
     * Determine if target country has adequacy status under source country legal frameworks.
     */
    boolean checkAdequacy(String sourceCountry, String targetCountry);
}
