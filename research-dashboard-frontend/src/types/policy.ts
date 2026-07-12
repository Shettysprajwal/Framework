export interface CreatePolicyCommand {
  name: string;
  owner: string;
  description: string;
}

export interface LinkRuleCommand {
  policyId: string;
  organizationalRuleName: string;
  regulatoryRuleId: string;
  description: string;
}

export interface RuleLinkResponse {
  id: string;
  policyId: string;
  organizationalRuleName: string;
  regulatoryRuleId: string;
  description: string;
}

export interface PolicyResponse {
  id: string;
  name: string;
  owner: string;
  description?: string;
  status: 'DRAFT' | 'ACTIVE' | 'DEPRECATED';
  ruleLinks: RuleLinkResponse[];
  createdAt: string;
  updatedAt: string;
}
export type PolicyStatus = 'DRAFT' | 'ACTIVE' | 'DEPRECATED';
