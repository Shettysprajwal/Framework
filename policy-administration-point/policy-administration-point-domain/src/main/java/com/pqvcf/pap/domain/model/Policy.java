package com.pqvcf.pap.domain.model;

import com.pqvcf.pap.domain.event.PolicyPublishedEvent;
import com.pqvcf.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing an organizational compliance policy.
 * Manages rule links and emits domain events for policy lifecycles.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public class Policy extends AggregateRoot<PolicyId> {

    private final PolicyId id;
    private String name;
    private String owner;
    private String description;
    private PolicyStatus status;
    private final List<RuleLink> ruleLinks;
    private final Instant createdAt;
    private Instant updatedAt;

    public static Policy create(String name, String owner, String description) {
        return new Policy(
                PolicyId.generate(),
                name,
                owner,
                description,
                PolicyStatus.DRAFT,
                new ArrayList<>(),
                Instant.now(),
                Instant.now()
        );
    }

    public static Policy reconstitute(
            PolicyId id,
            String name,
            String owner,
            String description,
            PolicyStatus status,
            List<RuleLink> ruleLinks,
            Instant createdAt,
            Instant updatedAt) {
        return new Policy(id, name, owner, description, status, ruleLinks, createdAt, updatedAt);
    }

    private Policy(
            PolicyId id,
            String name,
            String owner,
            String description,
            PolicyStatus status,
            List<RuleLink> ruleLinks,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "PolicyId required");
        this.name = requireNonBlank(name, "Policy name");
        this.owner = requireNonBlank(owner, "Policy owner");
        this.description = description != null ? description.trim() : "";
        this.status = Objects.requireNonNull(status, "Policy status required");
        this.ruleLinks = new ArrayList<>(ruleLinks != null ? ruleLinks : List.of());
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void updateDetails(String name, String owner, String description) {
        this.name = requireNonBlank(name, "Policy name");
        this.owner = requireNonBlank(owner, "Policy owner");
        this.description = description != null ? description.trim() : "";
        this.updatedAt = Instant.now();
    }

    public RuleLink addRuleLink(String name, UUID regulatoryRuleId, String description) {
        boolean duplicate = ruleLinks.stream()
                .anyMatch(link -> link.getOrganizationalRuleName().equalsIgnoreCase(name.trim()));
        if (duplicate) {
            throw new IllegalArgumentException(
                    String.format("Rule link with name '%s' already exists in policy '%s'", name, this.name));
        }

        RuleLink link = new RuleLink(this.id.getValue(), name, regulatoryRuleId, description);
        this.ruleLinks.add(link);
        this.updatedAt = Instant.now();
        return link;
    }

    public void removeRuleLink(UUID linkId) {
        boolean removed = this.ruleLinks.removeIf(link -> link.getId().equals(linkId));
        if (!removed) {
            throw new java.util.NoSuchElementException("Rule link not found with ID: " + linkId);
        }
        this.updatedAt = Instant.now();
    }

    public void activate() {
        if (status == PolicyStatus.DEPRECATED) {
            throw new IllegalStateException("Cannot activate a deprecated policy.");
        }
        this.status = PolicyStatus.ACTIVE;
        this.updatedAt = Instant.now();
        
        // Emit domain event for policy activation
        raiseEvent(new PolicyPublishedEvent(this.id, this.name, this.status.name()));
    }

    public void deprecate() {
        if (status == PolicyStatus.DRAFT) {
            throw new IllegalStateException("Cannot deprecate a draft policy without activating it first.");
        }
        this.status = PolicyStatus.DEPRECATED;
        this.updatedAt = Instant.now();
        
        raiseEvent(new PolicyPublishedEvent(this.id, this.name, this.status.name()));
    }

    private static String requireNonBlank(String val, String field) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
        return val.trim();
    }

    @Override
    public PolicyId getId() { return id; }
    public String getName() { return name; }
    public String getOwner() { return owner; }
    public String getDescription() { return description; }
    public PolicyStatus getStatus() { return status; }
    public List<RuleLink> getRuleLinks() { return Collections.unmodifiableList(ruleLinks); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
