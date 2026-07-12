package com.pqvcf.pap.application.service;

import com.pqvcf.pap.application.port.in.CreatePolicyUseCase.CreatePolicyCommand;
import com.pqvcf.pap.application.port.in.CreatePolicyUseCase.PolicyResponse;
import com.pqvcf.pap.application.port.in.LinkRuleUseCase.LinkRuleCommand;
import com.pqvcf.pap.domain.model.Policy;
import com.pqvcf.pap.domain.model.PolicyId;
import com.pqvcf.pap.domain.model.PolicyStatus;
import com.pqvcf.pap.domain.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyAdministrationServiceTest {

    @Mock
    private PolicyRepository repository;

    private PolicyAdministrationService service;

    @BeforeEach
    void setUp() {
        service = new PolicyAdministrationService(repository);
    }

    @Test
    @DisplayName("Should successfully create policy and save to database")
    void shouldCreatePolicy() {
        CreatePolicyCommand command = new CreatePolicyCommand(
                "GDPR Transfer Safe Policy",
                "Compliance Ops",
                "Organizational mappings"
        );

        when(repository.existsByName("GDPR Transfer Safe Policy")).thenReturn(false);
        when(repository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyResponse response = service.create(command);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("GDPR Transfer Safe Policy");
        assertThat(response.status()).isEqualTo(PolicyStatus.DRAFT.name());
        verify(repository).save(any(Policy.class));
    }

    @Test
    @DisplayName("Should fail creating policy if name is duplicated in repository database")
    void shouldFailIfDuplicateName() {
        CreatePolicyCommand command = new CreatePolicyCommand(
                "Duplicate Policy",
                "Owner",
                "Desc"
        );

        when(repository.existsByName("Duplicate Policy")).thenReturn(true);

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should bind/link regulatory rule to draft policy successfully")
    void shouldLinkRuleToDraftPolicy() {
        Policy policy = Policy.create("Corporate Policy", "Owner", "Desc");
        PolicyId id = policy.getId();
        UUID regRuleId = UUID.randomUUID();

        LinkRuleCommand command = new LinkRuleCommand(
                id.toString(),
                "Customer Data Link",
                regRuleId.toString(),
                "Rule maps to Module 2"
        );

        when(repository.findById(id)).thenReturn(Optional.of(policy));
        when(repository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyResponse response = service.linkRule(command);

        assertThat(response.ruleLinks()).hasSize(1);
        assertThat(response.ruleLinks().get(0).organizationalRuleName()).isEqualTo("Customer Data Link");
        assertThat(response.ruleLinks().get(0).regulatoryRuleId()).isEqualTo(regRuleId.toString());
    }
}
