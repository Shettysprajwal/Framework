package com.pqvcf.regulation.infrastructure.persistence.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "articles")
public class ArticleJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regulation_id", nullable = false)
    private RegulationJpaEntity regulation;

    @Column(name = "article_number", nullable = false, length = 50)
    private String articleNumber;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "deontic_formula", columnDefinition = "TEXT")
    private String deonticFormula;

    @Column(name = "odrl_policy", columnDefinition = "TEXT")
    private String odrlPolicy;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClauseJpaEntity> clauses = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Constructors
    public ArticleJpaEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public RegulationJpaEntity getRegulation() { return regulation; }
    public void setRegulation(RegulationJpaEntity regulation) { this.regulation = regulation; }

    public String getArticleNumber() { return articleNumber; }
    public void setArticleNumber(String articleNumber) { this.articleNumber = articleNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDeonticFormula() { return deonticFormula; }
    public void setDeonticFormula(String deonticFormula) { this.deonticFormula = deonticFormula; }

    public String getOdrlPolicy() { return odrlPolicy; }
    public void setOdrlPolicy(String odrlPolicy) { this.odrlPolicy = odrlPolicy; }

    public List<ClauseJpaEntity> getClauses() { return clauses; }
    public void setClauses(List<ClauseJpaEntity> clauses) { this.clauses = clauses; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
