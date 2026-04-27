import React, { useEffect, useState } from 'react';
import { api, formatINR } from '../api/client';
import ChatPanel from './ChatPanel';
import SettlementPanel from './SettlementPanel';

function dpdClass(bucket) {
  if (!bucket) return '';
  const k = bucket.toLowerCase();
  if (k.includes('npa')) return 'dpd-pill dpd-npa';
  if (k.includes('90')) return 'dpd-pill dpd-90';
  if (k.includes('60')) return 'dpd-pill dpd-60';
  if (k.includes('30')) return 'dpd-pill dpd-30';
  return 'dpd-pill';
}

/**
 * Customer-level (portfolio) view — bundles all of the customer's accounts
 * at the SELECTED LENDER into a single conversation with Claude.
 */
export default function PortfolioDetail({ customerId, agent, onDecision, onDecisionLoading, lenderId, settlementSavedAt }) {
  const [customer, setCustomer] = useState(null);
  const [tab, setTab] = useState('chat');

  // Re-fetch when lender changes OR a settlement was just saved (updates flags).
  useEffect(() => {
    api.getCustomer(customerId, lenderId).then(setCustomer);
  }, [customerId, lenderId, settlementSavedAt]);

  useEffect(() => {
    if (settlementSavedAt) setTab('settlement');
  }, [settlementSavedAt]);

  if (!customer) return <div className="loading">Loading customer…</div>;

  const eligibleAccounts = (customer.accounts || []).filter(a => !a.hasAcceptedSettlement);
  const total         = eligibleAccounts.reduce((s, a) => s + (a.outstandingAmount || 0), 0);
  const totalAllAccts = (customer.accounts || []).reduce((s, a) => s + (a.outstandingAmount || 0), 0);
  const worstDpd      = (customer.accounts || []).reduce((m, a) => Math.max(m, a.daysPastDue || 0), 0);
  const allSettled    = (customer.accounts || []).length > 0 && eligibleAccounts.length === 0;

  return (
    <div>
      <div className="portfolio-header">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <h3>⚑ Portfolio view — {customer.fullName}</h3>
            <div className="sub">
              {customer.accountCount} accounts at this lender · {customer.city}, {customer.state}
              &nbsp;·&nbsp; {customer.phone}
            </div>
          </div>
        </div>

        {allSettled && (
          <div
            style={{
              marginTop: 10, padding: '8px 12px', borderRadius: 6,
              background: '#fff3cd', border: '1px solid #ffc107', color: '#664d03',
              fontSize: 13, fontWeight: 500,
            }}
          >
            ⚠ All of this customer's accounts at this lender already have accepted settlements.
            BUNDLE proposals will be rejected.
          </div>
        )}

        <div className="account-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)' }}>
          <div className="metric">
            <div className="label">Bundle outstanding</div>
            <div className="value" style={{ color: 'var(--lc-accent-dark)' }}>{formatINR(total)}</div>
          </div>
          <div className="metric">
            <div className="label">Bundleable accounts</div>
            <div className="value">{eligibleAccounts.length} / {customer.accountCount}</div>
          </div>
          <div className="metric">
            <div className="label">Worst DPD</div>
            <div className="value">{worstDpd}d</div>
          </div>
          <div className="metric">
            <div className="label">Annual income</div>
            <div className="value">{formatINR(customer.annualIncome)}</div>
          </div>
        </div>
      </div>

      <div className="portfolio-accts">
        <table>
          <thead>
            <tr>
              <th>Account</th>
              <th>Product</th>
              <th>DPD</th>
              <th>Days</th>
              <th>Assigned</th>
              <th>Status</th>
              <th style={{ textAlign: 'right' }}>Outstanding</th>
            </tr>
          </thead>
          <tbody>
            {customer.accounts.map(a => (
              <tr key={a.id} style={a.hasAcceptedSettlement ? { opacity: 0.55 } : {}}>
                <td className="num">{a.accountNumber}</td>
                <td>{a.productType?.replace(/_/g, ' ')}</td>
                <td><span className={dpdClass(a.dpdBucket)}>{a.dpdBucket}</span></td>
                <td>{a.daysPastDue}</td>
                <td>{a.assignedAgentName || '—'}</td>
                <td>
                  {a.hasAcceptedSettlement
                    ? <span className="status-pill ACCEPTED" style={{ fontSize: 10 }}>SETTLED</span>
                    : <span style={{ color: '#059669', fontSize: 11 }}>eligible</span>}
                </td>
                <td style={{ textAlign: 'right', fontWeight: 600 }}>
                  {formatINR(a.outstandingAmount)}
                </td>
              </tr>
            ))}
            <tr className="total">
              <td colSpan={6}>TOTAL (all accounts)</td>
              <td style={{ textAlign: 'right' }}>{formatINR(totalAllAccts)}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div className="tabs">
        <button className={tab === 'chat' ? 'active' : ''} onClick={() => setTab('chat')}>💬 Chat</button>
        <button className={tab === 'settlement' ? 'active' : ''} onClick={() => setTab('settlement')}>🤝 Settlements</button>
      </div>

      {tab === 'chat' && (
        <ChatPanel
          scope="portfolio"
          scopeId={customer.id}
          agent={agent}
          onDecision={onDecision}
          onDecisionLoading={onDecisionLoading}
          lenderId={lenderId}
        />
      )}
      {tab === 'settlement' && (
        <SettlementPanel customerId={customer.id} refreshKey={settlementSavedAt} />
      )}
    </div>
  );
}
