package com.pqvcf.governance.application.port.out;

public interface LocalizationChecker {

    /**
     * Check if source country mandates local residency storage for the category.
     */
    boolean isLocalizationMandated(String sourceCountry, String dataCategory);
}
