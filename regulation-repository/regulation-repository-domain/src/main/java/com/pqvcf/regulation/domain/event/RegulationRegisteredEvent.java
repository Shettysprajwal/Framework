package com.pqvcf.regulation.domain.event;

import com.pqvcf.regulation.domain.model.RegulationId;
import com.pqvcf.shared.domain.DomainEvent;
import com.pqvcf.shared.types.JurisdictionCode;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event raised when a new regulation is successfully registered in PQVCF.
 *
 * <p>This event feeds the audit log and triggers downstream compliance engine updates.
 * When a new regulation is registered, all active compliance policies must be
 * re-evaluated to determine if their jurisdiction coverage needs updating.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public record RegulationRegisteredEvent(
        UUID eventId,
        Instant occurredAt,
        RegulationId regulationId,
        String shortName,
        JurisdictionCode jurisdiction,
        String version
) implements DomainEvent {

    public RegulationRegisteredEvent(RegulationId regulationId, String shortName,
                                      JurisdictionCode jurisdiction, String version) {
        this(UUID.randomUUID(), Instant.now(), regulationId, shortName, jurisdiction, version);
    }

    @Override public UUID getEventId() { return eventId; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    @Override public String getEventType() { return "RegulationRegistered"; }
}
