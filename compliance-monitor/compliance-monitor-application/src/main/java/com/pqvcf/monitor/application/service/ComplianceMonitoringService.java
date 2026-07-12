package com.pqvcf.monitor.application.service;

import com.pqvcf.monitor.application.port.in.IngestEventUseCase;
import com.pqvcf.monitor.application.port.in.MonitorQueryUseCase;
import com.pqvcf.monitor.application.port.out.GovernanceClientProvider;
import com.pqvcf.monitor.domain.model.ComplianceEvent;
import com.pqvcf.monitor.domain.model.PolicyViolation;
import com.pqvcf.monitor.domain.model.SlaMetrics;
import com.pqvcf.monitor.domain.repository.ComplianceMonitorRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ComplianceMonitoringService implements IngestEventUseCase, MonitorQueryUseCase {

    private final GovernanceClientProvider governanceClient;
    private final ComplianceMonitorRepository monitorRepository;

    public ComplianceMonitoringService(
            GovernanceClientProvider governanceClient,
            ComplianceMonitorRepository monitorRepository) {
        this.governanceClient = governanceClient;
        this.monitorRepository = monitorRepository;
    }

    @Override
    public void ingest(IngestEventCommand command) {
        ComplianceEvent event = new ComplianceEvent(
                UUID.randomUUID().toString(),
                command.source(),
                command.destination(),
                command.dataCategory(),
                command.sizeBytes(),
                Instant.now()
        );

        monitorRepository.logEvent(event);

        // Verify transfer pathway against Module 8 Data Governance rules
        boolean isApproved = governanceClient.verifyTransferLegality(
                event.getSource(),
                event.getDestination(),
                event.getDataCategory()
        );

        if (!isApproved) {
            PolicyViolation violation = new PolicyViolation(
                    UUID.randomUUID().toString(),
                    event.getId(),
                    "CRITICAL",
                    "Mandatory data residency restriction violated",
                    String.format("Data transfer of %s records from %s to %s violates local compliance localization boundaries.",
                            event.getDataCategory(), event.getSource(), event.getDestination())
            );
            monitorRepository.raiseViolation(violation);
        } else if ("HEALTH".equalsIgnoreCase(event.getDataCategory())) {
            // Health data requires explicit warning controls
            PolicyViolation warning = new PolicyViolation(
                    UUID.randomUUID().toString(),
                    event.getId(),
                    "WARNING",
                    "Conditional safeguards required",
                    String.format("Transfer of PHI medical data from %s to %s requires active HIPAA Business Associate Agreement (BAA) coverage.",
                            event.getSource(), event.getDestination())
            );
            monitorRepository.raiseViolation(warning);
        }
    }

    @Override
    public List<EventDto> listRecentEvents() {
        return monitorRepository.listEvents().stream()
                .map(e -> new EventDto(
                        e.getId(),
                        e.getSource(),
                        e.getDestination(),
                        e.getDataCategory(),
                        e.getSizeBytes(),
                        e.getTimestamp().toString()
                )).collect(Collectors.toList());
    }

    @Override
    public List<ViolationDto> listViolations() {
        return monitorRepository.listViolations().stream()
                .map(v -> new ViolationDto(
                        v.getViolationId(),
                        v.getEventId(),
                        v.getSeverity(),
                        v.getViolatedRule(),
                        v.getDescription(),
                        v.getRaisedAt().toString()
                )).collect(Collectors.toList());
    }

    @Override
    public SlaMetricsDto getCurrentMetrics() {
        SlaMetrics metrics = monitorRepository.getSlaMetrics();
        return new SlaMetricsDto(
                metrics.getTotalEvents(),
                metrics.getViolationCount(),
                metrics.getComplianceRate()
        );
    }

    @Override
    public void reset() {
        monitorRepository.resetMetrics();
    }
}
