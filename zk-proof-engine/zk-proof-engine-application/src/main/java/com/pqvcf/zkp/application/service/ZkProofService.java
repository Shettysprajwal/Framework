package com.pqvcf.zkp.application.service;

import com.pqvcf.zkp.application.port.in.GenerateProofUseCase;
import com.pqvcf.zkp.application.port.in.VerifyProofUseCase;
import com.pqvcf.zkp.application.port.in.ZkProofQueryUseCase;
import com.pqvcf.zkp.application.port.out.ZkProofProvider;
import com.pqvcf.zkp.domain.model.PedersenCommitment;
import com.pqvcf.zkp.domain.model.ZkProof;
import com.pqvcf.zkp.domain.model.ZkProofType;
import com.pqvcf.zkp.domain.repository.ZkProofRepository;

import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ZkProofService implements GenerateProofUseCase, VerifyProofUseCase, ZkProofQueryUseCase {

    private final ZkProofProvider proofProvider;
    private final ZkProofRepository proofRepository;

    public ZkProofService(
            ZkProofProvider proofProvider,
            ZkProofRepository proofRepository) {
        this.proofProvider = proofProvider;
        this.proofRepository = proofRepository;
    }

    @Override
    public ProofResponseDto generateProof(GenerateProofCommand command) {
        ZkProofType type = ZkProofType.valueOf(command.proofType().toUpperCase().replace("-", "_"));
        
        // 1. Pedersen commitment computation
        PedersenCommitment commitment = proofProvider.commitPedersen(command.secretWitnessValue());

        // 2. Proving Sigma protocol
        ZkProof proof = proofProvider.proveSigma(type, commitment, command.publicInputsJson());
        
        // 3. Save proof
        proofRepository.save(proof);

        return mapToResponse(proof);
    }

    @Override
    public boolean verifyProof(VerifyProofCommand command) {
        // Find existing proof to check constraints
        ZkProof existing = proofRepository.findByProofId(command.proofId())
                .orElseThrow(() -> new IllegalArgumentException("Proof not found in ledger database: " + command.proofId()));

        // Reconstruct proof structure from input hex strings
        byte[] challenge = HexFormat.of().parseHex(command.challengeHex());
        byte[] response = HexFormat.of().parseHex(command.responseHex());
        byte[] commit = HexFormat.of().parseHex(command.commitmentHex());

        PedersenCommitment pedCommit = new PedersenCommitment(
                commit,
                existing.getCommitment().getBlindingFactor(),
                existing.getCommitment().getSecretValue()
        );

        ZkProof target = new ZkProof(
                existing.getProofId(),
                existing.getProofType(),
                pedCommit,
                challenge,
                response,
                existing.getPublicInputs(),
                false
        );

        return proofProvider.verifySigma(target);
    }

    @Override
    public List<ProofResponseDto> listAllProofs() {
        return proofRepository.listAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProofResponseDto> getProofDetails(String proofId) {
        return proofRepository.findByProofId(proofId).map(this::mapToResponse);
    }

    @Override
    public void deleteProof(String proofId) {
        proofRepository.deleteByProofId(proofId);
    }

    private ProofResponseDto mapToResponse(ZkProof proof) {
        return new ProofResponseDto(
                proof.getProofId(),
                proof.getProofType().name(),
                HexFormat.of().formatHex(proof.getCommitment().getCommitmentBytes()),
                HexFormat.of().formatHex(proof.getChallengeBytes()),
                HexFormat.of().formatHex(proof.getResponseBytes()),
                proof.getPublicInputs(),
                proof.isVerified()
        );
    }
}
