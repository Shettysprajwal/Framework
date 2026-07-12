package com.pqvcf.translation.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataRuleTranslationRepository extends JpaRepository<RuleTranslationJpaEntity, UUID> {
    List<RuleTranslationJpaEntity> findByRegulationShortNameIgnoreCase(String regulationShortName);
    List<RuleTranslationJpaEntity> findByRegulationShortNameIgnoreCaseAndArticleNumberIgnoreCase(String regulationShortName, String articleNumber);
}
