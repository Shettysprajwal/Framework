package com.pqvcf.zkp.application.service;

import com.pqvcf.zkp.application.port.in.GenerateProofUseCase.GenerateProofCommand;
import com.pqvcf.zkp.application.port.in.GenerateProofUseCase.ProofResponseDto;
import com.pqvcf.zkp.application.port.in.VerifyProofUseCase.VerifyProofCommand;
import com.pqvcf.zkp.application.port.out.ZkProofProvider;
import com.pqvcf.zkp.domain.model.PedersenCommitment;
import com.pqvcf.zkp.domain.model.ZkProof;
import com.pqvcf.zkp.domain.model.ZkProofType;
import com.pqvcf.zkp.domain.repository.ZkProofRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZkProofServiceTest {

    @Mock
    private ZkProofProvider proofProvider;

    @Mock
    private ZkProofRepository proofRepository;

    private ZkProofService service;

    @BeforeEach
    void setUp() {
        service = new ZkProofService(proofProvider, proofRepository);
    }

    @Test
    @DisplayName("Should successfully delegate Pedersen commitment calculation and Sigma proof generation")
    void shouldGenerateProof() {
        GenerateProofCommand command = new GenerateProofCommand("DATA_RESIDENCY", 101, "{\"region\": \"EU\"}");

        PedersenCommitment fakeCommit = new PedersenCommitment(new byte[]{1}, new byte[]{2}, 101);
        ZkProof fakeProof = new ZkProof(
                "proof-id",
                ZkProofType.DATA_RESIDENCY,
                fakeCommit,
                new byte[]{3},
                new byte[]{4},
                "{\"region\": \"EU\"}",
                true
        );

        when(proofProvider.commitPedersen(101)).thenReturn(fakeCommit);
        when(proofProvider.proveSigma(ZkProofType.DATA_RESIDENCY, fakeCommit, "{\"region\": \"EU\"}"))
                .thenReturn(fakeProof);

        ProofResponseDto dto = service.generateProof(command);

        assertThat(dto).isNotNull();
        assertThat(dto.proofId()).isEqualTo("proof-id");
        assertThat(dto.proofType()).isEqualTo("DATA_RESIDENCY");

        verify(proofProvider).commitPedersen(101);
        verify(proofProvider).proveSigma(ZkProofType.DATA_RESIDENCY, fakeCommit, "{\"region\": \"EU\"}");
        verify(proofRepository).save(fakeProof);
    }

    @Test
    @DisplayName("Should load proof from repository and verify challenge-responses")
    void shouldVerifyProof() {
        PedersenCommitment fakeCommit = new PedersenCommitment(new byte[]{1}, new byte[]{2}, 101);
        ZkProof fakeProof = new ZkProof(
                "proof-id",
                ZkProofType.DATA_RESIDENCY,
                fakeCommit,
                new byte[]{3},
                new byte[]{4},
                "{}",
                true
        );
        when(proofRepository.findByProofId("proof-id")).thenReturn(Optional.of(fakeProof));
        when(proofProvider.verifySigma(any(ZkProof.class))).thenReturn(true);

        VerifyProofCommand command = new VerifyProofCommand(
                "proof-id",
                "03", // challengeHex
                "04", // responseHex
                "01"  // commitmentHex
        );

        boolean ok = service.verifyProof(command);
        assertThat(ok).isTrue();

        verify(proofRepository).findByProofId("proof-id");
        verify(proofProvider).verifySigma(any(ZkProof.class));
    }
}
