import React, { useEffect, useRef, useState } from 'react';
import { api } from '../api/client';
import { showToast } from './Toast';

function fmtTime(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleString('en-IN', {
    day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit',
  });
}

/* Skeleton shown while the transcript is loading */
function ChatSkeleton() {
  const bar = (w, mt = 8) => (
    <div className="skeleton" style={{ height: 12, width: w, borderRadius: 6, marginTop: mt }} />
  );
  const bubble = (align, w1, w2) => (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: align, margin: '12px 0' }}>
      {bar('30%', 0)}
      <div className="skeleton" style={{ height: 38, width: w1, borderRadius: 10, marginTop: 6 }} />
      {w2 && <div className="skeleton" style={{ height: 28, width: w2, borderRadius: 10, marginTop: 6 }} />}
    </div>
  );
  return (
    <div style={{ padding: '16px 12px' }}>
      {bubble('flex-start', '75%', '55%')}
      {bubble('flex-end',   '65%', null)}
      {bubble('flex-start', '80%', '45%')}
      {bubble('flex-end',   '60%', null)}
    </div>
  );
}

/* Typing indicator shown while waiting for AI response */
function TypingBubble() {
  const dot = (delay) => (
    <span style={{
      display: 'inline-block', width: 7, height: 7, borderRadius: '50%',
      background: 'var(--lc-primary)', margin: '0 2px',
      animation: 'lc-skeleton-pulse 1.2s ease-in-out infinite',
      animationDelay: delay,
    }} />
  );
  return (
    <div className="chat-bubble AGENT" style={{ display: 'inline-flex', alignItems: 'center', gap: 2, padding: '10px 14px' }}>
      <span style={{ fontSize: 11, color: 'var(--lc-text-muted)', marginRight: 6 }}>AI thinking</span>
      {dot('0s')}{dot('0.2s')}{dot('0.4s')}
    </div>
  );
}

/**
 * Chat panel — the agent ONLY types what the customer said.
 *
 * On send: we call the turn-based backend endpoint, which appends the
 * customer message, runs Claude, and auto-appends Claude's suggested
 * agent reply. The updated transcript comes back with both messages
 * and the decision is forwarded to the right-hand recommendation panel.
 *
 * Props:
 *   scope:            'account' | 'portfolio'
 *   scopeId:          account id (scope=account) or customer id (scope=portfolio)
 *   agent:            logged-in agent
 *   onDecision(d):    decision handler for right panel
 *   onTranscriptRefresh(): callback to refresh parent when something changes
 */
export default function ChatPanel({ scope, scopeId, agent, onDecision, onDecisionLoading, lenderId }) {
  const [transcript, setTranscript] = useState(null);
  const [loading, setLoading]       = useState(true);
  const [sending, setSending]       = useState(false);
  const [draft, setDraft]           = useState('');
  const bodyRef = useRef(null);

  // Load the latest OPEN transcript on mount / scope change.
  // Uses a `cancelled` flag so stale async results from a previous account
  // never overwrite the current account's floor/ceiling values.
  useEffect(() => {
    let cancelled = false;

    const done    = (d)  => { if (!cancelled) { onDecision && onDecision(d); onDecisionLoading && onDecisionLoading(false); } };
    const loading_ = (v) => { if (!cancelled)   onDecisionLoading && onDecisionLoading(v); };

    setLoading(true);
    setTranscript(null);
    // Keep the previous recommendation visible while loading — don't clear to null.

    const loader = scope === 'portfolio'
      ? api.listPortfolioTranscripts(scopeId)
      : api.listTranscripts(scopeId);

    loader
      .then(list => {
        if (cancelled) return;
        const t = list && list.length > 0 ? list[0] : null;
        setTranscript(t);
        loading_(true);   // show loader in recommendation panel

        if (t && t.messages && t.messages.length > 0) {
          // Existing conversation — restore last AI recommendation for this account.
          api.analyze(t.id).then(done).catch(() => loading_(false));
        } else {
          // No messages yet — blank turn: backend skips saving a customer message
          // when content is empty but still runs Claude against account context.
          const body = scope === 'portfolio'
            ? { customerId: scopeId, agentId: agent?.id, lenderId, senderType: 'CUSTOMER', content: '' }
            : { accountId:  scopeId, agentId: agent?.id, senderType: 'CUSTOMER', content: '' };
          const turn = scope === 'portfolio' ? api.customerTurn : api.accountTurn;
          turn(body)
            .then(result => {
              if (cancelled) return;
              if (result.transcript) setTranscript(result.transcript);
              done(result.decision);
            })
            .catch(() => loading_(false));
        }
      })
      .finally(() => { if (!cancelled) setLoading(false); });

    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [scope, scopeId]);

  useEffect(() => {
    if (bodyRef.current) bodyRef.current.scrollTop = bodyRef.current.scrollHeight;
  }, [transcript, sending]);

  const send = async () => {
    const utterance = draft.trim();
    if (!utterance) return;
    setSending(true);
    onDecisionLoading && onDecisionLoading(true);
    try {
      const body = scope === 'portfolio'
        ? { customerId: scopeId, agentId: agent.id, lenderId, senderType: 'CUSTOMER', content: utterance }
        : { accountId:  scopeId, agentId: agent.id, senderType: 'CUSTOMER', content: utterance };

      const result = scope === 'portfolio'
        ? await api.customerTurn(body)
        : await api.accountTurn(body);

      setTranscript(result.transcript);
      onDecision && onDecision(result.decision);
      setDraft('');
    } catch (e) {
      showToast(e.message || 'Turn failed', { kind: 'error', duration: 5200 });
    } finally {
      setSending(false);
      onDecisionLoading && onDecisionLoading(false);
    }
  };

  if (loading) return (
    <div className="chat-panel">
      <div className="chat-header">
        <h4><span className="skeleton" style={{ display: 'inline-block', width: 160, height: 14, borderRadius: 4 }} /></h4>
      </div>
      <div className="chat-body"><ChatSkeleton /></div>
      <div className="chat-input" style={{ opacity: 0.4, pointerEvents: 'none' }}>
        <textarea rows={2} disabled placeholder="Loading…" />
        <button className="btn btn-accent" disabled>Submit ▶</button>
      </div>
    </div>
  );

  const messages = transcript?.messages || [];

  return (
    <div className="chat-panel">
      <div className="chat-header">
        <h4>
          Conversation {transcript ? `#${transcript.id}` : '— new'}
          {scope === 'portfolio'
            ? <span className="muted"> · portfolio-level</span>
            : <span className="muted"> · account-level</span>}
        </h4>
        {transcript?.sentiment && (
          <span className={`sentiment-pill ${transcript.sentiment}`}>
            {transcript.sentiment}
            {transcript.sentimentScore != null &&
              ` · ${Number(transcript.sentimentScore).toFixed(2)}`}
          </span>
        )}
      </div>

      <div className="chat-body" ref={bodyRef}>
        {messages.length === 0 && !sending ? (
          <div className="empty-state">
            <div className="icon">💬</div>
            <div>
              No messages yet. Type the customer's first utterance below —
              the assistant will draft the agent's reply for you.
            </div>
          </div>
        ) : (
          messages.map(m => (
            <div key={m.id} className={`chat-bubble ${m.senderType}`}>
              <div className="sender">{m.senderName || m.senderType}</div>
              <div>{m.content}</div>
              <div className="meta">
                <span>{fmtTime(m.createdAt)}</span>
                {m.sentiment && <span className={`sentiment-pill ${m.sentiment}`}
                                      style={{ padding: '1px 6px', fontSize: 9 }}>
                  {m.sentiment}
                </span>}
              </div>
            </div>
          ))
        )}
        {/* Typing indicator while waiting for AI response */}
        {sending && <TypingBubble />}
      </div>

      <div className="chat-input">
        <textarea
          rows={2}
          value={draft}
          placeholder="Type what the CUSTOMER just said — the assistant will draft the agent's reply"
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) send();
          }}
        />
        <button className="btn btn-accent" onClick={send} disabled={sending || !draft.trim()}>
          {sending ? '…' : 'Submit ▶'}
        </button>
      </div>
    </div>
  );
}
