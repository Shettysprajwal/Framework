package com.pqvcf.governance.infrastructure.client;

import com.pqvcf.governance.application.port.out.AdequacyResolver;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ClientAdequacyResolver implements AdequacyResolver {

    // Simple static adequacy whitelists map
    private final Map<String, Set<String>> whitelists = new ConcurrentHashMap<>();

    public ClientAdequacyResolver() {
        // EU adequacy whitelists (e.g. from Germany to other whitelisted states)
        whitelists.put("DE", Set.of("FR", "IT", "IN", "JP", "CH", "UK"));
        whitelists.put("FR", Set.of("DE", "IT", "IN", "JP", "CH", "UK"));
        whitelists.put("IT", Set.of("DE", "FR", "IN", "JP", "CH", "UK"));
        whitelists.put("IN", Set.of("DE", "FR", "IT", "JP", "CH", "UK", "US"));
    }

    @Override
    public boolean checkAdequacy(String sourceCountry, String targetCountry) {
        if (sourceCountry == null || targetCountry == null) return false;
        
        String src = sourceCountry.trim().toUpperCase();
        String tgt = targetCountry.trim().toUpperCase();

        if (src.equals(tgt)) return true; // Intra-country transfers are always adequate

        return whitelists.containsKey(src) && whitelists.get(src).contains(tgt);
    }
}
