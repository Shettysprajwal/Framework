package com.pqvcf.pdp.api.grpc;

import com.pqvcf.pdp.application.port.in.EvaluateRequestUseCase;
import com.pqvcf.pdp.application.port.in.EvaluateRequestUseCase.EvaluateCommand;
import com.pqvcf.pdp.grpc.EvaluateRequest;
import com.pqvcf.pdp.grpc.EvaluateResponse;
import com.pqvcf.pdp.grpc.PolicyDecisionPointGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class PdpGrpcService extends PolicyDecisionPointGrpc.PolicyDecisionPointImplBase {

    private final EvaluateRequestUseCase evaluateUseCase;

    public PdpGrpcService(EvaluateRequestUseCase evaluateUseCase) {
        this.evaluateUseCase = evaluateUseCase;
    }

    @Override
    public void evaluateCompliance(
            EvaluateRequest request,
            StreamObserver<EvaluateResponse> responseObserver) {
        try {
            EvaluateCommand command = new EvaluateCommand(
                    request.getSubjectId(),
                    request.getResourceId(),
                    request.getActionId(),
                    request.getSourceCountry(),
                    request.getTargetCountry(),
                    request.getPolicyName()
            );

            EvaluateRequestUseCase.EvaluateResponse res = evaluateUseCase.evaluate(command);

            EvaluateResponse response = EvaluateResponse.newBuilder()
                    .setEffect(res.effect())
                    .setProofTrace(res.proofTrace())
                    .setValidationLog(res.validationLog())
                    .setSolvedAt(res.solvedAt())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to process compliance evaluation: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
