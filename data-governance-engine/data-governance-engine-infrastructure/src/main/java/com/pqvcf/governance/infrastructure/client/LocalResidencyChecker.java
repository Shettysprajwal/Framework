package com.pqvcf.governance.infrastructure.client;

import com.pqvcf.governance.application.port.out.LocalizationChecker;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocalResidencyChecker implements LocalizationChecker {

    // Countries with strict localization constraints for specific data categories
    private final Map<String, Set<String>> localizationRules = new ConcurrentHashMap<>();

    public LocalResidencyChecker() {
        // Russia mandates local residency for personal data
        localizationRules.put("RU", Set.of("PERSONAL", "HEALTH", "FINANCIAL"));
        // China PIPL mandates local residency for critical network operators data
        localizationRules.put("CN", Set.of("PERSONAL", "FINANCIAL", "CRITICAL"));
        // India DPDP restricts sensitive localized transfer categories
        localizationRules.put("IN", Set.of("HEALTH", "CRITICAL"));
    }

    @Override
    public boolean isLocalizationMandated(String sourceCountry, String dataCategory) {
        if (sourceCountry == null || dataCategory == null) return false;

        String src = sourceCountry.trim().toUpperCase();
        String cat = dataCategory.trim().toUpperCase();

        return localizationRules.containsKey(src) && localizationRules.get(src).contains(cat);
    }
}
