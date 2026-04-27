import React, { useEffect, useState } from 'react';
import { formatINR } from '../api/client';

function dpdClass(bucket) {
  if (!bucket) return '';
  const key = bucket.toLowerCase().replace(/_/g, '-');
  if (key.includes('npa')) return 'dpd-pill dpd-npa';
  if (key.includes('90')) return 'dpd-pill dpd-90';
  if (key.includes('60')) return 'dpd-pill dpd-60';
  if (key.includes('30')) return 'dpd-pill dpd-30';
  return 'dpd-pill';
}

/**
 * Left-hand customer list. The list is already filtered by the
 * currently-selected LENDER (App.jsx drives that filter) — so every
 * account shown here belongs to the selected lender.
 */
export default function CustomerList({ customers, selection, onSelect, lenderName }) {
  const [expanded, setExpanded] = useState({});

  // Re-expand the relevant customer whenever selection or the data set changes.
  useEffect(() => {
    const initial = {};
    if (selection?.mode === 'account') {
      customers.forEach(c => {
        if (c.accounts?.some(a => a.id === selection.id)) initial[c.id] = true;
      });
    } else if (selection?.mode === 'portfolio') {
      initial[selection.id] = true;
    }
    setExpanded(prev => ({ ...prev, ...initial }));
  }, [selection, customers]);

  const toggle = (id) => setExpanded(prev => ({ ...prev, [id]: !prev[id] }));

  const isAcctSelected = (aid) =>
    selection?.mode === 'account' && selection.id === aid;
  const isPortfolioSelected = (cid) =>
    selection?.mode === 'portfolio' && selection.id === cid;

  return (
    <div>
      <div className="customer-list-header">
        <h2>Customers ({customers.length})</h2>
        <div className="subtitle">
          {lenderName ? `Showing accounts at ${lenderName}. ` : ''}
          Click a customer to see accounts. "Portfolio" bundles all lender accounts for one call.
        </div>
      </div>

      {customers.length === 0 && (
        <div className="empty-state" style={{ padding: 30 }}>
          <div className="icon">🏦</div>
          <div>No customers have accounts at this lender.</div>
        </div>
      )}

      {customers.map(c => (
        <div
          key={c.id}
          className={`customer-card ${expanded[c.id] ? 'expanded' : ''}`}
        >
          <div className="cust-header" onClick={() => toggle(c.id)}>
            <div>
              <div className="cust-name">{c.fullName}</div>
              <div className="cust-meta">
                {c.city}{c.state ? `, ${c.state}` : ''} · {c.phone}
              </div>
            </div>
            <div className="cust-totals">
              <div className="amt">{formatINR(c.totalOutstanding)}</div>
              <div className="count">{c.accountCount} account{c.accountCount === 1 ? '' : 's'}</div>
            </div>
          </div>

          {expanded[c.id] && (
            <div>
              <button
                type="button"
                className={`portfolio-pill ${isPortfolioSelected(c.id) ? 'selected' : ''}`}
                onClick={(e) => { e.stopPropagation(); onSelect({ mode: 'portfolio', id: c.id }); }}
              >
                ⚑ Portfolio — all {c.accountCount} accounts ({formatINR(c.totalOutstanding)})
              </button>

              {c.accounts.map(a => (
                <div
                  key={a.id}
                  className={`account-item ${isAcctSelected(a.id) ? 'selected' : ''}`}
                  onClick={(e) => { e.stopPropagation(); onSelect({ mode: 'account', id: a.id, customerId: c.id }); }}
                >
                  <div className="acct-row">
                    <div className="acct-no">{a.accountNumber}</div>
                    <div className="acct-amt">{formatINR(a.outstandingAmount)}</div>
                  </div>
                  <div className="acct-meta">
                    <span>{a.productType?.replace(/_/g, ' ')}</span>
                    <span className={dpdClass(a.dpdBucket)}>{a.dpdBucket}</span>
                    <span>{a.daysPastDue}d overdue</span>
                    {a.hasAcceptedSettlement && (
                      <span className="status-pill ACCEPTED" style={{ fontSize: 10 }}>
                        ✓ SETTLED
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
