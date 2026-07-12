import React, { useEffect, useState } from 'react';
import { regulationsApi } from '../api/regulationsApi';
import { RegulationResponse } from '../types/regulation';
import { Search, Eye, PlusCircle, Globe, Calendar } from 'lucide-react';
import { Link } from 'react-router-dom';

export const RegulationsListPage: React.FC = () => {
  const [regulations, setRegulations] = useState<RegulationResponse[]>([]);
  const [search, setSearch] = useState('');
  const [jurisdiction, setJurisdiction] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchRegulations = () => {
    setLoading(true);
    regulationsApi.list(jurisdiction, search)
      .then((data) => {
        setRegulations(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setLoading(false);
      });
  };

  useEffect(() => {
    fetchRegulations();
  }, [jurisdiction]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchRegulations();
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '0.25rem' }}>
            Regulations Knowledge Store
          </h1>
          <p style={{ color: 'hsl(var(--text-secondary))' }}>
            Browse and query machine-readable statutes, articles, and formal specifications.
          </p>
        </div>
        <Link to="/register" class="glass-btn glass-btn-primary">
          <PlusCircle size={18} /> Add New Policy
        </Link>
      </div>

      {/* Filter and Search Bar */}
      <div class="glass-card" style={{ padding: '1rem' }}>
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <div style={{ flex: 1, position: 'relative' }}>
            <span style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'hsl(var(--text-muted))' }}>
              <Search size={18} />
            </span>
            <input
              type="text"
              class="glass-input"
              style={{ paddingLeft: '2.75rem' }}
              placeholder="Search by title, acronym, or description..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <div style={{ width: '220px' }}>
            <select
              class="glass-input"
              value={jurisdiction}
              onChange={(e) => setJurisdiction(e.target.value)}
              style={{ cursor: 'pointer' }}
            >
              <option value="">All Jurisdictions</option>
              <option value="EU">EU (Europe)</option>
              <option value="IN">IN (India)</option>
              <option value="US">US (United States)</option>
            </select>
          </div>

          <button type="submit" class="glass-btn">
            Search
          </button>
        </form>
      </div>

      {/* Regulations Table / Cards */}
      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '3rem' }}>
          <div style={{
            width: '40px',
            height: '40px',
            border: '4px solid rgba(255,255,255,0.05)',
            borderTopColor: 'hsl(var(--accent-cyan))',
            borderRadius: '50%',
            animation: 'spin 1s linear infinite'
          }}></div>
          <style>{`@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }`}</style>
        </div>
      ) : regulations.length === 0 ? (
        <div class="glass-card" style={{ textAlign: 'center', padding: '4rem 2rem', color: 'hsl(var(--text-secondary))' }}>
          <Globe size={48} style={{ marginBottom: '1rem', opacity: 0.5 }} />
          <h3>No Regulations Found</h3>
          <p>Try refining your search query or registering a new regulation.</p>
        </div>
      ) : (
        <div class="glass-card" style={{ padding: 0, overflow: 'hidden' }}>
          <table class="glass-table">
            <thead>
              <tr>
                <th>Identifier</th>
                <th>Full Name</th>
                <th>Jurisdiction</th>
                <th>Version</th>
                <th>Status</th>
                <th>Effective Date</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {regulations.map((reg) => (
                <tr key={reg.id}>
                  <td>
                    <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, color: 'hsl(var(--accent-cyan))' }}>
                      {reg.shortName}
                    </span>
                  </td>
                  <td>
                    <div>
                      <div style={{ fontWeight: 600 }}>{reg.name}</div>
                      <div style={{ fontSize: '0.8rem', color: 'hsl(var(--text-secondary))', maxWidth: '400px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {reg.description}
                      </div>
                    </div>
                  </td>
                  <td>
                    <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem' }}>
                      <Globe size={14} color="hsl(var(--text-muted))" />
                      {reg.primaryJurisdiction}
                    </span>
                  </td>
                  <td>{reg.version}</td>
                  <td>
                    <span class={`badge badge-${reg.status.toLowerCase()}`}>
                      {reg.status}
                    </span>
                  </td>
                  <td>
                    <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.85rem' }}>
                      <Calendar size={14} color="hsl(var(--text-muted))" />
                      {reg.effectiveDate ? reg.effectiveDate : 'Pending'}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <Link to={`/regulations/${reg.id}`} class="glass-btn" style={{ padding: '0.4rem 0.8rem' }}>
                      <Eye size={14} /> View Details
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
