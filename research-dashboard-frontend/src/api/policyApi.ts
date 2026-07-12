import { PolicyResponse, CreatePolicyCommand, LinkRuleCommand } from '../types/policy';

const API_BASE = '/api/v1/policies';

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

export const policyApi = {
  async listAll(): Promise<PolicyResponse[]> {
    const response = await fetch(API_BASE);
    return handleResponse<PolicyResponse[]>(response);
  },

  async getById(id: string): Promise<PolicyResponse> {
    const response = await fetch(`${API_BASE}/${id}`);
    return handleResponse<PolicyResponse>(response);
  },

  async create(command: CreatePolicyCommand): Promise<PolicyResponse> {
    const response = await fetch(API_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(command),
    });
    return handleResponse<PolicyResponse>(response);
  },

  async linkRule(policyId: string, command: Omit<LinkRuleCommand, 'policyId'>): Promise<PolicyResponse> {
    const response = await fetch(`${API_BASE}/${policyId}/links`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(command),
    });
    return handleResponse<PolicyResponse>(response);
  },

  async unlinkRule(policyId: string, linkId: string): Promise<PolicyResponse> {
    const response = await fetch(`${API_BASE}/${policyId}/links/${linkId}`, {
      method: 'DELETE',
    });
    return handleResponse<PolicyResponse>(response);
  },

  async activate(id: string): Promise<PolicyResponse> {
    const response = await fetch(`${API_BASE}/${id}/activate`, {
      method: 'POST',
    });
    return handleResponse<PolicyResponse>(response);
  },

  async deprecate(id: string): Promise<PolicyResponse> {
    const response = await fetch(`${API_BASE}/${id}/deprecate`, {
      method: 'POST',
    });
    return handleResponse<PolicyResponse>(response);
  },

  async delete(id: string): Promise<void> {
    const response = await fetch(`${API_BASE}/${id}`, {
      method: 'DELETE',
    });
    return handleResponse<void>(response);
  }
};
