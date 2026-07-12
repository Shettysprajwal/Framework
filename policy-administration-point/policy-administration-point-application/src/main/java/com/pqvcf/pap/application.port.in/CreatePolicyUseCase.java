package com.pqvcf.pap.application.port.in;

import java.time.Instant;
import java.util.List;

public interface CreatePolicyUseCase {

    PolicyResponse create(CreatePolicyCommand command);

    record CreatePolicyCommand(
            String name,
            String owner,
            String description
    ) {}

    record RuleLinkResponse(
            String id,
            String policyId,
            String organizationalRuleName,
            String regulatoryRuleId,
            String description
    ) {}

    record PolicyResponse(
            String id,
            String name,
            String owner,
            String description,
            String status,
            List<RuleLinkResponse> ruleLinks,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
