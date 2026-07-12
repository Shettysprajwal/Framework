package com.pqvcf.pqc.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PqcKeyPairTest {

    @Test
    @DisplayName("Should successfully create a valid PqcKeyPair value object and check expiration")
    void shouldCreatePqcKeyPair() {
        byte[] pub = new byte[]{1, 2, 3};
        byte[] priv = new byte[]{4, 5, 6};
        
        PqcKeyPair pair = new PqcKeyPair(
                "alias-key",
                PqcKeyType.ML_DSA_65,
                pub,
                priv,
                Instant.now(),
                Instant.now().plus(90, ChronoUnit.DAYS)
        );

        assertThat(pair).isNotNull();
        assertThat(pair.getKeyId()).isEqualTo("alias-key");
        assertThat(pair.getKeyType()).isEqualTo(PqcKeyType.ML_DSA_65);
        assertThat(pair.getPublicKeyBytes()).isEqualTo(pub);
        assertThat(pair.getPrivateKeyBytes()).isEqualTo(priv);
        assertThat(pair.isExpired()).isFalse();
    }

    @Test
    @DisplayName("Should verify structural equality contract for identical key pairs")
    void shouldVerifyEquality() {
        PqcKeyPair r1 = new PqcKeyPair("key", PqcKeyType.ML_KEM_768, new byte[]{1}, new byte[]{2}, Instant.MIN, Instant.MAX);
        PqcKeyPair r2 = new PqcKeyPair("key", PqcKeyType.ML_KEM_768, new byte[]{1}, new byte[]{2}, Instant.MIN, Instant.MAX);

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }
}
