package com.pqvcf.zkp.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PedersenCommitmentTest {

    @Test
    @DisplayName("Should successfully create a valid PedersenCommitment value object")
    void shouldCreateCommitment() {
        byte[] commit = new byte[]{1, 2, 3};
        byte[] blind = new byte[]{9, 8};
        long secret = 42;

        PedersenCommitment pc = new PedersenCommitment(commit, blind, secret);

        assertThat(pc).isNotNull();
        assertThat(pc.getCommitmentBytes()).isEqualTo(commit);
        assertThat(pc.getBlindingFactor()).isEqualTo(blind);
        assertThat(pc.getSecretValue()).isEqualTo(secret);
    }

    @Test
    @DisplayName("Should verify structural equality contract for identical commitments")
    void shouldVerifyEquality() {
        PedersenCommitment r1 = new PedersenCommitment(new byte[]{1}, new byte[]{2}, 5);
        PedersenCommitment r2 = new PedersenCommitment(new byte[]{1}, new byte[]{2}, 5);

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }
}
