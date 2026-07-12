package com.pqvcf.translation.application.service;

import com.pqvcf.translation.application.port.in.GetTranslatedRuleUseCase;
import com.pqvcf.translation.application.port.in.TranslateRuleUseCase;
import com.pqvcf.translation.application.translator.OdrlPolicyGenerator;
import com.pqvcf.translation.application.translator.SmtFormulaGenerator;
import com.pqvcf.translation.domain.model.DeonticFormula;
import com.pqvcf.translation.domain.model.LegalRule;
import com.pqvcf.translation.domain.model.LegalRuleId;
import com.pqvcf.translation.domain.parser.DeonticParser;
import com.pqvcf.translation.domain.repository.RuleTranslationRepository;
import com.pqvcf.translation.domain.validator.RuleValidator;
import com.pqvcf.translation.domain.validator.RuleValidator.ValidationResult;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RuleTranslationService implements TranslateRuleUseCase, GetTranslatedRuleUseCase {

    private final RuleTranslationRepository repository;
    private final DeonticParser cnlParser;
    private final RuleValidator validator;

    public RuleTranslationService(
            RuleTranslationRepository repository,
            DeonticParser cnlParser) {
        this.repository = repository;
        this.cnlParser = cnlParser;
        this.validator = new RuleValidator();
    }

    @Override
    public TranslationResponse translate(TranslateRuleCommand command) {
        // 1. Parse Controlled Natural Language to DeonticFormula AST representation
        DeonticFormula formula = cnlParser.parse(command.rawSourceText());

        // 2. Map targets
        String smtSpec = SmtFormulaGenerator.generate(formula);
        String odrlPolicy = OdrlPolicyGenerator.generate(formula);

        // 3. Query existing rules and validate logical consistency
        List<LegalRule> existingRules = repository.findByRegulation(command.regulationShortName());
        ValidationResult valResult = validator.validateConsistency(existingRules, formula);

        // 4. Persist LegalRule aggregate root
        LegalRule rule = LegalRule.create(
                command.regulationShortName(),
                command.articleNumber(),
                command.clauseNumber(),
                command.rawSourceText(),
                formula,
                smtSpec,
                odrlPolicy
        );

        LegalRule saved = repository.save(rule);

        return toResponse(saved, valResult.isValid(), valResult.message());
    }

    @Override
    public Optional<TranslationResponse> getById(String id) {
        return repository.findById(LegalRuleId.fromString(id))
                .map(rule -> toResponse(rule, true, "Retrieved from repository database"));
    }

    @Override
    public List<TranslationResponse> findByRegulation(String regulationShortName) {
        return repository.findByRegulation(regulationShortName).stream()
                .map(rule -> toResponse(rule, true, "Retrieved from repository database"))
                .collect(Collectors.toList());
    }

    @Override
    public List<TranslationResponse> findByRegulationAndArticle(String regulationShortName, String articleNumber) {
        return repository.findByRegulationAndArticle(regulationShortName, articleNumber).stream()
                .map(rule -> toResponse(rule, true, "Retrieved from repository database"))
                .collect(Collectors.toList());
    }

    @Override
    public List<TranslationResponse> listAll() {
        return repository.findAll().stream()
                .map(rule -> toResponse(rule, true, "Retrieved from repository database"))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        repository.deleteById(LegalRuleId.fromString(id));
    }

    private TranslationResponse toResponse(LegalRule rule, boolean isValid, String message) {
        DeonticFormula f = rule.getDeonticFormula();
        return new TranslationResponse(
                rule.getId().toString(),
                rule.getRegulationShortName(),
                rule.getArticleNumber(),
                rule.getClauseNumber(),
                rule.getRawSourceText(),
                f.getOperator().name(),
                f.getSubject(),
                f.getAction(),
                f.getTarget(),
                f.getConstraint(),
                rule.getSmtSpec(),
                rule.getOdrlPolicy(),
                isValid,
                message
        );
    }
}
