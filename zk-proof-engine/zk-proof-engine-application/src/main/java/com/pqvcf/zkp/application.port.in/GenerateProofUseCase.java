package com.pqvcf.zkp.application.port.in;

public interface GenerateProofUseCase {

    ProofResponseDto generateProof(GenerateProofCommand command);

    record GenerateProofCommand(
            String proofType,
            long secretWitnessValue,
            String publicInputsJson
    ) {}

    record ProofResponseDto(
            String proofId,
            String proofType,
            String commitmentHex,
            String challengeHex,
            String responseHex,
            String publicInputsJson,
            boolean verified
    ) {}
}
