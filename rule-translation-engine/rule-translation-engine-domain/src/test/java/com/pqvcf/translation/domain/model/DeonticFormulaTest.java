package com.pqvcf.translation.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeonticFormulaTest {

    @Test
    @DisplayName("Should successfully create a valid DeonticFormula value object")
    void shouldCreateDeonticFormula() {
        DeonticFormula formula = new DeonticFormula(
                DeonticOperator.PERMISSION,
                "controller",
                "transfer",
                "personal_data",
                "adequacy_decision_present"
        );

        assertThat(formula).isNotNull();
        assertThat(formula.getOperator()).isEqualTo(DeonticOperator.PERMISSION);
        assertThat(formula.getSubject()).isEqualTo("controller");
        assertThat(formula.getAction()).isEqualTo("transfer");
        assertThat(formula.getTarget()).isEqualTo("personal_data");
        assertThat(formula.getConstraint()).isEqualTo("adequacy_decision_present");
        assertThat(formula.hasConstraint()).isTrue();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if subject is empty")
    void shouldThrowIfSubjectEmpty() {
        assertThatThrownBy(() -> new DeonticFormula(
                DeonticOperator.PROHIBITION,
                "",
                "process",
                "data",
                "none"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Subject identifier");
    }

    @Test
    @DisplayName("Should verify equals and hashCode structural equality contract")
    void shouldVerifyEquality() {
        DeonticFormula f1 = new DeonticFormula(
                DeonticOperator.OBLIGATION,
                "processor",
                "encrypt",
                "health_records",
                ""
        );

        DeonticFormula f2 = new DeonticFormula(
                DeonticOperator.OBLIGATION,
                "processor",
                "encrypt",
                "health_records",
                ""
        );

        assertThat(f1).isEqualTo(f2);
        assertThat(f1.hashCode()).isEqualTo(f2.hashCode());
    }
}
