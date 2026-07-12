package com.pqvcf.monitor.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlaMetricsTest {

    @Test
    @DisplayName("Should successfully compute correct compliance SLA percentage rates")
    void shouldComputeSlaRate() {
        // Case A: No events, default to 100%
        SlaMetrics m1 = new SlaMetrics(0, 0);
        assertThat(m1.getComplianceRate()).isEqualTo(100.0);

        // Case B: 10 events, 2 violations -> 80% SLA
        SlaMetrics m2 = new SlaMetrics(10, 2);
        assertThat(m2.getComplianceRate()).isEqualTo(80.0);

        // Case C: 5 events, 5 violations -> 0% SLA
        SlaMetrics m3 = new SlaMetrics(5, 5);
        assertThat(m3.getComplianceRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should verify structural equality contract for identical metrics tuples")
    void shouldVerifyEquality() {
        SlaMetrics r1 = new SlaMetrics(10, 2);
        SlaMetrics r2 = new SlaMetrics(10, 2);

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }
}
