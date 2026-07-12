package com.pqvcf.shared.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Marker interface for all domain events in the PQVCF system.
 *
 * <p>Domain events represent significant state changes within aggregates. They are
 * used to:
 * <ul>
 *   <li>Trigger downstream processes (e.g., compliance recalculation)</li>
 *   <li>Feed the immutable audit trail required for compliance evidence generation</li>
 *   <li>Enable eventual consistency between bounded contexts</li>
 * </ul>
 *
 * <p>Research Note: Every domain event in the compliance lifecycle forms part of the
 * compliance evidence chain. Events are hash-linked to form a tamper-evident audit log
 * (implemented in Module 9 — Evidence Generator).
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface DomainEvent {

    /**
     * Unique identifier for this event occurrence.
     *
     * @return event ID, never null
     */
    UUID getEventId();

    /**
     * The instant at which this event occurred.
     *
     * @return event timestamp, never null
     */
    Instant getOccurredAt();

    /**
     * The type name of this event, used for serialization and routing.
     *
     * @return event type string (e.g., "RegulationRegistered"), never null
     */
    String getEventType();

    /**
     * The version of the event schema, for forward compatibility.
     *
     * @return schema version (default: 1)
     */
    default int getSchemaVersion() {
        return 1;
    }
}
