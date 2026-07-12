import { ResolveQuery, ResolvedContext, RegisterAttribute } from '../types/pip';

const API_BASE = '/api/v1/pip';

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `API error: ${response.status}`);
  }
  if (response.status === 201 || response.status === 204) {
    return {} as T;
  }
  return response.json();
}

export const pipApi = {
  async resolve(query: ResolveQuery): Promise<ResolvedContext> {
    const response = await fetch(`${API_BASE}/resolve`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(query),
    });
    return handleResponse<ResolvedContext>(response);
  },

  async registerSubjectAttribute(request: RegisterAttribute): Promise<void> {
    const response = await fetch(`${API_BASE}/attributes/subject`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    });
    return handleResponse<void>(response);
  },

  async registerResourceAttribute(request: RegisterAttribute): Promise<void> {
    const response = await fetch(`${API_BASE}/attributes/resource`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    });
    return handleResponse<void>(response);
  }
};
