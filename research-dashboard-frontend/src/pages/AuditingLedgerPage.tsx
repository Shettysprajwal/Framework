import React, { useEffect, useState } from 'react';
import { ledgerApi } from '../api/ledgerApi';
import { RecordDto, VerificationDto } from '../types/ledger';
import { ShieldAlert, CheckCircle2, Layers, Key, Edit2 } from 'lucide-react';

export const AuditingLedgerPage: React.FC = () => {
  const [records, setRecords] = useState<RecordDto[]>([]);
  const [verification, setVerification] = useState<VerificationDto | null>(null);
  
  // Seal Command Form
  const [action, setAction] = useState('EVALUATE_PDP');
  const [actor, setActor] = useState('SYSTEM_PDP');
  const [target, setTarget] = useState('Policy-DE-Health-Transfer');
  const [decision, setDecision] = useState('PERMIT');

  // Tamper Sandbox State
  const [tamperIndex, setTamperIndex] = useState(0);
  const [tamperVal, setTamperVal] = useState('DENY');
  
  const [loading, setLoading] = useState(false);

  const fetchRecords = () => {
    ledgerApi.listRecords()
      .then(setRecords)
      .catch(console.error);
  };

  useEffect(() => {
    fetchRecords();
  }, []);

  const handleSeal = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setVerification(null);

    ledgerApi.sealRecord({
      action,
      actor,
      target,
      decision
    })
    .then(() => {
      setLoading(false);
      fetchRecords();
    })
    .catch((err) => {
      console.error(err);
      setLoading(false);
    });
  };

  const handleVerify = () => {
    setLoading(true);
    ledgerApi.verifyLedger()
      .then((res) => {
        setVerification(res);
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setLoading(false);
      });
  };

  const handleTamper = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setVerification(null);

    ledgerApi.tamperRecord(tamperIndex, tamperVal)
      .then(() => {
        setLoading(false);
        fetchRecords();
        alert('Tampering injected successfully! Re-run cryptographic verification to check.');
      })
      .catch((err) => {
        console.error(err);
        setLoading(false);
        alert('Tampering failed: ' + err.message);
      });
  };

  const handleReset = () => {
    if (confirm('Clear all auditing ledger blocks?')) {
      ledgerApi.reset()
        .then(() => {
          setVerification(null);
          fetchRecords();
        })
        .catch(console.error);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '0.25rem' }}>
            Compliance Auditing Ledger
          </h1>
          <p style={{ color: 'hsl(var(--text-secondary))' }}>
            Cryptographically chain compliance decision logs using SHA-256 blocks to establish tamper-evident audit records.
          </p>
        </div>
        <button className="glass-btn" style={{ color: 'hsl(var(--color-danger))' }} onClick={handleReset}>
          Reset Ledger Chain
        </button>
      </div>

      {/* Verification dashboard status banner */}
      <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem', borderLeft: '5px solid hsl(var(--accent-cyan))' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Chain Integrity Verifier</h3>
            <p style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))', marginTop: '0.25rem' }}>
              Run cryptographic proof loops to verify that no historical log entry has been altered or tampered.
            </p>
          </div>
          <button className="glass-btn glass-btn-primary" onClick={handleVerify} disabled={loading}>
            Verify Ledger Chain
          </button>
        </div>

        {verification && (
          <div style={{
            padding: '1rem',
            borderRadius: '4px',
            fontSize: '0.85rem',
            fontWeight: 600,
            display: 'flex',
            alignItems: 'center',
            gap: '0.6rem',
            background: verification.valid ? 'rgba(16,185,129,0.1)' : 'rgba(244,63,94,0.1)',
            color: verification.valid ? 'hsl(var(--color-success))' : 'hsl(var(--color-danger))'
          }}>
            {verification.valid ? (
              <>
                <CheckCircle2 size={20} /> {verification.details}
              </>
            ) : (
              <>
                <ShieldAlert size={20} /> {verification.details}
              </>
            )}
          </div>
        )}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 2fr', gap: '2rem' }}>
        {/* Left: Operations forms */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          {/* Sealer form */}
          <div className="glass-card">
            <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1.2rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <Layers size={18} /> Seal Audit Block
            </h3>
            <form onSubmit={handleSeal} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label>Audit Action</label>
                <input type="text" className="glass-input" required value={action} onChange={e => setAction(e.target.value)} />
              </div>
              <div>
                <label>Actor</label>
                <input type="text" className="glass-input" required value={actor} onChange={e => setActor(e.target.value)} />
              </div>
              <div>
                <label>Target Node</label>
                <input type="text" className="glass-input" required value={target} onChange={e => setTarget(e.target.value)} />
              </div>
              <div>
                <label>Decision Verdict</label>
                <input type="text" className="glass-input" required value={decision} onChange={e => setDecision(e.target.value)} />
              </div>
              <button type="submit" className="glass-btn glass-btn-primary" disabled={loading}>
                Seal Block node
              </button>
            </form>
          </div>

          {/* Tamper simulator */}
          <div className="glass-card" style={{ borderColor: 'rgba(244,63,94,0.2)' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1.2rem', display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'hsl(var(--color-danger))' }}>
              <Edit2 size={18} /> Tamper Sandbox (Simulator)
            </h3>
            <form onSubmit={handleTamper} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label>Block Index to Alter</label>
                <input type="number" className="glass-input" min={0} max={Math.max(0, records.length - 1)} required value={tamperIndex} onChange={e => setTamperIndex(parseInt(e.target.value, 10))} />
              </div>
              <div>
                <label>Injected Decision Value</label>
                <input type="text" className="glass-input" placeholder="e.g. DENY" required value={tamperVal} onChange={e => setTamperVal(e.target.value)} />
              </div>
              <button type="submit" className="glass-btn" style={{ background: 'rgba(244,63,94,0.1)', color: 'hsl(var(--color-danger))', borderColor: 'hsl(var(--color-danger))' }} disabled={loading}>
                Simulate Tamper
              </button>
            </form>
          </div>
        </div>

        {/* Right: Cryptographic Timeline list */}
        <div className="glass-card" style={{ padding: '1.5rem' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Key size={18} /> SHA-256 Ledger Blockchain View
          </h3>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', position: 'relative' }}>
            {records.length === 0 ? (
              <div style={{ padding: '4rem', textAlign: 'center', color: 'hsl(var(--text-muted))' }}>
                Ledger is empty. Seal an audit block to begin the chain.
              </div>
            ) : (
              records.map((r, idx) => (
                <div key={r.id} style={{
                  padding: '1rem',
                  borderRadius: '6px',
                  background: 'rgba(255,255,255,0.03)',
                  border: '1px solid var(--glass-border)',
                  position: 'relative'
                }}>
                  {/* Visual index badge */}
                  <span style={{
                    position: 'absolute',
                    top: '10px',
                    right: '10px',
                    fontSize: '0.7rem',
                    fontWeight: 700,
                    opacity: 0.6
                  }}>
                    #{idx} {idx === 0 ? 'GENESIS' : 'BLOCK'}
                  </span>

                  <h4 style={{ fontSize: '0.9rem', fontWeight: 700, color: 'hsl(var(--accent-cyan))' }}>
                    {r.action}
                  </h4>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.8rem', fontSize: '0.75rem', marginTop: '0.5rem', opacity: 0.9 }}>
                    <div>
                      <span style={{ color: 'hsl(var(--text-secondary))' }}>Actor:</span> {r.actor}
                    </div>
                    <div>
                      <span style={{ color: 'hsl(var(--text-secondary))' }}>Decision:</span> 
                      <span style={{ fontWeight: 700, marginLeft: '0.2rem' }}>{r.decision}</span>
                    </div>
                  </div>

                  {/* Cryptographic Hash timeline bounds */}
                  <div style={{
                    marginTop: '0.8rem',
                    paddingTop: '0.8rem',
                    borderTop: '1px solid var(--glass-border)',
                    fontSize: '0.65rem',
                    fontFamily: 'var(--font-mono)',
                    color: 'hsl(var(--text-muted))',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '0.2rem'
                  }}>
                    <div style={{ textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                      Prev Hash: {r.previousHash}
                    </div>
                    <div style={{ textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap', color: 'hsl(var(--color-success))' }}>
                      Curr Hash: {r.currentHash}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
