import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Shield, BookOpen, Network, FilePlus, Settings, Database, Activity, Sparkles, Lock, EyeOff, Globe, Layers } from 'lucide-react';

interface LayoutProps {
  children: React.ReactNode;
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  const location = useLocation();

  const menuItems = [
    { path: '/', label: 'Overview', icon: <Activity size={18} /> },
    { path: '/regulations', label: 'Regulations Store', icon: <BookOpen size={18} /> },
    { path: '/translate', label: 'Deontic Translator', icon: <Sparkles size={18} /> },
    { path: '/policies', label: 'Policy Administration', icon: <Settings size={18} /> },
    { path: '/pip', label: 'PIP Context Resolver', icon: <Database size={18} /> },
    { path: '/pdp', label: 'PDP Compliance Solver', icon: <Shield size={18} /> },
    { path: '/pqc', label: 'PQC Crypto Layer', icon: <Lock size={18} /> },
    { path: '/zkp', label: 'ZK Proof Engine', icon: <EyeOff size={18} /> },
    { path: '/governance', label: 'Governance Engine', icon: <Globe size={18} /> },
    { path: '/monitor', label: 'Compliance Monitor', icon: <Activity size={18} /> },
    { path: '/ledger', label: 'Auditing Ledger', icon: <Layers size={18} /> },
    { path: '/register', label: 'Register Policy', icon: <FilePlus size={18} /> },
    { path: '/graph', label: 'Jurisdiction Graph', icon: <Network size={18} /> },
  ];

  return (
    <div className="app-wrapper">
      {/* Sidebar Panel */}
      <aside className="sidebar">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '2.5rem' }}>
          <div style={{
            background: 'linear-gradient(135deg, #06b6d4 0%, #8b5cf6 100%)',
            padding: '0.5rem',
            borderRadius: '10px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 15px rgba(139, 92, 246, 0.4)'
          }}>
            <Shield size={24} color="#fff" />
          </div>
          <div>
            <h1 style={{ fontSize: '1.2rem', fontWeight: 800, letterSpacing: '-0.02em', margin: 0 }}>
              <span className="grad-text">PQVCF</span>
            </h1>
            <span style={{ fontSize: '0.65rem', color: 'hsl(var(--text-secondary))', textTransform: 'uppercase', letterSpacing: '0.1em' }}>
              Research Node v1.0
            </span>
          </div>
        </div>

        <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', flex: 1 }}>
          {menuItems.map((item) => {
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.75rem',
                  padding: '0.75rem 1rem',
                  borderRadius: '8px',
                  fontSize: '0.9rem',
                  fontWeight: 500,
                  transition: 'all 0.2s',
                  background: isActive ? 'rgba(255, 255, 255, 0.05)' : 'transparent',
                  borderLeft: isActive ? '3px solid hsl(var(--accent-cyan))' : '3px solid transparent',
                  color: isActive ? '#fff' : 'hsl(var(--text-secondary))',
                }}
              >
                {item.icon}
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <div style={{ marginTop: 'auto', borderTop: '1px solid var(--glass-border)', paddingTop: '1rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <Database size={16} color="hsl(var(--text-muted))" />
          <span style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))' }}>
            Local PostgreSQL: Connected
          </span>
        </div>
      </aside>

      {/* Main Panel Content */}
      <div className="main-content">
        <header className="topbar">
          <div>
            <h2 style={{ fontSize: '1.1rem', fontWeight: 600 }}>Compliance Framework Controller</h2>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ textAlign: 'right' }}>
              <p style={{ fontSize: '0.8rem', fontWeight: 500 }}>Principal Architect</p>
              <p style={{ fontSize: '0.7rem', color: 'hsl(var(--text-secondary))' }}>Switzerland Lab</p>
            </div>
            <div style={{
              width: '36px',
              height: '36px',
              borderRadius: '50%',
              background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontWeight: 'bold',
              fontSize: '0.9rem',
              boxShadow: '0 0 10px rgba(99, 102, 241, 0.3)'
            }}>
              PA
            </div>
          </div>
        </header>

        <main className="page-container">
          {children}
        </main>
      </div>
    </div>
  );
};
