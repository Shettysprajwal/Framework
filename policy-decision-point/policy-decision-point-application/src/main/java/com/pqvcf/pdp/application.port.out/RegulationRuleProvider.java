package com.pqvcf.pdp.application.port.out;

/**
 * Output port interface to query SMT specifications from Module 2 (Rule Translation Engine).
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface RegulationRuleProvider {
    
    /**
     * Fetch raw Z3 SMT-LIB2 specifications associated with translated regulatory rule.
     */
    String fetchSmtSpec(String regulatoryRuleId);
}
