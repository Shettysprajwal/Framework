package com.pqvcf.pap.domain.model;

import com.pqvcf.pap.domain.event.PolicyPublishedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyTest {

    @Test
    @DisplayName("Should successfully create a Policy in DRAFT status")
    void shouldCreatePolicy() {
        Policy policy = Policy.create(
                "Corporate Data Security Policy",
                "Compliance Team",
                "Protects customer health and personal records"
        );

        assertThat(policy).isNotNull();
        assertThat(policy.getStatus()).isEqualTo(PolicyStatus.DRAFT);
        assertThat(policy.getName()).isEqualTo("Corporate Data Security Policy");
        assertThat(policy.getOwner()).isEqualTo("Compliance Team");
        assertThat(policy.getRuleLinks()).isEmpty();
    }

    @Test
    @DisplayName("Should add rule links successfully and prevent duplicates")
    void shouldAddRuleLinks() {
        Policy policy = Policy.create("Data Policy", "Owner", "Desc");
        UUID regRuleId = UUID.randomUUID();

        RuleLink link = policy.addRuleLink("Rule 1", regRuleId, "Maps to GDPR Art 46");

        assertThat(link).isNotNull();
        assertThat(link.getOrganizationalRuleName()).isEqualTo("Rule 1");
        assertThat(link.getRegulatoryRuleId()).isEqualTo(regRuleId);
        assertThat(policy.getRuleLinks()).hasSize(1);

        // Try adding duplicate rule link name
        assertThatThrownBy(() -> policy.addRuleLink("Rule 1", UUID.randomUUID(), "Duplicate"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should transition from DRAFT to ACTIVE and emit activation event")
    void shouldTransitionToActive() {
        Policy policy = Policy.create("Data Policy", "Owner", "Desc");
        assertThat(policy.getStatus()).isEqualTo(PolicyStatus.DRAFT);

        policy.activate();

        assertThat(policy.getStatus()).isEqualTo(PolicyStatus.ACTIVE);
        
        var events = policy.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(PolicyPublishedEvent.class);
        
        PolicyPublishedEvent event = (PolicyPublishedEvent) events.get(0);
        assertThat(event.name()).isEqualTo("Data Policy");
        assertThat(event.status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should deprecate active policies but fail on draft policies")
    void shouldHandleDeprecationFlow() {
        Policy policy = Policy.create("Data Policy", "Owner", "Desc");

        // Fail to deprecate draft
        assertThatThrownBy(policy::deprecate)
                .isInstanceOf(IllegalStateException.class);

        // Activate and deprecate
        policy.activate();
        policy.deprecate();

        assertThat(policy.getStatus()).isEqualTo(PolicyStatus.DEPRECATED);
    }
}
