package com.pqvcf.translation.domain.model;

import com.pqvcf.shared.domain.AggregateRoot;
import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate Root representing a translated Legal Rule in Module 2.
 * Tracks source CNL text, its deontic formula representations, and target format compilation.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public class LegalRule extends AggregateRoot<LegalRuleId> {

    private final LegalRuleId id;
    private String regulationShortName;
    private String articleNumber;
    private String clauseNumber;
    private String rawSourceText;
    private DeonticFormula deonticFormula;
    private String smtSpec;
    private String odrlPolicy;
    private final Instant createdAt;
    private Instant updatedAt;

    public static LegalRule create(
            String regulationShortName,
            String articleNumber,
            String clauseNumber,
            String rawSourceText,
            DeonticFormula deonticFormula,
            String smtSpec,
            String odrlPolicy) {
        
        return new LegalRule(
                LegalRuleId.generate(),
                regulationShortName,
                articleNumber,
                clauseNumber,
                rawSourceText,
                deonticFormula,
                smtSpec,
                odrlPolicy,
                Instant.now(),
                Instant.now()
        );
    }

    public static LegalRule reconstitute(
            LegalRuleId id,
            String regulationShortName,
            String articleNumber,
            String clauseNumber,
            String rawSourceText,
            DeonticFormula deonticFormula,
            String smtSpec,
            String odrlPolicy,
            Instant createdAt,
            Instant updatedAt) {
        return new LegalRule(id, regulationShortName, articleNumber, clauseNumber, rawSourceText, deonticFormula, smtSpec, odrlPolicy, createdAt, updatedAt);
    }

    private LegalRule(
            LegalRuleId id,
            String regulationShortName,
            String articleNumber,
            String clauseNumber,
            String rawSourceText,
            DeonticFormula deonticFormula,
            String smtSpec,
            String odrlPolicy,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "LegalRuleId required");
        this.regulationShortName = requireNonBlank(regulationShortName, "Regulation short name");
        this.articleNumber = requireNonBlank(articleNumber, "Article number");
        this.clauseNumber = clauseNumber != null ? clauseNumber.trim() : "";
        this.rawSourceText = requireNonBlank(rawSourceText, "Raw source text");
        this.deonticFormula = Objects.requireNonNull(deonticFormula, "Deontic formula required");
        this.smtSpec = Objects.requireNonNull(smtSpec, "SMT specification required");
        this.odrlPolicy = Objects.requireNonNull(odrlPolicy, "ODRL policy required");
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void updateTranslation(DeonticFormula deonticFormula, String smtSpec, String odrlPolicy) {
        this.deonticFormula = Objects.requireNonNull(deonticFormula);
        this.smtSpec = Objects.requireNonNull(smtSpec);
        this.odrlPolicy = Objects.requireNonNull(odrlPolicy);
        this.updatedAt = Instant.now();
    }

    private static String requireNonBlank(String val, String field) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
        return val.trim();
    }

    @Override
    public LegalRuleId getId() { return id; }
    public String getRegulationShortName() { return regulationShortName; }
    public String getArticleNumber() { return articleNumber; }
    public String getClauseNumber() { return clauseNumber; }
    public String getRawSourceText() { return rawSourceText; }
    public DeonticFormula getDeonticFormula() { return deonticFormula; }
    public String getSmtSpec() { return smtSpec; }
    public String getOdrlPolicy() { return odrlPolicy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
