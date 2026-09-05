import React, { useState, useEffect } from 'react';
import { api } from '../api';

export default function Dashboard({ onLogout }) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [data, setData] = useState({
    summary: null,
    recycling: null,
    materials: null,
    collectors: null,
    recyclers: null,
    funnel: null,
    epr: null
  });

  // Controls
  const [params, setParams] = useState({
    from: '',
    to: '',
    includeDemo: true
  });

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const q = { ...params };
      // empty strings -> undefined
      if (!q.from) delete q.from;
      if (!q.to) delete q.to;

      const [sum, rec, mat, col, recy, fun, epr] = await Promise.all([
        api.getAnalytics('/summary', q),
        api.getAnalytics('/recycling', q),
        api.getAnalytics('/materials', q),
        api.getAnalytics('/collectors', q),
        api.getAnalytics('/recyclers', q),
        api.getAnalytics('/traceability/funnel', q),
        api.getAnalytics('/epr', q)
      ]);

      setData({
        summary: sum.data,
        recycling: rec.data,
        materials: mat.data,
        collectors: col.data,
        recyclers: recy.data,
        funnel: fun.data.funnel,
        epr: epr.data,
        eprNotes: epr.notes
      });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [params]); // refetch when params change

  const formatCurrency = (val) => {
    const num = Number(val);
    if (isNaN(num)) return '—';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(num);
  };
  const formatNum = (val) => {
    const num = Number(val);
    if (isNaN(num)) return '0';
    return new Intl.NumberFormat('en-IN').format(num);
  };

  return (
    <>
      <header className="dashboard-header">
        <h1>Kabadiwala Connect | Admin EPR Dashboard</h1>
        <div className="dashboard-controls">
          <div className="date-filter">
            <label>From:</label>
            <input type="date" value={params.from} onChange={e => setParams({...params, from: e.target.value})} />
            <label>To:</label>
            <input type="date" value={params.to} onChange={e => setParams({...params, to: e.target.value})} />
          </div>
          <div className="toggle-filter">
            <label>
              <input type="checkbox" checked={params.includeDemo} onChange={e => setParams({...params, includeDemo: e.target.checked})} />
              Include Demo Data
            </label>
          </div>
          <button className="btn-primary" onClick={fetchData}>Refresh</button>
          <button className="btn-primary" onClick={onLogout} style={{background: 'var(--text-muted)'}}>Logout</button>
        </div>
      </header>

      <main className="dashboard-content">
        {error && <div className="alert-error">{error}</div>}
        {loading && <div className="loading">Loading Analytics...</div>}

        {!loading && !error && (
          <>
            {/* Top KPIs */}
            <section className="top-kpis">
              <div className="kpi-card">
                <div className="kpi-title">Total Completed Transactions</div>
                <div className="kpi-value">{formatNum(data.summary.totalCompletedTransactions)}</div>
              </div>
              <div className="kpi-card">
                <div className="kpi-title">Total Financial Settlements</div>
                <div className="kpi-value">{formatCurrency(data.summary.totalAmount)}</div>
              </div>
              <div className="kpi-card">
                <div className="kpi-title">Active Collectors</div>
                <div className="kpi-value">{formatNum(data.summary.activeCollectors)}</div>
              </div>
              <div className="kpi-card">
                <div className="kpi-title">Active Recyclers</div>
                <div className="kpi-value">{formatNum(data.summary.activeRecyclers)}</div>
              </div>
            </section>

            {/* EPR and Funnel Row */}
            <section className="dashboard-row">
              <div className="widget">
                <h3>Ext. Producer Responsibility (EPR)</h3>
                <div style={{display: 'flex', gap: '2rem', margin: '1.5rem 0'}}>
                   <div>
                      <div className="kpi-title">Diverted Landfill Waste</div>
                      <div className="kpi-value" style={{color: 'var(--primary-green-dark)'}}>{formatNum(data.epr.totalDivertedKg)} kg</div>
                   </div>
                   <div>
                      <div className="kpi-title">CO2 Emissions Offset</div>
                      <div className="kpi-value" style={{color: 'var(--text-muted)', fontSize: '1.2rem', marginTop: '0.8rem'}}>
                         {data.epr.co2SavedKg || <span style={{fontSize: '0.8rem', padding: '0.3rem 0.6rem', border: '1px solid var(--border-light)', borderRadius: '12px'}}>{data.eprNotes?.co2Impact || 'Estimate not available'}</span>}
                      </div>
                   </div>
                </div>
                <div className="kpi-title">Verified CPCB Compliance Recyclers: {data.epr.verifiedRecyclers}</div>
              </div>

              <div className="widget">
                <h3>Traceability Completion Funnel</h3>
                <div className="funnel-container">
                  <div className="funnel-step">
                    <div className="funnel-circle">{data.funnel.lotsCreated}</div>
                    <div className="funnel-label">Lots Created</div>
                  </div>
                  <div className="funnel-step">
                    <div className="funnel-circle">{data.funnel.offersAccepted}</div>
                    <div className="funnel-label">Offers Accepted</div>
                  </div>
                  <div className="funnel-step">
                    <div className="funnel-circle">{data.funnel.handoversVerified}</div>
                    <div className="funnel-label">Handovers Verified</div>
                  </div>
                  <div className="funnel-step">
                    <div className="funnel-circle" style={{background: 'var(--primary-green-dark)'}}>{data.funnel.paymentsSettled}</div>
                    <div className="funnel-label">Payments Settled</div>
                  </div>
                </div>
              </div>
            </section>

            {/* Tables Row #1: Material & Collectors */}
            <section className="dashboard-row">
              <div className="widget">
                <h3>Material Breakdown</h3>
                {data.materials.length === 0 ? <p className="loading">No records found</p> : (
                  <div>
                    {data.materials.map((m) => {
                      const maxKg = Math.max(...data.materials.map(x => x.totalWeightKg));
                      const pct = Math.max(5, (m.totalWeightKg / maxKg) * 100);
                      return (
                        <div key={m.category} className="bar-chart-row">
                          <div className="bar-label">{m.category}</div>
                          <div className="bar-track">
                            <div className="bar-fill" style={{width: `${pct}%`}}></div>
                          </div>
                          <div className="bar-label" style={{textAlign: 'right'}}>{formatNum(m.totalWeightKg)} kg</div>
                        </div>
                      )
                    })}
                  </div>
                )}

                <h4 style={{marginTop: '2rem', marginBottom: '1rem', color: 'var(--text-muted)'}}>Rate Overview</h4>
                <table>
                  <thead>
                    <tr><th>Category</th><th>Avg Rate/Kg</th><th>Revenue</th></tr>
                  </thead>
                  <tbody>
                    {data.materials.map(m => (
                      <tr key={m.category + '-tbl'}>
                        <td>{m.category}</td>
                        <td>{formatCurrency(m.avgRatePerKg)}</td>
                        <td>{formatCurrency(m.totalAmount)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="widget">
                <h3>Collector Leaderboard</h3>
                <p className="kpi-title">Top 10 by Earnings</p>
                <table>
                  <thead>
                    <tr><th>Name</th><th>Lots</th><th>Total Kg</th><th>Earnings</th></tr>
                  </thead>
                  <tbody>
                    {data.collectors.collectors.slice(0, 10).map(c => (
                      <tr key={c.id}>
                        <td>{c.name}</td>
                        <td>{c.lotsCreated}</td>
                        <td>{formatNum(c.weightCollectedKg)}</td>
                        <td>{formatCurrency(c.totalEarnings)}</td>
                      </tr>
                    ))}
                    {data.collectors.collectors.length === 0 && <tr><td colSpan="4" style={{textAlign:'center'}}>No active collectors</td></tr>}
                  </tbody>
                </table>
              </div>
            </section>

            {/* Tables Row #2: Recyclers */}
            <section className="dashboard-row">
               <div className="widget">
                <h3>Recycler Compliance & Engagement</h3>
                <table>
                  <thead>
                    <tr><th>Company</th><th>CPCB Auth Status</th><th>Total Kg Processed</th><th>Total Settlement</th></tr>
                  </thead>
                  <tbody>
                    {data.recyclers.recyclers.map(r => (
                      <tr key={r.id}>
                        <td>{r.companyName}</td>
                        <td style={{color: r.cpcbStatus === 'AUTHORIZED' ? 'var(--primary-green-dark)' : 'var(--error)'}}>
                           {r.cpcbStatus}
                        </td>
                        <td>{formatNum(r.purchasedWeightKg)}</td>
                        <td>{formatCurrency(r.totalSpent)}</td>
                      </tr>
                    ))}
                    {data.recyclers.recyclers.length === 0 && <tr><td colSpan="4" style={{textAlign:'center'}}>No active recyclers</td></tr>}
                  </tbody>
                </table>
               </div>
            </section>
          </>
        )}
      </main>
    </>
  );
}
