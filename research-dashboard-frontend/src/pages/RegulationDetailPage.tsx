import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { regulationsApi } from '../api/regulationsApi';
import { RegulationResponse } from '../types/regulation';
import { ArrowLeft, Play, ShieldAlert, Save, Code, PlusCircle, CheckCircle, Database } from 'lucide-react';

export const RegulationDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [regulation, setRegulation] = useState<RegulationResponse | null>(null);
  const [formalSpec, setFormalSpec] = useState('');
  const [loading, setLoading] = useState(true);
  const [savingSpec, setSavingSpec] = useState(false);
  
  // Article form state
  const [articleNumber, setArticleNumber] = useState('');
  const [articleTitle, setArticleTitle] = useState('');
  const [articleContent, setArticleContent] = useState('');
  const [deonticFormula, setDeonticFormula] = useState('');
  const [odrlPolicy, setOdrlPolicy] = useState('');
  const [addingArticle, setAddingArticle] = useState(false);

  const fetchDetails = () => {
    if (!id) return;
    setLoading(true);
    regulationsApi.getById(id)
      .then((data) => {
        setRegulation(data);
        setFormalSpec(data.formalSpec || '');
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setLoading(false);
      });
  };

  useEffect(() => {
    fetchDetails();
  }, [id]);

  const handleSaveFormalSpec = () => {
    if (!regulation) return;
    setSavingSpec(true);
    regulationsApi.updateFormalSpec(regulation.id, formalSpec)
      .then((updated) => {
        setRegulation(updated);
        setSavingSpec(false);
        alert('Formal SMT Specification updated successfully.');
      })
      .catch((err) => {
        console.error(err);
        setSavingSpec(false);
        alert('Failed to update formal spec: ' + err.message);
      });
  };

  const handleActivate = () => {
    if (!regulation) return;
    regulationsApi.activate(regulation.id)
      .then(setRegulation)
      .catch(err => alert(err.message));
  };

  const handleDeprecate = () => {
    if (!regulation) return;
    regulationsApi.deprecate(regulation.id)
      .then(setRegulation)
      .catch(err => alert(err.message));
  };

  const handleAddArticle = (e: React.FormEvent) => {
    e.preventDefault();
    if (!regulation) return;
    setAddingArticle(true);
    regulationsApi.addArticle({
      regulationId: regulation.id,
      articleNumber,
      title: articleTitle,
      content: articleContent,
      deonticFormula,
      odrlPolicy,
    })
    .then(() => {
      setAddingArticle(false);
      setArticleNumber('');
      setArticleTitle('');
      setArticleContent('');
      setDeonticFormula('');
      setOdrlPolicy('');
      fetchDetails(); // Reload regulation structures
    })
    .catch((err) => {
      console.error(err);
      setAddingArticle(false);
      alert('Failed to append article: ' + err.message);
    });
  };

  if (loading) return <div style={{ color: 'hsl(var(--text-secondary))' }}>Loading regulation details...</div>;
  if (!regulation) return <div style={{ color: 'hsl(var(--color-danger))' }}>Regulation not found.</div>;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Detail Header navigation */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <button onClick={() => navigate('/regulations')} class="glass-btn" style={{ padding: '0.5rem' }}>
          <ArrowLeft size={16} />
        </button>
        <div>
          <span style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))', textTransform: 'uppercase' }}>
            Regulation Scope ID: {regulation.id}
          </span>
          <h1 style={{ fontSize: '1.8rem', fontWeight: 800 }}>
            {regulation.name} (<span style={{ color: 'hsl(var(--accent-cyan))' }}>{regulation.shortName}</span>)
          </h1>
        </div>
      </div>

      {/* Primary Actions bar */}
      <div style={{ display: 'flex', gap: '1rem', borderBottom: '1px solid var(--glass-border)', paddingBottom: '1rem' }}>
        <span class={`badge badge-${regulation.status.toLowerCase()}`} style={{ fontSize: '0.9rem', padding: '0.4rem 1rem' }}>
          Status: {regulation.status}
        </span>
        {regulation.status === 'DRAFT' && (
          <button onClick={handleActivate} class="glass-btn" style={{ borderColor: 'hsl(var(--color-success))', color: 'hsl(var(--color-success))' }}>
            <Play size={14} /> Activate Policy
          </button>
        )}
        {regulation.status === 'ACTIVE' && (
          <button onClick={handleDeprecate} class="glass-btn" style={{ borderColor: 'hsl(var(--color-danger))', color: 'hsl(var(--color-danger))' }}>
            <ShieldAlert size={14} /> Deprecate Policy
          </button>
        )}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
        {/* Left Side: Articles Accordion & Appending Form */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <div class="glass-card">
            <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Database size={18} /> Cataloged Articles ({regulation.articles.length})
            </h3>
            {regulation.articles.length === 0 ? (
              <p style={{ color: 'hsl(var(--text-secondary))' }}>No articles recorded yet.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {regulation.articles.map((art) => (
                  <div key={art.id} style={{ border: '1px solid var(--glass-border)', borderRadius: '8px', padding: '1rem', background: 'rgba(255,255,255,0.01)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                      <span style={{ fontWeight: 600, color: 'hsl(var(--accent-cyan))' }}>{art.articleNumber}</span>
                      <span style={{ fontSize: '0.85rem', fontWeight: 500 }}>{art.title}</span>
                    </div>
                    <p style={{ fontSize: '0.9rem', color: 'hsl(var(--text-secondary))', marginBottom: '0.75rem' }}>
                      {art.content}
                    </p>
                    {art.deonticFormula && (
                      <div style={{ margin: '0.5rem 0', padding: '0.5rem', background: 'rgba(0,0,0,0.3)', borderRadius: '4px', borderLeft: '3px solid hsl(var(--accent-purple))' }}>
                        <code style={{ fontSize: '0.75rem', color: 'hsl(var(--text-primary))' }}>
                          Deontic logic: {art.deonticFormula}
                        </code>
                      </div>
                    )}
                    {art.clauses.length > 0 && (
                      <div style={{ marginTop: '0.5rem', display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                        {art.clauses.map(cls => (
                          <div key={cls.id} style={{ display: 'flex', gap: '0.5rem', fontSize: '0.8rem', background: 'rgba(255,255,255,0.02)', padding: '0.3rem 0.5rem', borderRadius: '4px' }}>
                            <span style={{ fontFamily: 'var(--font-mono)', color: 'hsl(var(--accent-cyan))', fontWeight: 600 }}>
                              {cls.clauseNumber}
                            </span>
                            <span style={{ color: 'hsl(var(--text-secondary))', flex: 1 }}>{cls.content}</span>
                            <span style={{ color: cls.clauseType === 'PROHIBITION' ? 'hsl(var(--color-danger))' : 'hsl(var(--color-success))', fontWeight: 600 }}>
                              {cls.clauseType}
                            </span>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Form to Append New Article */}
          <div class="glass-card">
            <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <PlusCircle size={18} /> Append New Article
            </h3>
            <form onSubmit={handleAddArticle} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label>Article Identifier (e.g. Article 47)</label>
                <input type="text" class="glass-input" required value={articleNumber} onChange={e => setArticleNumber(e.target.value)} placeholder="Article 47" />
              </div>
              <div>
                <label>Title</label>
                <input type="text" class="glass-input" required value={articleTitle} onChange={e => setArticleTitle(e.target.value)} placeholder="General conditions" />
              </div>
              <div>
                <label>Full Statutory Text</label>
                <textarea class="glass-input" style={{ minHeight: '80px' }} required value={articleContent} onChange={e => setArticleContent(e.target.value)} placeholder="Insert legal description..."></textarea>
              </div>
              <div>
                <label>Deontic Logic Formula (Z3 solver axiom)</label>
                <input type="text" class="glass-input" value={deonticFormula} onChange={e => setDeonticFormula(e.target.value)} placeholder="(assert (= destination_country IN))" />
              </div>
              <div>
                <label>ODRL Policy Representation (JSON)</label>
                <textarea class="glass-input" style={{ minHeight: '60px', fontFamily: 'var(--font-mono)' }} value={odrlPolicy} onChange={e => setOdrlPolicy(e.target.value)} placeholder='{"@type": "Policy"}'></textarea>
              </div>
              <button type="submit" class="glass-btn glass-btn-primary" disabled={addingArticle}>
                {addingArticle ? 'Appending...' : 'Append Article'}
              </button>
            </form>
          </div>
        </div>

        {/* Right Side: SMT formal specification editor */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <div class="glass-card" style={{ display: 'flex', flexDirection: 'column', flex: 1, minHeight: '400px' }}>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Code size={18} /> SMT-LIB2 Verification Model
            </h3>
            <p style={{ color: 'hsl(var(--text-secondary))', fontSize: '0.85rem', marginBottom: '1rem' }}>
              This defines the mathematical axioms used by the Z3 Constraint Solver (Module 4) to verify cross-border data movements under this regulation.
            </p>
            <div style={{ flex: 1, marginBottom: '1rem', position: 'relative' }}>
              <textarea
                class="glass-input"
                style={{
                  height: '100%',
                  minHeight: '280px',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.85rem',
                  lineHeight: '1.5',
                  background: 'rgba(0,0,0,0.4)',
                  border: '1px solid var(--glass-border)',
                  color: '#34d399'
                }}
                value={formalSpec}
                onChange={(e) => setFormalSpec(e.target.value)}
                placeholder="; Write Z3 axioms here..."
              ></textarea>
            </div>
            <button
              onClick={handleSaveFormalSpec}
              class="glass-btn glass-btn-primary"
              disabled={savingSpec}
              style={{ alignSelf: 'flex-start' }}
            >
              <Save size={16} /> {savingSpec ? 'Saving...' : 'Save Axioms'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
