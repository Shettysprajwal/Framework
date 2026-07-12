package com.pqvcf.regulation.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "clauses")
public class ClauseJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleJpaEntity article;

    @Column(name = "clause_number", nullable = false, length = 50)
    private String clauseNumber;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "clause_type", nullable = false, length = 50)
    private String clauseType;

    // Constructors
    public ClauseJpaEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ArticleJpaEntity getArticle() { return article; }
    public void setArticle(ArticleJpaEntity article) { this.article = article; }

    public String getClauseNumber() { return clauseNumber; }
    public void setClauseNumber(String clauseNumber) { this.clauseNumber = clauseNumber; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getClauseType() { return clauseType; }
    public void setClauseType(String clauseType) { this.clauseType = clauseType; }
}
