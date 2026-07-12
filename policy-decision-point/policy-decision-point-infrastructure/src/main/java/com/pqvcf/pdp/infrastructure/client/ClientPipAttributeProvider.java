package com.pqvcf.pdp.infrastructure.client;

import com.pqvcf.pdp.application.port.out.PipAttributeProvider;
import com.pqvcf.pip.grpc.AttributeResolveRequest;
import com.pqvcf.pip.grpc.AttributeResolveResponse;
import com.pqvcf.pip.grpc.PolicyInformationPointGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ClientPipAttributeProvider implements PipAttributeProvider {

    private static final Logger log = LoggerFactory.getLogger(ClientPipAttributeProvider.class);

    @Value("${pqvcf.pip.grpc.host:localhost}")
    private String pipHost;

    @Value("${pqvcf.pip.grpc.port:9094}")
    private int pipPort;

    @Override
    public ResolvedContextDto resolveContext(
            String subjectId,
            String resourceId,
            String actionId,
            String sourceCountry,
            String targetCountry) {

        log.info("Querying PIP context resolution gRPC: {}:{}", pipHost, pipPort);

        ManagedChannel channel = null;
        try {
            channel = ManagedChannelBuilder.forAddress(pipHost, pipPort)
                    .usePlaintext()
                    .build();

            PolicyInformationPointGrpc.PolicyInformationPointBlockingStub stub =
                    PolicyInformationPointGrpc.newBlockingStub(channel);

            AttributeResolveRequest request = AttributeResolveRequest.newBuilder()
                    .setSubjectId(subjectId)
                    .setResourceId(resourceId)
                    .setActionId(actionId)
                    .setSourceCountry(sourceCountry != null ? sourceCountry : "")
                    .setTargetCountry(targetCountry != null ? targetCountry : "")
                    .build();

            AttributeResolveResponse response = stub.resolveRequestContext(request);

            List<AttributeDto> attributeDtos = response.getAttributesList().stream()
                    .map(a -> new AttributeDto(
                            a.getCategory(),
                            a.getKey(),
                            a.getValue(),
                            a.getDataType()
                    )).collect(Collectors.toList());

            return new ResolvedContextDto(
                    response.getSubjectId(),
                    response.getResourceId(),
                    response.getActionId(),
                    attributeDtos,
                    response.getIsTransitiveAdequate()
            );

        } catch (Exception e) {
            log.warn("Failed to contact PIP gRPC service ({}). Executing mock attribute resolution fallback.", e.getMessage());
        } finally {
            if (channel != null) {
                try {
                    channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return resolveFallback(subjectId, resourceId, actionId, sourceCountry, targetCountry);
    }

    private ResolvedContextDto resolveFallback(
            String sub, String res, String act, String src, String tgt) {
        log.info("Resolving static PIP fallback attributes for: {}", sub);

        // Standard mock attributes
        List<AttributeDto> attributes = List.of(
                new AttributeDto("SUBJECT", "role", "analyst", "String"),
                new AttributeDto("SUBJECT", "clearance", "internal", "String"),
                new AttributeDto("SUBJECT", "mfa_enabled", "true", "Boolean"),
                new AttributeDto("RESOURCE", "classification", "personal", "String"),
                new AttributeDto("RESOURCE", "category", "privacy", "String")
        );

        // Fallback transit path check
        boolean transitiveAdequate = src != null && tgt != null &&
                src.equalsIgnoreCase("IN") && tgt.equalsIgnoreCase("EU");

        return new ResolvedContextDto(sub, res, act, attributes, transitiveAdequate);
    }
}
