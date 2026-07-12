export interface IngestEventCommand {
  source: string;
  destination: string;
  dataCategory: string;
  sizeBytes: number;
}

export interface EventDto {
  id: string;
  source: string;
  destination: string;
  dataCategory: string;
  sizeBytes: number;
  timestamp: string;
}

export interface ViolationDto {
  violationId: string;
  eventId: string;
  severity: 'INFO' | 'WARNING' | 'CRITICAL';
  violatedRule: string;
  description: string;
  raisedAt: string;
}

export interface SlaMetricsDto {
  totalEvents: number;
  violationCount: number;
  complianceRate: number;
}
