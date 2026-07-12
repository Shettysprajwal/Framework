package com.pqvcf.governance.application.service;

import com.pqvcf.governance.application.port.in.EvaluateFlowUseCase.EvaluateFlowCommand;
import com.pqvcf.governance.application.port.in.EvaluateFlowUseCase.FlowDecisionResponseDto;
import com.pqvcf.governance.application.port.out.AdequacyResolver;
import com.pqvcf.governance.application.port.out.LocalizationChecker;
import com.pqvcf.governance.domain.model.GovernanceDecision;
import com.pqvcf.governance.domain.repository.DataGovernanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataGovernanceServiceTest {

    @Mock
    private AdequacyResolver adequacyResolver;

    @Mock
    private LocalizationChecker localizationChecker;

    @Mock
    private DataGovernanceRepository repository;

    private DataGovernanceService service;

    @BeforeEach
    void setUp() {
        service = new DataGovernanceService(adequacyResolver, localizationChecker, repository);
    }

    @Test
    @DisplayName("Should successfully block transfer flows if localization is mandated")
    void shouldBlockLocalizedFlow() {
        EvaluateFlowCommand command = new EvaluateFlowCommand("RU", "DE", "PERSONAL", "BACKUP");

        when(localizationChecker.isLocalizationMandated("RU", "PERSONAL")).thenReturn(true);

        FlowDecisionResponseDto dto = service.evaluateFlow(command);

        assertThat(dto).isNotNull();
        assertThat(dto.decision()).isEqualTo("BLOCKED");
        assertThat(dto.citations()).contains("Russia FFDL No. 242-FZ");

        verify(localizationChecker).isLocalizationMandated("RU", "PERSONAL");
        verify(repository).save(any(GovernanceDecision.class));
    }

    @Test
    @DisplayName("Should approve transfer flows if target country has adequacy status")
    void shouldApproveAdequateFlow() {
        EvaluateFlowCommand command = new EvaluateFlowCommand("DE", "IN", "PERSONAL", "PROCESSING");

        when(localizationChecker.isLocalizationMandated("DE", "PERSONAL")).thenReturn(false);
        when(adequacyResolver.checkAdequacy("DE", "IN")).thenReturn(true);

        FlowDecisionResponseDto dto = service.evaluateFlow(command);

        assertThat(dto).isNotNull();
        assertThat(dto.decision()).isEqualTo("APPROVED");
        assertThat(dto.citations()).contains("GDPR Article 45 Adequacy Decision");

        verify(adequacyResolver).checkAdequacy("DE", "IN");
        verify(repository).save(any(GovernanceDecision.class));
    }
}
