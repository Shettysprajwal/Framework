package com.pqvcf.monitor.api.grpc;

import com.pqvcf.monitor.application.port.in.IngestEventUseCase;
import com.pqvcf.monitor.grpc.ComplianceMonitorGrpc;
import com.pqvcf.monitor.grpc.EventRequest;
import com.pqvcf.monitor.grpc.IngestResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class MonitorGrpcService extends ComplianceMonitorGrpc.ComplianceMonitorImplBase {

    private final IngestEventUseCase ingestUseCase;

    public MonitorGrpcService(IngestEventUseCase ingestUseCase) {
        this.ingestUseCase = ingestUseCase;
    }

    @Override
    public void ingestSimulatedEvent(
            EventRequest request,
            StreamObserver<IngestResponse> responseObserver) {
        try {
            IngestEventUseCase.IngestEventCommand command = new IngestEventUseCase.IngestEventCommand(
                    request.getSource(),
                    request.getDestination(),
                    request.getDataCategory(),
                    request.getSizeBytes()
            );

            ingestUseCase.ingest(command);

            IngestResponse response = IngestResponse.newBuilder()
                    .setAccepted(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to process event ingestion: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
