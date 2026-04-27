import React, { useEffect, useState } from 'react';
import { api, formatINR } from '../api/client';
import { showToast } from './Toast';
import PromptModal from './PromptModal';

function fmtDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

/**
 * Lists the settlement proposals for either a single account or a customer.
 * Each row shows every account the settlement covers (multiple for BUNDLE).
 *
 * Props:
 *   accountId?:   load settlements for this account (primary OR linked)
 *   customerId?:  load all settlements for this customer
 *   refreshKey:   bump to force reload
 */
export default function SettlementPanel({ accountId, customerId, refreshKey }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  // { settlementId, status } — when set, the themed modal is visible.
  const [pending, setPending] = useState(null);

  const load = () => {
    setLoading(true);
    const loader = customerId
      ? api.listSettlementsForCustomer(customerId)
      : api.listSettlements(accountId);
    loader
      .then(setItems)
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); /* eslint-disable-next-line react-hooks/exhaustive-deps */ }, [accountId, customerId, refreshKey]);

  const askForResponse = (id, status) => setPending({ id, status });

  const submitStatus = async (response) => {
    if (!pending) return;
    const { id, status } = pending;
    try {
      await api.updateSettlement(id, { status, customerResponse: response });
      showToast(`Settlement marked ${status.toLowerCase()}.`, { kind: 'success' });
      load();
    } catch (e) {
      showToast(e.message || 'Update failed', { kind: 'error', duration: 5200 });
    }
  };

  const kindFor = (status) => {
    if (status === 'ACCEPTED') return 'success';
    if (status === 'REJECTED') return 'danger';
    return 'primary';
  };

  if (loading) return <div className="loading">Loading settlements…</div>;

  if (items.length === 0) {
    return (
      <div className="empty-state">
        <div className="icon">🤝</div>
        <div>No settlement proposals yet.</div>
        <div className="muted" style={{ marginTop: 6 }}>
          Ask the assistant to analyze a conversation and it'll recommend one.
        </div>
      </div>
    );
  }

  return (
    <div>
      {items.map(s => {
        const isBundle = (s.strategyCode || '').toUpperCase() === 'BUNDLE'
          || (s.linkedAccounts && s.linkedAccounts.length > 1);
        return (
          <div key={s.id} className="settlement-card">
            <div className="s-header">
              <div>
                <div className="s-offer">
                  {formatINR(s.offeredAmount)} offer
                  &nbsp;<span className="s-discount">({Number(s.discountPercent || 0).toFixed(1)}% off)</span>
                  {isBundle && (
                    <span
                      style={{
                        marginLeft: 10, padding: '2px 8px', borderRadius: 12,
                        background: '#fbbf24', color: '#78350f',
                        fontSize: 10, fontWeight: 700, letterSpacing: 0.5,
                      }}
                    >
                      BUNDLE · {s.linkedAccounts?.length || 0} accts
                    </span>
                  )}
                  {!isBundle && s.strategyCode && (
                    <span
                      style={{
                        marginLeft: 10, padding: '2px 8px', borderRadius: 12,
                        background: '#e5e7eb', color: '#374151',
                        fontSize: 10, fontWeight: 700, letterSpacing: 0.5,
                      }}
                    >
                      {s.strategyCode}
                    </span>
                  )}
                </div>
                <div className="muted">
                  on outstanding {formatINR(s.outstandingAtOffer)}
                  &nbsp;·&nbsp; {s.paymentPlan?.replace(/_/g, ' ')}
                  {s.numberOfInstallments > 1 && ` · ${s.numberOfInstallments} installments`}
                  {s.lenderName && <> &nbsp;·&nbsp; {s.lenderName}</>}
                </div>
              </div>
              <span className={`status-pill ${s.status}`}>{s.status}</span>
            </div>

            {s.linkedAccounts && s.linkedAccounts.length > 0 && (
              <div style={{ margin: '8px 0', padding: '8px 10px', background: '#f8fafc', borderRadius: 6 }}>
                <div style={{ fontSize: 10, fontWeight: 700, color: '#64748b', letterSpacing: 0.8, marginBottom: 4 }}>
                  {isBundle ? 'ACCOUNTS BUNDLED IN THIS SETTLEMENT' : 'ACCOUNT'}
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                  {s.linkedAccounts.map(la => (
                    <span
                      key={la.id}
                      style={{
                        display: 'inline-flex', alignItems: 'center', gap: 6,
                        padding: '4px 8px', borderRadius: 6,
                        background: '#fff', border: '1px solid #e2e8f0', fontSize: 11,
                      }}
                      title={la.productType}
                    >
                      <strong>{la.accountNumber}</strong>
                      <span style={{ color: '#64748b' }}>
                        {la.productType?.replace(/_/g, ' ')}
                      </span>
                      <span style={{ fontWeight: 600 }}>{formatINR(la.outstandingAmount)}</span>
                    </span>
                  ))}
                </div>
              </div>
            )}

            <div className="s-meta">
              <div>
                <div className="k">Proposed by</div>
                <div className="v">{s.source === 'CLAUDE_AI' ? '🧠 AI assistant' : (s.proposedByAgentName || '—')}</div>
              </div>
              <div>
                <div className="k">Proposed on</div>
                <div className="v">{fmtDate(s.createdAt)}</div>
              </div>
              <div>
                <div className="k">Target payment</div>
                <div className="v">{s.proposedPaymentDate || '—'}</div>
              </div>
            </div>

            {s.rationale && <div className="rationale"><strong>Rationale:</strong> {s.rationale}</div>}
            {s.customerResponse && <div className="rationale"><strong>Customer response:</strong> {s.customerResponse}</div>}

            {s.status === 'PROPOSED' && (
              <div style={{ marginTop: 10, display: 'flex', gap: 6 }}>
                <button className="btn btn-sm" onClick={() => askForResponse(s.id, 'ACCEPTED')}>Mark accepted</button>
                <button className="btn btn-sm btn-outline" onClick={() => askForResponse(s.id, 'COUNTERED')}>Countered</button>
                <button className="btn btn-sm btn-outline" onClick={() => askForResponse(s.id, 'REJECTED')}>Rejected</button>
              </div>
            )}
          </div>
        );
      })}

      {pending && (
        <PromptModal
          title={`Mark settlement ${pending.status.toLowerCase()}`}
          label="Record what the customer said so it shows in the settlement history."
          placeholder={
            pending.status === 'ACCEPTED' ? 'e.g. Confirmed payment plan, will pay ₹15,000 on 30 Apr.' :
            pending.status === 'COUNTERED' ? 'e.g. Asked for ₹25,000 instead.' :
            'e.g. Refused, cited job loss.'
          }
          confirmLabel={`Mark ${pending.status.toLowerCase()}`}
          kind={kindFor(pending.status)}
          multiline
          onSubmit={submitStatus}
          onClose={() => setPending(null)}
        />
      )}
    </div>
  );
}
