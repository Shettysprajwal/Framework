package com.pqvcf.governance.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GovernanceDecisionTest {

    @Test
    @DisplayName("Should successfully create a valid GovernanceDecision value object")
    void shouldCreateDecision() {
        DataFlow flow = new DataFlow("DE", "IN", "PERSONAL", "PROCESSING");
        List<String> cites = List.of("GDPR Article 45");
        
        GovernanceDecision gd = new GovernanceDecision(
                "dec-1",
                flow,
                TransferDecision.APPROVED,
                "Approved safeguards",
                cites,
                "evidence-1"
        );

        assertThat(gd).isNotNull();
        assertThat(gd.getId()).isEqualTo("dec-1");
        assertThat(gd.getFlow()).isEqualTo(flow);
        assertThat(gd.getDecision()).isEqualTo(TransferDecision.APPROVED);
        assertThat(gd.getReasoning()).isEqualTo("Approved safeguards");
        assertThat(gd.getCitations()).contains("GDPR Article 45");
        assertThat(gd.getEvidenceLink()).isEqualTo("evidence-1");
        assertThat(gd.getCreatedAt()).isNotNull();
    }
}
