package com.pqvcf.monitor.application.service;

import com.pqvcf.monitor.application.port.in.IngestEventUseCase.IngestEventCommand;
import com.pqvcf.monitor.application.port.out.GovernanceClientProvider;
import com.pqvcf.monitor.domain.model.ComplianceEvent;
import com.pqvcf.monitor.domain.model.PolicyViolation;
import com.pqvcf.monitor.domain.repository.ComplianceMonitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplianceMonitoringServiceTest {

    @Mock
    private GovernanceClientProvider governanceClient;

    @Mock
    private ComplianceMonitorRepository monitorRepository;

    private ComplianceMonitoringService service;

    @BeforeEach
    void setUp() {
        service = new ComplianceMonitoringService(governanceClient, monitorRepository);
    }

    @Test
    @DisplayName("Should successfully ingest compliant events without raising critical alerts")
    void shouldIngestCompliantEvent() {
        IngestEventCommand command = new IngestEventCommand("DE", "IN", "PERSONAL", 1024);

        when(governanceClient.verifyTransferLegality("DE", "IN", "PERSONAL")).thenReturn(true);

        service.ingest(command);

        verify(monitorRepository).logEvent(any(ComplianceEvent.class));
        verify(monitorRepository, never()).raiseViolation(any(PolicyViolation.class));
    }

    @Test
    @DisplayName("Should raise critical violations when data flows are blocked by governance rules")
    void shouldRaiseViolationOnBlockedFlow() {
        IngestEventCommand command = new IngestEventCommand("RU", "DE", "PERSONAL", 1024);

        when(governanceClient.verifyTransferLegality("RU", "DE", "PERSONAL")).thenReturn(false);

        service.ingest(command);

        verify(monitorRepository).logEvent(any(ComplianceEvent.class));
        verify(monitorRepository).raiseViolation(any(PolicyViolation.class));
    }
}
