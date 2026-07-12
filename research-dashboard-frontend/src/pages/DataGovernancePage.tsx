import React, { useEffect, useState } from 'react';
import { governanceApi } from '../api/governanceApi';
import { FlowDecisionResponse } from '../types/governance';
import { Globe, ShieldAlert, Sparkles, Map, Play, Trash2, Database, BookOpen, AlertTriangle, CheckCircle } from 'lucide-react';

export const DataGovernancePage: React.FC = () => {
  const [decisions, setDecisions] = useState<FlowDecisionResponse[]>([]);
  const [sourceCountry, setSourceCountry] = useState('DE');
  const [targetCountry, setTargetCountry] = useState('IN');
  const [dataCategory, setDataCategory] = useState('PERSONAL');
  const [processingPurpose, setProcessingPurpose] = useState('PROCESSING');
  const [loading, setLoading] = useState(false);
  const [lastDecision, setLastDecision] = useState<FlowDecisionResponse | null>(null);

  const fetchDecisions = () => {
    governanceApi.listDecisions()
      .then(setDecisions)
      .catch(console.error);
  };

  useEffect(() => {
    fetchDecisions();
  }, []);

  const handleEvaluate = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    governanceApi.evaluateFlow({
      sourceCountry,
      targetCountry,
      dataCategory,
      processingPurpose
    })
    .then((res) => {
      setLastDecision(res);
      setLoading(false);
      fetchDecisions();
    })
    .catch((err) => {
      console.error(err);
      setLoading(false);
      alert('Evaluation failed: ' + err.message);
    });
  };

  const handleDelete = (id: string) => {
    if (confirm('Delete evaluation record ' + id + ' from ledger?')) {
      governanceApi.deleteDecision(id)
        .then(() => {
          if (lastDecision?.decisionId === id) {
            setLastDecision(null);
          }
          fetchDecisions();
        })
        .catch(console.error);
    }
  };

  const loadExample = (src: string, tgt: string, cat: string, purp: string) => {
    setSourceCountry(src);
    setTargetCountry(tgt);
    setDataCategory(cat);
    setProcessingPurpose(purp);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '2rem', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '0.25rem' }}>
          Data Governance Engine
        </h1>
        <p style={{ color: 'hsl(var(--text-secondary))' }}>
          Assess transfer pathways legality and localization constraints across global jurisdictions in real-time.
        </p>
      </div>

      {/* Preset scenarios */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem' }}>
        <div className="glass-card" style={{ cursor: 'pointer', padding: '1rem' }} onClick={() => loadExample('DE', 'IN', 'PERSONAL', 'PROCESSING')}>
          <h4 style={{ color: 'hsl(var(--color-success))', fontSize: '0.85rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Globe size={14} /> EU to India Adequacy Transfer
          </h4>
          <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>DE (Germany) whitelists IN (India) under adequacy parameters (Approved).</p>
        </div>

        <div className="glass-card" style={{ cursor: 'pointer', padding: '1rem' }} onClick={() => loadExample('RU', 'DE', 'PERSONAL', 'BACKUP')}>
          <h4 style={{ color: 'hsl(var(--color-danger))', fontSize: '0.85rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <AlertTriangle size={14} /> Russian Local Residency Block
          </h4>
          <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>RU mandates localized database storage for all personal records (Blocked).</p>
        </div>

        <div className="glass-card" style={{ cursor: 'pointer', padding: '1rem' }} onClick={() => loadExample('US', 'IN', 'HEALTH', 'RESEARCH')}>
          <h4 style={{ color: 'hsl(var(--color-warning))', fontSize: '0.85rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Sparkles size={14} /> US to India Health Safeguards
          </h4>
          <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>Requires explicit HIPAA BAA or SCCs verification safeguards (Conditional).</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 2fr', gap: '2rem' }}>
        {/* Left column: Evaluate planner */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <div className="glass-card">
            <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1.2rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Map size={18} /> Cross-Border Transfer Planner
            </h3>
            <form onSubmit={handleEvaluate} style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label>Source Country Code</label>
                  <input type="text" className="glass-input" placeholder="e.g. DE" required value={sourceCountry} onChange={e => setSourceCountry(e.target.value.toUpperCase())} />
                </div>
                <div>
                  <label>Destination Country Code</label>
                  <input type="text" className="glass-input" placeholder="e.g. IN" required value={targetCountry} onChange={e => setTargetCountry(e.target.value.toUpperCase())} />
                </div>
              </div>
              <div>
                <label>Data Category</label>
                <select className="glass-input" value={dataCategory} onChange={e => setDataCategory(e.target.value)}>
                  <option value="PERSONAL">Personal Data (PII)</option>
                  <option value="HEALTH">Protected Health Info (PHI)</option>
                  <option value="FINANCIAL">Financial / Bank records</option>
                  <option value="CRITICAL">Critical National Infrastructure data</option>
                </select>
              </div>
              <div>
                <label>Processing Purpose</label>
                <select className="glass-input" value={processingPurpose} onChange={e => setProcessingPurpose(e.target.value)}>
                  <option value="PROCESSING">Standard Data Processing</option>
                  <option value="BACKUP">Cloud Archiving / Backup</option>
                  <option value="MARKETING">Targeted Profiling & Marketing</option>
                </select>
              </div>
              <button type="submit" className="glass-btn glass-btn-primary" disabled={loading}>
                <Play size={16} /> {loading ? 'Evaluating Legality...' : 'Run Transfer Assessment'}
              </button>
            </form>
          </div>
        </div>

        {/* Right column: Decision outcomes and flows */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          {lastDecision ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              {/* Verdict Card */}
              <div className="glass-card" style={{
                borderLeft: `5px solid ${
                  lastDecision.decision === 'APPROVED' ? 'hsl(var(--color-success))' :
                  lastDecision.decision === 'BLOCKED' ? 'hsl(var(--color-danger))' : 'hsl(var(--color-warning))'
                }`,
                background: 
                  lastDecision.decision === 'APPROVED' ? 'rgba(16,185,129,0.05)' :
                  lastDecision.decision === 'BLOCKED' ? 'rgba(244,63,94,0.05)' : 'rgba(245,158,11,0.05)'
              }}>
                <h4 style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  fontWeight: 700,
                  color: 
                    lastDecision.decision === 'APPROVED' ? 'hsl(var(--color-success))' :
                    lastDecision.decision === 'BLOCKED' ? 'hsl(var(--color-danger))' : 'hsl(var(--color-warning))'
                }}>
                  {lastDecision.decision === 'APPROVED' ? (
                    <><CheckCircle size={20} /> Transfer Legally Approved</>
                  ) : lastDecision.decision === 'BLOCKED' ? (
                    <><ShieldAlert size={20} /> Transfer Legally Blocked</>
                  ) : (
                    <><AlertTriangle size={20} /> Conditional Transfer Safeguards Mandatory</>
                  )}
                </h4>
                <p style={{ fontSize: '0.85rem', color: 'hsl(var(--text-secondary))', marginTop: '0.5rem' }}>
                  {lastDecision.reasoning}
                </p>
                <div style={{ marginTop: '1rem', display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                  <span style={{ fontSize: '0.75rem', fontWeight: 600 }}>Governing Citations:</span>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                    {lastDecision.citations.map((c, idx) => (
                      <span key={idx} style={{
                        padding: '0.1rem 0.4rem',
                        background: 'rgba(255,255,255,0.08)',
                        borderRadius: '3px',
                        fontSize: '0.7rem',
                        border: '1px solid var(--glass-border)',
                        color: 'hsl(var(--accent-cyan))'
                      }}>
                        {c}
                      </span>
                    ))}
                  </div>
                </div>
              </div>

              {/* Pathway Flowchart representation */}
              <div className="glass-card">
                <h4 style={{ fontSize: '0.9rem', fontWeight: 600, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}><BookOpen size={16} /> Evaluation Decision Path</h4>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', fontFamily: 'var(--font-mono)' }}>
                  <div style={{ padding: '0.4rem', background: 'rgba(255,255,255,0.05)', borderRadius: '4px', border: '1px solid var(--glass-border)' }}>
                    Geo: {lastDecision.sourceCountry} → {lastDecision.targetCountry}
                  </div>
                  <span>➜</span>
                  <div style={{
                    padding: '0.4rem',
                    background: lastDecision.decision === 'BLOCKED' ? 'rgba(244,63,94,0.1)' : 'rgba(255,255,255,0.05)',
                    borderRadius: '4px',
                    border: '1px solid var(--glass-border)'
                  }}>
                    Residency check
                  </div>
                  <span>➜</span>
                  <div style={{
                    padding: '0.4rem',
                    background: lastDecision.decision === 'APPROVED' ? 'rgba(16,185,129,0.1)' : 'rgba(255,255,255,0.05)',
                    borderRadius: '4px',
                    border: '1px solid var(--glass-border)'
                  }}>
                    Adequacy check
                  </div>
                  <span>➜</span>
                  <div style={{
                    padding: '0.4rem',
                    background: lastDecision.decision === 'CONDITIONAL' ? 'rgba(245,158,11,0.1)' : 'rgba(255,255,255,0.05)',
                    borderRadius: '4px',
                    border: '1px solid var(--glass-border)'
                  }}>
                    safeguards proofs
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '6rem 2rem', color: 'hsl(var(--text-secondary))', borderStyle: 'dashed' }}>
              <Globe size={48} style={{ opacity: 0.3, marginBottom: '1rem' }} />
              <h4>Evaluation Verdict Pending</h4>
              <p style={{ fontSize: '0.85rem' }}>Select pathway countries and data categories to evaluate legality.</p>
            </div>
          )}
        </div>
      </div>

      {/* ledger database */}
      <div className="glass-card" style={{ padding: 0, overflow: 'hidden' }}>
        <div style={{ padding: '1.2rem', borderBottom: '1px solid var(--glass-border)' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Database size={18} /> Cross-Border Governance Auditing Ledger
          </h3>
        </div>
        {decisions.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'hsl(var(--text-muted))' }}>
            No decisions evaluated. Run an assessment to log audit trails.
          </div>
        ) : (
          <table className="glass-table">
            <thead>
              <tr>
                <th>Verdict</th>
                <th>Transfer Path</th>
                <th>Data Category</th>
                <th>Reasoning Summary</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {decisions.map(d => (
                <tr key={d.decisionId} style={{ cursor: 'pointer' }} onClick={() => setLastDecision(d)}>
                  <td>
                    <span style={{
                      padding: '0.2rem 0.5rem',
                      borderRadius: '4px',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      background: 
                        d.decision === 'APPROVED' ? 'rgba(16,185,129,0.1)' :
                        d.decision === 'BLOCKED' ? 'rgba(244,63,94,0.1)' : 'rgba(245,158,11,0.1)',
                      color: 
                        d.decision === 'APPROVED' ? 'hsl(var(--color-success))' :
                        d.decision === 'BLOCKED' ? 'hsl(var(--color-danger))' : 'hsl(var(--color-warning))'
                    }}>
                      {d.decision}
                    </span>
                  </td>
                  <td>
                    <span style={{ fontWeight: 600 }}>{d.sourceCountry}</span> → <span style={{ color: 'hsl(var(--text-secondary))' }}>{d.targetCountry}</span>
                  </td>
                  <td>
                    <span style={{ fontSize: '0.8rem', opacity: 0.8 }}>{d.dataCategory}</span>
                  </td>
                  <td style={{ fontSize: '0.8rem', opacity: 0.7, maxWidth: '280px', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                    {d.reasoning}
                  </td>
                  <td style={{ textAlign: 'right' }} onClick={e => e.stopPropagation()}>
                    <button className="glass-btn" style={{ color: 'hsl(var(--color-danger))' }} onClick={() => handleDelete(d.decisionId)}>
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
