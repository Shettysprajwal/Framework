import React, { useEffect, useState } from 'react';
import { regulationsApi } from '../api/regulationsApi';
import { GraphData } from '../types/regulation';
import { Network, Plus, ArrowRight, CheckCircle, ShieldAlert, Globe } from 'lucide-react';

export const GraphExplorerPage: React.FC = () => {
  const [graph, setGraph] = useState<GraphData | null>(null);
  const [source, setSource] = useState('EU');
  const [target, setTarget] = useState('GB');
  
  // Adequacy Checker state
  const [checkSource, setCheckSource] = useState('EU');
  const [checkTarget, setCheckTarget] = useState('IN');
  const [adequacyResult, setAdequacyResult] = useState<boolean | null>(null);
  const [checking, setChecking] = useState(false);

  const fetchGraph = () => {
    regulationsApi.getGraph()
      .then(setGraph)
      .catch(console.error);
  };

  useEffect(() => {
    fetchGraph();
  }, []);

  const handleDeclareAdequacy = (e: React.FormEvent) => {
    e.preventDefault();
    regulationsApi.declareAdequacy(source, target)
      .then(() => {
        alert(`Adequacy declared: ${source} recognizes ${target}`);
        setSource('');
        setTarget('');
        fetchGraph();
      })
      .catch(err => alert(err.message));
  };

  const handleCheckAdequacy = (e: React.FormEvent) => {
    e.preventDefault();
    setChecking(true);
    regulationsApi.checkAdequacy(checkSource, checkTarget)
      .then((res) => {
        setAdequacyResult(res.isAdequate);
        setChecking(false);
      })
      .catch((err) => {
        console.error(err);
        setChecking(false);
      });
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '2rem', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '0.25rem' }}>
          Jurisdiction Adequacy Graph
        </h1>
        <p style={{ color: 'hsl(var(--text-secondary))' }}>
          Model and trace structural cross-border data transfer relationships and adequacy decisions.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1.2fr', gap: '2rem' }}>
        {/* Left Panel: Graph visualization catalog */}
        <div class="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Network size={18} /> Active Graph Nodes & Relationships
          </h3>

          {!graph || graph.nodes.length === 0 ? (
            <p style={{ color: 'hsl(var(--text-secondary))' }}>No nodes registered. Seed data or declare adequacy agreements first.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              <div>
                <h4 style={{ color: 'hsl(var(--accent-cyan))', fontSize: '0.9rem', marginBottom: '0.5rem' }}>Sovereignty Nodes</h4>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem' }}>
                  {graph.nodes.map(node => (
                    <div
                      key={node.id}
                      style={{
                        padding: '0.75rem 1rem',
                        background: 'rgba(255,255,255,0.03)',
                        border: '1px solid var(--glass-border)',
                        borderRadius: '8px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.5rem',
                        fontSize: '0.85rem'
                      }}
                    >
                      <Globe size={14} color="hsl(var(--accent-cyan))" />
                      <span style={{ fontWeight: 600 }}>
                        {node.properties.code || node.properties.shortName || node.properties.articleNumber || 'Unknown'}
                      </span>
                      <span style={{ fontSize: '0.7rem', color: 'hsl(var(--text-muted))' }}>
                        ({node.labels.join(', ')})
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              <div>
                <h4 style={{ color: 'hsl(var(--accent-purple))', fontSize: '0.9rem', marginBottom: '0.5rem' }}>Adequacy & Structural Edges</h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  {graph.edges.map(edge => (
                    <div
                      key={edge.id}
                      style={{
                        padding: '0.75rem 1rem',
                        background: 'rgba(0,0,0,0.2)',
                        border: '1px solid var(--glass-border)',
                        borderRadius: '6px',
                        fontSize: '0.85rem',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between'
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        <span style={{ fontFamily: 'var(--font-mono)' }}>{edge.source}</span>
                        <ArrowRight size={14} color="hsl(var(--text-muted))" />
                        <span style={{ fontFamily: 'var(--font-mono)' }}>{edge.target}</span>
                      </div>
                      <span style={{
                        fontSize: '0.75rem',
                        fontWeight: 600,
                        color: 'hsl(var(--accent-purple))',
                        background: 'rgba(139, 92, 246, 0.1)',
                        padding: '0.2rem 0.5rem',
                        borderRadius: '4px'
                      }}>
                        {edge.type}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Right Panel: Operations Forms */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          {/* Form 1: Declare Adequacy Decision */}
          <div class="glass-card">
            <h3 style={{ fontSize: '1.25rem', fontWeight: 600, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Plus size={18} /> Declare Adequacy
            </h3>
            <form onSubmit={handleDeclareAdequacy} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label>Recognizing Jurisdiction (Source)</label>
                <input type="text" class="glass-input" required value={source} onChange={e => setSource(e.target.value)} placeholder="e.g. EU" />
              </div>
              <div>
                <label>Deemed Adequate Jurisdiction (Target)</label>
                <input type="text" class="glass-input" required value={target} onChange={e => setTarget(e.target.value)} placeholder="e.g. CH" />
              </div>
              <button type="submit" class="glass-btn glass-btn-primary">
                Establish Adequacy Edge
              </button>
            </form>
          </div>

          {/* Form 2: Transitive Adequacy Solver */}
          <div class="glass-card">
            <h3 style={{ fontSize: '1.25rem', fontWeight: 600, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              Transitive Path Check
            </h3>
            <form onSubmit={handleCheckAdequacy} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label>Origin Jurisdiction</label>
                <input type="text" class="glass-input" required value={checkSource} onChange={e => setCheckSource(e.target.value)} />
              </div>
              <div>
                <label>Destination Jurisdiction</label>
                <input type="text" class="glass-input" required value={checkTarget} onChange={e => setCheckTarget(e.target.value)} />
              </div>
              <button type="submit" class="glass-btn" disabled={checking}>
                {checking ? 'Solving...' : 'Verify Path'}
              </button>
            </form>

            {adequacyResult !== null && (
              <div style={{
                marginTop: '1.2rem',
                padding: '1rem',
                borderRadius: '8px',
                display: 'flex',
                alignItems: 'center',
                gap: '0.75rem',
                border: '1px solid',
                background: adequacyResult ? 'rgba(16,185,129,0.1)' : 'rgba(244,63,94,0.1)',
                borderColor: adequacyResult ? 'rgba(16,185,129,0.3)' : 'rgba(244,63,94,0.3)',
                color: adequacyResult ? 'hsl(var(--color-success))' : 'hsl(var(--color-danger))'
              }}>
                {adequacyResult ? (
                  <>
                    <CheckCircle size={20} />
                    <div>
                      <p style={{ fontWeight: 600, fontSize: '0.9rem' }}>Legally Adequate</p>
                      <p style={{ fontSize: '0.75rem', opacity: 0.8 }}>Transitive data transfer path verified under target rules.</p>
                    </div>
                  </>
                ) : (
                  <>
                    <ShieldAlert size={20} />
                    <div>
                      <p style={{ fontWeight: 600, fontSize: '0.9rem' }}>Adequacy Denied</p>
                      <p style={{ fontSize: '0.75rem', opacity: 0.8 }}>No valid path / direct adequacy agreement found.</p>
                    </div>
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
