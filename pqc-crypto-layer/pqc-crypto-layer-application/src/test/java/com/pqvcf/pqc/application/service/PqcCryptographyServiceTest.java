package com.pqvcf.pqc.application.service;

import com.pqvcf.pqc.application.port.in.GenerateKeyUseCase.GenerateKeyCommand;
import com.pqvcf.pqc.application.port.in.GenerateKeyUseCase.KeyResponseDto;
import com.pqvcf.pqc.application.port.in.SignPayloadUseCase.SignCommand;
import com.pqvcf.pqc.application.port.in.SignPayloadUseCase.SignResponseDto;
import com.pqvcf.pqc.application.port.in.SignPayloadUseCase.VerifyCommand;
import com.pqvcf.pqc.application.port.out.PqcCryptoProvider;
import com.pqvcf.pqc.domain.model.PqcKeyPair;
import com.pqvcf.pqc.domain.model.PqcKeyType;
import com.pqvcf.pqc.domain.repository.PqcKmsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PqcCryptographyServiceTest {

    @Mock
    private PqcCryptoProvider cryptoProvider;

    @Mock
    private PqcKmsRepository kmsRepository;

    private PqcCryptographyService service;

    @BeforeEach
    void setUp() {
        service = new PqcCryptographyService(cryptoProvider, kmsRepository);
    }

    @Test
    @DisplayName("Should successfully delegate key generation to provider and store key in KMS")
    void shouldGenerateKey() {
        GenerateKeyCommand command = new GenerateKeyCommand("ML_DSA_65", "test-alias");
        
        PqcKeyPair fakePair = new PqcKeyPair(
                "test-alias",
                PqcKeyType.ML_DSA_65,
                new byte[]{1, 2},
                new byte[]{3, 4},
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );
        when(cryptoProvider.generatePqcKeyPair(PqcKeyType.ML_DSA_65, "test-alias")).thenReturn(fakePair);

        KeyResponseDto dto = service.generateKey(command);

        assertThat(dto).isNotNull();
        assertThat(dto.keyId()).isEqualTo("test-alias");
        assertThat(dto.algorithm()).isEqualTo("ML_DSA_65");
        
        verify(cryptoProvider).generatePqcKeyPair(PqcKeyType.ML_DSA_65, "test-alias");
        verify(kmsRepository).save(fakePair);
    }

    @Test
    @DisplayName("Should sign payload and verify signatures through providers wrapper")
    void shouldSignAndVerifyPayloads() {
        PqcKeyPair fakePair = new PqcKeyPair(
                "alias",
                PqcKeyType.ML_DSA_65,
                new byte[]{1, 2},
                new byte[]{3, 4},
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );
        when(kmsRepository.findByKeyId("alias")).thenReturn(Optional.of(fakePair));

        byte[] payload = new byte[]{10, 20};
        byte[] sig = new byte[]{99, 88};
        
        when(cryptoProvider.sign(fakePair, payload)).thenReturn(sig);
        when(cryptoProvider.verify(fakePair, payload, sig)).thenReturn(true);

        SignResponseDto signDto = service.sign(new SignCommand("alias", "0a14"));
        assertThat(signDto.signatureHex()).isEqualTo("6358");

        boolean ok = service.verify(new VerifyCommand("alias", "0a14", "6358"));
        assertThat(ok).isTrue();

        verify(cryptoProvider).sign(fakePair, payload);
        verify(cryptoProvider).verify(fakePair, payload, sig);
    }
}
