package com.pqvcf.regulation.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a single clause within a regulatory {@link Article}.
 *
 * <p>A Clause is the atomic unit of legal text in the PQVCF system. It represents
 * a single numbered provision, sub-provision, or paragraph within an article.
 * Each clause is classified by its {@link ClauseType} (deontic operator), which
 * enables formal compliance reasoning.
 *
 * <p><b>Examples of real clauses:</b>
 * <ul>
 *   <li>GDPR Art. 46(1): "In the absence of [an adequacy decision], a controller or
 *       processor may transfer personal data to a third country [...] only if the
 *       controller or processor has provided appropriate safeguards..." → OBLIGATION</li>
 *   <li>GDPR Art. 44: "Any transfer of personal data which are undergoing processing
 *       [...] to a third country [...] shall take place only if [conditions met]." → PROHIBITION</li>
 * </ul>
 *
 * <p><b>Invariants:</b>
 * <ul>
 *   <li>Content is never null or blank</li>
 *   <li>Clause type is always specified</li>
 * </ul>
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public class Clause {

    private final UUID id;
    private final UUID articleId;
    private String clauseNumber;
    private String content;
    private ClauseType clauseType;

    /**
     * Creates a new Clause. Use the factory method on Article to create clauses.
     */
    Clause(UUID articleId, String clauseNumber, String content, ClauseType clauseType) {
        this.id = UUID.randomUUID();
        this.articleId = Objects.requireNonNull(articleId, "Article ID must not be null");
        this.clauseNumber = validateClauseNumber(clauseNumber);
        this.content = validateContent(content);
        this.clauseType = Objects.requireNonNull(clauseType, "Clause type must not be null");
    }

    /**
     * Reconstructs a Clause from persistence (infrastructure use only).
     */
    public static Clause reconstitute(UUID id, UUID articleId, String clauseNumber,
                                       String content, ClauseType clauseType) {
        Clause clause = new Clause(articleId, clauseNumber, content, clauseType);
        // Override the generated ID with the persisted one (reflection-free reconstitution)
        return new Clause(id, articleId, clauseNumber, content, clauseType);
    }

    private Clause(UUID id, UUID articleId, String clauseNumber, String content, ClauseType clauseType) {
        this.id = Objects.requireNonNull(id);
        this.articleId = Objects.requireNonNull(articleId);
        this.clauseNumber = clauseNumber;
        this.content = content;
        this.clauseType = clauseType;
    }

    private static String validateClauseNumber(String clauseNumber) {
        if (clauseNumber == null || clauseNumber.isBlank()) {
            throw new IllegalArgumentException("Clause number must not be null or blank");
        }
        return clauseNumber.strip();
    }

    private static String validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Clause content must not be null or blank");
        }
        return content.strip();
    }

    // ---- Business logic ----

    /**
     * Updates the clause content (e.g., when a regulation is amended).
     *
     * @param newContent the new content text
     */
    public void updateContent(String newContent) {
        this.content = validateContent(newContent);
    }

    /**
     * Reclassifies the deontic type of this clause.
     * Used when the formal encoding is refined by legal experts.
     */
    public void reclassify(ClauseType newType) {
        this.clauseType = Objects.requireNonNull(newType, "New clause type must not be null");
    }

    /**
     * Returns {@code true} if this clause imposes a restriction on data movement.
     */
    public boolean restrictsDataMovement() {
        return clauseType == ClauseType.PROHIBITION || clauseType == ClauseType.OBLIGATION;
    }

    /**
     * Returns {@code true} if this clause grants an exemption from restrictions.
     */
    public boolean isExemption() { return clauseType == ClauseType.EXEMPTION; }

    // ---- Getters ----

    public UUID getId() { return id; }
    public UUID getArticleId() { return articleId; }
    public String getClauseNumber() { return clauseNumber; }
    public String getContent() { return content; }
    public ClauseType getClauseType() { return clauseType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Clause that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Clause[%s %s (%s)]".formatted(clauseNumber, clauseType.getSymbol(), id);
    }
}
