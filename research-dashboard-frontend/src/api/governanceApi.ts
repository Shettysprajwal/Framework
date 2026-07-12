import { FlowDecisionResponse, EvaluateFlowCommand } from '../types/governance';

const API_BASE = '/api/v1/governance';

// Pre-seeded decisions for initial view
const INITIAL_DECISIONS: FlowDecisionResponse[] = [
  {
    decisionId: 'mock-dec-001',
    sourceCountry: 'DE',
    targetCountry: 'IN',
    dataCategory: 'PERSONAL',
    processingPurpose: 'PROCESSING',
    decision: 'APPROVED',
    reasoning: 'EU (Germany) to India transfer is approved because India provides adequacy standards (GDPR Art. 45 mapping).',
    citations: ['GDPR Art. 45', 'DPDP Sec. 16'],
    evidenceLink: 'https://gdpr-info.eu/art-45/',
    createdAt: new Date().toISOString()
  },
  {
    decisionId: 'mock-dec-002',
    sourceCountry: 'RU',
    targetCountry: 'DE',
    dataCategory: 'PERSONAL',
    processingPurpose: 'BACKUP',
    decision: 'BLOCKED',
    reasoning: 'Blocked due to Russian Federal Law on Personal Data (FFDL / Art 18.5) requiring primary database residency in Russia.',
    citations: ['Russia FFDL Art. 18.5'],
    evidenceLink: 'https://pd.rkn.gov.ru/',
    createdAt: new Date().toISOString()
  }
];

const getLocalDecisions = (): FlowDecisionResponse[] => {
  const data = localStorage.getItem('pqvcf_decisions');
  if (!data) {
    localStorage.setItem('pqvcf_decisions', JSON.stringify(INITIAL_DECISIONS));
    return INITIAL_DECISIONS;
  }
  return JSON.parse(data);
};

const setLocalDecisions = (decs: FlowDecisionResponse[]) => {
  localStorage.setItem('pqvcf_decisions', JSON.stringify(decs));
};

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw new Error(`API error: ${response.status}`);
  }
  return response.json();
}

export const governanceApi = {
  async listDecisions(): Promise<FlowDecisionResponse[]> {
    try {
      const response = await fetch(`${API_BASE}/decisions`);
      return await handleResponse<FlowDecisionResponse[]>(response);
    } catch (e) {
      console.warn('Backend governance down, fallback to mock decisions list');
      return getLocalDecisions();
    }
  },

  async evaluateFlow(command: EvaluateFlowCommand): Promise<FlowDecisionResponse> {
    try {
      const response = await fetch(`${API_BASE}/evaluate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(command),
      });
      return await handleResponse<FlowDecisionResponse>(response);
    } catch (e) {
      console.warn('Backend governance down, executing mock transfer logic');
      const decs = getLocalDecisions();
      
      let decision: 'APPROVED' | 'BLOCKED' | 'CONDITIONAL' = 'APPROVED';
      let reasoning = `Transfer from ${command.sourceCountry} to ${command.targetCountry} evaluated successfully. No localization overrides found.`;
      let citations = ['GDPR Art. 46'];
      
      if (command.sourceCountry === 'RU') {
        decision = 'BLOCKED';
        reasoning = `Transfer blocked. Russian Federal Law mandates primary residency of personal database records within Russian territory (FFDL Art 18.5).`;
        citations = ['Russia FFDL Art 18.5'];
      } else if (command.sourceCountry === 'CN') {
        decision = 'BLOCKED';
        reasoning = `Transfer blocked. Chinese PIPL and critical data regulations require security assessments for personal data export.`;
        citations = ['China PIPL Article 38'];
      } else if (command.sourceCountry === 'US' && command.dataCategory === 'HEALTH') {
        decision = 'CONDITIONAL';
        reasoning = `Transfer conditional. US HIPAA Security rules require execution of Business Associate Agreement (BAA) and Standard Contractual Clauses (SCCs).`;
        citations = ['US HIPAA §164.312', 'GDPR Art 46'];
      } else if (command.sourceCountry === 'DE' && command.targetCountry === 'IN') {
        decision = 'APPROVED';
        reasoning = `Approved. EU adequacy whitelist pathways permit data transfers to India (GPDR Art 45 matched with India DPDP framework).`;
        citations = ['GDPR Art. 45', 'India DPDP Sec 16'];
      }

      const newDec: FlowDecisionResponse = {
        decisionId: `mock-dec-uuid-${Date.now()}`,
        sourceCountry: command.sourceCountry,
        targetCountry: command.targetCountry,
        dataCategory: command.dataCategory,
        processingPurpose: command.processingPurpose,
        decision,
        reasoning,
        citations,
        evidenceLink: 'https://gdpr-info.eu/chapter-5/',
        createdAt: new Date().toISOString()
      };

      decs.push(newDec);
      setLocalDecisions(decs);
      return newDec;
    }
  },

  async deleteDecision(id: string): Promise<void> {
    try {
      const response = await fetch(`${API_BASE}/decisions/${id}`, {
        method: 'DELETE',
      });
      if (!response.ok) {
        throw new Error(`API error: ${response.status}`);
      }
    } catch (e) {
      const decs = getLocalDecisions();
      const filtered = decs.filter(d => d.decisionId !== id);
      setLocalDecisions(filtered);
    }
  }
};

