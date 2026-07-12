package com.pqvcf.monitor.domain.model;

import com.pqvcf.shared.domain.ValueObject;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a compliance violation warning alert.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class PolicyViolation extends ValueObject {

    private final String violationId;
    private final String eventId;
    private final String severity; // INFO, WARNING, CRITICAL
    private final String violatedRule;
    private final String description;
    private final Instant raisedAt;

    public PolicyViolation(
            String violationId,
            String eventId,
            String severity,
            String violatedRule,
            String description) {
        this.violationId = violationId != null ? violationId.trim() : UUID.randomUUID().toString();
        this.eventId = Objects.requireNonNull(eventId, "Event ID required").trim();
        this.severity = Objects.requireNonNull(severity, "Severity required").trim().toUpperCase();
        this.violatedRule = Objects.requireNonNull(violatedRule, "Violated rule required").trim();
        this.description = description != null ? description.trim() : "";
        this.raisedAt = Instant.now();
    }

    public String getViolationId() { return violationId; }
    public String getEventId() { return eventId; }
    public String getSeverity() { return severity; }
    public String getViolatedRule() { return violatedRule; }
    public String getDescription() { return description; }
    public Instant getRaisedAt() { return raisedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PolicyViolation that)) return false;
        return Objects.equals(violationId, that.violationId) &&
                Objects.equals(eventId, that.eventId) &&
                Objects.equals(severity, that.severity) &&
                Objects.equals(violatedRule, that.violatedRule) &&
                Objects.equals(description, that.description) &&
                Objects.equals(raisedAt, that.raisedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(violationId, eventId, severity, violatedRule, description, raisedAt);
    }
}
