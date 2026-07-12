package com.pqvcf.monitor.domain.model;

import com.pqvcf.shared.domain.AggregateRoot;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing a cloud data transfer movement event.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class ComplianceEvent extends AggregateRoot {

    private final String id;
    private final String source;
    private final String destination;
    private final String dataCategory;
    private final long sizeBytes;
    private final Instant timestamp;

    public ComplianceEvent(
            String id,
            String source,
            String destination,
            String dataCategory,
            long sizeBytes,
            Instant timestamp) {
        this.id = id != null ? id.trim() : UUID.randomUUID().toString();
        this.source = Objects.requireNonNull(source, "Source required").trim();
        this.destination = Objects.requireNonNull(destination, "Destination required").trim();
        this.dataCategory = Objects.requireNonNull(dataCategory, "Data category required").trim();
        this.sizeBytes = sizeBytes;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public String getId() { return id; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public String getDataCategory() { return dataCategory; }
    public long getSizeBytes() { return sizeBytes; }
    public Instant getTimestamp() { return timestamp; }
}
