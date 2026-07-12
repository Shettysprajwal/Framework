package com.pqvcf.governance.domain.model;

import com.pqvcf.shared.domain.AggregateRoot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing a data transfer governance evaluation.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class GovernanceDecision extends AggregateRoot {

    private final String id;
    private final DataFlow flow;
    private final TransferDecision decision;
    private final String reasoning;
    private final List<String> citations;
    private final String evidenceLink;
    private final Instant createdAt;

    public GovernanceDecision(
            String id,
            DataFlow flow,
            TransferDecision decision,
            String reasoning,
            List<String> citations,
            String evidenceLink) {
        this.id = id != null ? id.trim() : UUID.randomUUID().toString();
        this.flow = Objects.requireNonNull(flow, "DataFlow must not be null");
        this.decision = Objects.requireNonNull(decision, "TransferDecision must not be null");
        this.reasoning = reasoning != null ? reasoning.trim() : "";
        this.citations = citations != null ? new ArrayList<>(citations) : new ArrayList<>();
        this.evidenceLink = evidenceLink != null ? evidenceLink.trim() : "";
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public DataFlow getFlow() { return flow; }
    public TransferDecision getDecision() { return decision; }
    public String getReasoning() { return reasoning; }
    public List<String> getCitations() { return Collections.unmodifiableList(citations); }
    public String getEvidenceLink() { return evidenceLink; }
    public Instant getCreatedAt() { return createdAt; }
}
