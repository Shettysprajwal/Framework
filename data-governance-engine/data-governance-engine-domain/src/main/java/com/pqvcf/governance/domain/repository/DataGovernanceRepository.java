package com.pqvcf.governance.domain.repository;

import com.pqvcf.governance.domain.model.GovernanceDecision;
import java.util.List;
import java.util.Optional;

/**
 * Output port interface for managing governance decisions auditing ledger.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface DataGovernanceRepository {
    void save(GovernanceDecision decision);
    Optional<GovernanceDecision> findById(String id);
    List<GovernanceDecision> listAll();
    void deleteById(String id);
}
