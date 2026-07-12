package com.pqvcf.pqc.api.grpc;

import com.pqvcf.pqc.application.port.in.GenerateKeyUseCase;
import com.pqvcf.pqc.application.port.in.SignPayloadUseCase;
import com.pqvcf.pqc.grpc.KeyPairRequest;
import com.pqvcf.pqc.grpc.KeyPairResponse;
import com.pqvcf.pqc.grpc.PqcCryptographyGrpc;
import com.pqvcf.pqc.grpc.SignPayloadRequest;
import com.pqvcf.pqc.grpc.SignPayloadResponse;
import com.pqvcf.pqc.grpc.VerifySignatureRequest;
import com.pqvcf.pqc.grpc.VerifySignatureResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class PqcGrpcService extends PqcCryptographyGrpc.PqcCryptographyImplBase {

    private final GenerateKeyUseCase generateUseCase;
    private final SignPayloadUseCase signUseCase;

    public PqcGrpcService(GenerateKeyUseCase generateUseCase, SignPayloadUseCase signUseCase) {
        this.generateUseCase = generateUseCase;
        this.signUseCase = signUseCase;
    }

    @Override
    public void generateKeyPair(
            KeyPairRequest request,
            StreamObserver<KeyPairResponse> responseObserver) {
        try {
            GenerateKeyUseCase.GenerateKeyCommand command = new GenerateKeyUseCase.GenerateKeyCommand(
                    request.getAlgorithm(),
                    request.getAlias()
            );

            GenerateKeyUseCase.KeyResponseDto dto = generateUseCase.generateKey(command);

            KeyPairResponse response = KeyPairResponse.newBuilder()
                    .setKeyId(dto.keyId())
                    .setAlgorithm(dto.algorithm())
                    .setPublicKeyHex(dto.publicKeyHex())
                    .setCreatedAt(dto.createdAt().toString())
                    .setExpiresAt(dto.expiresAt().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to generate PQC key pair: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void signPayload(
            SignPayloadRequest request,
            StreamObserver<SignPayloadResponse> responseObserver) {
        try {
            SignPayloadUseCase.SignCommand command = new SignPayloadUseCase.SignCommand(
                    request.getKeyId(),
                    request.getPayloadHex()
            );

            SignPayloadUseCase.SignResponseDto dto = signUseCase.sign(command);

            SignPayloadResponse response = SignPayloadResponse.newBuilder()
                    .setSignatureHex(dto.signatureHex())
                    .setAlgorithm(dto.algorithm())
                    .setKeyId(dto.keyId())
                    .setSignatureLength(dto.length())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to generate PQC signature: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void verifySignature(
            VerifySignatureRequest request,
            StreamObserver<VerifySignatureResponse> responseObserver) {
        try {
            SignPayloadUseCase.VerifyCommand command = new SignPayloadUseCase.VerifyCommand(
                    request.getKeyId(),
                    request.getPayloadHex(),
                    request.getSignatureHex()
            );

            boolean isValid = signUseCase.verify(command);

            VerifySignatureResponse response = VerifySignatureResponse.newBuilder()
                    .setIsValid(isValid)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to verify PQC signature: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
