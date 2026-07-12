import React, { useState } from 'react';
import { pipApi } from '../api/pipApi';
import { ResolvedContext } from '../types/pip';
import { ShieldCheck, ShieldAlert, Sparkles, User, Database, Globe, Play, Plus, RefreshCw, Key } from 'lucide-react';

export const PipPage: React.FC = () => {
  // Query Form State
  const [subId, setSubId] = useState('analyst');
  const [resId, setResId] = useState('health-records');
  const [actId, setActId] = useState('transfer');
  const [srcCountry, setSrcCountry] = useState('IN');
  const [tgtCountry, setTgtCountry] = useState('EU');

  // Cache Overwrite Form State
  const [targetType, setTargetType] = useState<'subject' | 'resource'>('subject');
  const [targetId, setTargetId] = useState('analyst');
  const [attrKey, setAttrKey] = useState('clearance');
  const [attrVal, setAttrVal] = useState('restricted-access');
  const [dataType, setDataType] = useState('String');

  const [resolvedContext, setResolvedContext] = useState<ResolvedContext | null>(null);
  const [loading, setLoading] = useState(false);
  const [updatingCache, setUpdatingCache] = useState(false);

  const handleResolve = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    setLoading(true);
    pipApi.resolve({
      subjectId: subId,
      resourceId: resId,
      actionId: actId,
      sourceCountry: srcCountry,
      targetCountry: tgtCountry
    })
    .then((data) => {
      setResolvedContext(data);
      setLoading(false);
    })
    .catch((err) => {
      console.error(err);
      setLoading(false);
      alert('Attribute resolution failed: ' + err.message);
    });
  };

  const handleCacheRegister = (e: React.FormEvent) => {
    e.preventDefault();
    setUpdatingCache(true);
    const apiCall = targetType === 'subject'
      ? pipApi.registerSubjectAttribute
      : pipApi.registerResourceAttribute;

    apiCall({
      id: targetId,
      key: attrKey,
      value: attrVal,
      dataType: dataType
    })
    .then(() => {
      setUpdatingCache(false);
      alert(`${targetType.toUpperCase()} attribute successfully cached!`);
      // Run resolution to refresh views
      handleResolve();
    })
    .catch((err) => {
      console.error(err);
      setUpdatingCache(false);
      alert('Failed to register cache: ' + err.message);
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
          Policy Information Point (PIP)
        </h1>
        <p style={{ color: 'hsl(var(--text-secondary))' }}>
          Retrieve subject/resource context attributes dynamically from caching layers and verify geo-adequacy transitivity paths.
        </p>
      </div>

      {/* Templates */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1rem' }}>
        <div class="glass-card" style={{ cursor: 'pointer', padding: '1rem' }} onClick={() => loadExample('admin', 'health-records', 'read', 'IN', 'EU')}>
          <h4 style={{ color: 'hsl(var(--accent-cyan))', fontSize: '0.85rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Sparkles size={14} /> Admin / Medical Record
          </h4>
          <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>IN → EU adequacy transitivity (Resolves top-secret, MFA admin)</p>
        </div>

        <div class="glass-card" style={{ cursor: 'pointer', padding: '1rem' }} onClick={() => loadExample('external_user', 'personal-data', 'transfer', 'US', 'IN')}>
          <h4 style={{ color: 'hsl(var(--color-danger))', fontSize: '0.85rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Sparkles size={14} /> Contractor / Personal Data
          </h4>
          <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>US → IN adequacy transitivity (Resolves public clearance, unadequate)</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 2fr', gap: '2rem' }}>
        {/* Left Side: Resolver Query and Cache Editor */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <div class="glass-card">
            <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <RefreshCw size={18} /> Resolve Context
            </h3>
            <form onSubmit={handleResolve} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
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
              <button type="submit" class="glass-btn glass-btn-primary" disabled={loading}>
                <Play size={16} /> {loading ? 'Querying PIP...' : 'Execute Resolution'}
              </button>
            </form>
          </div>

          {/* Cache Overwrite Form */}
          <div class="glass-card">
            <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Plus size={18} /> Configure PIP Cache
            </h3>
            <form onSubmit={handleCacheRegister} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label>Cache Category</label>
                  <select class="glass-input" value={targetType} onChange={e => setTargetType(e.target.value as 'subject' | 'resource')}>
                    <option value="subject">Subject Attribute</option>
                    <option value="resource">Resource Attribute</option>
                  </select>
                </div>
                <div>
                  <label>Target ID</label>
                  <input type="text" class="glass-input" required value={targetId} onChange={e => setTargetId(e.target.value)} placeholder="e.g. analyst or health-records" />
                </div>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label>Attribute Key</label>
                  <input type="text" class="glass-input" required value={attrKey} onChange={e => setAttrKey(e.target.value)} />
                </div>
                <div>
                  <label>Attribute Value</label>
                  <input type="text" class="glass-input" required value={attrVal} onChange={e => setAttrVal(e.target.value)} />
                </div>
              </div>
              <div>
                <label>Data Type</label>
                <select class="glass-input" value={dataType} onChange={e => setDataType(e.target.value)}>
                  <option value="String">String</option>
                  <option value="Boolean">Boolean</option>
                  <option value="Integer">Integer</option>
                </select>
              </div>
              <button type="submit" class="glass-btn" style={{ borderColor: 'hsl(var(--accent-purple))', color: 'hsl(var(--accent-purple))' }} disabled={updatingCache}>
                Save to Caching Layer
              </button>
            </form>
          </div>
        </div>

        {/* Right Side: Resolution Results Grid */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          {resolvedContext ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
              
              {/* Transitive Adequacy Card */}
              <div class="glass-card" style={{
                borderLeft: `5px solid ${resolvedContext.transitiveAdequate ? 'hsl(var(--color-success))' : 'hsl(var(--color-danger))'}`,
                background: resolvedContext.transitiveAdequate ? 'rgba(16,185,129,0.05)' : 'rgba(244,63,94,0.05)'
              }}>
                <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 700, color: resolvedContext.transitiveAdequate ? 'hsl(var(--color-success))' : 'hsl(var(--color-danger))' }}>
                  {resolvedContext.transitiveAdequate ? (
                    <>
                      <ShieldCheck size={20} /> Transitive Adequacy Safe
                    </>
                  ) : (
                    <>
                      <ShieldAlert size={20} /> Non-Adequate Pathway
                    </>
                  )}
                </h4>
                <p style={{ fontSize: '0.85rem', color: 'hsl(var(--text-secondary))', marginTop: '0.5rem' }}>
                  Geo-adequacy transit from <span style={{ fontWeight: 600, color: 'hsl(var(--text-primary))' }}>{srcCountry}</span> to <span style={{ fontWeight: 600, color: 'hsl(var(--text-primary))' }}>{tgtCountry}</span> checks out {resolvedContext.transitiveAdequate ? 'valid' : 'invalid'} against Graph adequacy paths.
                </p>
              </div>

              {/* Resolved Attributes List */}
              <div class="glass-card">
                <h3 style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: '1.2rem' }}>Resolved Context Attributes</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                  
                  {/* Subject Category */}
                  <div>
                    <h4 style={{ fontSize: '0.85rem', color: 'hsl(var(--accent-cyan))', textTransform: 'uppercase', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <User size={14} /> Subject: {resolvedContext.subjectId}
                    </h4>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '0.75rem' }}>
                      {resolvedContext.attributes.filter(a => a.category === 'SUBJECT').map(attr => (
                        <div key={attr.key} style={{ padding: '0.75rem', background: 'rgba(255,255,255,0.02)', border: '1px solid var(--glass-border)', borderRadius: '6px' }}>
                          <span style={{ fontSize: '0.7rem', color: 'hsl(var(--text-secondary))' }}>{attr.key} ({attr.dataType})</span>
                          <p style={{ fontWeight: 600, fontSize: '0.9rem', color: 'hsl(var(--text-primary))' }}>{attr.value}</p>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Resource Category */}
                  <div>
                    <h4 style={{ fontSize: '0.85rem', color: 'hsl(var(--accent-purple))', textTransform: 'uppercase', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <Database size={14} /> Resource: {resolvedContext.resourceId}
                    </h4>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '0.75rem' }}>
                      {resolvedContext.attributes.filter(a => a.category === 'RESOURCE').map(attr => (
                        <div key={attr.key} style={{ padding: '0.75rem', background: 'rgba(255,255,255,0.02)', border: '1px solid var(--glass-border)', borderRadius: '6px' }}>
                          <span style={{ fontSize: '0.7rem', color: 'hsl(var(--text-secondary))' }}>{attr.key} ({attr.dataType})</span>
                          <p style={{ fontWeight: 600, fontSize: '0.9rem', color: 'hsl(var(--text-primary))' }}>{attr.value}</p>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div class="glass-card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', flex: 1, padding: '8rem 2rem', color: 'hsl(var(--text-secondary))', borderStyle: 'dashed' }}>
              <Key size={48} style={{ opacity: 0.3, marginBottom: '1rem' }} />
              <h4>Context Resolution Pending</h4>
              <p style={{ textAlign: 'center', fontSize: '0.85rem' }}>Run context resolution to compile subject, resource classification, and geo transitivity properties.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
