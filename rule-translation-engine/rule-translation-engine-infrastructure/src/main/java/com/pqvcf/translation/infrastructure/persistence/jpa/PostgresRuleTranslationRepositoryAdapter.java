package com.pqvcf.translation.infrastructure.persistence.jpa;

import com.pqvcf.translation.domain.model.DeonticFormula;
import com.pqvcf.translation.domain.model.DeonticOperator;
import com.pqvcf.translation.domain.model.LegalRule;
import com.pqvcf.translation.domain.model.LegalRuleId;
import com.pqvcf.translation.domain.repository.RuleTranslationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PostgresRuleTranslationRepositoryAdapter implements RuleTranslationRepository {

    private final SpringDataRuleTranslationRepository springRepository;

    public PostgresRuleTranslationRepositoryAdapter(SpringDataRuleTranslationRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public LegalRule save(LegalRule rule) {
        RuleTranslationJpaEntity entity = mapToJpa(rule);
        RuleTranslationJpaEntity saved = springRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<LegalRule> findById(LegalRuleId id) {
        return springRepository.findById(id.getValue()).map(this::mapToDomain);
    }

    @Override
    public List<LegalRule> findByRegulation(String regulationShortName) {
        return springRepository.findByRegulationShortNameIgnoreCase(regulationShortName).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LegalRule> findByRegulationAndArticle(String regulationShortName, String articleNumber) {
        return springRepository.findByRegulationShortNameIgnoreCaseAndArticleNumberIgnoreCase(regulationShortName, articleNumber).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LegalRule> findAll() {
        return springRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(LegalRuleId id) {
        springRepository.deleteById(id.getValue());
    }

    private RuleTranslationJpaEntity mapToJpa(LegalRule domain) {
        RuleTranslationJpaEntity jpa = new RuleTranslationJpaEntity();
        jpa.setId(domain.getId().getValue());
        jpa.setRegulationShortName(domain.getRegulationShortName());
        jpa.setArticleNumber(domain.getArticleNumber());
        jpa.setClauseNumber(domain.getClauseNumber());
        jpa.setRawSourceText(domain.getRawSourceText());

        DeonticFormula f = domain.getDeonticFormula();
        jpa.setDeonticOperator(f.getOperator().name());
        jpa.setSubject(f.getSubject());
        jpa.setAction(f.getAction());
        jpa.setTarget(f.getTarget());
        jpa.setConstraintText(f.getConstraint());
        
        jpa.setSmtSpec(domain.getSmtSpec());
        jpa.setOdrlPolicy(domain.getOdrlPolicy());
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        return jpa;
    }

    private LegalRule mapToDomain(RuleTranslationJpaEntity jpa) {
        DeonticFormula formula = new DeonticFormula(
                DeonticOperator.valueOf(jpa.getDeonticOperator().toUpperCase()),
                jpa.getSubject(),
                jpa.getAction(),
                jpa.getTarget(),
                jpa.getConstraintText()
        );

        return LegalRule.reconstitute(
                LegalRuleId.of(jpa.getId()),
                jpa.getRegulationShortName(),
                jpa.getArticleNumber(),
                jpa.getClauseNumber(),
                jpa.getRawSourceText(),
                formula,
                jpa.getSmtSpec(),
                jpa.getOdrlPolicy(),
                jpa.getCreatedAt(),
                jpa.getUpdatedAt()
        );
    }
}
