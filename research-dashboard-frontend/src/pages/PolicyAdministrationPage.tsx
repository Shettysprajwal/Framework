import React, { useEffect, useState } from 'react';
import { policyApi } from '../api/policyApi';
import { translationApi, TranslationResponse } from '../api/translationApi';
import { PolicyResponse } from '../types/policy';
import { FilePlus, ShieldAlert, Play, Trash2, Link, Database, Eye, Plus, Sparkles, BookOpen } from 'lucide-react';

export const PolicyAdministrationPage: React.FC = () => {
  const [policies, setPolicies] = useState<PolicyResponse[]>([]);
  const [selectedPolicy, setSelectedPolicy] = useState<PolicyResponse | null>(null);
  const [translatedRules, setTranslatedRules] = useState<TranslationResponse[]>([]);
  
  // Policy Form State
  const [policyName, setPolicyName] = useState('');
  const [policyOwner, setPolicyOwner] = useState('Compliance Team');
  const [policyDesc, setPolicyDesc] = useState('');
  const [creatingPolicy, setCreatingPolicy] = useState(false);

  // Link Rule Form State
  const [orgRuleName, setOrgRuleName] = useState('');
  const [selectedRegRuleId, setSelectedRegRuleId] = useState('');
  const [linkDesc, setLinkDesc] = useState('');
  const [linkingRule, setLinkingRule] = useState(false);
  
  const [loading, setLoading] = useState(false);

  const fetchPolicies = () => {
    setLoading(true);
    policyApi.listAll()
      .then((data) => {
        setPolicies(data);
        if (selectedPolicy) {
          const updated = data.find(p => p.id === selectedPolicy.id);
          if (updated) setSelectedPolicy(updated);
        }
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setLoading(false);
      });
  };

  const fetchRules = () => {
    translationApi.listAll()
      .then((rules) => {
        setTranslatedRules(rules);
        if (rules.length > 0) {
          setSelectedRegRuleId(rules[0].id);
        }
      })
      .catch(console.error);
  };

  useEffect(() => {
    fetchPolicies();
    fetchRules();
  }, []);

  const handleCreatePolicy = (e: React.FormEvent) => {
    e.preventDefault();
    setCreatingPolicy(true);
    policyApi.create({
      name: policyName,
      owner: policyOwner,
      description: policyDesc,
    })
    .then((created) => {
      setCreatingPolicy(false);
      setPolicyName('');
      setPolicyDesc('');
      setSelectedPolicy(created);
      fetchPolicies();
    })
    .catch((err) => {
      console.error(err);
      setCreatingPolicy(false);
      alert('Failed to create policy: ' + err.message);
    });
  };

  const handleLinkRule = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedPolicy || !selectedRegRuleId) return;
    setLinkingRule(true);
    policyApi.linkRule(selectedPolicy.id, {
      organizationalRuleName: orgRuleName,
      regulatoryRuleId: selectedRegRuleId,
      description: linkDesc,
    })
    .then((updated) => {
      setLinkingRule(false);
      setOrgRuleName('');
      setLinkDesc('');
      setSelectedPolicy(updated);
      fetchPolicies();
    })
    .catch((err) => {
      console.error(err);
      setLinkingRule(false);
      alert('Link failed: ' + err.message);
    });
  };

  const handleUnlinkRule = (linkId: string) => {
    if (!selectedPolicy) return;
    if (confirm('Remove this regulatory rule link?')) {
      policyApi.unlinkRule(selectedPolicy.id, linkId)
        .then((updated) => {
          setSelectedPolicy(updated);
          fetchPolicies();
        })
        .catch(err => alert(err.message));
    }
  };

  const handleActivate = (id: string) => {
    policyApi.activate(id)
      .then((updated) => {
        if (selectedPolicy?.id === id) setSelectedPolicy(updated);
        fetchPolicies();
      })
      .catch(err => alert(err.message));
  };

  const handleDeprecate = (id: string) => {
    policyApi.deprecate(id)
      .then((updated) => {
        if (selectedPolicy?.id === id) setSelectedPolicy(updated);
        fetchPolicies();
      })
      .catch(err => alert(err.message));
  };

  const handleDeletePolicy = (id: string) => {
    if (confirm('Delete this draft policy?')) {
      policyApi.delete(id)
        .then(() => {
          if (selectedPolicy?.id === id) setSelectedPolicy(null);
          fetchPolicies();
        })
        .catch(err => alert(err.message));
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '2rem', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '0.25rem' }}>
          Policy Administration Point (PAP)
        </h1>
        <p style={{ color: 'hsl(var(--text-secondary))' }}>
          Author organizational compliance policies and bind them to active regulatory logic statements parsed in Module 2.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 2fr', gap: '2rem' }}>
        {/* Left column: Policies list and creation */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <div class="glass-card">
            <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <FilePlus size={18} /> Author Policy
            </h3>
            <form onSubmit={handleCreatePolicy} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label>Policy Title</label>
                <input type="text" class="glass-input" required value={policyName} onChange={e => setPolicyName(e.target.value)} placeholder="e.g. Cross-border Safe Transfer" />
              </div>
              <div>
                <label>Department Owner</label>
                <input type="text" class="glass-input" required value={policyOwner} onChange={e => setPolicyOwner(e.target.value)} />
              </div>
              <div>
                <label>Detailed Scope</label>
                <textarea class="glass-input" style={{ minHeight: '80px' }} value={policyDesc} onChange={e => setPolicyDesc(e.target.value)} placeholder="Internal compliance rules..." />
              </div>
              <button type="submit" class="glass-btn glass-btn-primary" disabled={creatingPolicy}>
                Create Policy
              </button>
            </form>
          </div>

          {/* Active Policies List */}
          <div class="glass-card">
            <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <BookOpen size={18} /> Policies Ledger
            </h3>
            {loading ? (
              <p style={{ color: 'hsl(var(--text-secondary))' }}>Loading ledger...</p>
            ) : policies.length === 0 ? (
              <p style={{ color: 'hsl(var(--text-muted))' }}>No policies cataloged yet.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {policies.map(p => (
                  <div
                    key={p.id}
                    style={{
                      padding: '1rem',
                      borderRadius: '8px',
                      background: selectedPolicy?.id === p.id ? 'rgba(255,255,255,0.06)' : 'rgba(255,255,255,0.01)',
                      border: '1px solid',
                      borderColor: selectedPolicy?.id === p.id ? 'hsl(var(--accent-cyan))' : 'var(--glass-border)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      cursor: 'pointer'
                    }}
                    onClick={() => setSelectedPolicy(p)}
                  >
                    <div>
                      <h4 style={{ fontWeight: 600, fontSize: '0.95rem' }}>{p.name}</h4>
                      <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.25rem' }}>
                        <span class={`badge badge-${p.status.toLowerCase()}`} style={{ fontSize: '0.65rem' }}>
                          {p.status}
                        </span>
                        <span style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>
                          {p.ruleLinks.length} bindings
                        </span>
                      </div>
                    </div>
                    <button class="glass-btn" style={{ padding: '0.3rem 0.6rem' }} onClick={(e) => { e.stopPropagation(); setSelectedPolicy(p); }}>
                      <Eye size={12} />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Policy details and links configuration */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          {selectedPolicy ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
              {/* Info Panel */}
              <div class="glass-card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                  <div>
                    <span style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))', textTransform: 'uppercase' }}>Scope Owner: {selectedPolicy.owner}</span>
                    <h2 style={{ fontSize: '1.6rem', fontWeight: 800 }}>{selectedPolicy.name}</h2>
                  </div>
                  <span class={`badge badge-${selectedPolicy.status.toLowerCase()}`} style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem' }}>
                    {selectedPolicy.status}
                  </span>
                </div>
                <p style={{ color: 'hsl(var(--text-secondary))', fontSize: '0.95rem', marginBottom: '1.2rem' }}>{selectedPolicy.description}</p>
                
                <div style={{ display: 'flex', gap: '1rem' }}>
                  {selectedPolicy.status === 'DRAFT' && (
                    <>
                      <button onClick={() => handleActivate(selectedPolicy.id)} class="glass-btn" style={{ borderColor: 'hsl(var(--color-success))', color: 'hsl(var(--color-success))' }}>
                        <Play size={14} /> Publish Policy
                      </button>
                      <button onClick={() => handleDeletePolicy(selectedPolicy.id)} class="glass-btn" style={{ borderColor: 'hsl(var(--color-danger))', color: 'hsl(var(--color-danger))' }}>
                        <Trash2 size={14} /> Delete Draft
                      </button>
                    </>
                  )}
                  {selectedPolicy.status === 'ACTIVE' && (
                    <button onClick={() => handleDeprecate(selectedPolicy.id)} class="glass-btn" style={{ borderColor: 'hsl(var(--color-warning))', color: 'hsl(var(--color-warning))' }}>
                      <ShieldAlert size={14} /> Deprecate Policy
                    </button>
                  )}
                </div>
              </div>

              {/* Bound Rules mapping links list */}
              <div class="glass-card">
                <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1.2rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Database size={18} /> Regulatory Rule Bindings ({selectedPolicy.ruleLinks.length})
                </h3>
                {selectedPolicy.ruleLinks.length === 0 ? (
                  <p style={{ color: 'hsl(var(--text-secondary))' }}>No rules bound to this policy. Add one below.</p>
                ) : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    {selectedPolicy.ruleLinks.map(link => (
                      <div key={link.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.8rem 1rem', background: 'rgba(255,255,255,0.02)', border: '1px solid var(--glass-border)', borderRadius: '6px' }}>
                        <div>
                          <h4 style={{ fontWeight: 600, color: 'hsl(var(--accent-cyan))', fontSize: '0.9rem' }}>{link.organizationalRuleName}</h4>
                          <span style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))', display: 'flex', alignItems: 'center', gap: '0.25rem', marginTop: '0.2rem' }}>
                            <Link size={10} /> Regulatory UUID: {link.regulatoryRuleId}
                          </span>
                          <p style={{ fontSize: '0.8rem', color: 'hsl(var(--text-muted))', marginTop: '0.4rem' }}>{link.description}</p>
                        </div>
                        {selectedPolicy.status === 'DRAFT' && (
                          <button class="glass-btn" style={{ padding: '0.3rem', color: 'hsl(var(--color-danger))', borderColor: 'rgba(244,63,94,0.3)' }} onClick={() => handleUnlinkRule(link.id)}>
                            <Trash2 size={12} />
                          </button>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Form to Bind Link Rule */}
              {selectedPolicy.status === 'DRAFT' && (
                <div class="glass-card">
                  <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Plus size={18} /> Bind Regulatory Rule
                  </h3>
                  <form onSubmit={handleLinkRule} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    <div>
                      <label>Internal Rule Name</label>
                      <input type="text" class="glass-input" required value={orgRuleName} onChange={e => setOrgRuleName(e.target.value)} placeholder="e.g. EU customer data check" />
                    </div>
                    <div>
                      <label>Select Regulatory Source Rule (Module 2 ASTs)</label>
                      {translatedRules.length === 0 ? (
                        <p style={{ fontSize: '0.8rem', color: 'hsl(var(--color-danger))' }}>No translated rules available. Go to Deontic Translator page first to add rules.</p>
                      ) : (
                        <select class="glass-input" value={selectedRegRuleId} onChange={e => setSelectedRegRuleId(e.target.value)} style={{ cursor: 'pointer' }}>
                          {translatedRules.map(rule => (
                            <option key={rule.id} value={rule.id}>
                              [{rule.regulationShortName} {rule.articleNumber}] {rule.rawSourceText.slice(0, 50)}...
                            </option>
                          ))}
                        </select>
                      )}
                    </div>
                    <div>
                      <label>Binding Description</label>
                      <input type="text" class="glass-input" value={linkDesc} onChange={e => setLinkDesc(e.target.value)} placeholder="Describes target scope context..." />
                    </div>
                    <button type="submit" class="glass-btn glass-btn-primary" disabled={linkingRule || translatedRules.length === 0}>
                      Bind Rule Link
                    </button>
                  </form>
                </div>
              )}
            </div>
          ) : (
            <div class="glass-card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '6rem 2rem', color: 'hsl(var(--text-secondary))', borderStyle: 'dashed' }}>
              <Eye size={48} style={{ opacity: 0.3, marginBottom: '1rem' }} />
              <h4>No Policy Selected</h4>
              <p style={{ fontSize: '0.85rem' }}>Select a policy from the ledger on the left to configure rule bindings and manage lifecycles.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
