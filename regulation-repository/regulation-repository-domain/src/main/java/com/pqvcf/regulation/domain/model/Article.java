package com.pqvcf.regulation.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a single article within a regulatory {@link Regulation} aggregate.
 *
 * <p>An Article is a numbered section of a regulation (e.g., "GDPR Article 46",
 * "DPDP Section 16", "HIPAA §164.312"). It contains:
 * <ul>
 *   <li>The original legal text content</li>
 *   <li>A deontic logic formula encoding the article's normative content</li>
 *   <li>An ODRL (Open Digital Rights Language) policy expression</li>
 *   <li>Zero or more {@link Clause}s representing sub-provisions</li>
 * </ul>
 *
 * <p>Articles are managed only through the {@link Regulation} aggregate root.
 * Direct persistence of articles bypassing the aggregate is prohibited.
 *
 * <p><b>Formal Encoding Example:</b>
 * <pre>
 * Article: GDPR Art. 46(1)
 * DeonticFormula: O(controller, ensure_safeguards) ∧ ¬GDPR_Art45_Adequacy(dest_country)
 *   → P(controller, transfer_data, dest_country)
 * </pre>
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public class Article {

    private final UUID id;
    private final UUID regulationId;
    private String articleNumber;
    private String title;
    private String content;

    /**
     * Formal deontic logic formula encoding this article's normative content.
     * Format: SMT-LIB2 compatible expression for Z3 solver consumption.
     */
    private String deonticFormula;

    /**
     * ODRL policy expression (JSON-LD serialized) for interoperability.
     * See: https://www.w3.org/TR/odrl-model/
     */
    private String odrlPolicy;

    private final List<Clause> clauses;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Package-private constructor — articles are created only through {@link Regulation}.
     */
    Article(UUID regulationId, String articleNumber, String title, String content) {
        this.id = UUID.randomUUID();
        this.regulationId = Objects.requireNonNull(regulationId, "Regulation ID must not be null");
        this.articleNumber = validateArticleNumber(articleNumber);
        this.title = validateTitle(title);
        this.content = validateContent(content);
        this.clauses = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Reconstitution constructor for infrastructure layer.
     */
    public static Article reconstitute(UUID id, UUID regulationId, String articleNumber,
                                        String title, String content, String deonticFormula,
                                        String odrlPolicy, List<Clause> clauses,
                                        Instant createdAt, Instant updatedAt) {
        Article article = new Article(id, regulationId, articleNumber, title, content,
                deonticFormula, odrlPolicy, clauses, createdAt, updatedAt);
        return article;
    }

    private Article(UUID id, UUID regulationId, String articleNumber, String title,
                    String content, String deonticFormula, String odrlPolicy,
                    List<Clause> clauses, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.regulationId = regulationId;
        this.articleNumber = articleNumber;
        this.title = title;
        this.content = content;
        this.deonticFormula = deonticFormula;
        this.odrlPolicy = odrlPolicy;
        this.clauses = new ArrayList<>(clauses != null ? clauses : List.of());
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ---- Business operations ----

    /**
     * Adds a clause to this article.
     *
     * @param clauseNumber the clause identifier (e.g., "1(a)", "2")
     * @param content      the clause text
     * @param type         the deontic type
     * @return the created Clause
     */
    public Clause addClause(String clauseNumber, String content, ClauseType type) {
        boolean duplicate = clauses.stream()
                .anyMatch(c -> c.getClauseNumber().equals(clauseNumber));
        if (duplicate) {
            throw new IllegalArgumentException(
                    "Clause '%s' already exists in Article %s".formatted(clauseNumber, articleNumber));
        }
        Clause clause = new Clause(this.id, clauseNumber, content, type);
        this.clauses.add(clause);
        this.updatedAt = Instant.now();
        return clause;
    }

    /**
     * Records the formal deontic formula for this article.
     * The formula must be in a format compatible with the Z3 SMT solver (SMT-LIB2).
     *
     * @param formula the formal deontic formula string
     */
    public void setDeonticFormula(String formula) {
        this.deonticFormula = formula;
        this.updatedAt = Instant.now();
    }

    /**
     * Records the ODRL policy expression for this article.
     *
     * @param odrlPolicy the ODRL JSON-LD policy string
     */
    public void setOdrlPolicy(String odrlPolicy) {
        this.odrlPolicy = odrlPolicy;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the article content (e.g., when the regulation is amended).
     */
    public void updateContent(String newContent) {
        this.content = validateContent(newContent);
        this.updatedAt = Instant.now();
    }

    /**
     * Returns {@code true} if this article has a machine-readable formal specification.
     */
    public boolean isFormalized() {
        return deonticFormula != null && !deonticFormula.isBlank();
    }

    /**
     * Counts the number of restricting clauses (prohibitions + obligations).
     */
    public long countRestrictingClauses() {
        return clauses.stream().filter(Clause::restrictsDataMovement).count();
    }

    // ---- Validation helpers ----

    private static String validateArticleNumber(String n) {
        if (n == null || n.isBlank()) throw new IllegalArgumentException("Article number must not be blank");
        return n.strip();
    }

    private static String validateTitle(String t) {
        if (t == null || t.isBlank()) throw new IllegalArgumentException("Article title must not be blank");
        return t.strip();
    }

    private static String validateContent(String c) {
        if (c == null || c.isBlank()) throw new IllegalArgumentException("Article content must not be blank");
        return c.strip();
    }

    // ---- Getters ----

    public UUID getId() { return id; }
    public UUID getRegulationId() { return regulationId; }
    public String getArticleNumber() { return articleNumber; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getDeonticFormula() { return deonticFormula; }
    public String getOdrlPolicy() { return odrlPolicy; }
    public List<Clause> getClauses() { return Collections.unmodifiableList(clauses); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Article[%s '%s' (%d clauses)]".formatted(articleNumber, title, clauses.size());
    }
}
