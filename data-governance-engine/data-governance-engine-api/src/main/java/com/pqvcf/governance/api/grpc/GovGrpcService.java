package com.pqvcf.governance.api.grpc;

import com.pqvcf.governance.application.port.in.EvaluateFlowUseCase;
import com.pqvcf.governance.grpc.DataGovernanceGrpc;
import com.pqvcf.governance.grpc.TransferRequest;
import com.pqvcf.governance.grpc.TransferResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class GovGrpcService extends DataGovernanceGrpc.DataGovernanceImplBase {

    private final EvaluateFlowUseCase evaluateUseCase;

    public GovGrpcService(EvaluateFlowUseCase evaluateUseCase) {
        this.evaluateUseCase = evaluateUseCase;
    }

    @Override
    public void evaluateTransfer(
            TransferRequest request,
            StreamObserver<TransferResponse> responseObserver) {
        try {
            EvaluateFlowUseCase.EvaluateFlowCommand command = new EvaluateFlowUseCase.EvaluateFlowCommand(
                    request.getSourceCountry(),
                    request.getTargetCountry(),
                    request.getDataCategory(),
                    request.getProcessingPurpose()
            );

            EvaluateFlowUseCase.FlowDecisionResponseDto dto = evaluateUseCase.evaluateFlow(command);

            TransferResponse response = TransferResponse.newBuilder()
                    .setDecisionId(dto.decisionId())
                    .setSourceCountry(dto.sourceCountry())
                    .setTargetCountry(dto.targetCountry())
                    .setDataCategory(dto.dataCategory())
                    .setProcessingPurpose(dto.processingPurpose())
                    .setDecision(dto.decision())
                    .setReasoning(dto.reasoning())
                    .addAllCitations(dto.citations())
                    .setEvidenceLink(dto.evidenceLink())
                    .setCreatedAt(dto.createdAt())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to process transfer evaluation: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
