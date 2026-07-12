package com.pqvcf.translation.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "translated_rules")
public class RuleTranslationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "regulation_short_name", nullable = false, length = 50)
    private String regulationShortName;

    @Column(name = "article_number", nullable = false, length = 50)
    private String articleNumber;

    @Column(name = "clause_number", length = 50)
    private String clauseNumber;

    @Column(name = "raw_source_text", nullable = false, columnDefinition = "TEXT")
    private String rawSourceText;

    @Column(name = "deontic_operator", nullable = false, length = 20)
    private String deonticOperator;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "action", nullable = false, length = 255)
    private String action;

    @Column(name = "target", nullable = false, length = 255)
    private String target;

    @Column(name = "constraint_text", length = 500)
    private String constraintText;

    @Column(name = "smt_spec", nullable = false, columnDefinition = "TEXT")
    private String smtSpec;

    @Column(name = "odrl_policy", nullable = false, columnDefinition = "TEXT")
    private String odrlPolicy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Constructors
    public RuleTranslationJpaEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getRegulationShortName() { return regulationShortName; }
    public void setRegulationShortName(String regulationShortName) { this.regulationShortName = regulationShortName; }

    public String getArticleNumber() { return articleNumber; }
    public void setArticleNumber(String articleNumber) { this.articleNumber = articleNumber; }

    public String getClauseNumber() { return clauseNumber; }
    public void setClauseNumber(String clauseNumber) { this.clauseNumber = clauseNumber; }

    public String getRawSourceText() { return rawSourceText; }
    public void setRawSourceText(String rawSourceText) { this.rawSourceText = rawSourceText; }

    public String getDeonticOperator() { return deonticOperator; }
    public void setDeonticOperator(String deonticOperator) { this.deonticOperator = deonticOperator; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getConstraintText() { return constraintText; }
    public void setConstraintText(String constraintText) { this.constraintText = constraintText; }

    public String getSmtSpec() { return smtSpec; }
    public void setSmtSpec(String smtSpec) { this.smtSpec = smtSpec; }

    public String getOdrlPolicy() { return odrlPolicy; }
    public void setOdrlPolicy(String odrlPolicy) { this.odrlPolicy = odrlPolicy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
