package com.pqvcf.pip.api.grpc;

import com.pqvcf.pip.application.port.in.ResolveAttributesUseCase;
import com.pqvcf.pip.application.port.in.ResolveAttributesUseCase.ResolveAttributesQuery;
import com.pqvcf.pip.application.port.in.ResolveAttributesUseCase.ResolvedContextResponse;
import com.pqvcf.pip.grpc.AttributeDetail;
import com.pqvcf.pip.grpc.AttributeResolveRequest;
import com.pqvcf.pip.grpc.AttributeResolveResponse;
import com.pqvcf.pip.grpc.PolicyInformationPointGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PipGrpcService extends PolicyInformationPointGrpc.PolicyInformationPointImplBase {

    private final ResolveAttributesUseCase resolveUseCase;

    public PipGrpcService(ResolveAttributesUseCase resolveUseCase) {
        this.resolveUseCase = resolveUseCase;
    }

    @Override
    public void resolveRequestContext(
            AttributeResolveRequest request,
            StreamObserver<AttributeResolveResponse> responseObserver) {
        try {
            ResolveAttributesQuery query = new ResolveAttributesQuery(
                    request.getSubjectId(),
                    request.getResourceId(),
                    request.getActionId(),
                    request.getSourceCountry(),
                    request.getTargetCountry()
            );

            ResolvedContextResponse res = resolveUseCase.resolve(query);

            List<AttributeDetail> attributeDetails = res.attributes().stream()
                    .map(attr -> AttributeDetail.newBuilder()
                            .setCategory(attr.category())
                            .setKey(attr.key())
                            .setValue(attr.value())
                            .setDataType(attr.dataType())
                            .build()
                    ).collect(Collectors.toList());

            AttributeResolveResponse response = AttributeResolveResponse.newBuilder()
                    .setSubjectId(res.subjectId())
                    .setResourceId(res.resourceId())
                    .setActionId(res.actionId())
                    .addAllAttributes(attributeDetails)
                    .setIsTransitiveAdequate(res.isTransitiveAdequate())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to resolve request attributes: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
