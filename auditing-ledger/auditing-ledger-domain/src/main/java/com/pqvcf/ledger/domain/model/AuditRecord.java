package com.pqvcf.ledger.domain.model;

import com.pqvcf.shared.domain.AggregateRoot;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing an immutable, cryptographically chained audit log record.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class AuditRecord extends AggregateRoot {

    private final String id;
    private final Instant timestamp;
    private final String action;
    private final String actor;
    private final String target;
    private String decision; // Non-final to allow simulating tampering!
    private final String previousHash;
    private String currentHash; // Non-final to calculate on sealing

    public AuditRecord(
            String id,
            Instant timestamp,
            String action,
            String actor,
            String target,
            String decision,
            String previousHash,
            String currentHash) {
        this.id = id != null ? id.trim() : UUID.randomUUID().toString();
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.action = Objects.requireNonNull(action, "Action required").trim();
        this.actor = Objects.requireNonNull(actor, "Actor required").trim();
        this.target = Objects.requireNonNull(target, "Target required").trim();
        this.decision = Objects.requireNonNull(decision, "Decision required").trim();
        this.previousHash = previousHash != null ? previousHash.trim() : "";
        this.currentHash = currentHash != null ? currentHash.trim() : "";
    }

    public String getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public String getAction() { return action; }
    public String getActor() { return actor; }
    public String getTarget() { return target; }
    public String getDecision() { return decision; }
    public String getPreviousHash() { return previousHash; }
    public String getCurrentHash() { return currentHash; }

    public void setCurrentHash(String hash) {
        this.currentHash = hash;
    }

    /**
     * Tamper with the record value to break cryptographic chaining.
     */
    public void tamperDecision(String tamperedDecision) {
        this.decision = tamperedDecision;
    }
}
