import React, { useEffect, useState } from 'react';
import Login from './components/Login';
import CustomerList from './components/CustomerList';
import AccountDetail from './components/AccountDetail';
import PortfolioDetail from './components/PortfolioDetail';
import RecommendationPanel from './components/RecommendationPanel';
import { ToastHost, showToast } from './components/Toast';
import { api } from './api/client';

export default function App() {
  const [agent, setAgent] = useState(() => {
    const raw = window.sessionStorage.getItem('lc_agent');
    return raw ? JSON.parse(raw) : null;
  });

  const [lenders, setLenders]   = useState([]);
  const [lenderId, setLenderId] = useState(() => {
    const raw = window.sessionStorage.getItem('lc_lender_id');
    return raw ? Number(raw) : null;
  });

  const [customers, setCustomers] = useState([]);
  /**
   * selection: { mode: 'account', id, customerId } | { mode: 'portfolio', id } | null
   */
  const [selection, setSelection] = useState(null);
  const [decision, setDecision]               = useState(null);
  const [decisionLoading, setDecisionLoading] = useState(false);
  const [loadingCustomers, setLoadingCustomers] = useState(false);

  // Bumps on every successful settlement save so child panels reload.
  const [settlementSavedAt, setSettlementSavedAt] = useState(0);

  // Load lenders once logged in; default-select the agent's own lender.
  useEffect(() => {
    if (!agent) return;
    api.listLenders().then(list => {
      setLenders(list || []);
      if (!lenderId && list && list.length) {
        // Try to match the agent's lender by name — fallback to first.
        const matched = list.find(l => l.name === agent.lenderName);
        const pick = matched ? matched.id : list[0].id;
        setLenderId(pick);
        window.sessionStorage.setItem('lc_lender_id', String(pick));
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [agent]);

  // Reload the customer list whenever the selected lender changes.
  useEffect(() => {
    if (!agent || !lenderId) return;
    setLoadingCustomers(true);
    // Drop any selection that belongs to the old lender.
    setSelection(null);
    setDecision(null);
    api.listCustomers(lenderId)
      .then(setCustomers)
      .finally(() => setLoadingCustomers(false));
  }, [agent, lenderId]);

  const handleLogin = (a) => {
    window.sessionStorage.setItem('lc_agent', JSON.stringify(a));
    setAgent(a);
  };

  const handleLogout = () => {
    window.sessionStorage.removeItem('lc_agent');
    window.sessionStorage.removeItem('lc_lender_id');
    setAgent(null);
    setLenderId(null);
    setSelection(null);
    setDecision(null);
  };

  const handleLenderChange = (id) => {
    const num = Number(id);
    setLenderId(num);
    window.sessionStorage.setItem('lc_lender_id', String(num));
  };

  const reloadCustomers = () => {
    if (!lenderId) return;
    api.listCustomers(lenderId).then(setCustomers);
  };

  const handleSelect = (sel) => {
    setSelection(sel);
    setDecision(null);
    setDecisionLoading(false);
  };

  const handleSaveSettlement = async () => {
    if (!decision) return;
    try {
      let targetAccountId = null;

      if (selection?.mode === 'account') {
        targetAccountId = selection.id;
      } else if (selection?.mode === 'portfolio') {
        // For non-BUNDLE strategies the decision carries the single primary
        // account the offer numbers were computed for — save against that one.
        // For BUNDLE we can use any eligible account; the backend will link
        // all eligible accounts.
        const cust = customers.find(c => c.id === selection.id);
        if (!cust || !cust.accounts || cust.accounts.length === 0) {
          showToast('This customer has no accounts at the selected lender.', { kind: 'warning' });
          return;
        }
        if (decision.primaryAccountId) {
          targetAccountId = decision.primaryAccountId;
        } else {
          targetAccountId = cust.accounts[0].id;
        }
      } else {
        showToast('Pick an account or portfolio first.', { kind: 'warning' });
        return;
      }

      const saved = await api.createFromClaude({
        accountId: targetAccountId,
        agentId:   agent.id,
        decision,
      });

      // Bump the refresh key so Settlement panels reload, and refresh sidebar badges.
      setSettlementSavedAt(Date.now());
      reloadCustomers();

      const count = saved?.linkedAccounts?.length || 1;
      const scope = count > 1
        ? ` (bundled across ${count} accounts)`
        : '';
      showToast(`Settlement proposal saved${scope}.`, { kind: 'success' });
    } catch (e) {
      showToast(e.message || 'Failed to save settlement', { kind: 'error', duration: 5200 });
    }
  };

  if (!agent) return <Login onLogin={handleLogin} />;

  const scopeLabel = selection?.mode === 'portfolio'
    ? 'Customer-level: the assistant is weighing all accounts of this lender together.'
    : selection?.mode === 'account'
      ? 'Account-level guidance for this conversation.'
      : null;

  const selectedLender = lenders.find(l => l.id === lenderId);

  return (
    <div>
      <div className="lc-topbar">
        <div className="brand">
          <div className="logo">{selectedLender?.code || 'LC'}</div>
          <div>
            Collections Workbench
            <span className="subtitle">AI-assisted settlement &amp; recovery</span>
          </div>
        </div>
        <div className="agent-info">
          <div className="lender-switcher">
            <label>LENDER</label>
            <select
              value={lenderId || ''}
              onChange={(e) => handleLenderChange(e.target.value)}
            >
              {lenders.map(l => (
                <option key={l.id} value={l.id}>
                  {l.name}{l.code ? ` (${l.code})` : ''}
                </option>
              ))}
            </select>
          </div>
          <div>
            <span className="agent-name">{agent.name}</span>
            <span className="agent-role">{agent.employeeCode} · {agent.region}</span>
          </div>
          <button className="logout" onClick={handleLogout}>Sign out</button>
        </div>
      </div>

      <div className="workbench">
        {/* -------- Left: customer / account list -------- */}
        <div className="sidebar">
          {loadingCustomers
            ? <div className="loading">Loading customers…</div>
            : <CustomerList
                customers={customers}
                selection={selection}
                onSelect={handleSelect}
                lenderName={selectedLender?.name}
              />
          }
        </div>

        {/* -------- Middle: detail + chat -------- */}
        <div className="main-area">
          {selection?.mode === 'account' && (
            <AccountDetail
              accountId={selection.id}
              agent={agent}
              onDecision={setDecision}
              onDecisionLoading={setDecisionLoading}
              lenderId={lenderId}
              settlementSavedAt={settlementSavedAt}
            />
          )}
          {selection?.mode === 'portfolio' && (
            <PortfolioDetail
              customerId={selection.id}
              agent={agent}
              onDecision={setDecision}
              onDecisionLoading={setDecisionLoading}
              lenderId={lenderId}
              settlementSavedAt={settlementSavedAt}
            />
          )}
          {!selection && (
            <div className="empty-state">
              <div className="icon">👤</div>
              <h3 style={{ margin: 0, color: 'var(--lc-primary-dark)' }}>
                Select a customer to begin
              </h3>
              <p>
                Pick an individual account from the left, or hit the gold
                "Portfolio" bundle to negotiate all of a customer's accounts at this lender together.
              </p>
            </div>
          )}
        </div>

        {/* -------- Right: Claude recommendations -------- */}
        <div className="right-panel">
          <RecommendationPanel
            decision={decision}
            loading={decisionLoading}
            onSaveSettlement={decision?.shouldOfferSettlement ? handleSaveSettlement : null}
            scopeLabel={scopeLabel}
          />
        </div>
      </div>

      <ToastHost />
    </div>
  );
}
