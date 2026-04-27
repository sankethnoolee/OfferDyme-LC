import React, { useEffect, useState } from 'react';
import { api } from '../api/client';

export default function Login({ onLogin }) {
  const [agents, setAgents] = useState([]);
  const [selectedId, setSelectedId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.listAgents()
      .then(data => {
        setAgents(data);
        if (data.length) setSelectedId(String(data[0].id));
      })
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    const agent = agents.find(a => String(a.id) === selectedId);
    if (agent) onLogin(agent);
  };

  return (
    <div className="login-shell">
      <form className="login-card" onSubmit={handleSubmit}>
        <div className="login-logo">
          <div className="l-mark">LC</div>
          <div style={{ fontSize: 11, color: 'var(--lc-text-muted)', letterSpacing: 1 }}>
            COLLECTIONS WORKBENCH
          </div>
        </div>
        <h1>Field Agent Sign-in</h1>
        <p className="sub">Select your profile to continue</p>
        {error && <div style={{ color: 'var(--lc-danger)', fontSize: 12, marginBottom: 10 }}>{error}</div>}

        <label>Field Agent</label>
        <select
          value={selectedId}
          onChange={(e) => setSelectedId(e.target.value)}
          disabled={loading}
        >
          {loading && <option>Loading agents…</option>}
          {agents.map(a => (
            <option key={a.id} value={a.id}>
              {a.name} · {a.employeeCode} · {a.region}
            </option>
          ))}
        </select>

        <button type="submit" className="btn btn-accent" disabled={loading || !selectedId}>
          Sign in
        </button>
      </form>
    </div>
  );
}
