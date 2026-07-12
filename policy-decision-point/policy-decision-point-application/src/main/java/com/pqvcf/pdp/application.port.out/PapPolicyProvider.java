package com.pqvcf.pdp.application.port.out;

import java.util.List;

public interface PapPolicyProvider {
    
    List<ActivePolicyDto> fetchActivePolicies();

    record ActivePolicyDto(
            String id,
            String name,
            String owner,
            List<ActiveRuleLinkDto> ruleLinks
    ) {}

    record ActiveRuleLinkDto(
            String id,
            String organizationalRuleName,
            String regulatoryRuleId
    ) {}
}
