package com.pqvcf.pip.domain.resolver;

/**
 * Port interface interfacing with Graph Store (Module 1) to verify adequacy agreements transitivities.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface AdequacyPathResolver {

    /**
     * Resolve whether data can transit safely from sourceCountry to targetCountry
     * based on transitive adequacy agreement links.
     */
    boolean isAdequate(String sourceCountry, String targetCountry);
}
