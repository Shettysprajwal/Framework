package com.pqvcf.pdp.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "decision_audit_logs")
public class DecisionAuditLogJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "subject_id", nullable = false, length = 255)
    private String subjectId;

    @Column(name = "resource_id", nullable = false, length = 255)
    private String resourceId;

    @Column(name = "action_id", nullable = false, length = 255)
    private String actionId;

    @Column(name = "source_country", length = 50)
    private String sourceCountry;

    @Column(name = "target_country", length = 50)
    private String targetCountry;

    @Column(name = "policy_name", length = 255)
    private String policyName;

    @Column(name = "effect", nullable = false, length = 50)
    private String effect;

    @Column(name = "proof_trace", columnDefinition = "TEXT")
    private String proofTrace;

    @Column(name = "validation_log", columnDefinition = "TEXT")
    private String validationLog;

    @Column(name = "solved_at", nullable = false)
    private Instant solvedAt;

    // Constructors
    public DecisionAuditLogJpaEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getActionId() { return actionId; }
    public void setActionId(String actionId) { this.actionId = actionId; }

    public String getSourceCountry() { return sourceCountry; }
    public void setSourceCountry(String sourceCountry) { this.sourceCountry = sourceCountry; }

    public String getTargetCountry() { return targetCountry; }
    public void setTargetCountry(String targetCountry) { this.targetCountry = targetCountry; }

    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }

    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = effect; }

    public String getProofTrace() { return proofTrace; }
    public void setProofTrace(String proofTrace) { this.proofTrace = proofTrace; }

    public String getValidationLog() { return validationLog; }
    public void setValidationLog(String validationLog) { this.validationLog = validationLog; }

    public Instant getSolvedAt() { return solvedAt; }
    public void setSolvedAt(Instant solvedAt) { this.solvedAt = solvedAt; }
}
