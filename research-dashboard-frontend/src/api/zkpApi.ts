import { ProofResponse, GenerateProofCommand, VerifyProofCommand } from '../types/zkp';

const API_BASE = '/api/v1/zkp';

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `API error: ${response.status}`);
  }
  return response.json();
}

export const zkpApi = {
  async listProofs(): Promise<ProofResponse[]> {
    const response = await fetch(`${API_BASE}/proofs`);
    return handleResponse<ProofResponse[]>(response);
  },

  async generateProof(command: GenerateProofCommand): Promise<ProofResponse> {
    const response = await fetch(`${API_BASE}/prove`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(command),
    });
    return handleResponse<ProofResponse>(response);
  },

  async verify(command: VerifyProofCommand): Promise<boolean> {
    const response = await fetch(`${API_BASE}/verify`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(command),
    });
    return handleResponse<boolean>(response);
  },

  async deleteProof(id: string): Promise<void> {
    const response = await fetch(`${API_BASE}/proofs/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok) {
      throw new Error(`API error: ${response.status}`);
    }
  }
};
