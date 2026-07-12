package com.pqvcf.pap.application.dto;

import com.pqvcf.pap.application.port.in.CreatePolicyUseCase.PolicyResponse;
import com.pqvcf.pap.application.port.in.CreatePolicyUseCase.RuleLinkResponse;
import com.pqvcf.pap.domain.model.Policy;
import com.pqvcf.pap.domain.model.RuleLink;

import java.util.List;
import java.util.stream.Collectors;

public final class DtoMapper {

    private DtoMapper() {}

    public static RuleLinkResponse toResponse(RuleLink link) {
        if (link == null) return null;
        return new RuleLinkResponse(
                link.getId().toString(),
                link.getPolicyId().toString(),
                link.getOrganizationalRuleName(),
                link.getRegulatoryRuleId().toString(),
                link.getDescription()
        );
    }

    public static PolicyResponse toResponse(Policy policy) {
        if (policy == null) return null;
        List<RuleLinkResponse> links = policy.getRuleLinks().stream()
                .map(DtoMapper::toResponse)
                .collect(Collectors.toList());

        return new PolicyResponse(
                policy.getId().toString(),
                policy.getName(),
                policy.getOwner(),
                policy.getDescription(),
                policy.getStatus().name(),
                links,
                policy.getCreatedAt(),
                policy.getUpdatedAt()
        );
    }
}
