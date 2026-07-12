import React, { useEffect, useState } from 'react';
import { zkpApi } from '../api/zkpApi';
import { ProofResponse } from '../types/zkp';
import { ShieldCheck, ShieldAlert, Sparkles, Cpu, EyeOff, Check, FileCheck, RefreshCw, Trash2, Key, Database } from 'lucide-react';

export const ZkProofPage: React.FC = () => {
  const [proofs, setProofs] = useState<ProofResponse[]>([]);
  const [proofType, setProofType] = useState<'DATA_RESIDENCY' | 'TRANSFER_BASIS' | 'PURPOSE_LIMITATION'>('DATA_RESIDENCY');
  const [secretVal, setSecretVal] = useState('101');
  const [publicInputs, setPublicInputs] = useState('{"required_zone": "EU_WEST", "compliance_article": "GDPR_Art_45"}');
  const [loading, setLoading] = useState(false);
  const [selectedProof, setSelectedProof] = useState<ProofResponse | null>(null);

  // Verification Sandbox State
  const [verifying, setVerifying] = useState(false);
  const [verificationResult, setVerificationResult] = useState<boolean | null>(null);
  const [tamperBytes, setTamperBytes] = useState(false);

  const fetchProofs = () => {
    zkpApi.listProofs()
      .then((data) => {
        setProofs(data);
        if (data.length > 0) {
          setSelectedProof(data[data.length - 1]);
        }
      })
      .catch(console.error);
  };

  useEffect(() => {
    fetchProofs();
  }, []);

  const handleProve = (e: React.FormEvent) => {
    e.preventDefault();
    const witness = parseInt(secretVal, 10);
    if (isNaN(witness)) {
      alert('Secret Witness must be an integer.');
      return;
    }

    setLoading(true);
    setVerificationResult(null);
    
    zkpApi.generateProof({
      proofType,
      secretWitnessValue: witness,
      publicInputsJson: publicInputs
    })
    .then((res) => {
      setLoading(false);
      setSelectedProof(res);
      fetchProofs();
    })
    .catch((err) => {
      console.error(err);
      setLoading(false);
      alert('Proving failed: ' + err.message);
    });
  };

  const handleVerify = (tamper: boolean) => {
    if (!selectedProof) return;
    setVerifying(true);
    setVerificationResult(null);

    const challenge = selectedProof.challengeHex;
    const response = selectedProof.responseHex;
    // Tamper with commitment if selected
    const commitment = tamper 
      ? selectedProof.commitmentHex.substring(2) + '00' 
      : selectedProof.commitmentHex;

    zkpApi.verify({
      proofId: selectedProof.proofId,
      challengeHex: challenge,
      responseHex: response,
      commitmentHex: commitment
    })
    .then((res) => {
      setVerificationResult(res);
      setVerifying(false);
    })
    .catch((err) => {
      console.error(err);
      setVerifying(false);
    });
  };

  const handleDelete = (id: string) => {
    if (confirm('Delete zero-knowledge proof ' + id + ' from auditing ledger?')) {
      zkpApi.deleteProof(id)
        .then(() => {
          setSelectedProof(null);
          fetchProofs();
        })
        .catch(console.error);
    }
  };

  const loadExample = (type: 'DATA_RESIDENCY' | 'TRANSFER_BASIS' | 'PURPOSE_LIMITATION', witness: string, pubInputs: string) => {
    setProofType(type);
    setSecretVal(witness);
    setPublicInputs(pubInputs);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '2rem', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '0.25rem' }}>
          Zero-Knowledge Compliance Proof Engine (ZKP)
        </h1>
        <p style={{ color: 'hsl(var(--text-secondary))' }}>
          Generate and verify mathematical compliance proofs without revealing confidential infrastructure details.
        </p>
      </div>

      {/* Examples presets */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem' }}>
        <div class="glass-card" style={{ cursor: 'pointer', padding: '1rem' }} onClick={() => loadExample('DATA_RESIDENCY', '555', '{"required_zone": "EU_WEST_1"}')}>
          <h4 style={{ color: 'hsl(var(--accent-cyan))', fontSize: '0.85rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <EyeOff size={14} /> Data Residency Witness
          </h4>
          <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>Commit to server ID 555 and prove it matches required EU zones.</p>
        </div>

        <div class="glass-card" style={{ cursor: 'pointer', padding: '1rem' }} onClick={() => loadExample('TRANSFER_BASIS', '1', '{"article": "GDPR_Art_46_SCCs"}')}>
          <h4 style={{ color: 'hsl(var(--color-success))', fontSize: '0.85rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <EyeOff size={14} /> Legal Transfer Basis Witness
          </h4>
          <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>Commit to active SCC status (witness: 1) under Art 46.</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 2fr', gap: '2rem' }}>
        {/* Left column: ZK circuit generation */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <div class="glass-card">
            <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1.2rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Cpu size={18} /> ZK Circuit Prover
            </h3>
            <form onSubmit={handleProve} style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
              <div>
                <label>ZK Proof Circuit Statement</label>
                <select class="glass-input" value={proofType} onChange={e => setProofType(e.target.value as any)}>
                  <option value="DATA_RESIDENCY">Data Residency (Prove J zone matches)</option>
                  <option value="TRANSFER_BASIS">Transfer Basis (Prove SCCs validity)</option>
                  <option value="PURPOSE_LIMITATION">Purpose Limitation (Prove consent bounds)</option>
                </select>
              </div>
              <div>
                <label>Secret Witness Value (Private Input)</label>
                <input type="password" class="glass-input" placeholder="Enter secret integer (e.g. server code)" required value={secretVal} onChange={e => setSecretVal(e.target.value)} />
                <span style={{ fontSize: '0.7rem', color: 'hsl(var(--text-muted))', marginTop: '0.2rem', display: 'block' }}>
                  * This witness is hidden behind a Pedersen commitment point $C = g^x \cdot h^r$ and never revealed to the verifier.
                </span>
              </div>
              <div>
                <label>Public Inputs (JSON Spec)</label>
                <textarea class="glass-input" style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', height: '60px' }} required value={publicInputs} onChange={e => setPublicInputs(e.target.value)} />
              </div>
              <button type="submit" class="glass-btn glass-btn-primary" disabled={loading}>
                <Sparkles size={16} /> {loading ? 'Running Sigma Prover...' : 'Generate ZK Proof'}
              </button>
            </form>
          </div>
        </div>

        {/* Right column: Proof challenge details and verify */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          {selectedProof ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              {/* Proof details */}
              <div class="glass-card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.2rem' }}>
                  <h3 style={{ fontSize: '1.2rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Key size={18} /> ZK Proof Certificate
                  </h3>
                  <button class="glass-btn" style={{ color: 'hsl(var(--color-danger))' }} onClick={() => handleDelete(selectedProof.proofId)}>
                    <Trash2 size={14} /> Delete
                  </button>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.8rem', fontSize: '0.8rem' }}>
                  <div>
                    <span style={{ color: 'hsl(var(--text-secondary))' }}>Proof ID:</span>
                    <div style={{ fontFamily: 'var(--font-mono)', opacity: 0.8 }}>{selectedProof.proofId}</div>
                  </div>
                  <div>
                    <span style={{ color: 'hsl(var(--text-secondary))' }}>Pedersen Commitment ($C = g^x \cdot h^r$):</span>
                    <div style={{ fontFamily: 'var(--font-mono)', opacity: 0.6, wordBreak: 'break-all', fontSize: '0.75rem', color: 'hsl(var(--accent-cyan))' }}>
                      {selectedProof.commitmentHex}
                    </div>
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                    <div>
                      <span style={{ color: 'hsl(var(--text-secondary))' }}>Challenge ($c$):</span>
                      <div style={{ fontFamily: 'var(--font-mono)', opacity: 0.6, wordBreak: 'break-all', fontSize: '0.75rem' }}>
                        {selectedProof.challengeHex}
                      </div>
                    </div>
                    <div>
                      <span style={{ color: 'hsl(var(--text-secondary))' }}>Response ($s_1 || s_2 || T$):</span>
                      <div style={{ fontFamily: 'var(--font-mono)', opacity: 0.6, wordBreak: 'break-all', fontSize: '0.75rem' }}>
                        {selectedProof.responseHex.substring(0, 40)}...
                      </div>
                    </div>
                  </div>
                </div>

                {/* Verification controls */}
                <div style={{ borderTop: '1px solid var(--glass-border)', marginTop: '1.5rem', paddingTop: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  <div style={{ display: 'flex', gap: '1rem' }}>
                    <button class="glass-btn glass-btn-primary" style={{ flex: 1 }} onClick={() => handleVerify(false)}>
                      Verify Proof
                    </button>
                    <button class="glass-btn" style={{ flex: 1, color: 'hsl(var(--color-warning))' }} onClick={() => handleVerify(true)}>
                      Tamper Commitment & Verify
                    </button>
                  </div>

                  {verificationResult !== null && (
                    <div style={{
                      padding: '0.8rem',
                      borderRadius: '4px',
                      fontSize: '0.85rem',
                      fontWeight: 600,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.5rem',
                      background: verificationResult ? 'rgba(16,185,129,0.1)' : 'rgba(244,63,94,0.1)',
                      color: verificationResult ? 'hsl(var(--color-success))' : 'hsl(var(--color-danger))'
                    }}>
                      {verificationResult ? (
                        <>
                          <ShieldCheck size={18} /> Proof verified successfully! Prover knows the secret witness values.
                        </>
                      ) : (
                        <>
                          <ShieldAlert size={18} /> Proof verification failed! The commitment was tampered or invalid.
                        </>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </div>
          ) : (
            <div class="glass-card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '6rem 2rem', color: 'hsl(var(--text-secondary))', borderStyle: 'dashed' }}>
              <EyeOff size={48} style={{ opacity: 0.3, marginBottom: '1rem' }} />
              <h4>Verification Certificate Pending</h4>
              <p style={{ fontSize: '0.85rem' }}>Select a circuit and click Generate on the left to run Sigma provers.</p>
            </div>
          )}
        </div>
      </div>

      {/* Proof Audits Ledger table */}
      <div class="glass-card" style={{ padding: 0, overflow: 'hidden' }}>
        <div style={{ padding: '1.2rem', borderBottom: '1px solid var(--glass-border)' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Database size={18} /> Zero-Knowledge Proof Auditing Ledger
          </h3>
        </div>
        {proofs.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'hsl(var(--text-muted))' }}>
            No proofs logged in auditing ledger yet.
          </div>
        ) : (
          <table class="glass-table">
            <thead>
              <tr>
                <th>Certificate ID</th>
                <th>Circuit Type</th>
                <th>Commitment Point</th>
                <th>Public Inputs Spec</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {proofs.map(p => (
                <tr key={p.proofId} style={{ cursor: 'pointer' }} onClick={() => setSelectedProof(p)}>
                  <td style={{ fontFamily: 'var(--font-mono)', fontSize: '0.8rem', fontWeight: 600 }}>{p.proofId.substring(0, 8)}...</td>
                  <td>
                    <span style={{
                      padding: '0.2rem 0.5rem',
                      borderRadius: '4px',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      background: 'rgba(6,182,212,0.1)',
                      color: 'hsl(var(--accent-cyan))'
                    }}>
                      {p.proofType}
                    </span>
                  </td>
                  <td style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', opacity: 0.7 }}>
                    {p.commitmentHex.substring(0, 24)}...
                  </td>
                  <td style={{ fontSize: '0.8rem', opacity: 0.8 }}>
                    {p.publicInputsJson}
                  </td>
                  <td style={{ textAlign: 'right' }} onClick={e => e.stopPropagation()}>
                    <button class="glass-btn" style={{ color: 'hsl(var(--color-danger))' }} onClick={() => handleDelete(p.proofId)}>
                      <Trash2 size={14} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};
