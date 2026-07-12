export interface GenerateProofCommand {
  proofType: 'DATA_RESIDENCY' | 'TRANSFER_BASIS' | 'PURPOSE_LIMITATION';
  secretWitnessValue: number;
  publicInputsJson: string;
}

export interface ProofResponse {
  proofId: string;
  proofType: string;
  commitmentHex: string;
  challengeHex: string;
  responseHex: string;
  publicInputsJson: string;
  verified: boolean;
}

export interface VerifyProofCommand {
  proofId: string;
  challengeHex: string;
  responseHex: string;
  commitmentHex: string;
}
