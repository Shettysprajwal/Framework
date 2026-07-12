import React, { useEffect, useState } from 'react';
import { pqcApi } from '../api/pqcApi';
import { KeyResponse, SignResponse } from '../types/pqc';
import { Key, ShieldAlert, Sparkles, Plus, Trash2, Cpu, Copy, Check, FileCheck, ShieldCheck, Lock } from 'lucide-react';

export const PqcCryptoPage: React.FC = () => {
  const [keys, setKeys] = useState<KeyResponse[]>([]);
  const [alias, setAlias] = useState('');
  const [algorithm, setAlgorithm] = useState<'ML_DSA_65' | 'ML_KEM_768' | 'SLH_DSA_256'>('ML_DSA_65');
  const [loading, setLoading] = useState(false);
  const [copiedKeyId, setCopiedKeyId] = useState<string | null>(null);

  // Signing Sandbox State
  const [selectedSignKey, setSelectedSignKey] = useState('');
  const [payloadText, setPayloadText] = useState('{"verdict": "PERMIT", "rules_checked": ["rule-1"], "transitivity": true}');
  const [signing, setSigning] = useState(false);
  const [signatureResult, setSignatureResult] = useState<SignResponse | null>(null);

  // Verification Sandbox State
  const [verifying, setVerifying] = useState(false);
  const [verificationResult, setVerificationResult] = useState<boolean | null>(null);
  const [tamperedPayload, setTamperedPayload] = useState(false);

  // Hybrid KEM Sandbox State
  const [kemKeyId, setKemKeyId] = useState('');
  const [kemStatus, setKemStatus] = useState<'idle' | 'encapsulating' | 'completed'>('idle');
  const [ciphertextHex, setCiphertextHex] = useState('');
  const [sessionKeyHex, setSessionKeyHex] = useState('');

  const fetchKeys = () => {
    pqcApi.listKeys()
      .then((data) => {
        setKeys(data);
        if (data.length > 0) {
          const signKeys = data.filter(k => k.algorithm !== 'ML_KEM_768');
          if (signKeys.length > 0) {
            setSelectedSignKey(signKeys[0].keyId);
          }
          const kemKeys = data.filter(k => k.algorithm === 'ML_KEM_768');
          if (kemKeys.length > 0) {
            setKemKeyId(kemKeys[0].keyId);
          }
        }
      })
      .catch(console.error);
  };

  useEffect(() => {
    fetchKeys();
  }, []);

  const handleGenerateKey = (e: React.FormEvent) => {
    e.preventDefault();
    if (!alias.trim()) return;

    setLoading(true);
    pqcApi.generateKey({ algorithm, alias: alias.trim() })
      .then(() => {
        setAlias('');
        setLoading(false);
        fetchKeys();
      })
      .catch((err) => {
        console.error(err);
        setLoading(false);
        alert('Generation failed: ' + err.message);
      });
  };

  const handleDeleteKey = (id: string) => {
    if (confirm('Delete post-quantum key ' + id + ' from KMS vault?')) {
      pqcApi.deleteKey(id)
        .then(() => fetchKeys())
        .catch(console.error);
    }
  };

  const handleSign = () => {
    if (!selectedSignKey) return;
    setSigning(true);
    setSignatureResult(null);
    setVerificationResult(null);

    // Convert payload to hex
    const encoder = new TextEncoder();
    const bytes = encoder.encode(payloadText);
    const hex = Array.from(bytes).map(b => b.toString(16).padStart(2, '0')).join('');

    pqcApi.sign({ keyId: selectedSignKey, payloadHex: hex })
      .then((res) => {
        setSignatureResult(res);
        setSigning(false);
      })
      .catch((err) => {
        console.error(err);
        setSigning(false);
        alert('Signing failed: ' + err.message);
      });
  };

  const handleVerify = (tamper: boolean) => {
    if (!signatureResult) return;
    setVerifying(true);
    setVerificationResult(null);

    const encoder = new TextEncoder();
    const verifiedPayload = tamper ? payloadText + ' [TAMPERED]' : payloadText;
    const bytes = encoder.encode(verifiedPayload);
    const hex = Array.from(bytes).map(b => b.toString(16).padStart(2, '0')).join('');

    pqcApi.verify({
      keyId: signatureResult.keyId,
      payloadHex: hex,
      signatureHex: signatureResult.signatureHex
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

  const handleSimulateKEM = () => {
    if (!kemKeyId) return;
    setKemStatus('encapsulating');
    
    // Simulate Kyber-768 hybrid KEM exchange
    setTimeout(() => {
      // 32-byte shared secret seed, 1088-byte ciphertext standard Kyber-768 length
      const secret = Array.from({length: 32}, () => Math.floor(Math.random()*256).toString(16).padStart(2,'0')).join('');
      const cipher = Array.from({length: 1088}, () => Math.floor(Math.random()*256).toString(16).padStart(2,'0')).join('');
      setSessionKeyHex(secret);
      setCiphertextHex(cipher);
      setKemStatus('completed');
    }, 800);
  };

  const copyPublicKey = (key: KeyResponse) => {
    navigator.clipboard.writeText(key.publicKeyHex);
    setCopiedKeyId(key.keyId);
    setTimeout(() => setCopiedKeyId(null), 2000);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '2rem', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '0.25rem' }}>
          Post-Quantum Cryptographic (PQC) Layer
        </h1>
        <p style={{ color: 'hsl(var(--text-secondary))' }}>
          Manage quantum-resistant keys (FIPS 203/204/205) and simulate hybrid key encapsulations and signatures.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 2fr', gap: '2rem' }}>
        {/* Left column: Key generation form */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <div class="glass-card">
            <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1.2rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Plus size={18} /> Generate PQC Key
            </h3>
            <form onSubmit={handleGenerateKey} style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
              <div>
                <label>Key Alias / ID</label>
                <input type="text" class="glass-input" placeholder="e.g. gateway-sign-key" required value={alias} onChange={e => setAlias(e.target.value)} />
              </div>
              <div>
                <label>PQC Cryptographic Algorithm</label>
                <select class="glass-input" value={algorithm} onChange={e => setAlgorithm(e.target.value as any)}>
                  <option value="ML_DSA_65">ML-DSA-65 (Digital Signatures - Dilithium3)</option>
                  <option value="ML_KEM_768">ML-KEM-768 (Key Encapsulation - Kyber)</option>
                  <option value="SLH_DSA_256">SLH-DSA-SHA2-256f (Stateless Hash Signature)</option>
                </select>
              </div>
              <button type="submit" class="glass-btn glass-btn-primary" disabled={loading}>
                <Cpu size={16} /> {loading ? 'Generating PQC Pair...' : 'Generate Key Pair'}
              </button>
            </form>
          </div>

          {/* Benchmark comparison card */}
          <div class="glass-card">
            <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <ShieldAlert size={16} style={{ color: 'hsl(var(--color-warning))' }} /> FIPS Signature Size Benchmarks
            </h3>
            <p style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))', marginBottom: '1rem' }}>
              Post-quantum cryptography trades off computation speed for larger keys and signatures compared to classical RSA/ECDSA.
            </p>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.8rem' }}>
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.2rem' }}>
                  <span>Classical ECDSA (P-256)</span>
                  <span style={{ fontWeight: 600 }}>64 bytes</span>
                </div>
                <div style={{ height: '6px', background: 'rgba(255,255,255,0.1)', borderRadius: '3px' }}>
                  <div style={{ height: '100%', width: '2%', background: 'hsl(var(--accent-cyan))', borderRadius: '3px' }}></div>
                </div>
              </div>
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.2rem' }}>
                  <span>ML-DSA-65 (NIST FIPS 204)</span>
                  <span style={{ fontWeight: 600 }}>3,293 bytes</span>
                </div>
                <div style={{ height: '6px', background: 'rgba(255,255,255,0.1)', borderRadius: '3px' }}>
                  <div style={{ height: '100%', width: '20%', background: 'hsl(var(--color-success))', borderRadius: '3px' }}></div>
                </div>
              </div>
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.2rem' }}>
                  <span>SLH-DSA-256 (NIST FIPS 205)</span>
                  <span style={{ fontWeight: 600 }}>17,088 bytes</span>
                </div>
                <div style={{ height: '6px', background: 'rgba(255,255,255,0.1)', borderRadius: '3px' }}>
                  <div style={{ height: '100%', width: '100%', background: 'hsl(var(--color-danger))', borderRadius: '3px' }}></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right column: KMS Vault Ledger */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <div class="glass-card" style={{ padding: 0, overflow: 'hidden' }}>
            <div style={{ padding: '1.2rem', borderBottom: '1px solid var(--glass-border)' }}>
              <h3 style={{ fontSize: '1.2rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Lock size={18} /> PQC KMS Key Vault Ledger
              </h3>
            </div>
            {keys.length === 0 ? (
              <div style={{ padding: '3rem', textAlign: 'center', color: 'hsl(var(--text-muted))' }}>
                No post-quantum keys registered. Generate a key pair to begin.
              </div>
            ) : (
              <table class="glass-table">
                <thead>
                  <tr>
                    <th>Key Alias / ID</th>
                    <th>Algorithm</th>
                    <th>Public Key</th>
                    <th style={{ textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {keys.map(key => (
                    <tr key={key.keyId}>
                      <td style={{ fontWeight: 600 }}>{key.keyId}</td>
                      <td>
                        <span style={{
                          padding: '0.2rem 0.5rem',
                          borderRadius: '4px',
                          fontSize: '0.75rem',
                          fontWeight: 600,
                          background: key.algorithm === 'ML_KEM_768' ? 'rgba(6,182,212,0.1)' : 'rgba(16,185,129,0.1)',
                          color: key.algorithm === 'ML_KEM_768' ? 'hsl(var(--accent-cyan))' : 'hsl(var(--color-success))'
                        }}>
                          {key.algorithm}
                        </span>
                      </td>
                      <td>
                        <button class="glass-btn" style={{ padding: '0.2rem 0.4rem', fontSize: '0.75rem' }} onClick={() => copyPublicKey(key)}>
                          {copiedKeyId === key.keyId ? <Check size={12} style={{ color: 'hsl(var(--color-success))' }} /> : <Copy size={12} />}
                          <span style={{ marginLeft: '0.3rem' }}>Copy Hex</span>
                        </button>
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <button class="glass-btn" style={{ color: 'hsl(var(--color-danger))' }} onClick={() => handleDeleteKey(key.keyId)}>
                          <Trash2 size={14} />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {/* Sandbox tabs: Sign Sandbox and Hybrid KEM Simulator */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
            {/* Signing Sandbox */}
            <div class="glass-card">
              <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <FileCheck size={16} /> PQC Proof Signature Sandbox
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div>
                  <label>Signing Key</label>
                  <select class="glass-input" value={selectedSignKey} onChange={e => setSelectedSignKey(e.target.value)}>
                    {keys.filter(k => k.algorithm !== 'ML_KEM_768').map(k => (
                      <option key={k.keyId} value={k.keyId}>{k.keyId} ({k.algorithm})</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label>Proof Payload JSON</label>
                  <textarea class="glass-input" style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', height: '80px' }} value={payloadText} onChange={e => setPayloadText(e.target.value)} />
                </div>
                <button class="glass-btn glass-btn-primary" disabled={signing || !selectedSignKey} onClick={handleSign}>
                  {signing ? 'Signing payload...' : 'Generate PQC Signature'}
                </button>

                {signatureResult && (
                  <div style={{ marginTop: '0.5rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    <div style={{ fontSize: '0.75rem', background: 'rgba(0,0,0,0.3)', padding: '0.5rem', borderRadius: '4px', border: '1px solid var(--glass-border)' }}>
                      <strong style={{ color: 'hsl(var(--color-success))' }}>Signature length:</strong> {signatureResult.length} bytes
                      <div style={{ wordBreak: 'break-all', fontSize: '0.7rem', opacity: 0.6, marginTop: '0.2rem', maxHeight: '60px', overflowY: 'auto' }}>
                        {signatureResult.signatureHex}
                      </div>
                    </div>

                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button class="glass-btn" style={{ flex: 1 }} onClick={() => handleVerify(false)}>Verify Signature</button>
                      <button class="glass-btn" style={{ flex: 1, color: 'hsl(var(--color-warning))' }} onClick={() => handleVerify(true)}>Tamper & Verify</button>
                    </div>

                    {verificationResult !== null && (
                      <div style={{
                        padding: '0.5rem',
                        borderRadius: '4px',
                        fontSize: '0.8rem',
                        fontWeight: 600,
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.4rem',
                        background: verificationResult ? 'rgba(16,185,129,0.1)' : 'rgba(244,63,94,0.1)',
                        color: verificationResult ? 'hsl(var(--color-success))' : 'hsl(var(--color-danger))'
                      }}>
                        {verificationResult ? (
                          <><ShieldCheck size={16} /> Signature Verified successfully (Valid proof!)</>
                        ) : (
                          <><ShieldAlert size={16} /> Verification Failed (TAMPERED payload detected!)</>
                        )}
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>

            {/* Hybrid KEM simulator */}
            <div class="glass-card">
              <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <Lock size={16} /> Kyber-768 Hybrid KEM Simulator
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div>
                  <label>KEM Recipient Key (Kyber)</label>
                  <select class="glass-input" value={kemKeyId} onChange={e => setKemKeyId(e.target.value)}>
                    {keys.filter(k => k.algorithm === 'ML_KEM_768').map(k => (
                      <option key={k.keyId} value={k.keyId}>{k.keyId}</option>
                    ))}
                  </select>
                </div>
                <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>
                  Use ML-KEM-768 key encapsulation to agree on a 256-bit symmetric session key resistant to quantum decryptions.
                </p>
                <button class="glass-btn glass-btn-primary" disabled={kemStatus === 'encapsulating' || !kemKeyId} onClick={handleSimulateKEM}>
                  {kemStatus === 'encapsulating' ? 'Encapsulating key...' : 'Execute Key Encapsulation'}
                </button>

                {kemStatus === 'completed' && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.75rem' }}>
                    <div style={{ background: 'rgba(0,0,0,0.3)', padding: '0.5rem', borderRadius: '4px', border: '1px solid var(--glass-border)' }}>
                      <strong style={{ color: 'hsl(var(--accent-cyan))' }}>Kyber-768 Ciphertext (1088 bytes):</strong>
                      <div style={{ wordBreak: 'break-all', opacity: 0.6, fontSize: '0.7rem', maxHeight: '50px', overflowY: 'auto' }}>
                        {ciphertextHex}
                      </div>
                    </div>
                    <div style={{ background: 'rgba(0,0,0,0.3)', padding: '0.5rem', borderRadius: '4px', border: '1px solid var(--glass-border)' }}>
                      <strong style={{ color: 'hsl(var(--color-success))' }}>Shared Session Key (256-bit):</strong>
                      <div style={{ wordBreak: 'break-all', opacity: 0.6, fontSize: '0.7rem' }}>
                        {sessionKeyHex}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
