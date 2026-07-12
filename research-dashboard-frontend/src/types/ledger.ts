export interface SealRecordCommand {
  action: string;
  actor: string;
  target: string;
  decision: string;
}

export interface RecordDto {
  id: string;
  timestamp: string;
  action: string;
  actor: string;
  target: string;
  decision: string;
  previousHash: string;
  currentHash: string;
}

export interface VerificationDto {
  valid: boolean;
  tamperedIndex: number;
  details: string;
}
