import { KeyResponse, GenerateKeyCommand, SignCommand, SignResponse, VerifyCommand } from '../types/pqc';

const API_BASE = '/api/v1/pqc';

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `API error: ${response.status}`);
  }
  return response.json();
}

export const pqcApi = {
  async listKeys(): Promise<KeyResponse[]> {
    const response = await fetch(`${API_BASE}/keys`);
    return handleResponse<KeyResponse[]>(response);
  },

  async generateKey(command: GenerateKeyCommand): Promise<KeyResponse> {
    const response = await fetch(`${API_BASE}/keys`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(command),
    });
    return handleResponse<KeyResponse>(response);
  },

  async sign(command: SignCommand): Promise<SignResponse> {
    const response = await fetch(`${API_BASE}/sign`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(command),
    });
    return handleResponse<SignResponse>(response);
  },

  async verify(command: VerifyCommand): Promise<boolean> {
    const response = await fetch(`${API_BASE}/verify`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(command),
    });
    return handleResponse<boolean>(response);
  },

  async deleteKey(id: string): Promise<void> {
    const response = await fetch(`${API_BASE}/keys/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok) {
      throw new Error(`API error: ${response.status}`);
    }
  }
};
