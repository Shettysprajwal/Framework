package com.pqvcf.pap.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "policy_rule_links")
public class RuleLinkJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private PolicyJpaEntity policy;

    @Column(name = "organizational_rule_name", nullable = false, length = 255)
    private String organizationalRuleName;

    @Column(name = "regulatory_rule_id", nullable = false)
    private UUID regulatoryRuleId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Constructors
    public RuleLinkJpaEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public PolicyJpaEntity getPolicy() { return policy; }
    public void setPolicy(PolicyJpaEntity policy) { this.policy = policy; }

    public String getOrganizationalRuleName() { return organizationalRuleName; }
    public void setOrganizationalRuleName(String organizationalRuleName) { this.organizationalRuleName = organizationalRuleName; }

    public UUID getRegulatoryRuleId() { return regulatoryRuleId; }
    public void setRegulatoryRuleId(UUID regulatoryRuleId) { this.regulatoryRuleId = regulatoryRuleId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
