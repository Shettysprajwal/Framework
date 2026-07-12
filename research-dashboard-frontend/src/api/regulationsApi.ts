import { RegulationResponse, ArticleResponse, GraphData } from '../types/regulation';

export interface RegisterCommand {
  name: string;
  shortName: string;
  jurisdiction: string;
  version: string;
  description: string;
}

export interface AddArticleCommand {
  regulationId: string;
  articleNumber: string;
  title: string;
  content: string;
  deonticFormula?: string;
  odrlPolicy?: string;
  clauses?: Array<{
    clauseNumber: string;
    content: string;
    clauseType: string;
  }>;
}

const API_BASE = '/api/v1';

// Seed Initial Data
const SEEDED_REGS: RegulationResponse[] = [
  {
    id: 'gdpr-uuid-001',
    name: 'General Data Protection Regulation',
    shortName: 'GDPR',
    primaryJurisdiction: 'EU',
    version: '2016/679',
    description: 'EU framework regulating processing of personal data and cross-border transfers.',
    status: 'ACTIVE',
    formalSpec: 'assert GDPR_Art_44_Compliance',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    articles: [
      {
        id: 'gdpr-art-44',
        regulationId: 'gdpr-uuid-001',
        articleNumber: 'Art. 44',
        title: 'General principle for transfers',
        content: 'Any transfer of personal data to a third country shall take place only if the conditions in this Chapter are complied with.',
        deonticFormula: 'Obligation(Subject: Controller, Action: Transfer, Target: ThirdCountry, Constraint: ChapterV_Compliant)',
        odrlPolicy: '{"@context":"http://www.w3.org/ns/odrl/2/","@type":"Agreement","permission":[{"action":"transfer","constraint":[{"leftOperand":"spatial","operator":"eq","rightOperand":"adequacy"}]}]}',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        clauses: [
          { id: 'gdpr-art-44-c1', articleId: 'gdpr-art-44', clauseNumber: '44.1', content: 'Apply all rules in Chapter V.', clauseType: 'OBLIGATION' }
        ]
      },
      {
        id: 'gdpr-art-45',
        regulationId: 'gdpr-uuid-001',
        articleNumber: 'Art. 45',
        title: 'Transfers on the basis of an adequacy decision',
        content: 'A transfer of personal data to a third country may take place where the Commission has decided that the third country ensures an adequate level of protection.',
        deonticFormula: 'Permission(Subject: Controller, Action: Transfer, Target: AdequateCountry)',
        odrlPolicy: '{"@context":"http://www.w3.org/ns/odrl/2/","permission":[{"action":"transfer"}]}',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        clauses: [
          { id: 'gdpr-art-45-c1', articleId: 'gdpr-art-45', clauseNumber: '45.1', content: 'Transfer to whitelisted adequate countries permitted.', clauseType: 'PERMISSION' }
        ]
      }
    ]
  },
  {
    id: 'dpdp-uuid-002',
    name: 'Digital Personal Data Protection Act',
    shortName: 'DPDP',
    primaryJurisdiction: 'IN',
    version: '2023',
    description: 'India regulatory framework for digital personal data processing.',
    status: 'ACTIVE',
    formalSpec: 'assert DPDP_Section_16_Compliance',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    articles: [
      {
        id: 'dpdp-sec-16',
        regulationId: 'dpdp-uuid-002',
        articleNumber: 'Sec. 16',
        title: 'Transfer of personal data outside India',
        content: 'The Central Government may, by notification, restrict the transfer of personal data by a significant data fiduciary to such country outside India.',
        deonticFormula: 'Prohibition(Subject: Fiduciary, Action: Transfer, Target: RestrictedCountry)',
        odrlPolicy: '{"@context":"http://www.w3.org/ns/odrl/2/","prohibition":[{"action":"transfer","constraint":[{"operator":"eq","rightOperand":"restricted"}]}]}',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        clauses: [
          { id: 'dpdp-sec-16-c1', articleId: 'dpdp-sec-16', clauseNumber: '16.1', content: 'Restrict transfers to blacklisted countries.', clauseType: 'PROHIBITION' }
        ]
      }
    ]
  }
];

const SEEDED_GRAPH: GraphData = {
  nodes: [
    { id: 'EU', labels: ['JURISDICTION'], properties: { name: 'European Union' } },
    { id: 'IN', labels: ['JURISDICTION'], properties: { name: 'India' } },
    { id: 'US', labels: ['JURISDICTION'], properties: { name: 'United States' } },
    { id: 'RU', labels: ['JURISDICTION'], properties: { name: 'Russia' } },
    { id: 'CN', labels: ['JURISDICTION'], properties: { name: 'China' } }
  ],
  edges: [
    { id: 'e1', source: 'EU', target: 'IN', type: 'ADEQUATE' },
    { id: 'e2', source: 'EU', target: 'US', type: 'CONDITIONAL' }
  ]
};

// Local Db helper
const getLocalRegs = (): RegulationResponse[] => {
  const data = localStorage.getItem('pqvcf_regulations');
  if (!data) {
    localStorage.setItem('pqvcf_regulations', JSON.stringify(SEEDED_REGS));
    return SEEDED_REGS;
  }
  return JSON.parse(data);
};

const setLocalRegs = (regs: RegulationResponse[]) => {
  localStorage.setItem('pqvcf_regulations', JSON.stringify(regs));
};

const getLocalGraph = (): GraphData => {
  const data = localStorage.getItem('pqvcf_graph');
  if (!data) {
    localStorage.setItem('pqvcf_graph', JSON.stringify(SEEDED_GRAPH));
    return SEEDED_GRAPH;
  }
  return JSON.parse(data);
};

const setLocalGraph = (g: GraphData) => {
  localStorage.setItem('pqvcf_graph', JSON.stringify(g));
};

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw new Error(`API error: ${response.status}`);
  }
  if (response.status === 204) {
    return {} as T;
  }
  return response.json();
}

export const regulationsApi = {
  async list(jurisdiction?: string, search?: string): Promise<RegulationResponse[]> {
    try {
      const params = new URLSearchParams();
      if (jurisdiction) params.append('jurisdiction', jurisdiction);
      if (search) params.append('search', search);
      const response = await fetch(`${API_BASE}/regulations?${params.toString()}`);
      return await handleResponse<RegulationResponse[]>(response);
    } catch (e) {
      console.warn('Backend regulations down, fallback to mock DB');
      let regs = getLocalRegs();
      if (jurisdiction) {
        regs = regs.filter(r => r.primaryJurisdiction.toLowerCase() === jurisdiction.toLowerCase());
      }
      if (search) {
        regs = regs.filter(r => r.name.toLowerCase().includes(search.toLowerCase()) || r.shortName.toLowerCase().includes(search.toLowerCase()));
      }
      return regs;
    }
  },

  async getById(id: string): Promise<RegulationResponse> {
    try {
      const response = await fetch(`${API_BASE}/regulations/${id}`);
      return await handleResponse<RegulationResponse>(response);
    } catch (e) {
      const reg = getLocalRegs().find(r => r.id === id);
      if (!reg) throw new Error('Regulation not found in mock DB');
      return reg;
    }
  },

  async getByShortName(shortName: string): Promise<RegulationResponse> {
    try {
      const response = await fetch(`${API_BASE}/regulations/short/${shortName}`);
      return await handleResponse<RegulationResponse>(response);
    } catch (e) {
      const reg = getLocalRegs().find(r => r.shortName.toLowerCase() === shortName.toLowerCase());
      if (!reg) throw new Error('Regulation not found in mock DB');
      return reg;
    }
  },

  async register(command: RegisterCommand): Promise<RegulationResponse> {
    try {
      const response = await fetch(`${API_BASE}/regulations`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(command),
      });
      return await handleResponse<RegulationResponse>(response);
    } catch (e) {
      const regs = getLocalRegs();
      const newReg: RegulationResponse = {
        id: `mock-uuid-${Date.now()}`,
        name: command.name,
        shortName: command.shortName,
        primaryJurisdiction: command.jurisdiction,
        version: command.version,
        description: command.description,
        status: 'DRAFT',
        formalSpec: '',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        articles: []
      };
      regs.push(newReg);
      setLocalRegs(regs);
      return newReg;
    }
  },

  async updateMetadata(id: string, metadata: { name: string; description: string; version: string }): Promise<RegulationResponse> {
    try {
      const response = await fetch(`${API_BASE}/regulations/${id}/metadata`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(metadata),
      });
      return await handleResponse<RegulationResponse>(response);
    } catch (e) {
      const regs = getLocalRegs();
      const idx = regs.findIndex(r => r.id === id);
      if (idx === -1) throw new Error('Not found');
      regs[idx].name = metadata.name;
      regs[idx].description = metadata.description;
      regs[idx].version = metadata.version;
      regs[idx].updatedAt = new Date().toISOString();
      setLocalRegs(regs);
      return regs[idx];
    }
  },

  async updateFormalSpec(id: string, formalSpec: string): Promise<RegulationResponse> {
    try {
      const response = await fetch(`${API_BASE}/regulations/${id}/formal-spec`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: formalSpec,
      });
      return await handleResponse<RegulationResponse>(response);
    } catch (e) {
      const regs = getLocalRegs();
      const idx = regs.findIndex(r => r.id === id);
      if (idx === -1) throw new Error('Not found');
      regs[idx].formalSpec = formalSpec;
      regs[idx].updatedAt = new Date().toISOString();
      setLocalRegs(regs);
      return regs[idx];
    }
  },

  async activate(id: string): Promise<RegulationResponse> {
    try {
      const response = await fetch(`${API_BASE}/regulations/${id}/activate`, {
        method: 'POST',
      });
      return await handleResponse<RegulationResponse>(response);
    } catch (e) {
      const regs = getLocalRegs();
      const idx = regs.findIndex(r => r.id === id);
      if (idx === -1) throw new Error('Not found');
      regs[idx].status = 'ACTIVE';
      regs[idx].updatedAt = new Date().toISOString();
      setLocalRegs(regs);
      return regs[idx];
    }
  },

  async deprecate(id: string): Promise<RegulationResponse> {
    try {
      const response = await fetch(`${API_BASE}/regulations/${id}/deprecate`, {
        method: 'POST',
      });
      return await handleResponse<RegulationResponse>(response);
    } catch (e) {
      const regs = getLocalRegs();
      const idx = regs.findIndex(r => r.id === id);
      if (idx === -1) throw new Error('Not found');
      regs[idx].status = 'DEPRECATED';
      regs[idx].updatedAt = new Date().toISOString();
      setLocalRegs(regs);
      return regs[idx];
    }
  },

  async delete(id: string): Promise<void> {
    try {
      const response = await fetch(`${API_BASE}/regulations/${id}`, {
        method: 'DELETE',
      });
      await handleResponse<void>(response);
    } catch (e) {
      const regs = getLocalRegs();
      const filtered = regs.filter(r => r.id !== id);
      setLocalRegs(filtered);
    }
  },

  async addArticle(command: AddArticleCommand): Promise<ArticleResponse> {
    try {
      const response = await fetch(`${API_BASE}/regulations/articles`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(command),
      });
      return await handleResponse<ArticleResponse>(response);
    } catch (e) {
      const regs = getLocalRegs();
      const idx = regs.findIndex(r => r.id === command.regulationId);
      if (idx === -1) throw new Error('Regulation not found');
      const artId = `art-uuid-${Date.now()}`;
      const newArt: ArticleResponse = {
        id: artId,
        regulationId: command.regulationId,
        articleNumber: command.articleNumber,
        title: command.title,
        content: command.content,
        deonticFormula: command.deonticFormula || '',
        odrlPolicy: command.odrlPolicy || '',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        clauses: (command.clauses || []).map((c, cidx) => ({
          id: `clause-uuid-${Date.now()}-${cidx}`,
          articleId: artId,
          clauseNumber: c.clauseNumber,
          content: c.content,
          clauseType: c.clauseType as any
        }))
      };
      regs[idx].articles.push(newArt);
      setLocalRegs(regs);
      return newArt;
    }
  },

  async getGraph(): Promise<GraphData> {
    try {
      const response = await fetch(`${API_BASE}/graph`);
      return await handleResponse<GraphData>(response);
    } catch (e) {
      return getLocalGraph();
    }
  },

  async declareAdequacy(source: string, target: string): Promise<void> {
    try {
      const response = await fetch(`${API_BASE}/graph/adequacy`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ source, target }),
      });
      await handleResponse<void>(response);
    } catch (e) {
      const graph = getLocalGraph();
      const hasEdge = graph.edges.some(edge => edge.source === source && edge.target === target);
      if (!hasEdge) {
        graph.edges.push({
          id: `edge-${Date.now()}`,
          source,
          target,
          type: 'ADEQUATE'
        });
        setLocalGraph(graph);
      }
    }
  },

  async checkAdequacy(source: string, target: string): Promise<{ source: string; target: string; isAdequate: boolean }> {
    try {
      const response = await fetch(`${API_BASE}/graph/adequacy/check?source=${source}&target=${target}`);
      return await handleResponse<{ source: string; target: string; isAdequate: boolean }>(response);
    } catch (e) {
      const graph = getLocalGraph();
      // Simple DFS or direct link adequacy lookup
      const isAdequate = graph.edges.some(edge => edge.source === source && edge.target === target && edge.type === 'ADEQUATE');
      return { source, target, isAdequate };
    }
  }
};


