import React, { useEffect, useState } from 'react';
import { api, formatINR } from '../api/client';
import ChatPanel from './ChatPanel';
import SettlementPanel from './SettlementPanel';
import HistoryPanel from './HistoryPanel';

function dpdClass(bucket) {
  if (!bucket) return '';
  const k = bucket.toLowerCase();
  if (k.includes('npa')) return 'dpd-pill dpd-npa';
  if (k.includes('90')) return 'dpd-pill dpd-90';
  if (k.includes('60')) return 'dpd-pill dpd-60';
  if (k.includes('30')) return 'dpd-pill dpd-30';
  return 'dpd-pill';
}

export default function AccountDetail({ accountId, agent, onDecision, onDecisionLoading, lenderId, settlementSavedAt }) {
  const [account, setAccount] = useState(null);
  const [tab, setTab] = useState('chat');

  // Re-fetch account whenever id changes OR a settlement was just saved.
  useEffect(() => {
    api.getAccount(accountId).then(setAccount);
  }, [accountId, settlementSavedAt]);

  // Whenever a new settlement is saved, jump to the Settlements tab so the
  // user sees the record they just created.
  useEffect(() => {
    if (settlementSavedAt) setTab('settlement');
  }, [settlementSavedAt]);

  if (!account) return <div className="loading">Loading account…</div>;

  return (
    <div>
      <div className="account-header">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <h3>
              {account.accountNumber}
              <span className="muted" style={{ marginLeft: 10, fontWeight: 400 }}>
                · {account.productType?.replace(/_/g, ' ')}
              </span>
              {account.lenderName && (
                <span className="muted" style={{ marginLeft: 10, fontWeight: 400, fontSize: 13 }}>
                  · {account.lenderName}
                </span>
              )}
            </h3>
            <div className="sub">
              Customer: <strong>{account.customerName}</strong>
              &nbsp;·&nbsp; Assigned to: <strong>{account.assignedAgentName || 'Unassigned'}</strong>
            </div>
          </div>
          <span className={dpdClass(account.dpdBucket)}>{account.dpdBucket}</span>
        </div>

        {account.hasAcceptedSettlement && (
          <div
            style={{
              marginTop: 10, padding: '8px 12px', borderRadius: 6,
              background: '#fff3cd', border: '1px solid #ffc107', color: '#664d03',
              fontSize: 13, fontWeight: 500,
            }}
          >
            ⚠ A settlement has already been accepted for this account. New proposals will be rejected.
          </div>
        )}

        <div className="account-grid">
          <div className="metric">
            <div className="label">Outstanding</div>
            <div className="value" style={{ color: 'var(--lc-accent-dark)' }}>
              {formatINR(account.outstandingAmount)}
            </div>
          </div>
          <div className="metric">
            <div className="label">Principal</div>
            <div className="value">{formatINR(account.principalOutstanding)}</div>
          </div>
          <div className="metric">
            <div className="label">Interest + Penalty</div>
            <div className="value">
              {formatINR((account.interestOutstanding || 0) + (account.penaltyAmount || 0))}
            </div>
          </div>
          <div className="metric">
            <div className="label">Days past due</div>
            <div className="value">{account.daysPastDue}d</div>
          </div>
          <div className="metric">
            <div className="label">Sanctioned</div>
            <div className="value">{formatINR(account.sanctionedAmount)}</div>
          </div>
          <div className="metric">
            <div className="label">Last payment</div>
            <div className="value" style={{ fontSize: 13 }}>
              {formatINR(account.lastPaymentAmount)}
              <div className="muted" style={{ fontSize: 10, fontWeight: 400 }}>
                on {account.lastPaymentDate || '—'}
              </div>
            </div>
          </div>
          <div className="metric">
            <div className="label">Rate</div>
            <div className="value">{account.interestRate}%</div>
          </div>
          <div className="metric">
            <div className="label">Status</div>
            <div className="value" style={{ fontSize: 13 }}>{account.status}</div>
          </div>
        </div>
      </div>

      <div className="tabs">
        <button className={tab === 'chat' ? 'active' : ''} onClick={() => setTab('chat')}>💬 Chat</button>
        <button className={tab === 'settlement' ? 'active' : ''} onClick={() => setTab('settlement')}>🤝 Settlements</button>
        <button className={tab === 'history' ? 'active' : ''} onClick={() => setTab('history')}>📜 History</button>
      </div>

      {tab === 'chat' && (
        <ChatPanel
          scope="account"
          scopeId={account.id}
          agent={agent}
          onDecision={onDecision}
          onDecisionLoading={onDecisionLoading}
          lenderId={lenderId}
        />
      )}
      {tab === 'settlement' && (
        <SettlementPanel accountId={accountId} refreshKey={settlementSavedAt} />
      )}
      {tab === 'history' && <HistoryPanel accountId={accountId} />}
    </div>
  );
}
