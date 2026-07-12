export interface ClauseResponse {
  id: string;
  articleId: string;
  clauseNumber: string;
  content: string;
  clauseType: 'PERMISSION' | 'OBLIGATION' | 'PROHIBITION' | 'EXEMPTION' | 'DEFINITION' | 'PROVISION';
}

export interface ArticleResponse {
  id: string;
  regulationId: string;
  articleNumber: string;
  title: string;
  content: string;
  deonticFormula?: string;
  odrlPolicy?: string;
  clauses: ClauseResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface RegulationResponse {
  id: string;
  name: string;
  shortName: string;
  primaryJurisdiction: string;
  version: string;
  effectiveDate?: string;
  description?: string;
  status: 'DRAFT' | 'ACTIVE' | 'DEPRECATED';
  formalSpec?: string;
  articles: ArticleResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface GraphNode {
  id: string;
  labels: string[];
  properties: Record<string, unknown>;
}

export interface GraphEdge {
  id: string;
  type: string;
  source: string;
  target: string;
}

export interface GraphData {
  nodes: GraphNode[];
  edges: GraphEdge[];
}
