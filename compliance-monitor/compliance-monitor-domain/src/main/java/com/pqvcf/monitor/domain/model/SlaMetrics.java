package com.pqvcf.monitor.domain.model;

import com.pqvcf.shared.domain.ValueObject;
import java.util.Objects;

/**
 * Value Object compiling continuous compliance SLA metrics.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class SlaMetrics extends ValueObject {

    private final int totalEvents;
    private final int violationCount;
    private final double complianceRate;

    public SlaMetrics(int totalEvents, int violationCount) {
        this.totalEvents = totalEvents;
        this.violationCount = violationCount;
        this.complianceRate = totalEvents == 0 
                ? 100.0 
                : Math.max(0.0, Math.min(100.0, ((double) (totalEvents - violationCount) / totalEvents) * 100.0));
    }

    public int getTotalEvents() { return totalEvents; }
    public int getViolationCount() { return violationCount; }
    public double getComplianceRate() { return complianceRate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SlaMetrics that)) return false;
        return totalEvents == that.totalEvents &&
                violationCount == that.violationCount &&
                Double.compare(that.complianceRate, complianceRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalEvents, violationCount, complianceRate);
    }
}
