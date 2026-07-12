export interface EvaluateCommand {
  subjectId: string;
  resourceId: string;
  actionId: string;
  sourceCountry: string;
  targetCountry: string;
  policyName?: string;
}

export interface EvaluateResponse {
  effect: 'PERMIT' | 'DENY' | 'INDETERMINATE';
  proofTrace: string;
  validationLog: string;
  solvedAt: string;
}

export interface AuditLogResponse {
  id: string;
  subjectId: string;
  resourceId: string;
  actionId: string;
  sourceCountry: string;
  targetCountry: string;
  policyName: string;
  effect: 'PERMIT' | 'DENY' | 'INDETERMINATE';
  proofTrace: string;
  validationLog: string;
  solvedAt: string;
}
