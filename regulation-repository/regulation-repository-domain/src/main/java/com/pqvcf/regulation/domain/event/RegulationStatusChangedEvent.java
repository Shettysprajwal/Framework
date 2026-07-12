package com.pqvcf.regulation.domain.event;

import com.pqvcf.regulation.domain.model.RegulationId;
import com.pqvcf.regulation.domain.model.RegulationStatus;
import com.pqvcf.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event raised when a regulation's lifecycle status changes.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public record RegulationStatusChangedEvent(
        UUID eventId,
        Instant occurredAt,
        RegulationId regulationId,
        RegulationStatus previousStatus,
        RegulationStatus newStatus
) implements DomainEvent {

    public RegulationStatusChangedEvent(RegulationId regulationId,
                                         RegulationStatus previousStatus,
                                         RegulationStatus newStatus) {
        this(UUID.randomUUID(), Instant.now(), regulationId, previousStatus, newStatus);
    }

    @Override public UUID getEventId() { return eventId; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    @Override public String getEventType() { return "RegulationStatusChanged"; }
}
