import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { regulationsApi } from '../api/regulationsApi';
import { FilePlus, Info, CheckCircle } from 'lucide-react';

export const RegisterRegulationPage: React.FC = () => {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [shortName, setShortName] = useState('');
  const [jurisdiction, setJurisdiction] = useState('EU');
  const [version, setVersion] = useState('');
  const [description, setDescription] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    regulationsApi.register({
      name,
      shortName,
      jurisdiction,
      version,
      description,
    })
    .then((data) => {
      setSaving(false);
      // Navigate to detail page
      navigate(`/regulations/${data.id}`);
    })
    .catch((err) => {
      console.error(err);
      setSaving(false);
      alert('Failed to register policy: ' + err.message);
    });
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', width: '100%', display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '2.2rem', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '0.25rem' }}>
          Register New Policy
        </h1>
        <p style={{ color: 'hsl(var(--text-secondary))' }}>
          Add a new international data protection policy to the PQVCF database schema.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '2rem' }}>
        {/* Form panel */}
        <div class="glass-card">
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div>
              <label>Statutory Full Title</label>
              <input
                type="text"
                class="glass-input"
                required
                placeholder="e.g. General Data Protection Regulation"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div>
                <label>Short Acronym (Uppercase)</label>
                <input
                  type="text"
                  class="glass-input"
                  required
                  placeholder="e.g. GDPR"
                  value={shortName}
                  onChange={(e) => setShortName(e.target.value)}
                />
              </div>

              <div>
                <label>Primary Jurisdiction</label>
                <input
                  type="text"
                  class="glass-input"
                  required
                  placeholder="e.g. EU or US_CA"
                  value={jurisdiction}
                  onChange={(e) => setJurisdiction(e.target.value)}
                />
              </div>
            </div>

            <div>
              <label>Statute Version</label>
              <input
                type="text"
                class="glass-input"
                required
                placeholder="e.g. 2016/679"
                value={version}
                onChange={(e) => setVersion(e.target.value)}
              />
            </div>

            <div>
              <label>Scope Description</label>
              <textarea
                class="glass-input"
                style={{ minHeight: '120px' }}
                placeholder="Brief summary of policy coverage rules..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              ></textarea>
            </div>

            <button type="submit" class="glass-btn glass-btn-primary" disabled={saving}>
              <FilePlus size={18} /> {saving ? 'Registering...' : 'Register Policy'}
            </button>
          </form>
        </div>

        {/* Side Tip panel */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div class="glass-card" style={{ display: 'flex', gap: '0.75rem' }}>
            <Info size={24} color="hsl(var(--accent-cyan))" style={{ flexShrink: 0 }} />
            <div>
              <h4 style={{ fontWeight: 600, marginBottom: '0.25rem' }}>System Note</h4>
              <p style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))' }}>
                All regulations are registered in **DRAFT** status initially. You must append relevant articles and edit verification spec logic before transitioning to **ACTIVE** enforcement status.
              </p>
            </div>
          </div>

          <div class="glass-card" style={{ display: 'flex', gap: '0.75rem' }}>
            <CheckCircle size={24} color="hsl(var(--color-success))" style={{ flexShrink: 0 }} />
            <div>
              <h4 style={{ fontWeight: 600, marginBottom: '0.25rem' }}>Axiom Seeding</h4>
              <p style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))' }}>
                You can write custom Z3 constraint variables in the SMT Spec editor once the draft is created.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
