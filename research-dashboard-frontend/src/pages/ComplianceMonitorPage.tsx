import React, { useEffect, useState } from 'react';
import { monitorApi } from '../api/monitorApi';
import { EventDto, ViolationDto, SlaMetricsDto } from '../types/monitor';
import { Activity, AlertOctagon, Terminal, Trash2, Cpu, Send } from 'lucide-react';

export const ComplianceMonitorPage: React.FC = () => {
  const [events, setEvents] = useState<EventDto[]>([]);
  const [violations, setViolations] = useState<ViolationDto[]>([]);
  const [metrics, setMetrics] = useState<SlaMetricsDto>({ totalEvents: 0, violationCount: 0, complianceRate: 100.0 });
  const [source, setSource] = useState('DE');
  const [destination, setDestination] = useState('IN');
  const [dataCategory, setDataCategory] = useState('PERSONAL');
  const [packetSize, setPacketSize] = useState('2048');
  const [loading, setLoading] = useState(false);

  const refreshData = () => {
    monitorApi.getMetrics().then(setMetrics).catch(console.error);
    monitorApi.listEvents().then(setEvents).catch(console.error);
    monitorApi.listViolations().then(setViolations).catch(console.error);
  };

  useEffect(() => {
    refreshData();
    // Refresh every 10 seconds automatically to simulate real-time metrics
    const interval = setInterval(refreshData, 10000);
    return () => clearInterval(interval);
  }, []);

  const handleIngest = (e: React.FormEvent) => {
    e.preventDefault();
    const size = parseInt(packetSize, 10);
    if (isNaN(size)) {
      alert('Packet size must be an integer.');
      return;
    }

    setLoading(true);
    monitorApi.ingest({
      source,
      destination,
      dataCategory,
      sizeBytes: size
    })
    .then(() => {
      setLoading(false);
      refreshData();
    })
    .catch((err) => {
      console.error(err);
      setLoading(false);
      alert('Event ingestion failed: ' + err.message);
    });
  };

  const handleReset = () => {
    if (confirm('Reset monitor stats and clear traffic archives?')) {
      monitorApi.reset()
        .then(refreshData)
        .catch(console.error);
    }
  };

  const simulateStream = (malicious: boolean) => {
    setLoading(true);
    const mockIngests = malicious 
      ? [
          { source: 'RU', destination: 'DE', dataCategory: 'PERSONAL', sizeBytes: 1024 },
          { source: 'CN', destination: 'DE', dataCategory: 'CRITICAL', sizeBytes: 5120 },
          { source: 'US', destination: 'DE', dataCategory: 'HEALTH', sizeBytes: 2048 }
        ]
      : [
          { source: 'DE', destination: 'IN', dataCategory: 'PERSONAL', sizeBytes: 4096 },
          { source: 'FR', destination: 'JP', dataCategory: 'PERSONAL', sizeBytes: 2048 },
          { source: 'IT', destination: 'CH', dataCategory: 'PERSONAL', sizeBytes: 1024 }
        ];

    const promises = mockIngests.map(cmd => monitorApi.ingest(cmd));

    Promise.all(promises)
      .then(() => {
        setLoading(false);
        refreshData();
      })
      .catch((err) => {
        console.error(err);
        setLoading(false);
      });
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '0.25rem' }}>
            Continuous Compliance Monitor
          </h1>
          <p style={{ color: 'hsl(var(--text-secondary))' }}>
            Observe real-time cloud traffic movements, track rolling SLA metrics, and dispatch critical policy drift alerts.
          </p>
        </div>
        <button className="glass-btn" style={{ color: 'hsl(var(--color-danger))' }} onClick={handleReset}>
          <Trash2 size={14} /> Clear Ledger Logs
        </button>
      </div>

      {/* SLA Metrics Health Dials & Stats Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 2fr', gap: '2rem' }}>
        {/* Left: SLA Dial Card */}
        <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center', padding: '2rem 1.5rem' }}>
          <h4 style={{ fontSize: '0.9rem', fontWeight: 600, color: 'hsl(var(--text-secondary))', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Activity size={16} /> Compliance SLA Health Dials
          </h4>

          {/* Dials Circle visual representation */}
          <div style={{
            width: '130px',
            height: '130px',
            borderRadius: '50%',
            border: `8px solid ${
              metrics.complianceRate >= 90 ? 'rgba(16,185,129,0.1)' :
              metrics.complianceRate >= 70 ? 'rgba(245,158,11,0.1)' : 'rgba(244,63,94,0.1)'
            }`,
            borderTopColor: 
              metrics.complianceRate >= 90 ? 'hsl(var(--color-success))' :
              metrics.complianceRate >= 70 ? 'hsl(var(--color-warning))' : 'hsl(var(--color-danger))',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            marginBottom: '1rem',
            transform: 'rotate(-45deg)',
            transition: 'border-color 0.4s ease'
          }}>
            <div style={{ transform: 'rotate(45deg)', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <span style={{ fontSize: '1.8rem', fontWeight: 800 }}>
                {metrics.complianceRate.toFixed(1)}%
              </span>
              <span style={{ fontSize: '0.65rem', color: 'hsl(var(--text-muted))', fontWeight: 600 }}>SLA RATE</span>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', width: '100%', marginTop: '1rem', fontSize: '0.8rem', borderTop: '1px solid var(--glass-border)', paddingTop: '1rem' }}>
            <div>
              <div style={{ color: 'hsl(var(--text-secondary))' }}>Total Ingested</div>
              <div style={{ fontSize: '1.1rem', fontWeight: 700, marginTop: '0.2rem' }}>{metrics.totalEvents}</div>
            </div>
            <div>
              <div style={{ color: 'hsl(var(--text-secondary))' }}>Critical Alarms</div>
              <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'hsl(var(--color-danger))', marginTop: '0.2rem' }}>{metrics.violationCount}</div>
            </div>
          </div>
        </div>

        {/* Right: Simulation control panels */}
        <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Cpu size={18} /> Cloud Ingestion Simulator
          </h3>
          
          <form onSubmit={handleIngest} style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem' }}>
            <div>
              <label>Source geo</label>
              <input type="text" className="glass-input" required value={source} onChange={e => setSource(e.target.value.toUpperCase())} />
            </div>
            <div>
              <label>Dest geo</label>
              <input type="text" className="glass-input" required value={destination} onChange={e => setDestination(e.target.value.toUpperCase())} />
            </div>
            <div>
              <label>Data Category</label>
              <select className="glass-input" value={dataCategory} onChange={e => setDataCategory(e.target.value)}>
                <option value="PERSONAL">Personal Data</option>
                <option value="HEALTH">Health Data (PHI)</option>
                <option value="FINANCIAL">Financial Data</option>
                <option value="CRITICAL">Critical Infrastructure</option>
              </select>
            </div>
            <div>
              <label>Bytes</label>
              <input type="text" className="glass-input" required value={packetSize} onChange={e => setPacketSize(e.target.value)} />
            </div>
            <button type="submit" className="glass-btn glass-btn-primary" style={{ gridColumn: '1 / -1' }} disabled={loading}>
              <Send size={16} /> Disseminate Single Packet
            </button>
          </form>

          <div style={{ display: 'flex', gap: '1rem', borderTop: '1px solid var(--glass-border)', paddingTop: '1.2rem' }}>
            <button className="glass-btn" style={{ flex: 1, borderColor: 'rgba(16,185,129,0.3)' }} onClick={() => simulateStream(false)} disabled={loading}>
              Simulate Compliant Stream (DE➜IN, FR➜JP)
            </button>
            <button className="glass-btn" style={{ flex: 1, borderColor: 'rgba(244,63,94,0.3)', color: 'hsl(var(--color-danger))' }} onClick={() => simulateStream(true)} disabled={loading}>
              Simulate Violations Stream (RU➜DE, CN➜DE)
            </button>
          </div>
        </div>
      </div>

      {/* Main logs & Alerts splits */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '2rem' }}>
        {/* Left: Active Alerts violations tickers console */}
        <div className="glass-card" style={{ padding: 0 }}>
          <div style={{ padding: '1.2rem', borderBottom: '1px solid var(--glass-border)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <AlertOctagon size={18} style={{ color: 'hsl(var(--color-danger))' }} />
            <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>Active Compliance Alerts</h3>
          </div>
          <div style={{ maxHeight: '350px', overflowY: 'auto', display: 'flex', flexDirection: 'column' }}>
            {violations.length === 0 ? (
              <div style={{ padding: '3rem', textAlign: 'center', color: 'hsl(var(--text-muted))' }}>
                No active violations detected. Compliance SLA is 100%.
              </div>
            ) : (
              violations.map(v => (
                <div key={v.violationId} style={{
                  padding: '1rem 1.2rem',
                  borderBottom: '1px solid var(--glass-border)',
                  borderLeft: `4px solid ${v.severity === 'CRITICAL' ? 'hsl(var(--color-danger))' : 'hsl(var(--color-warning))'}`,
                  background: v.severity === 'CRITICAL' ? 'rgba(244,63,94,0.02)' : 'rgba(245,158,11,0.02)'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{
                      padding: '0.1rem 0.4rem',
                      borderRadius: '3px',
                      fontSize: '0.65rem',
                      fontWeight: 700,
                      background: v.severity === 'CRITICAL' ? 'rgba(244,63,94,0.1)' : 'rgba(245,158,11,0.1)',
                      color: v.severity === 'CRITICAL' ? 'hsl(var(--color-danger))' : 'hsl(var(--color-warning))'
                    }}>
                      {v.severity}
                    </span>
                    <span style={{ fontSize: '0.7rem', color: 'hsl(var(--text-muted))' }}>
                      Event: {v.eventId.substring(0,8)}...
                    </span>
                  </div>
                  <h4 style={{ fontSize: '0.85rem', fontWeight: 600, marginTop: '0.4rem' }}>{v.violatedRule}</h4>
                  <p style={{ fontSize: '0.75rem', color: 'hsl(var(--text-secondary))', marginTop: '0.2rem' }}>{v.description}</p>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Right: Traffic event log */}
        <div className="glass-card" style={{ padding: 0 }}>
          <div style={{ padding: '1.2rem', borderBottom: '1px solid var(--glass-border)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Terminal size={18} />
            <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>Simulated Data Stream Logs</h3>
          </div>
          <div style={{ maxHeight: '350px', overflowY: 'auto' }}>
            {events.length === 0 ? (
              <div style={{ padding: '3rem', textAlign: 'center', color: 'hsl(var(--text-muted))' }}>
                Traffic channel is silent. Stream events to inspect.
              </div>
            ) : (
              <table className="glass-table">
                <thead>
                  <tr>
                    <th>Flow Route</th>
                    <th>Category</th>
                    <th style={{ textAlign: 'right' }}>Size</th>
                  </tr>
                </thead>
                <tbody>
                  {events.map(e => (
                    <tr key={e.id}>
                      <td style={{ fontSize: '0.8rem', fontFamily: 'var(--font-mono)' }}>
                        <span style={{ fontWeight: 600 }}>{e.source}</span> ➜ {e.destination}
                      </td>
                      <td>
                        <span style={{ fontSize: '0.75rem', opacity: 0.8 }}>{e.dataCategory}</span>
                      </td>
                      <td style={{ textAlign: 'right', fontSize: '0.75rem', fontFamily: 'var(--font-mono)' }}>
                        {e.sizeBytes} B
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
