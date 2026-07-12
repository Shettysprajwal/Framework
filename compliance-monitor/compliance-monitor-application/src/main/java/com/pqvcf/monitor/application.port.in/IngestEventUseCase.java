package com.pqvcf.monitor.application.port.in;

import java.util.List;

public interface IngestEventUseCase {

    void ingest(IngestEventCommand command);

    record IngestEventCommand(
            String source,
            String destination,
            String dataCategory,
            long sizeBytes
    ) {}

    record EventDto(
            String id,
            String source,
            String destination,
            String dataCategory,
            long sizeBytes,
            String timestamp
    ) {}

    record ViolationDto(
            String violationId,
            String eventId,
            String severity,
            String violatedRule,
            String description,
            String raisedAt
    ) {}

    record SlaMetricsDto(
            int totalEvents,
            int violationCount,
            double complianceRate
    ) {}
}
