export interface EvaluateFlowCommand {
  sourceCountry: string;
  targetCountry: string;
  dataCategory: string;
  processingPurpose: string;
}

export interface FlowDecisionResponse {
  decisionId: string;
  sourceCountry: string;
  targetCountry: string;
  dataCategory: string;
  processingPurpose: string;
  decision: 'APPROVED' | 'BLOCKED' | 'CONDITIONAL';
  reasoning: string;
  citations: string[];
  evidenceLink: string;
  createdAt: string;
}
