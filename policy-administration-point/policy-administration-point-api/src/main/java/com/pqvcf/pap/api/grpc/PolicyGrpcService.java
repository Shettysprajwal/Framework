package com.pqvcf.pap.api.grpc;

import com.pqvcf.pap.application.port.in.CreatePolicyUseCase;
import com.pqvcf.pap.application.port.in.CreatePolicyUseCase.PolicyResponse;
import com.pqvcf.pap.application.port.in.CreatePolicyUseCase.RuleLinkResponse;
import com.pqvcf.pap.application.port.in.GetPolicyUseCase;
import com.pqvcf.pap.grpc.ActiveRequest;
import com.pqvcf.pap.grpc.PoliciesListResponse;
import com.pqvcf.pap.grpc.PolicyAdministrationServiceGrpc;
import com.pqvcf.pap.grpc.PolicyDetailResponse;
import com.pqvcf.pap.grpc.PolicyRequest;
import com.pqvcf.pap.grpc.RuleLinkDetail;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PolicyGrpcService extends PolicyAdministrationServiceGrpc.PolicyAdministrationServiceImplBase {

    private final CreatePolicyUseCase createUseCase;
    private final GetPolicyUseCase getUseCase;

    public PolicyGrpcService(
            CreatePolicyUseCase createUseCase,
            GetPolicyUseCase getUseCase) {
        this.createUseCase = createUseCase;
        this.getUseCase = getUseCase;
    }

    @Override
    public void createCompliancePolicy(PolicyRequest request, StreamObserver<PolicyDetailResponse> responseObserver) {
        try {
            CreatePolicyUseCase.CreatePolicyCommand command = new CreatePolicyUseCase.CreatePolicyCommand(
                    request.getName(),
                    request.getOwner(),
                    request.getDescription()
            );

            PolicyResponse res = createUseCase.create(command);
            PolicyDetailResponse response = mapToDetail(res);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to create compliance policy: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getActivePolicies(ActiveRequest request, StreamObserver<PoliciesListResponse> responseObserver) {
        try {
            List<PolicyResponse> active = getUseCase.listAll().stream()
                    .filter(p -> "ACTIVE".equalsIgnoreCase(p.status()))
                    .collect(Collectors.toList());

            List<PolicyDetailResponse> details = active.stream()
                    .map(this::mapToDetail)
                    .collect(Collectors.toList());

            PoliciesListResponse response = PoliciesListResponse.newBuilder()
                    .addAllPolicies(details)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to retrieve active policies: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private PolicyDetailResponse mapToDetail(PolicyResponse res) {
        List<RuleLinkDetail> linkDetails = res.ruleLinks().stream()
                .map(link -> RuleLinkDetail.newBuilder()
                        .setId(link.id())
                        .setPolicyId(link.policyId())
                        .setOrganizationalRuleName(link.organizationalRuleName())
                        .setRegulatoryRuleId(link.regulatoryRuleId())
                        .setDescription(link.description() != null ? link.description() : "")
                        .build()
                ).collect(Collectors.toList());

        return PolicyDetailResponse.newBuilder()
                .setId(res.id())
                .setName(res.name())
                .setOwner(res.owner())
                .setDescription(res.description() != null ? res.description() : "")
                .setStatus(res.status())
                .addAllRuleLinks(linkDetails)
                .build();
    }
}
