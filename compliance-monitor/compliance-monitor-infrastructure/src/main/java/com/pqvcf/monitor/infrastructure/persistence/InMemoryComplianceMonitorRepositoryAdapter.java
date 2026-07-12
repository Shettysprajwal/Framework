package com.pqvcf.monitor.infrastructure.persistence;

import com.pqvcf.monitor.domain.model.ComplianceEvent;
import com.pqvcf.monitor.domain.model.PolicyViolation;
import com.pqvcf.monitor.domain.model.SlaMetrics;
import com.pqvcf.monitor.domain.repository.ComplianceMonitorRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class InMemoryComplianceMonitorRepositoryAdapter implements ComplianceMonitorRepository {

    private final List<ComplianceEvent> events = new CopyOnWriteArrayList<>();
    private final List<PolicyViolation> violations = new CopyOnWriteArrayList<>();

    @Override
    public void logEvent(ComplianceEvent event) {
        if (event == null) return;
        events.add(event);
    }

    @Override
    public void raiseViolation(PolicyViolation violation) {
        if (violation == null) return;
        violations.add(violation);
    }

    @Override
    public List<ComplianceEvent> listEvents() {
        // Return reversed to get newest first
        List<ComplianceEvent> list = new ArrayList<>(events);
        Collections.reverse(list);
        return list;
    }

    @Override
    public List<PolicyViolation> listViolations() {
        List<PolicyViolation> list = new ArrayList<>(violations);
        Collections.reverse(list);
        return list;
    }

    @Override
    public SlaMetrics getSlaMetrics() {
        int total = events.size();
        
        // Count critical violations only for SLA degradation
        long criticalCount = violations.stream()
                .filter(v -> "CRITICAL".equalsIgnoreCase(v.getSeverity()))
                .count();

        return new SlaMetrics(total, (int) criticalCount);
    }

    @Override
    public void resetMetrics() {
        events.clear();
        violations.clear();
    }
}
