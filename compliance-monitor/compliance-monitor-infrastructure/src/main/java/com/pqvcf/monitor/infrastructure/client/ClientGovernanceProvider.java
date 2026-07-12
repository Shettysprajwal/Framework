package com.pqvcf.monitor.infrastructure.client;

import com.pqvcf.governance.grpc.DataGovernanceGrpc;
import com.pqvcf.governance.grpc.TransferRequest;
import com.pqvcf.governance.grpc.TransferResponse;
import com.pqvcf.monitor.application.port.out.GovernanceClientProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ClientGovernanceProvider implements GovernanceClientProvider {

    private static final Logger log = LoggerFactory.getLogger(ClientGovernanceProvider.class);

    @Value("${pqvcf.governance.grpc.host:localhost}")
    private String govHost;

    @Value("${pqvcf.governance.grpc.port:9098}")
    private int govPort;

    @Override
    public boolean verifyTransferLegality(String source, String destination, String dataCategory) {
        log.info("Contacting Governance Engine gRPC: {}:{}", govHost, govPort);

        ManagedChannel channel = null;
        try {
            channel = ManagedChannelBuilder.forAddress(govHost, govPort)
                    .usePlaintext()
                    .build();

            DataGovernanceGrpc.DataGovernanceBlockingStub stub =
                    DataGovernanceGrpc.newBlockingStub(channel);

            TransferRequest request = TransferRequest.newBuilder()
                    .setSourceCountry(source)
                    .setTargetCountry(destination)
                    .setDataCategory(dataCategory)
                    .setProcessingPurpose("MONITOR")
                    .build();

            TransferResponse response = stub.evaluateTransfer(request);
            log.info("Governance response decision verdict: {}", response.getDecision());

            return !"BLOCKED".equalsIgnoreCase(response.getDecision());

        } catch (Exception e) {
            log.warn("Failed to contact Governance microservice ({}). Executing static fallback whitelists.", e.getMessage());
        } finally {
            if (channel != null) {
                try {
                    channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return resolveFallback(source, destination, dataCategory);
    }

    private boolean resolveFallback(String src, String dst, String cat) {
        log.info("Resolving static governance checks fallback rules...");
        
        // Block transfers from RU (Russia) or CN (China) personal/financial data (strict residency rules)
        if ("RU".equalsIgnoreCase(src) && ("PERSONAL".equalsIgnoreCase(cat) || "HEALTH".equalsIgnoreCase(cat))) {
            return false; // Block
        }
        if ("CN".equalsIgnoreCase(src) && ("PERSONAL".equalsIgnoreCase(cat) || "CRITICAL".equalsIgnoreCase(cat))) {
            return false; // Block
        }

        return true; // Approve others conditionally
    }
}
