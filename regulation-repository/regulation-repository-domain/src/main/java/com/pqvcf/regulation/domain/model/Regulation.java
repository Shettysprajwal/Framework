package com.pqvcf.regulation.domain.model;

import com.pqvcf.regulation.domain.event.ArticleAddedEvent;
import com.pqvcf.regulation.domain.event.RegulationRegisteredEvent;
import com.pqvcf.regulation.domain.event.RegulationStatusChangedEvent;
import com.pqvcf.shared.domain.AggregateRoot;
import com.pqvcf.shared.types.JurisdictionCode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root for a legal regulation in the PQVCF system.
 *
 * <p>A Regulation represents a complete legal instrument (e.g., GDPR, DPDP Act, HIPAA)
 * stored in machine-readable form. It is the central object in Module 1 and serves as
 * the source of truth for compliance reasoning in Modules 3, 4, and 6.
 *
 * <p><b>Aggregate boundaries:</b>
 * <ul>
 *   <li>The Regulation aggregate root owns all its {@link Article}s and their {@link Clause}s.</li>
 *   <li>All mutations to articles and clauses must go through this aggregate root.</li>
 *   <li>The aggregate root emits domain events for every significant state change.</li>
 * </ul>
 *
 * <p><b>Invariants enforced:</b>
 * <ol>
 *   <li>Short name is unique per jurisdiction (enforced at repository level).</li>
 *   <li>Article numbers are unique within a regulation.</li>
 *   <li>A regulation in DEPRECATED status cannot be activated directly (must create new version).</li>
 *   <li>Effective date must not be in the future for ACTIVE regulations.</li>
 * </ol>
 *
 * <p><b>Research Note:</b>
 * Regulations form the formal specification layer of PQVCF. The {@code formalSpec} field
 * (SMT-LIB2 format) represents the entire regulation as a set of logical axioms that can
 * be fed to the Z3 SMT solver in Module 4 for automated compliance verification.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public class Regulation extends AggregateRoot<RegulationId> {

    // ---- Identity ----
    private final RegulationId id;

    // ---- Core attributes ----
    private String name;
    private String shortName;
    private JurisdictionCode primaryJurisdiction;
    private String version;
    private LocalDate effectiveDate;
    private String description;
    private RegulationStatus status;

    /**
     * The complete formal specification of this regulation in SMT-LIB2 format.
     * Used by the Formal Verification Engine (Module 4) for automated verification.
     * May be null for DRAFT regulations.
     */
    private String formalSpec;

    // ---- Sub-entities (part of this aggregate) ----
    private final List<Article> articles;

    // ---- Audit fields ----
    private final Instant createdAt;
    private Instant updatedAt;

    // ================================================================
    // Factory method — creates a new, unsaved regulation
    // ================================================================

    /**
     * Creates a new regulation in DRAFT status.
     * Raises a {@link RegulationRegisteredEvent} domain event.
     *
     * @param name                the full legal name (e.g., "General Data Protection Regulation")
     * @param shortName           the short identifier (e.g., "GDPR") — must be unique
     * @param primaryJurisdiction the primary jurisdiction this regulation governs
     * @param version             the version string (e.g., "2016/679")
     * @param description         human-readable description
     * @return a new Regulation instance in DRAFT status
     */
    public static Regulation create(String name, String shortName,
                                     JurisdictionCode primaryJurisdiction,
                                     String version, String description) {
        Regulation regulation = new Regulation(
                RegulationId.generate(), name, shortName, primaryJurisdiction,
                version, null, description, RegulationStatus.DRAFT,
                null, new ArrayList<>(), Instant.now(), Instant.now()
        );
        regulation.raiseEvent(new RegulationRegisteredEvent(regulation.id, regulation.shortName,
                regulation.primaryJurisdiction, regulation.version));
        return regulation;
    }

    // ================================================================
    // Reconstitution constructor — for infrastructure layer only
    // ================================================================

    public static Regulation reconstitute(RegulationId id, String name, String shortName,
                                           JurisdictionCode primaryJurisdiction, String version,
                                           LocalDate effectiveDate, String description,
                                           RegulationStatus status, String formalSpec,
                                           List<Article> articles, Instant createdAt,
                                           Instant updatedAt) {
        return new Regulation(id, name, shortName, primaryJurisdiction, version,
                effectiveDate, description, status, formalSpec,
                new ArrayList<>(articles), createdAt, updatedAt);
    }

    private Regulation(RegulationId id, String name, String shortName,
                        JurisdictionCode primaryJurisdiction, String version,
                        LocalDate effectiveDate, String description, RegulationStatus status,
                        String formalSpec, List<Article> articles,
                        Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Regulation ID must not be null");
        this.name = validateName(name);
        this.shortName = validateShortName(shortName);
        this.primaryJurisdiction = Objects.requireNonNull(primaryJurisdiction, "Primary jurisdiction required");
        this.version = validateVersion(version);
        this.effectiveDate = effectiveDate;
        this.description = description;
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.formalSpec = formalSpec;
        this.articles = articles;
        this.createdAt = Objects.requireNonNull(createdAt, "Created-at must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated-at must not be null");
    }

    // ================================================================
    // Business operations
    // ================================================================

    /**
     * Activates this regulation, making it enforceable in compliance evaluation.
     * Sets the effective date to today if not already set.
     *
     * @throws IllegalStateException if the regulation is already DEPRECATED
     */
    public void activate() {
        if (status == RegulationStatus.DEPRECATED) {
            throw new IllegalStateException(
                    "Cannot activate a deprecated regulation. Create a new version instead.");
        }
        RegulationStatus previous = this.status;
        this.status = RegulationStatus.ACTIVE;
        if (this.effectiveDate == null) {
            this.effectiveDate = LocalDate.now();
        }
        this.updatedAt = Instant.now();
        raiseEvent(new RegulationStatusChangedEvent(id, previous, RegulationStatus.ACTIVE));
    }

    /**
     * Deprecates this regulation (superseded by a newer version).
     *
     * @throws IllegalStateException if the regulation is still DRAFT
     */
    public void deprecate() {
        if (status == RegulationStatus.DRAFT) {
            throw new IllegalStateException("Cannot deprecate a draft regulation. Activate it first.");
        }
        RegulationStatus previous = this.status;
        this.status = RegulationStatus.DEPRECATED;
        this.updatedAt = Instant.now();
        raiseEvent(new RegulationStatusChangedEvent(id, previous, RegulationStatus.DEPRECATED));
    }

    /**
     * Adds an article to this regulation.
     *
     * @param articleNumber the article identifier (e.g., "Art. 46")
     * @param title         the article title
     * @param content       the full legal text of the article
     * @return the created Article
     * @throws IllegalArgumentException if an article with the same number already exists
     */
    public Article addArticle(String articleNumber, String title, String content) {
        boolean duplicate = articles.stream()
                .anyMatch(a -> a.getArticleNumber().equals(articleNumber));
        if (duplicate) {
            throw new IllegalArgumentException(
                    "Article '%s' already exists in regulation '%s'".formatted(articleNumber, shortName));
        }
        Article article = new Article(this.id.getValue(), articleNumber, title, content);
        this.articles.add(article);
        this.updatedAt = Instant.now();
        raiseEvent(new ArticleAddedEvent(id, ArticleId.of(article.getId()), articleNumber));
        return article;
    }

    /**
     * Finds an article by its article number.
     *
     * @param articleNumber the article number to search for
     * @return the Article if found
     * @throws java.util.NoSuchElementException if not found
     */
    public Article findArticle(String articleNumber) {
        return articles.stream()
                .filter(a -> a.getArticleNumber().equals(articleNumber))
                .findFirst()
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "Article '%s' not found in regulation '%s'".formatted(articleNumber, shortName)));
    }

    /**
     * Updates the formal SMT-LIB2 specification for this regulation.
     * Called by the Rule Translation Engine (Module 2) when a new formal spec is generated.
     *
     * @param formalSpec the complete SMT-LIB2 specification string
     */
    public void updateFormalSpec(String formalSpec) {
        this.formalSpec = formalSpec;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates basic metadata of the regulation.
     */
    public void updateMetadata(String name, String description, String version) {
        this.name = validateName(name);
        this.description = description;
        this.version = validateVersion(version);
        this.updatedAt = Instant.now();
    }

    /**
     * Sets the effective date of this regulation.
     */
    public void setEffectiveDate(LocalDate date) {
        this.effectiveDate = date;
        this.updatedAt = Instant.now();
    }

    // ---- Domain query methods ----

    /** Returns the number of articles with formal deontic specifications. */
    public long countFormalizedArticles() {
        return articles.stream().filter(Article::isFormalized).count();
    }

    /** Returns {@code true} if this regulation is currently being enforced. */
    public boolean isEnforced() { return status.isEnforced(); }

    /** Returns the total count of clauses across all articles. */
    public long totalClauseCount() {
        return articles.stream().mapToLong(a -> a.getClauses().size()).sum();
    }

    // ---- Validation ----

    private static String validateName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Regulation name must not be blank");
        if (name.length() > 500) throw new IllegalArgumentException("Regulation name exceeds 500 characters");
        return name.strip();
    }

    private static String validateShortName(String shortName) {
        if (shortName == null || shortName.isBlank()) throw new IllegalArgumentException("Short name must not be blank");
        if (shortName.length() > 50) throw new IllegalArgumentException("Short name exceeds 50 characters");
        return shortName.strip().toUpperCase();
    }

    private static String validateVersion(String version) {
        if (version == null || version.isBlank()) throw new IllegalArgumentException("Version must not be blank");
        return version.strip();
    }

    // ---- Getters ----

    @Override public RegulationId getId() { return id; }
    public String getName() { return name; }
    public String getShortName() { return shortName; }
    public JurisdictionCode getPrimaryJurisdiction() { return primaryJurisdiction; }
    public String getVersion() { return version; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public String getDescription() { return description; }
    public RegulationStatus getStatus() { return status; }
    public String getFormalSpec() { return formalSpec; }
    public List<Article> getArticles() { return Collections.unmodifiableList(articles); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return "Regulation[%s v%s (%s) — %s]".formatted(shortName, version, primaryJurisdiction, status);
    }
}
