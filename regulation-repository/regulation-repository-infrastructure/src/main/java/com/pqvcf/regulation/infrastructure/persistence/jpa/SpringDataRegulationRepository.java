package com.pqvcf.regulation.infrastructure.persistence.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataRegulationRepository extends JpaRepository<RegulationJpaEntity, UUID> {

    Optional<RegulationJpaEntity> findByShortName(String shortName);

    List<RegulationJpaEntity> findByPrimaryJurisdiction(String primaryJurisdiction);

    List<RegulationJpaEntity> findByStatus(String status);

    boolean existsByShortName(String shortName);

    @Query("SELECT r FROM RegulationJpaEntity r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.shortName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<RegulationJpaEntity> search(@Param("query") String query);
}
