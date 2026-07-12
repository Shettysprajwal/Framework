package com.pqvcf.regulation.infrastructure.persistence.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "regulations")
public class RegulationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "short_name", nullable = false, unique = true, length = 50)
    private String shortName;

    @Column(name = "primary_jurisdiction", nullable = false, length = 20)
    private String primaryJurisdiction;

    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "formal_spec", columnDefinition = "TEXT")
    private String formalSpec;

    @OneToMany(mappedBy = "regulation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ArticleJpaEntity> articles = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Constructors
    public RegulationJpaEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public String getPrimaryJurisdiction() { return primaryJurisdiction; }
    public void setPrimaryJurisdiction(String primaryJurisdiction) { this.primaryJurisdiction = primaryJurisdiction; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFormalSpec() { return formalSpec; }
    public void setFormalSpec(String formalSpec) { this.formalSpec = formalSpec; }

    public List<ArticleJpaEntity> getArticles() { return articles; }
    public void setArticles(List<ArticleJpaEntity> articles) { this.articles = articles; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
