package com.pqvcf.pap.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataPolicyRepository extends JpaRepository<PolicyJpaEntity, UUID> {
    boolean existsByNameIgnoreCase(String name);
}
