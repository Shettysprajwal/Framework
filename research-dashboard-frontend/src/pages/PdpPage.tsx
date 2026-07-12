import React, { useEffect, useState } from 'react';
import { pdpApi } from '../api/pdpApi';
import { policyApi } from '../api/policyApi';
import { PolicyResponse } from '../types/policy';
import { EvaluateResponse, AuditLogResponse } from '../types/pdp';
import { ShieldCheck, ShieldAlert, Sparkles, User, Play, RefreshCw, Key, Database, FileText, CheckCircle } from 'lucide-react';

export const PdpPage: React.FC = () => {
  // Query Form State
  const [subId, setSubId] = useState('analyst');
  const [resId, setResId] = useState('health-records');
  const [actId, setActId] = useState('transfer');
  const [srcCountry, setSrcCountry] = useState('IN');
  const [tgtCountry, setTgtCountry] = useState('EU');
  const [selectedPolicyName, setSelectedPolicyName] = useState('');
  
  const [policies, setPolicies] = useState<PolicyResponse[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLogResponse[]>([]);
  const [evaluation, setEvaluation] = useState<EvaluateResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [fetchingAudits, setFetchingAudits] = useState(false);

  const fetchActivePolicies = () => {
    policyApi.listAll()
      .then((data) => {
        const active = data.filter(p => p.status === 'ACTIVE');
        setPolicies(active);
        if (active.length > 0) {
          setSelectedPolicyName(active[0].name);
        }
      })
      .catch(console.error);
  };

  const fetchAudits = () => {
    setFetchingAudits(true);
    pdpApi.listAudits()
      .then((data) => {
        setAuditLogs(data);
        setFetchingAudits(false);
      })
      .catch((err) => {
        console.error(err);
        setFetchingAudits(false);
      });
  };

  useEffect(() => {
    fetchActivePolicies();
    fetchAudits();
  }, []);

  const handleEvaluate = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    pdpApi.evaluate({
      subjectId: subId,
      resourceId: resId,
      actionId: actId,
      sourceCountry: srcCountry,
      targetCountry: tgtCountry,
      policyName: selectedPolicyName
    })
    .then((res) => {
      setEvaluation(res);
      setLoading(false);
      fetchAudits(); // refresh log ledger
    })
    .catch((err) => {
      console.error(err);
      setLoading(false);
      alert('Evaluation error: ' + err.message);
    });
  };

  const loadExample = (sub: string, res: string, act: string, src: string, tgt: string) => {
    setSubId(sub);
    setResId(res);
    setActId(act);
    setSrcCountry(src);
    setTgtCountry(tgt);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '2rem', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '0.25rem' }}>
          Policy Decision Point (PDP)
        </h1>
        <p style={{ color: 'hsl(var(--text-secondary))' }}>
          Evaluate compliance access requests against active PAP policies using real-time Z3 satisfiability solvers.
        </p>
      </div>

      {/* Examples */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem' }}>
        <div class="glass-card" style={{ cursor: 'pointer', padding: '1rem' }} onClick={() => loadExample('analyst', 'health-records', 'transfer', 'IN', 'EU')}>
          <h4 style={{ color: 'hsl(var(--color-success))', fontSize: '0.85rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Sparkles size={14} /> Compliant Safe Path Check
          </h4>
          <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>IN → EU adequacy transitivity satisfies Article 46 rules (Permit).</p>
        </div>

        <div class="glass-card" style={{ cursor: 'pointer', padding: '1rem' }} onClick={() => loadExample('analyst', 'health-records', 'transfer', 'US', 'IN')}>
          <h4 style={{ color: 'hsl(var(--color-danger))', fontSize: '0.85rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Sparkles size={14} /> Contradictory Violation Check
          </h4>
          <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>US → IN transfer path violates adequacy transitivity (Deny).</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 2fr', gap: '2rem' }}>
        {/* Left column: Request Sandbox */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <div class="glass-card">
            <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1.2rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Key size={18} /> Request Sandbox
            </h3>
            <form onSubmit={handleEvaluate} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label>Subject ID</label>
                <input type="text" class="glass-input" required value={subId} onChange={e => setSubId(e.target.value)} />
              </div>
              <div>
                <label>Resource ID</label>
                <input type="text" class="glass-input" required value={resId} onChange={e => setResId(e.target.value)} />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label>Source Country</label>
                  <input type="text" class="glass-input" required value={srcCountry} onChange={e => setSrcCountry(e.target.value)} />
                </div>
                <div>
                  <label>Target Country</label>
                  <input type="text" class="glass-input" required value={tgtCountry} onChange={e => setTgtCountry(e.target.value)} />
                </div>
              </div>
              <div>
                <label>Select Active Policy Scope</label>
                {policies.length === 0 ? (
                  <p style={{ fontSize: '0.8rem', color: 'hsl(var(--color-warning))' }}>No ACTIVE policies available. Pre-populating Global Privacy Policy fallback.</p>
                ) : (
                  <select class="glass-input" value={selectedPolicyName} onChange={e => setSelectedPolicyName(e.target.value)}>
                    <option value="">All Active Policies</option>
                    {policies.map(p => (
                      <option key={p.id} value={p.name}>{p.name}</option>
                    ))}
                  </select>
                )}
              </div>
              <button type="submit" class="glass-btn glass-btn-primary" disabled={loading}>
                <Play size={16} /> {loading ? 'Running SMT Verification...' : 'Evaluate compliance'}
              </button>
            </form>
          </div>
        </div>

        {/* Right column: Solver trace and results */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          {evaluation ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
              {/* Verdict Card */}
              <div class="glass-card" style={{
                borderLeft: `5px solid ${evaluation.effect === 'PERMIT' ? 'hsl(var(--color-success))' : 'hsl(var(--color-danger))'}`,
                background: evaluation.effect === 'PERMIT' ? 'rgba(16,185,129,0.05)' : 'rgba(244,63,94,0.05)'
              }}>
                <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 700, color: evaluation.effect === 'PERMIT' ? 'hsl(var(--color-success))' : 'hsl(var(--color-danger))' }}>
                  {evaluation.effect === 'PERMIT' ? (
                    <>
                      <ShieldCheck size={20} /> Access Permitted
                    </>
                  ) : (
                    <>
                      <ShieldAlert size={20} /> Access Denied
                    </>
                  )}
                </h4>
                <pre style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))', marginTop: '0.5rem', whiteSpace: 'pre-wrap', fontFamily: 'var(--font-mono)' }}>
                  {evaluation.validationLog}
                </pre>
              </div>

              {/* SMT Formula viewer */}
              <div class="glass-card">
                <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}><FileText size={16} /> Compiled Z3 SMT-LIB2 Spec</h3>
                <pre style={{ background: 'rgba(0,0,0,0.4)', padding: '1rem', borderRadius: '6px', fontSize: '0.75rem', overflowX: 'auto', border: '1px solid var(--glass-border)', color: '#10b981', maxHeight: '350px' }}>
                  {evaluation.proofTrace}
                </pre>
              </div>
            </div>
          ) : (
            <div class="glass-card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '6rem 2rem', color: 'hsl(var(--text-secondary))', borderStyle: 'dashed' }}>
              <Key size={48} style={{ opacity: 0.3, marginBottom: '1rem' }} />
              <h4>Evaluation Verdict Pending</h4>
              <p style={{ fontSize: '0.85rem' }}>Submit the Request Sandbox form on the left to execute Z3 mathematical checks.</p>
            </div>
          )}
        </div>
      </div>

      {/* Audit Logs table */}
      <div class="glass-card" style={{ padding: 0, overflow: 'hidden' }}>
        <div style={{ padding: '1.5rem', borderBottom: '1px solid var(--glass-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem' }}><CheckCircle size={18} /> Compliance Auditing Logs Ledger</h3>
        </div>
        {fetchingAudits ? (
          <div style={{ padding: '2rem', textAlign: 'center', color: 'hsl(var(--text-secondary))' }}>Loading logs...</div>
        ) : auditLogs.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'hsl(var(--text-muted))' }}>No audits logged yet.</div>
        ) : (
          <table class="glass-table">
            <thead>
              <tr>
                <th>Verdict</th>
                <th>Subject / Resource</th>
                <th>Transfer Pathway</th>
                <th>Policy Scope</th>
                <th style={{ textAlign: 'right' }}>Solved At</th>
              </tr>
            </thead>
            <tbody>
              {auditLogs.map((log) => (
                <tr key={log.id}>
                  <td>
                    <span style={{
                      padding: '0.2rem 0.5rem',
                      borderRadius: '4px',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      background: log.effect === 'DENY' ? 'rgba(244,63,94,0.1)' : 'rgba(16,185,129,0.1)',
                      color: log.effect === 'DENY' ? 'hsl(var(--color-danger))' : 'hsl(var(--color-success))'
                    }}>
                      {log.effect}
                    </span>
                  </td>
                  <td>
                    <span style={{ fontWeight: 600 }}>{log.subjectId}</span> → <span style={{ color: 'hsl(var(--text-secondary))' }}>{log.resourceId}</span>
                  </td>
                  <td>
                    <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.8rem', color: 'hsl(var(--accent-cyan))' }}>
                      {log.sourceCountry || 'N/A'} → {log.targetCountry || 'N/A'}
                    </span>
                  </td>
                  <td>
                    <span style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))' }}>
                      {log.policyName || 'All Policies'}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right', fontSize: '0.8rem', color: 'hsl(var(--text-muted))' }}>
                    {new Date(log.solvedAt).toLocaleTimeString()}
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
