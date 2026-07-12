package com.pqvcf.pdp.infrastructure.client;

import com.pqvcf.pap.grpc.ActiveRequest;
import com.pqvcf.pap.grpc.PoliciesListResponse;
import com.pqvcf.pap.grpc.PolicyAdministrationServiceGrpc;
import com.pqvcf.pdp.application.port.out.PapPolicyProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ClientPapPolicyProvider implements PapPolicyProvider {

    private static final Logger log = LoggerFactory.getLogger(ClientPapPolicyProvider.class);

    @Value("${pqvcf.pap.grpc.host:localhost}")
    private String papHost;

    @Value("${pqvcf.pap.grpc.port:9093}")
    private int papPort;

    @Override
    public List<ActivePolicyDto> fetchActivePolicies() {
        log.info("Fetching active policies from PAP gRPC: {}:{}", papHost, papPort);
        
        ManagedChannel channel = null;
        try {
            channel = ManagedChannelBuilder.forAddress(papHost, papPort)
                    .usePlaintext()
                    .build();

            PolicyAdministrationServiceGrpc.PolicyAdministrationServiceBlockingStub stub =
                    PolicyAdministrationServiceGrpc.newBlockingStub(channel);

            PoliciesListResponse response = stub.getActivePolicies(ActiveRequest.newBuilder().build());

            return response.getPoliciesList().stream()
                    .map(p -> {
                        List<ActiveRuleLinkDto> links = p.getRuleLinksList().stream()
                                .map(l -> new ActiveRuleLinkDto(
                                        l.getId(),
                                        l.getOrganizationalRuleName(),
                                        l.getRegulatoryRuleId()
                                )).collect(Collectors.toList());

                        return new ActivePolicyDto(
                                p.getId(),
                                p.getName(),
                                p.getOwner(),
                                links
                        );
                    }).collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Failed to contact PAP gRPC microservice ({}). Triggering static policy fallback.", e.getMessage());
        } finally {
            if (channel != null) {
                try {
                    channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return resolveFallback();
    }

    private List<ActivePolicyDto> resolveFallback() {
        log.info("Resolving static active compliance policy fallback...");
        // Mock a standard active corporate transfer policy binding to a translated rule
        List<ActiveRuleLinkDto> links = List.of(
                new ActiveRuleLinkDto(
                        "99999999-9999-9999-9999-999999999999",
                        "Cross-border Safety Binding Link",
                        "88888888-8888-8888-8888-888888888888" // Binds to translated rule ID
                )
        );

        return List.of(
                new ActivePolicyDto(
                        "11111111-1111-1111-1111-111111111111",
                        "Global Privacy Transfer Policy",
                        "Legal Operations",
                        links
                )
        );
    }
}
