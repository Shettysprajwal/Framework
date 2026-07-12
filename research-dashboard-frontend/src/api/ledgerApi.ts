import { RecordDto, VerificationDto, SealRecordCommand } from '../types/ledger';

const API_BASE = '/api/v1/ledger';

// Seed Initial Ledger Genesis block
const INITIAL_RECORDS: RecordDto[] = [
  {
    id: 'block-000',
    action: 'GENESIS_SEALING',
    actor: 'SYSTEM',
    target: 'Genesis',
    decision: 'GENESIS',
    previousHash: '0000000000000000000000000000000000000000000000000000000000000000',
    currentHash: '3a5f82c1634b3d7de0833a69a23eb81a4a49938b8e0cfa0b32ad8d39e23eb001',
    timestamp: new Date(Date.now() - 120000).toISOString()
  }
];

// In-memory/LocalStorage helper
const getLocalRecords = (): RecordDto[] => {
  const data = localStorage.getItem('pqvcf_ledger_records');
  if (!data) {
    localStorage.setItem('pqvcf_ledger_records', JSON.stringify(INITIAL_RECORDS));
    return INITIAL_RECORDS;
  }
  return JSON.parse(data);
};

const setLocalRecords = (recs: RecordDto[]) => {
  localStorage.setItem('pqvcf_ledger_records', JSON.stringify(recs));
};

// Simple pseudo-hash generator
const computeHash = (prev: string, action: string, actor: string, target: string, decision: string, ts: string): string => {
  const str = prev + action + actor + target + decision + ts;
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0; // Convert to 32bit integer
  }
  const hex = Math.abs(hash).toString(16).padStart(8, '0');
  return `hash-sha256-mock-${hex}-${hex}`;
};

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw new Error(`API error: ${response.status}`);
  }
  return response.json();
}

export const ledgerApi = {
  async listRecords(): Promise<RecordDto[]> {
    try {
      const response = await fetch(`${API_BASE}/records`);
      return await handleResponse<RecordDto[]>(response);
    } catch (e) {
      console.warn('Backend ledger down, fallback to mock ledger timeline');
      return getLocalRecords();
    }
  },

  async sealRecord(command: SealRecordCommand): Promise<RecordDto> {
    try {
      const response = await fetch(`${API_BASE}/seal`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(command),
      });
      return await handleResponse<RecordDto>(response);
    } catch (e) {
      console.warn('Backend ledger down, executing mock cryptographic block seal');
      const records = getLocalRecords();
      const prevBlock = records[records.length - 1];
      const prevHash = prevBlock ? prevBlock.currentHash : '0000000000000000000000000000000000000000000000000000000000000000';
      const ts = new Date().toISOString();
      const currentHash = computeHash(prevHash, command.action, command.actor, command.target, command.decision, ts);

      const newBlock: RecordDto = {
        id: `block-uuid-${Date.now()}`,
        action: command.action,
        actor: command.actor,
        target: command.target,
        decision: command.decision,
        previousHash: prevHash,
        currentHash,
        timestamp: ts
      };

      records.push(newBlock);
      setLocalRecords(records);
      return newBlock;
    }
  },

  async verifyLedger(): Promise<VerificationDto> {
    try {
      const response = await fetch(`${API_BASE}/verify`);
      return await handleResponse<VerificationDto>(response);
    } catch (e) {
      console.warn('Backend ledger down, executing local cryptographic verification checks');
      const records = getLocalRecords();
      
      // We check if any record's stored previousHash matches the preceding record's currentHash
      // And we also verify if the currentHash matches the recomputed hash of its contents (to detect tampering)
      let isValid = true;
      let brokenIndex = -1;
      let msg = 'Cryptographic verification complete. All blocks are securely linked.';

      // Check if we have altered records stored in a tamper database
      const tamperedRecordIndex = localStorage.getItem('pqvcf_ledger_tampered_index');
      if (tamperedRecordIndex !== null) {
        isValid = false;
        brokenIndex = parseInt(tamperedRecordIndex, 10);
        msg = `Cryptographic breach! Tampering detected in Block #${brokenIndex}. Hash chain integrity verification failed.`;
      } else {
        for (let i = 1; i < records.length; i++) {
          const curr = records[i];
          const prev = records[i - 1];
          
          // Check chain link
          if (curr.previousHash !== prev.currentHash) {
            isValid = false;
            brokenIndex = i;
            msg = `Cryptographic breach! Link broken between block #${i-1} and #${i}.`;
            break;
          }

          // Check block content hash consistency
          const computed = computeHash(curr.previousHash, curr.action, curr.actor, curr.target, curr.decision, curr.timestamp);
          if (curr.currentHash !== computed) {
            isValid = false;
            brokenIndex = i;
            msg = `Cryptographic breach! Block #${i} content signature mismatch (Tampered Decision value detected).`;
            break;
          }
        }
      }

      return {
        valid: isValid,
        tamperedIndex: brokenIndex,
        details: msg
      };
    }
  },

  async tamperRecord(index: number, tamperedValue: string): Promise<void> {
    try {
      const response = await fetch(`${API_BASE}/tamper/${index}?tamperedValue=${encodeURIComponent(tamperedValue)}`, {
        method: 'POST',
      });
      if (!response.ok) {
        throw new Error(`API error: ${response.status}`);
      }
    } catch (e) {
      console.warn('Backend ledger down, injecting tampering status in local sandbox');
      const records = getLocalRecords();
      if (index >= 0 && index < records.length) {
        records[index].decision = tamperedValue;
        setLocalRecords(records);
        // Track the tampered index to trigger validation failure
        localStorage.setItem('pqvcf_ledger_tampered_index', index.toString());
      } else {
        throw new Error('Index out of bounds');
      }
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
      localStorage.removeItem('pqvcf_ledger_records');
      localStorage.removeItem('pqvcf_ledger_tampered_index');
    }
  }
};

