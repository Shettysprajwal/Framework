package com.pqvcf.pdp.application.service;

import com.pqvcf.pdp.application.port.in.EvaluateRequestUseCase.EvaluateCommand;
import com.pqvcf.pdp.application.port.in.EvaluateRequestUseCase.EvaluateResponse;
import com.pqvcf.pdp.application.port.out.PapPolicyProvider;
import com.pqvcf.pdp.application.port.out.PipAttributeProvider;
import com.pqvcf.pdp.application.port.out.RegulationRuleProvider;
import com.pqvcf.pdp.domain.model.DecisionEffect;
import com.pqvcf.pdp.domain.model.DecisionRequest;
import com.pqvcf.pdp.domain.model.DecisionResult;
import com.pqvcf.pdp.domain.repository.PolicyDecisionRepository;
import com.pqvcf.pdp.domain.solver.SmtComplianceSolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyDecisionServiceTest {

    @Mock
    private PapPolicyProvider papProvider;

    @Mock
    private PipAttributeProvider pipProvider;

    @Mock
    private RegulationRuleProvider ruleProvider;

    @Mock
    private SmtComplianceSolver solver;

    @Mock
    private PolicyDecisionRepository auditRepository;

    private PolicyDecisionService service;

    @BeforeEach
    void setUp() {
        service = new PolicyDecisionService(papProvider, pipProvider, ruleProvider, solver, auditRepository);
    }

    @Test
    @DisplayName("Should successfully coordinate PIP/PAP, compile SMT, execute Z3, and log audits")
    void shouldEvaluateAccessRequest() {
        EvaluateCommand command = new EvaluateCommand(
                "analyst",
                "health-records",
                "transfer",
                "IN",
                "EU",
                "Global Privacy Policy"
        );

        // Mock PAP policies
        List<PapPolicyProvider.ActiveRuleLinkDto> ruleLinks = List.of(
                new PapPolicyProvider.ActiveRuleLinkDto("link-1", "Transfer Adequacy Link", "rule-1")
        );
        List<PapPolicyProvider.ActivePolicyDto> activePolicies = List.of(
                new PapPolicyProvider.ActivePolicyDto("policy-1", "Global Privacy Policy", "Legal Team", ruleLinks)
        );
        when(papProvider.fetchActivePolicies()).thenReturn(activePolicies);

        // Mock PIP attributes
        List<PipAttributeProvider.AttributeDto> attributes = List.of(
                new PipAttributeProvider.AttributeDto("SUBJECT", "role", "analyst", "String")
        );
        PipAttributeProvider.ResolvedContextDto resolvedContext = new PipAttributeProvider.ResolvedContextDto(
                "analyst", "health-records", "transfer", attributes, true
        );
        when(pipProvider.resolveContext("analyst", "health-records", "transfer", "IN", "EU"))
                .thenReturn(resolvedContext);

        // Mock Rule SMT Fetcher
        when(ruleProvider.fetchSmtSpec("rule-1"))
                .thenReturn("(assert (=> (= action \"transfer\") (= transitive_adequate true)))");

        // Mock Z3 solver
        DecisionResult mockResult = new DecisionResult(
                DecisionEffect.PERMIT,
                "smt assertions trace",
                "Permitted by mock solver"
        );
        when(solver.solve(anyString())).thenReturn(mockResult);

        EvaluateResponse response = service.evaluate(command);

        assertThat(response).isNotNull();
        assertThat(response.effect()).isEqualTo("PERMIT");
        assertThat(response.validationLog()).contains("Permitted");
        
        verify(papProvider).fetchActivePolicies();
        verify(pipProvider).resolveContext("analyst", "health-records", "transfer", "IN", "EU");
        verify(ruleProvider).fetchSmtSpec("rule-1");
        verify(solver).solve(anyString());
        verify(auditRepository).logDecision(any(DecisionRequest.class), any(DecisionResult.class));
    }
}
