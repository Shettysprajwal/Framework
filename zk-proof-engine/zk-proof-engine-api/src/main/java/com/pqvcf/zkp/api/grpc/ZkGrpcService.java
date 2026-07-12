package com.pqvcf.zkp.api.grpc;

import com.pqvcf.zkp.application.port.in.GenerateProofUseCase;
import com.pqvcf.zkp.application.port.in.VerifyProofUseCase;
import com.pqvcf.zkp.grpc.ProofRequest;
import com.pqvcf.zkp.grpc.ProofResponse;
import com.pqvcf.zkp.grpc.VerifyRequest;
import com.pqvcf.zkp.grpc.VerifyResponse;
import com.pqvcf.zkp.grpc.ZkProofServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class ZkGrpcService extends ZkProofServiceGrpc.ZkProofServiceImplBase {

    private final GenerateProofUseCase generateUseCase;
    private final VerifyProofUseCase verifyUseCase;

    public ZkGrpcService(GenerateProofUseCase generateUseCase, VerifyProofUseCase verifyUseCase) {
        this.generateUseCase = generateUseCase;
        this.verifyUseCase = verifyUseCase;
    }

    @Override
    public void generateProof(
            ProofRequest request,
            StreamObserver<ProofResponse> responseObserver) {
        try {
            GenerateProofUseCase.GenerateProofCommand command = new GenerateProofUseCase.GenerateProofCommand(
                    request.getProofType(),
                    request.getSecretWitnessValue(),
                    request.getPublicInputsJson()
            );

            GenerateProofUseCase.ProofResponseDto dto = generateUseCase.generateProof(command);

            ProofResponse response = ProofResponse.newBuilder()
                    .setProofId(dto.proofId())
                    .setProofType(dto.proofType())
                    .setCommitmentHex(dto.commitmentHex())
                    .setChallengeHex(dto.challengeHex())
                    .setResponseHex(dto.responseHex())
                    .setPublicInputsJson(dto.publicInputsJson())
                    .setVerified(dto.verified())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to generate ZK proof: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void verifyProof(
            VerifyRequest request,
            StreamObserver<VerifyResponse> responseObserver) {
        try {
            VerifyProofUseCase.VerifyProofCommand command = new VerifyProofUseCase.VerifyProofCommand(
                    request.getProofId(),
                    request.getChallengeHex(),
                    request.getResponseHex(),
                    request.getCommitmentHex()
            );

            boolean isValid = verifyUseCase.verifyProof(command);

            VerifyResponse response = VerifyResponse.newBuilder()
                    .setIsValid(isValid)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to verify ZK proof: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
