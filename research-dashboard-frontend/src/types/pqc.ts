export interface GenerateKeyCommand {
  algorithm: 'ML_KEM_768' | 'ML_DSA_65' | 'SLH_DSA_256';
  alias: string;
}

export interface KeyResponse {
  keyId: string;
  algorithm: string;
  publicKeyHex: string;
  createdAt: string;
  expiresAt: string;
}

export interface SignCommand {
  keyId: string;
  payloadHex: string;
}

export interface SignResponse {
  signatureHex: string;
  algorithm: string;
  keyId: string;
  length: number;
}

export interface VerifyCommand {
  keyId: string;
  payloadHex: string;
  signatureHex: string;
}
