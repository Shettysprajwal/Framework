import React, { useEffect, useState } from 'react';
import { regulationsApi } from '../api/regulationsApi';
import { monitorApi } from '../api/monitorApi';
import { ledgerApi } from '../api/ledgerApi';
import { BookOpen, CheckCircle, ShieldCheck, Layers, Award } from 'lucide-react';
import { Link } from 'react-router-dom';

export const HomePage: React.FC = () => {
  const [stats, setStats] = useState({
    totalRegulations: 0,
    activeRegulations: 0,
    totalArticles: 0,
    totalClauses: 0,
    jurisdictionCount: 0,
  });

  const [slaRate, setSlaRate] = useState(100.0);
  const [ledgerValid, setLedgerValid] = useState<boolean | null>(null);

  useEffect(() => {
    regulationsApi.list().then((regs) => {
      const jurisdictions = new Set(regs.map(r => r.primaryJurisdiction));
      let totalArticles = 0;
      let totalClauses = 0;
      regs.forEach(r => {
        totalArticles += r.articles.length;
        r.articles.forEach(a => {
          totalClauses += a.clauses.length;
        });
      });

      setStats({
        totalRegulations: regs.length,
        activeRegulations: regs.filter(r => r.status === 'ACTIVE').length,
        totalArticles,
        totalClauses,
        jurisdictionCount: jurisdictions.size,
      });
    }).catch(console.error);

    monitorApi.getMetrics().then((m) => {
      setSlaRate(m.complianceRate);
    }).catch(console.error);

    ledgerApi.verifyLedger().then((l) => {
      setLedgerValid(l.valid);
    }).catch(console.error);
  }, []);

  const modules = [
    { id: 1, name: 'Regulation Repository', status: 'Completed', color: 'hsl(var(--accent-purple))', path: '/regulations' },
    { id: 2, name: 'Legal Rule Translation', status: 'Completed', color: 'hsl(var(--accent-cyan))', path: '/translate' },
    { id: 3, name: 'Policy Admin Point (PAP)', status: 'Completed', color: 'hsl(var(--color-success))', path: '/policies' },
    { id: 4, name: 'Policy Info Point (PIP)', status: 'Completed', color: 'hsl(var(--color-warning))', path: '/pip' },
    { id: 5, name: 'Policy Decision Point (PDP)', status: 'Completed', color: 'hsl(var(--accent-purple))', path: '/pdp' },
    { id: 6, name: 'PQC Cryptography Layer', status: 'Completed', color: 'hsl(var(--accent-cyan))', path: '/pqc' },
    { id: 7, name: 'ZK Proof Engine', status: 'Completed', color: 'hsl(var(--color-success))', path: '/zkp' },
    { id: 8, name: 'Data Governance Engine', status: 'Completed', color: 'hsl(var(--color-warning))', path: '/governance' },
    { id: 9, name: 'Continuous Monitor', status: 'Completed', color: 'hsl(var(--accent-purple))', path: '/monitor' },
    { id: 10, name: 'Compliance Auditing Ledger', status: 'Completed', color: 'hsl(var(--accent-cyan))', path: '/ledger' }
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '2.2rem', fontWeight: 800, letterSpacing: '-0.03em', marginBottom: '0.5rem' }}>
          Welcome to the <span className="grad-text">PQVCF Platform</span>
        </h1>
        <p style={{ color: 'hsl(var(--text-secondary))', fontSize: '1.05rem', maxWidth: '800px' }}>
          Post-Quantum Verifiable Compliance Framework. Real-time cryptographic compliance verification dashboard.
        </p>
      </div>

      {/* KPI Cards Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.5rem' }}>
        <div className="glass-card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ background: 'rgba(139, 92, 246, 0.15)', color: 'hsl(var(--accent-purple))', padding: '0.75rem', borderRadius: '10px' }}>
            <BookOpen size={24} />
          </div>
          <div>
            <p style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))', textTransform: 'uppercase' }}>Regulations</p>
            <p style={{ fontSize: '1.8rem', fontWeight: 700 }}>{stats.totalRegulations}</p>
          </div>
        </div>

        <div className="glass-card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ background: 'rgba(16, 185, 129, 0.15)', color: 'hsl(var(--color-success))', padding: '0.75rem', borderRadius: '10px' }}>
            <ShieldCheck size={24} />
          </div>
          <div>
            <p style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))', textTransform: 'uppercase' }}>Active SLA Health</p>
            <p style={{ fontSize: '1.8rem', fontWeight: 700, color: slaRate >= 90 ? 'hsl(var(--color-success))' : 'hsl(var(--color-warning))' }}>
              {slaRate.toFixed(1)}%
            </p>
          </div>
        </div>

        <div className="glass-card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ background: 'rgba(6, 182, 212, 0.15)', color: 'hsl(var(--accent-cyan))', padding: '0.75rem', borderRadius: '10px' }}>
            <Layers size={24} />
          </div>
          <div>
            <p style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))', textTransform: 'uppercase' }}>Chain Integrity</p>
            <p style={{ fontSize: '1.8rem', fontWeight: 700, color: ledgerValid ? 'hsl(var(--color-success))' : 'hsl(var(--color-danger))' }}>
              {ledgerValid === null ? 'Checking...' : ledgerValid ? 'VALID' : 'BROKEN'}
            </p>
          </div>
        </div>

        <div className="glass-card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ background: 'rgba(245, 158, 11, 0.15)', color: 'hsl(var(--color-warning))', padding: '0.75rem', borderRadius: '10px' }}>
            <Award size={24} />
          </div>
          <div>
            <p style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))', textTransform: 'uppercase' }}>Total Clauses</p>
            <p style={{ fontSize: '1.8rem', fontWeight: 700 }}>{stats.totalClauses}</p>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.6fr 1fr', gap: '1.5rem' }}>
        {/* Core Research Vision Panel */}
        <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 600 }}>Active Framework Status</h3>
          
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            {modules.map(m => (
              <Link to={m.path} key={m.id} style={{ textDecoration: 'none', color: 'inherit' }}>
                <div style={{
                  background: 'rgba(255,255,255,0.02)',
                  border: '1px solid var(--glass-border)',
                  padding: '0.8rem 1rem',
                  borderRadius: '8px',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  cursor: 'pointer'
                }}>
                  <div style={{ display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: '0.75rem', color: 'hsl(var(--text-muted))', fontWeight: 600 }}>MODULE {m.id}</span>
                    <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>{m.name}</span>
                  </div>
                  <span style={{
                    padding: '0.1rem 0.4rem',
                    borderRadius: '4px',
                    fontSize: '0.7rem',
                    fontWeight: 700,
                    background: 'rgba(16,185,129,0.1)',
                    color: 'hsl(var(--color-success))'
                  }}>
                    {m.status}
                  </span>
                </div>
              </Link>
            ))}
          </div>
        </div>

        {/* System Checklist Panel */}
        <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 600 }}>Verification Engines</h3>
          <ul style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.85rem', listStyle: 'none' }}>
            <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'hsl(var(--color-success))' }}>
              <CheckCircle size={16} /> <span>Z3 SMT Solvers Evaluation (Module 5)</span>
            </li>
            <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'hsl(var(--color-success))' }}>
              <CheckCircle size={16} /> <span>Post-Quantum Signature Schemes (Module 6)</span>
            </li>
            <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'hsl(var(--color-success))' }}>
              <CheckCircle size={16} /> <span>Zero-Knowledge Sigma Provers (Module 7)</span>
            </li>
            <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'hsl(var(--color-success))' }}>
              <CheckCircle size={16} /> <span>Tamper-evident Hash Chains (Module 10)</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
};

