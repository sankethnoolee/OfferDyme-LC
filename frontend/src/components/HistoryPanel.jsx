import React, { useEffect, useState } from 'react';
import { api } from '../api/client';

function fmtTime(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

export default function HistoryPanel({ accountId }) {
  const [transcripts, setTranscripts] = useState([]);
  const [selected, setSelected] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    api.listTranscripts(accountId).then(list => {
      setTranscripts(list);
      if (list.length > 0) setSelected(list[0].id);
    }).finally(() => setLoading(false));
  }, [accountId]);

  useEffect(() => {
    if (!selected) { setHistory([]); return; }
    api.getHistory(selected).then(setHistory);
  }, [selected]);

  if (loading) return <div className="loading">Loading history…</div>;

  if (transcripts.length === 0) {
    return (
      <div className="empty-state">
        <div className="icon">📜</div>
        <div>No transcript history yet.</div>
      </div>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: 12 }}>
        <label className="muted" style={{ display: 'block', marginBottom: 4 }}>Transcript</label>
        <select
          value={selected || ''}
          onChange={(e) => setSelected(Number(e.target.value))}
          style={{ padding: '8px 10px', border: '1px solid var(--lc-border)',
                   borderRadius: 6, fontSize: 13, background: 'white' }}
        >
          {transcripts.map(t => (
            <option key={t.id} value={t.id}>
              #{t.id} · {t.channel} · {t.status} · {fmtTime(t.startedAt)}
            </option>
          ))}
        </select>
      </div>

      {history.length === 0 && (
        <div className="muted">No snapshot history captured for this transcript yet.</div>
      )}

      {history.map(h => (
        <div key={h.id} className="history-entry">
          <div className="he-header">
            <span><strong>{h.changeReason}</strong> · {fmtTime(h.capturedAt)}</span>
            {h.sentiment && (
              <span className={`sentiment-pill ${h.sentiment}`}>{h.sentiment}</span>
            )}
          </div>
          {h.summary && <div className="he-summary">{h.summary}</div>}
          {h.fullText && <div className="he-full">{h.fullText}</div>}
        </div>
      ))}
    </div>
  );
}
