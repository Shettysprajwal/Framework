package com.pqvcf.pap.domain.event;

import com.pqvcf.pap.domain.model.PolicyId;
import com.pqvcf.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event raised when an organizational policy is activated/published.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public record PolicyPublishedEvent(
        UUID eventId,
        Instant occurredAt,
        PolicyId policyId,
        String name,
        String status
) implements DomainEvent {

    public PolicyPublishedEvent(PolicyId policyId, String name, String status) {
        this(UUID.randomUUID(), Instant.now(), policyId, name, status);
    }

    @Override public UUID getEventId() { return eventId; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    @Override public String getEventType() { return "PolicyPublished"; }
}
