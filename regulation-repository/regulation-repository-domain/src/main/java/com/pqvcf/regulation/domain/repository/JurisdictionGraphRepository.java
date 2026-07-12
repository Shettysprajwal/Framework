package com.pqvcf.regulation.domain.repository;

import com.pqvcf.shared.types.JurisdictionCode;

import java.util.List;
import java.util.Map;

/**
 * Port interface for structural regulation and jurisdiction graph operations (Neo4j).
 *
 * <p>Enables structural reasoning over jurisdictions, adequacy decisions, conflicts,
 * and data categories.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface JurisdictionGraphRepository {

    /**
     * Registers a jurisdiction in the graph database.
     *
     * @param code the jurisdiction code
     */
    void addJurisdiction(JurisdictionCode code);

    /**
     * Declares that one jurisdiction has determined another to have an adequate level of protection
     * (e.g., GDPR Art. 45 adequacy decisions).
     *
     * @param source the declaring jurisdiction (e.g., "EU")
     * @param target the adequate jurisdiction (e.g., "GB")
     */
    void addAdequacyDecision(JurisdictionCode source, JurisdictionCode target);

    /**
     * Checks if a target jurisdiction is deemed adequate by a source jurisdiction (direct or transitive).
     *
     * @param source the source jurisdiction
     * @param target the target jurisdiction
     * @return true if adequate, false otherwise
     */
    boolean isAdequate(JurisdictionCode source, JurisdictionCode target);

    /**
     * Syncs a regulation and its articles to the graph.
     *
     * @param regulationId the regulation ID
     * @param shortName the regulation short name
     * @param jurisdiction the primary jurisdiction
     */
    void syncRegulation(String regulationId, String shortName, JurisdictionCode jurisdiction);

    /**
     * Syncs an article to the graph.
     */
    void syncArticle(String articleId, String regulationId, String articleNumber, String title);

    /**
     * Syncs a clause to the graph and links it to its article.
     */
    void syncClause(String clauseId, String articleId, String clauseNumber, String clauseType);

    /**
     * Returns a representation of the regulation graph for visualization in the research dashboard.
     *
     * @return a map containing nodes and relationships
     */
    Map<String, Object> getGraphData();
}
