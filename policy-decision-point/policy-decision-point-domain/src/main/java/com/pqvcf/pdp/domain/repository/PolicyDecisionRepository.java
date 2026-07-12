package com.pqvcf.pdp.domain.repository;

import com.pqvcf.pdp.domain.model.DecisionRequest;
import com.pqvcf.pdp.domain.model.DecisionResult;
import java.util.List;

/**
 * Output port interface for logging and querying compliance audit traces.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface PolicyDecisionRepository {
    
    /**
     * Log compliance verification audit trail.
     */
    void logDecision(DecisionRequest request, DecisionResult result);

    /**
     * Fetch past verification audits.
     */
    List<DecisionAuditLog> fetchAllAuditLogs();

    record DecisionAuditLog(
            String id,
            String subjectId,
            String resourceId,
            String actionId,
            String sourceCountry,
            String targetCountry,
            String policyName,
            String effect,
            String proofTrace,
            String validationLog,
            String solvedAt
    ) {}
}
