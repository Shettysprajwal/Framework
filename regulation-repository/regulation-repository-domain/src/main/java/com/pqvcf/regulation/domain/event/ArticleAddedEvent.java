package com.pqvcf.regulation.domain.event;

import com.pqvcf.regulation.domain.model.ArticleId;
import com.pqvcf.regulation.domain.model.RegulationId;
import com.pqvcf.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event raised when an article is added to a regulation.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public record ArticleAddedEvent(
        UUID eventId,
        Instant occurredAt,
        RegulationId regulationId,
        ArticleId articleId,
        String articleNumber
) implements DomainEvent {

    public ArticleAddedEvent(RegulationId regulationId, ArticleId articleId, String articleNumber) {
        this(UUID.randomUUID(), Instant.now(), regulationId, articleId, articleNumber);
    }

    @Override public UUID getEventId() { return eventId; }
    @Override public Instant getOccurredAt() { return occurredAt; }
    @Override public String getEventType() { return "ArticleAdded"; }
}
