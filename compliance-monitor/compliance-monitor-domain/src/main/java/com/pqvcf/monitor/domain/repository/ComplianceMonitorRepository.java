package com.pqvcf.monitor.domain.repository;

import com.pqvcf.monitor.domain.model.ComplianceEvent;
import com.pqvcf.monitor.domain.model.PolicyViolation;
import com.pqvcf.monitor.domain.model.SlaMetrics;

import java.util.List;

/**
 * Output port interface for querying monitoring caches and alerts records.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface ComplianceMonitorRepository {
    void logEvent(ComplianceEvent event);
    void raiseViolation(PolicyViolation violation);
    List<ComplianceEvent> listEvents();
    List<PolicyViolation> listViolations();
    SlaMetrics getSlaMetrics();
    void resetMetrics();
}
