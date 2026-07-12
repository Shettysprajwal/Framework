package com.pqvcf.pip.infrastructure.grpc;

import com.pqvcf.pip.domain.resolver.AdequacyPathResolver;
import com.pqvcf.regulation.grpc.AdequacyRequest;
import com.pqvcf.regulation.grpc.AdequacyResponse;
import com.pqvcf.regulation.grpc.RegulationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class GrpcAdequacyPathResolver implements AdequacyPathResolver {

    private static final Logger log = LoggerFactory.getLogger(GrpcAdequacyPathResolver.class);

    @Value("${pqvcf.regulation.grpc.host:localhost}")
    private String grpcHost;

    @Value("${pqvcf.regulation.grpc.port:9091}")
    private int grpcPort;

    // Local fallback configurations if Module 1 gRPC is offline
    private static final Map<String, Map<String, Boolean>> FALLBACK_ADEQUACY = Map.of(
            "IN", Map.of("EU", true, "US", true),
            "US", Map.of("EU", true, "IN", false),
            "EU", Map.of("US", true, "IN", false)
    );

    @Override
    public boolean isAdequate(String sourceCountry, String targetCountry) {
        if (sourceCountry == null || targetCountry == null) return false;
        
        String src = sourceCountry.toUpperCase().trim();
        String tgt = targetCountry.toUpperCase().trim();

        if (src.equals(tgt)) return true;

        log.info("Resolving adequacy pathway transit from {} to {} via Module 1 gRPC: {}:{}", src, tgt, grpcHost, grpcPort);
        
        ManagedChannel channel = null;
        try {
            channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
                    .usePlaintext()
                    .build();

            RegulationServiceGrpc.RegulationServiceBlockingStub stub = RegulationServiceGrpc.newBlockingStub(channel);
            
            AdequacyRequest request = AdequacyRequest.newBuilder()
                    .setSourceJurisdiction(src)
                    .setTargetJurisdiction(tgt)
                    .build();

            AdequacyResponse response = stub.checkJurisdictionAdequacy(request);
            
            log.info("gRPC response resolved adequacy: {}", response.getIsAdequate());
            return response.getIsAdequate();
            
        } catch (Exception e) {
            log.warn("Failed to reach Module 1 gRPC service ({}). Executing static adequacy lookup fallback.", e.getMessage());
        } finally {
            if (channel != null) {
                try {
                    channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return resolveFallback(src, tgt);
    }

    private boolean resolveFallback(String src, String tgt) {
        log.info("Executing static fallback lookup for {} -> {}", src, tgt);
        return FALLBACK_ADEQUACY.getOrDefault(src, Map.of()).getOrDefault(tgt, false);
    }
}
