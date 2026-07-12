package com.pqvcf.pdp.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionResultTest {

    @Test
    @DisplayName("Should successfully create a valid DecisionResult and populate timestamps")
    void shouldCreateDecisionResult() {
        DecisionResult result = new DecisionResult(
                DecisionEffect.PERMIT,
                "(check-sat) ; verified",
                "Verified compliant via mock solver"
        );

        assertThat(result).isNotNull();
        assertThat(result.getEffect()).isEqualTo(DecisionEffect.PERMIT);
        assertThat(result.getProofTrace()).contains("check-sat");
        assertThat(result.getValidationLog()).contains("Verified");
        assertThat(result.getSolvedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should verify structural equality contract for identical result tuples")
    void shouldVerifyEquality() {
        DecisionResult r1 = new DecisionResult(DecisionEffect.DENY, "trace", "log");
        DecisionResult r2 = new DecisionResult(DecisionEffect.DENY, "trace", "log");

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }
}
