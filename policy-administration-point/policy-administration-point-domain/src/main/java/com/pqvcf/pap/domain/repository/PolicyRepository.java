package com.pqvcf.pap.domain.repository;

import com.pqvcf.pap.domain.model.Policy;
import com.pqvcf.pap.domain.model.PolicyId;
import java.util.List;
import java.util.Optional;

/**
 * Output port interface for persisting organizational policies.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface PolicyRepository {
    Policy save(Policy policy);
    Optional<Policy> findById(PolicyId id);
    List<Policy> findAll();
    void deleteById(PolicyId id);
    boolean existsByName(String name);
}
