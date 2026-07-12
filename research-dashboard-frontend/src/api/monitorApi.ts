import { EventDto, ViolationDto, SlaMetricsDto, IngestEventCommand } from '../types/monitor';

const API_BASE = '/api/v1/monitor';

// Pre-seeded compliance monitor data
const INITIAL_EVENTS: EventDto[] = [
  { id: 'ev-001', source: 'DE', destination: 'IN', dataCategory: 'PERSONAL', sizeBytes: 1024, timestamp: new Date(Date.now() - 60000).toISOString() },
  { id: 'ev-002', source: 'FR', destination: 'JP', dataCategory: 'PERSONAL', sizeBytes: 4096, timestamp: new Date(Date.now() - 30000).toISOString() }
];

const INITIAL_VIOLATIONS: ViolationDto[] = [];
const INITIAL_METRICS: SlaMetricsDto = { totalEvents: 2, violationCount: 0, complianceRate: 100.0 };

const getLocalEvents = (): EventDto[] => {
  const data = localStorage.getItem('pqvcf_monitor_events');
  if (!data) {
    localStorage.setItem('pqvcf_monitor_events', JSON.stringify(INITIAL_EVENTS));
    return INITIAL_EVENTS;
  }
  return JSON.parse(data);
};

const setLocalEvents = (evs: EventDto[]) => {
  localStorage.setItem('pqvcf_monitor_events', JSON.stringify(evs));
};

const getLocalViolations = (): ViolationDto[] => {
  const data = localStorage.getItem('pqvcf_monitor_violations');
  if (!data) {
    localStorage.setItem('pqvcf_monitor_violations', JSON.stringify(INITIAL_VIOLATIONS));
    return INITIAL_VIOLATIONS;
  }
  return JSON.parse(data);
};

const setLocalViolations = (vils: ViolationDto[]) => {
  localStorage.setItem('pqvcf_monitor_violations', JSON.stringify(vils));
};

const getLocalMetrics = (): SlaMetricsDto => {
  const data = localStorage.getItem('pqvcf_monitor_metrics');
  if (!data) {
    localStorage.setItem('pqvcf_monitor_metrics', JSON.stringify(INITIAL_METRICS));
    return INITIAL_METRICS;
  }
  return JSON.parse(data);
};

const setLocalMetrics = (m: SlaMetricsDto) => {
  localStorage.setItem('pqvcf_monitor_metrics', JSON.stringify(m));
};

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw new Error(`API error: ${response.status}`);
  }
  return response.json();
}

export const monitorApi = {
  async ingest(command: IngestEventCommand): Promise<void> {
    try {
      const response = await fetch(`${API_BASE}/ingest`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(command),
      });
      if (!response.ok) {
        throw new Error(`API error: ${response.status}`);
      }
    } catch (e) {
      console.warn('Backend monitor down, simulating stream packet ingest locally');
      const events = getLocalEvents();
      const violations = getLocalViolations();

      const newEv: EventDto = {
        id: `mock-ev-uuid-${Date.now()}`,
        source: command.source,
        destination: command.destination,
        dataCategory: command.dataCategory,
        sizeBytes: command.sizeBytes,
        timestamp: new Date().toISOString()
      };
      events.push(newEv);

      // Check validation logic
      let hasViolation = false;
      let rule = '';
      let msg = '';
      let severity: 'WARNING' | 'CRITICAL' = 'WARNING';

      if (command.source === 'RU' || command.destination === 'RU') {
        hasViolation = true;
        rule = 'Russia Data Localization Residency Rule';
        msg = `Packet data category ${command.dataCategory} transit from RU violates database localization constraints.`;
        severity = 'CRITICAL';
      } else if (command.source === 'CN' || command.destination === 'CN') {
        hasViolation = true;
        rule = 'China PIPL Critical Export constraints';
        msg = `Packet containing ${command.dataCategory} details crossed Chinese sovereign critical data borders without state filing.`;
        severity = 'CRITICAL';
      } else if (command.dataCategory === 'HEALTH' && (command.source === 'US' || command.destination === 'US')) {
        hasViolation = true;
        rule = 'HIPAA PHI Data Protection Warning';
        msg = `Medical PHI records transfer safeguards requires explicit Business Associate Contract validation checks.`;
        severity = 'WARNING';
      }

      if (hasViolation) {
        violations.push({
          violationId: `mock-vil-uuid-${Date.now()}`,
          eventId: newEv.id,
          violatedRule: rule,
          severity,
          description: msg,
          raisedAt: new Date().toISOString()
        });
      }

      // Re-evaluate SLA metrics
      const totalEvents = events.length;
      const violationCount = violations.filter(v => v.severity === 'CRITICAL').length;
      const complianceRate = totalEvents > 0 ? ((totalEvents - violationCount) / totalEvents) * 100 : 100.0;

      setLocalEvents(events);
      setLocalViolations(violations);
      setLocalMetrics({ totalEvents, violationCount, complianceRate });
    }
  },

  async listEvents(): Promise<EventDto[]> {
    try {
      const response = await fetch(`${API_BASE}/events`);
      return await handleResponse<EventDto[]>(response);
    } catch (e) {
      return getLocalEvents();
    }
  },

  async listViolations(): Promise<ViolationDto[]> {
    try {
      const response = await fetch(`${API_BASE}/violations`);
      return await handleResponse<ViolationDto[]>(response);
    } catch (e) {
      return getLocalViolations();
    }
  },

  async getMetrics(): Promise<SlaMetricsDto> {
    try {
      const response = await fetch(`${API_BASE}/metrics`);
      return await handleResponse<SlaMetricsDto>(response);
    } catch (e) {
      return getLocalMetrics();
    }
  },

  async reset(): Promise<void> {
    try {
      const response = await fetch(`${API_BASE}/reset`, {
        method: 'POST',
      });
      if (!response.ok) {
        throw new Error(`API error: ${response.status}`);
      }
    } catch (e) {
      localStorage.removeItem('pqvcf_monitor_events');
      localStorage.removeItem('pqvcf_monitor_violations');
      localStorage.removeItem('pqvcf_monitor_metrics');
    }
  }
};

