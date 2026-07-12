package com.pqvcf.ledger.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashChainTest {

    @Test
    @DisplayName("Should successfully create a valid HashChain verification outcome")
    void shouldCreateHashChain() {
        HashChain hc = new HashChain(true, -1, "Integrity verified");

        assertThat(hc).isNotNull();
        assertThat(hc.isValid()).isTrue();
        assertThat(hc.getTamperedIndex()).isEqualTo(-1);
        assertThat(hc.getDetails()).isEqualTo("Integrity verified");
    }

    @Test
    @DisplayName("Should verify structural equality contract for identical chain verification tuples")
    void shouldVerifyEquality() {
        HashChain c1 = new HashChain(false, 3, "Tampered");
        HashChain c2 = new HashChain(false, 3, "Tampered");

        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }
}
