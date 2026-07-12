package com.pqvcf.pdp.infrastructure.client;

import com.pqvcf.pdp.application.port.out.RegulationRuleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ClientRegulationRuleProvider implements RegulationRuleProvider {

    private static final Logger log = LoggerFactory.getLogger(ClientRegulationRuleProvider.class);

    private final JdbcTemplate jdbcTemplate;

    public ClientRegulationRuleProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String fetchSmtSpec(String regulatoryRuleId) {
        log.info("Fetching SMT spec for translated rule: {}", regulatoryRuleId);
        
        try {
            // Retrieve directly from postgres translated_rules table populated in Module 2
            String query = "SELECT smt_spec FROM translated_rules WHERE id = ?";
            String smt = jdbcTemplate.queryForObject(query, String.class, UUID.fromString(regulatoryRuleId));
            
            if (smt != null && !smt.isBlank()) {
                log.info("Found SMT spec in database for: {}", regulatoryRuleId);
                return smt;
            }
        } catch (Exception e) {
            log.warn("Could not retrieve SMT spec from DB ({}). Executing static fallback spec.", e.getMessage());
        }

        return resolveFallback();
    }

    private String resolveFallback() {
        // Returns the Z3 SMT assertion requiring transitivity check:
        // "If action is transfer, transitive_adequate must be true"
        return "(assert (=> (= action \"transfer\") (= transitive_adequate true)))";
    }
}
