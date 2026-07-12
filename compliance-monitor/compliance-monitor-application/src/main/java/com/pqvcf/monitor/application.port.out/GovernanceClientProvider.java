package com.pqvcf.monitor.application.port.out;

public interface GovernanceClientProvider {
    
    /**
     * Call the governance microservice to check if a geographic data flow is approved.
     */
    boolean verifyTransferLegality(String source, String destination, String dataCategory);
}
