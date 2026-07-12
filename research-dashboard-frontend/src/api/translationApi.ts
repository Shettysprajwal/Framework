import { TranslateCommand } from '../types/translation';

export interface TranslationResponse {
  id: string;
  regulationShortName: string;
  articleNumber: string;
  clauseNumber?: string;
  rawSourceText: string;
  deonticOperator: string;
  subject: string;
  action: string;
  target: string;
  constraint?: string;
  smtSpec: string;
  odrlPolicy: string;
  valid: boolean;
  validationMessage: string;
}

const API_BASE = '/api/v1/translation';

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `API error: ${response.status}`);
  }
  if (response.status === 204) {
    return {} as T;
  }
  return response.json();
}

export const translationApi = {
  async translate(command: { regulationShortName: string; articleNumber: string; clauseNumber: string; rawSourceText: string }): Promise<TranslationResponse> {
    const response = await fetch(`${API_BASE}/translate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(command),
    });
    return handleResponse<TranslationResponse>(response);
  },

  async listAll(): Promise<TranslationResponse[]> {
    const response = await fetch(`${API_BASE}/rules`);
    return handleResponse<TranslationResponse[]>(response);
  },

  async getById(id: string): Promise<TranslationResponse> {
    const response = await fetch(`${API_BASE}/rules/${id}`);
    return handleResponse<TranslationResponse>(response);
  },

  async getByRegulation(regulation: string, article?: string): Promise<TranslationResponse[]> {
    const url = article 
      ? `${API_BASE}/rules/regulation/${regulation}?article=${article}`
      : `${API_BASE}/rules/regulation/${regulation}`;
    const response = await fetch(url);
    return handleResponse<TranslationResponse[]>(response);
  },

  async delete(id: string): Promise<void> {
    const response = await fetch(`${API_BASE}/rules/${id}`, {
      method: 'DELETE',
    });
    return handleResponse<void>(response);
  }
};
