package com.pqvcf.pap.application.port.in;

import com.pqvcf.pap.application.port.in.CreatePolicyUseCase.PolicyResponse;

public interface LinkRuleUseCase {
    
    PolicyResponse linkRule(LinkRuleCommand command);
    PolicyResponse unlinkRule(String policyId, String ruleLinkId);

    record LinkRuleCommand(
            String policyId,
            String organizationalRuleName,
            String regulatoryRuleId,
            String description
    ) {}
}
