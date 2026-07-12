package com.pqvcf.pap.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a binding/link between an organizational policy rule
 * and a regulatory rule translated by the Rule Translation Engine (Module 2).
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public class RuleLink {

    private final UUID id;
    private final UUID policyId;
    private String organizationalRuleName;
    private UUID regulatoryRuleId;
    private String description;

    RuleLink(UUID policyId, String organizationalRuleName, UUID regulatoryRuleId, String description) {
        this.id = UUID.randomUUID();
        this.policyId = Objects.requireNonNull(policyId, "Policy ID must not be null");
        this.organizationalRuleName = validateName(organizationalRuleName);
        this.regulatoryRuleId = Objects.requireNonNull(regulatoryRuleId, "Regulatory Rule ID must not be null");
        this.description = description != null ? description.trim() : "";
    }

    public static RuleLink reconstitute(UUID id, UUID policyId, String name, UUID regRuleId, String desc) {
        return new RuleLink(id, policyId, name, regRuleId, desc);
    }

    private RuleLink(UUID id, UUID policyId, String name, UUID regRuleId, String desc) {
        this.id = id;
        this.policyId = policyId;
        this.organizationalRuleName = name;
        this.regulatoryRuleId = regRuleId;
        this.description = desc;
    }

    public void updateDetails(String name, UUID regRuleId, String desc) {
        this.organizationalRuleName = validateName(name);
        this.regulatoryRuleId = Objects.requireNonNull(regRuleId);
        this.description = desc != null ? desc.trim() : "";
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Rule link name must not be empty");
        }
        return name.trim();
    }

    public UUID getId() { return id; }
    public UUID getPolicyId() { return policyId; }
    public String getOrganizationalRuleName() { return organizationalRuleName; }
    public UUID getRegulatoryRuleId() { return regulatoryRuleId; }
    public String getDescription() { return description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleLink that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
