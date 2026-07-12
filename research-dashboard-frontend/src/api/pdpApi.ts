import { EvaluateCommand, EvaluateResponse, AuditLogResponse } from '../types/pdp';

const API_BASE = '/api/v1/pdp';

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `API error: ${response.status}`);
  }
  return response.json();
}

export const pdpApi = {
  async evaluate(command: EvaluateCommand): Promise<EvaluateResponse> {
    const response = await fetch(`${API_BASE}/evaluate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(command),
    });
    return handleResponse<EvaluateResponse>(response);
  },

  async listAudits(): Promise<AuditLogResponse[]> {
    const response = await fetch(`${API_BASE}/audits`);
    return handleResponse<AuditLogResponse[]>(response);
  }
};
