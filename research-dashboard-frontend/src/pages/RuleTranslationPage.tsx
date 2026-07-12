import React, { useEffect, useState } from 'react';
import { translationApi, TranslationResponse } from '../api/translationApi';
import { FilePlus, Code, ShieldCheck, ShieldAlert, Trash2, Copy, Play, Sparkles, BookOpen } from 'lucide-react';

export const RuleTranslationPage: React.FC = () => {
  const [regulation, setRegulation] = useState('GDPR');
  const [article, setArticle] = useState('Article 46');
  const [clause, setClause] = useState('1');
  const [rawText, setRawText] = useState('a controller is permitted to transfer personal data if appropriate safeguards are implemented');
  
  const [rules, setRules] = useState<TranslationResponse[]>([]);
  const [currentResult, setCurrentResult] = useState<TranslationResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(false);

  const fetchRules = () => {
    setFetching(true);
    translationApi.listAll()
      .then((data) => {
        setRules(data);
        setFetching(false);
      })
      .catch((err) => {
        console.error(err);
        setFetching(false);
      });
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const handleTranslate = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    translationApi.translate({
      regulationShortName: regulation.toUpperCase(),
      articleNumber: article,
      clauseNumber: clause,
      rawSourceText: rawText,
    })
    .then((data) => {
      setCurrentResult(data);
      setLoading(false);
      fetchRules(); // reload list
    })
    .catch((err) => {
      console.error(err);
      setLoading(false);
      alert('Translation failed: ' + err.message);
    });
  };

  const handleDelete = (id: string) => {
    if (confirm('Delete this rule translation?')) {
      translationApi.delete(id)
        .then(() => {
          if (currentResult?.id === id) {
            setCurrentResult(null);
          }
          fetchRules();
        })
        .catch(err => alert(err.message));
    }
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    alert('Copied to clipboard!');
  };

  const loadExample = (exText: string, reg: string, art: string, cls: string) => {
    setRawText(exText);
    setRegulation(reg);
    setArticle(art);
    setClause(cls);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '2rem', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '0.25rem' }}>
          Deontic Translation Engine
        </h1>
        <p style={{ color: 'hsl(var(--text-secondary))' }}>
          Translate Controlled Natural Language (CNL) policy statutes into verifiable deontic logic, SMT-LIB2 models, and ODRL policy JSONs.
        </p>
      </div>

      {/* Examples Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem' }}>
        <div class="glass-card" style={{ cursor: 'pointer', padding: '1rem' }} onClick={() => loadExample('a controller is permitted to transfer personal data if appropriate safeguards are implemented', 'GDPR', 'Article 46', '1')}>
          <h4 style={{ color: 'hsl(var(--accent-cyan))', fontSize: '0.85rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Sparkles size={14} /> GDPR Art 46 Safeguards
          </h4>
          <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>"a controller is permitted to transfer personal data if appropriate safeguards are implemented"</p>
        </div>

        <div class="glass-card" style={{ cursor: 'pointer', padding: '1rem' }} onClick={() => loadExample('a controller is forbidden to transfer personal data', 'GDPR', 'Article 44', '1')}>
          <h4 style={{ color: 'hsl(var(--color-danger))', fontSize: '0.85rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Sparkles size={14} /> GDPR Art 44 Restriction
          </h4>
          <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>"a controller is forbidden to transfer personal data"</p>
        </div>

        <div class="glass-card" style={{ cursor: 'pointer', padding: '1rem' }} onClick={() => loadExample('a fiduciary is obliged to restrict data processing if consent is withdrawn', 'DPDP', 'Section 16', '1')}>
          <h4 style={{ color: 'hsl(var(--color-warning))', fontSize: '0.85rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Sparkles size={14} /> India DPDP Obligation
          </h4>
          <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>"a fiduciary is obliged to restrict data processing if consent is withdrawn"</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 2fr', gap: '2rem' }}>
        {/* Left Side: Translation Form */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <div class="glass-card">
            <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1.2rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <FilePlus size={18} /> Translate Statement
            </h3>
            <form onSubmit={handleTranslate} style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
              <div>
                <label>Regulation Acronym</label>
                <input type="text" class="glass-input" required value={regulation} onChange={e => setRegulation(e.target.value)} />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label>Article Reference</label>
                  <input type="text" class="glass-input" required value={article} onChange={e => setArticle(e.target.value)} />
                </div>
                <div>
                  <label>Clause Reference</label>
                  <input type="text" class="glass-input" value={clause} onChange={e => setClause(e.target.value)} />
                </div>
              </div>
              <div>
                <label>CNL Statement (Natural Language)</label>
                <textarea
                  class="glass-input"
                  style={{ minHeight: '100px' }}
                  required
                  value={rawText}
                  onChange={e => setRawText(e.target.value)}
                  placeholder="e.g. a controller is permitted to transfer data if appropriate safeguards are implemented"
                ></textarea>
              </div>
              <button type="submit" class="glass-btn glass-btn-primary" disabled={loading}>
                <Play size={16} /> {loading ? 'Processing NLP...' : 'Run Translation'}
              </button>
            </form>
          </div>

          {/* Validation Result Box */}
          {currentResult && (
            <div class="glass-card" style={{
              borderLeft: `5px solid ${currentResult.valid ? 'hsl(var(--color-success))' : 'hsl(var(--color-danger))'}`,
              background: currentResult.valid ? 'rgba(16,185,129,0.05)' : 'rgba(244,63,94,0.05)'
            }}>
              <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 700, color: currentResult.valid ? 'hsl(var(--color-success))' : 'hsl(var(--color-danger))' }}>
                {currentResult.valid ? (
                  <>
                    <ShieldCheck size={20} /> Consistency Verified
                  </>
                ) : (
                  <>
                    <ShieldAlert size={20} /> Logical Conflict Found
                  </>
                )}
              </h4>
              <p style={{ fontSize: '0.85rem', color: 'hsl(var(--text-secondary))', marginTop: '0.5rem' }}>
                {currentResult.validationMessage}
              </p>
            </div>
          )}
        </div>

        {/* Right Side: Translation Outputs */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          {currentResult ? (
            <div class="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              <div>
                <h3 style={{ fontSize: '1.2rem', fontWeight: 600 }}>Deontic Logic AST Extraction</h3>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: '1rem', marginTop: '1rem' }}>
                  <div style={{ background: 'rgba(255,255,255,0.02)', padding: '0.75rem', borderRadius: '6px', border: '1px solid var(--glass-border)' }}>
                    <span style={{ fontSize: '0.7rem', color: 'hsl(var(--text-secondary))', textTransform: 'uppercase' }}>Operator</span>
                    <p style={{ fontWeight: 700, color: 'hsl(var(--accent-purple))' }}>{currentResult.deonticOperator}</p>
                  </div>
                  <div style={{ background: 'rgba(255,255,255,0.02)', padding: '0.75rem', borderRadius: '6px', border: '1px solid var(--glass-border)' }}>
                    <span style={{ fontSize: '0.7rem', color: 'hsl(var(--text-secondary))', textTransform: 'uppercase' }}>Subject</span>
                    <p style={{ fontWeight: 600 }}>{currentResult.subject}</p>
                  </div>
                  <div style={{ background: 'rgba(255,255,255,0.02)', padding: '0.75rem', borderRadius: '6px', border: '1px solid var(--glass-border)' }}>
                    <span style={{ fontSize: '0.7rem', color: 'hsl(var(--text-secondary))', textTransform: 'uppercase' }}>Action</span>
                    <p style={{ fontWeight: 600 }}>{currentResult.action}</p>
                  </div>
                  <div style={{ background: 'rgba(255,255,255,0.02)', padding: '0.75rem', borderRadius: '6px', border: '1px solid var(--glass-border)' }}>
                    <span style={{ fontSize: '0.7rem', color: 'hsl(var(--text-secondary))', textTransform: 'uppercase' }}>Target</span>
                    <p style={{ fontWeight: 600 }}>{currentResult.target}</p>
                  </div>
                </div>
                {currentResult.constraint && (
                  <div style={{ background: 'rgba(255,255,255,0.02)', padding: '0.75rem', borderRadius: '6px', border: '1px solid var(--glass-border)', marginTop: '1rem' }}>
                    <span style={{ fontSize: '0.7rem', color: 'hsl(var(--text-secondary))', textTransform: 'uppercase' }}>Logical Constraint Condition</span>
                    <p style={{ fontWeight: 500, color: 'hsl(var(--accent-cyan))' }}>{currentResult.constraint}</p>
                  </div>
                )}
              </div>

              {/* Code tabs */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                    <span style={{ fontSize: '0.85rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.3rem' }}><Code size={14} /> SMT Spec</span>
                    <button class="glass-btn" style={{ padding: '0.2rem 0.5rem', fontSize: '0.7rem' }} onClick={() => copyToClipboard(currentResult.smtSpec)}><Copy size={12} /></button>
                  </div>
                  <pre style={{ background: 'rgba(0,0,0,0.4)', padding: '1rem', borderRadius: '6px', fontSize: '0.75rem', overflowX: 'auto', border: '1px solid var(--glass-border)', maxHeight: '250px', color: '#10b981' }}>
                    {currentResult.smtSpec}
                  </pre>
                </div>

                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                    <span style={{ fontSize: '0.85rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.3rem' }}><Code size={14} /> ODRL JSON-LD</span>
                    <button class="glass-btn" style={{ padding: '0.2rem 0.5rem', fontSize: '0.7rem' }} onClick={() => copyToClipboard(currentResult.odrlPolicy)}><Copy size={12} /></button>
                  </div>
                  <pre style={{ background: 'rgba(0,0,0,0.4)', padding: '1rem', borderRadius: '6px', fontSize: '0.75rem', overflowX: 'auto', border: '1px solid var(--glass-border)', maxHeight: '250px', color: '#a855f7' }}>
                    {currentResult.odrlPolicy}
                  </pre>
                </div>
              </div>
            </div>
          ) : (
            <div class="glass-card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', flex: 1, padding: '4rem 2rem', color: 'hsl(var(--text-secondary))', borderStyle: 'dashed' }}>
              <Code size={48} style={{ opacity: 0.3, marginBottom: '1rem' }} />
              <h4>Translation Outputs Pending</h4>
              <p style={{ textAlign: 'center', fontSize: '0.85rem' }}>Input a Controlled Natural Language sentence and run translation to view SMT specs and ODRL properties.</p>
            </div>
          )}
        </div>
      </div>

      {/* Rules ledger table */}
      <div class="glass-card" style={{ padding: 0, overflow: 'hidden' }}>
        <div style={{ padding: '1.5rem', borderBottom: '1px solid var(--glass-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem' }}><BookOpen size={18} /> Active Translated Rules Ledger</h3>
        </div>
        {fetching ? (
          <div style={{ padding: '2rem', textAlign: 'center', color: 'hsl(var(--text-secondary))' }}>Loading ledger...</div>
        ) : rules.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'hsl(var(--text-muted))' }}>No rules translated yet.</div>
        ) : (
          <table class="glass-table">
            <thead>
              <tr>
                <th>Scope</th>
                <th>Original CNL Text</th>
                <th>Deontic Operator</th>
                <th>Subject / Target</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {rules.map((rule) => (
                <tr key={rule.id}>
                  <td>
                    <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, color: 'hsl(var(--accent-cyan))' }}>
                      {rule.regulationShortName} {rule.articleNumber}
                    </span>
                  </td>
                  <td>
                    <p style={{ fontSize: '0.85rem', maxWidth: '400px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {rule.rawSourceText}
                    </p>
                  </td>
                  <td>
                    <span style={{
                      padding: '0.2rem 0.5rem',
                      borderRadius: '4px',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      background: rule.deonticOperator === 'PROHIBITION' ? 'rgba(244,63,94,0.1)' : 'rgba(16,185,129,0.1)',
                      color: rule.deonticOperator === 'PROHIBITION' ? 'hsl(var(--color-danger))' : 'hsl(var(--color-success))'
                    }}>
                      {rule.deonticOperator}
                    </span>
                  </td>
                  <td>
                    <span style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))' }}>
                      {rule.subject} → {rule.target}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <button class="glass-btn" style={{ padding: '0.4rem', borderColor: 'rgba(244,63,94,0.3)', color: 'hsl(var(--color-danger))' }} onClick={() => handleDelete(rule.id)}>
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
