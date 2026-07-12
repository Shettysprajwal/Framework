package com.pqvcf.ledger.api.grpc;

import com.pqvcf.ledger.application.port.in.SealRecordUseCase;
import com.pqvcf.ledger.grpc.AuditRequest;
import com.pqvcf.ledger.grpc.AuditResponse;
import com.pqvcf.ledger.grpc.AuditingLedgerGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class LedgerGrpcService extends AuditingLedgerGrpc.AuditingLedgerImplBase {

    private final SealRecordUseCase sealUseCase;

    public LedgerGrpcService(SealRecordUseCase sealUseCase) {
        this.sealUseCase = sealUseCase;
    }

    @Override
    public void logAudit(
            AuditRequest request,
            StreamObserver<AuditResponse> responseObserver) {
        try {
            SealRecordUseCase.SealRecordCommand command = new SealRecordUseCase.SealRecordCommand(
                    request.getAction(),
                    request.getActor(),
                    request.getTarget(),
                    request.getDecision()
            );

            SealRecordUseCase.RecordDto dto = sealUseCase.seal(command);

            AuditResponse response = AuditResponse.newBuilder()
                    .setRecordId(dto.id())
                    .setTimestamp(dto.timestamp())
                    .setAction(dto.action())
                    .setActor(dto.actor())
                    .setTarget(dto.target())
                    .setDecision(dto.decision())
                    .setPreviousHash(dto.previousHash())
                    .setCurrentHash(dto.currentHash())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to log audit record: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
