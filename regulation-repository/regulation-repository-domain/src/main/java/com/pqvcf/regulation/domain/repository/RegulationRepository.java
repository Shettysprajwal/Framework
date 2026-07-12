package com.pqvcf.regulation.domain.repository;

import com.pqvcf.regulation.domain.model.Regulation;
import com.pqvcf.regulation.domain.model.RegulationId;
import com.pqvcf.regulation.domain.model.RegulationStatus;
import com.pqvcf.shared.types.JurisdictionCode;

import java.util.List;
import java.util.Optional;

/**
 * Repository port (output port) for the {@link Regulation} aggregate.
 *
 * <p>This interface is defined in the domain layer and implemented by the infrastructure
 * layer (PostgreSQL adapter). The application layer depends only on this interface,
 * never on the infrastructure implementation — enforcing the Dependency Inversion Principle.
 *
 * <p>All methods operate on domain objects, never on JPA entities or database primitives.
 * The infrastructure adapter is responsible for all object-relational mapping.
 *
 * <p><b>Transaction semantics:</b> The infrastructure implementation must ensure that
 * saving a regulation persists all its articles and clauses atomically. Domain events
 * raised during a transaction must be dispatched only after successful commit.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface RegulationRepository {

    /**
     * Persists a regulation (insert or update).
     * After saving, the infrastructure must dispatch domain events via {@code pullDomainEvents()}.
     *
     * @param regulation the regulation to save
     * @return the saved regulation (may contain generated values)
     */
    Regulation save(Regulation regulation);

    /**
     * Retrieves a regulation by its unique identity.
     *
     * @param id the regulation identity
     * @return an Optional containing the regulation if found
     */
    Optional<Regulation> findById(RegulationId id);

    /**
     * Retrieves a regulation by its unique short name (e.g., "GDPR").
     *
     * @param shortName the normalized short name
     * @return an Optional containing the regulation if found
     */
    Optional<Regulation> findByShortName(String shortName);

    /**
     * Lists all regulations applicable to a given jurisdiction.
     *
     * @param jurisdictionCode the jurisdiction code
     * @return list of regulations applicable to that jurisdiction
     */
    List<Regulation> findByJurisdiction(JurisdictionCode jurisdictionCode);

    /**
     * Lists all regulations with the given status.
     *
     * @param status the lifecycle status to filter by
     * @return list of regulations with that status
     */
    List<Regulation> findByStatus(RegulationStatus status);

    /**
     * Lists all regulations in the system.
     *
     * @return all regulations, sorted by short name
     */
    List<Regulation> findAll();

    /**
     * Returns a paginated list of regulations.
     *
     * @param page     the zero-based page number
     * @param pageSize the number of results per page
     * @return paginated list of regulations
     */
    List<Regulation> findAll(int page, int pageSize);

    /**
     * Returns the total count of regulations.
     *
     * @return total number of regulations in the repository
     */
    long count();

    /**
     * Checks whether a regulation with the given short name already exists.
     *
     * @param shortName the short name to check
     * @return {@code true} if a regulation with that short name exists
     */
    boolean existsByShortName(String shortName);

    /**
     * Deletes a regulation by its identity.
     * Only DRAFT regulations should be deleted; ACTIVE/DEPRECATED should be deprecated instead.
     *
     * @param id the regulation identity
     */
    void deleteById(RegulationId id);

    /**
     * Full-text search across regulation names and descriptions.
     *
     * @param query the search term
     * @return list of matching regulations
     */
    List<Regulation> search(String query);
}
