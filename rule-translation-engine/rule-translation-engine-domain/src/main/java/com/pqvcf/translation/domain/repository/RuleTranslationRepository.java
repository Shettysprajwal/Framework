package com.pqvcf.translation.domain.repository;

import com.pqvcf.translation.domain.model.LegalRule;
import com.pqvcf.translation.domain.model.LegalRuleId;
import java.util.List;
import java.util.Optional;

/**
 * Port interface for persisting translated legal rules.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface RuleTranslationRepository {
    LegalRule save(LegalRule rule);
    Optional<LegalRule> findById(LegalRuleId id);
    List<LegalRule> findByRegulation(String regulationShortName);
    List<LegalRule> findByRegulationAndArticle(String regulationShortName, String articleNumber);
    List<LegalRule> findAll();
    void deleteById(LegalRuleId id);
}
