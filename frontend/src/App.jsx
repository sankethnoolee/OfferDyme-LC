import React, { useEffect, useState } from 'react';
import { api } from './api.js';
import CustomerList from './components/CustomerList.jsx';
import AccountPanel from './components/AccountPanel.jsx';
import NegotiationPanel from './components/NegotiationPanel.jsx';
import OfferHistory from './components/OfferHistory.jsx';

export default function App() {
  const [agents, setAgents] = useState([]);
  const [agentId, setAgentId] = useState(1);
  const [customers, setCustomers] = useState([]);
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [accounts, setAccounts] = useState([]);
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [session, setSession] = useState(null);
  const [transcript, setTranscript] = useState([]);
  const [offers, setOffers] = useState([]);
  const [lastTurn, setLastTurn] = useState(null);
  const [audit, setAudit] = useState(null);

  useEffect(() => {
    api.listAgents().then(setAgents);
    api.listCustomers().then(setCustomers);
    api.guardrailAudit().then(setAudit);
  }, []);

  useEffect(() => {
    if (!selectedCustomer) { setAccounts([]); return; }
    api.customerAccounts(selectedCustomer.customerId).then(setAccounts);
    setSelectedAccount(null);
    resetSession();
  }, [selectedCustomer]);

  const resetSession = () => {
    setSession(null); setTranscript([]); setOffers([]); setLastTurn(null);
  };

  const startSession = async (bundle) => {
    if (!selectedCustomer || !selectedAccount) return;
    const s = await api.startSession(agentId, {
      customerId: selectedCustomer.customerId,
      accountId:  selectedAccount.accountId,
      attemptBundle: !!bundle
    });
    setSession(s);
    refreshSession(s.sessionId);
  };

  const refreshSession = async (sid) => {
    const id = sid || session?.sessionId;
    if (!id) return;
    setTranscript(await api.transcript(id));
    setOffers(await api.offers(id));
    setAudit(await api.guardrailAudit());
  };

  const sendTurn = async (text) => {
    if (!session) return;
    const t = await api.sendTurn(agentId, session.sessionId, text);
    setLastTurn(t);
    refreshSession(session.sessionId);
  };

  const accept = async () => {
    if (!session) return;
    await api.accept(agentId, session.sessionId);
    alert('Settlement recorded.');
    refreshSession(session.sessionId);
  };

  const reject = async () => {
    if (!session) return;
    await api.reject(agentId, session.sessionId);
    refreshSession(session.sessionId);
  };

  return (
    <>
      <header>
        <div>
          <h1>OfferDyne · Dynamic Settlement Optimizer</h1>
          <div className="sub">
            Live negotiation · guardrail-compliant · strategy-aware
          </div>
        </div>
        <div className="toolbar">
          <label style={{fontSize:12, color:'#9aa3b2'}}>Agent</label>
          <select value={agentId} onChange={e => setAgentId(Number(e.target.value))}>
            {agents.map(a =>
              <option key={a.agentId} value={a.agentId}>
                {a.agentCode} · {a.agentName}
              </option>
            )}
          </select>
          {audit && (
            <span className="badge negotiation" title="All offers pass guardrail">
              Guardrail: {audit.complianceRatePct}% · {audit.totalOffers} offers
            </span>
          )}
        </div>
      </header>

      <div className="layout">
        <div className="panel">
          <CustomerList
            customers={customers}
            selected={selectedCustomer}
            onSelect={setSelectedCustomer} />
          {selectedCustomer && (
            <AccountPanel
              accounts={accounts}
              selected={selectedAccount}
              onSelect={setSelectedAccount} />
          )}
        </div>

        <div className="panel" style={{padding:0}}>
          <NegotiationPanel
            customer={selectedCustomer}
            account={selectedAccount}
            session={session}
            transcript={transcript}
            lastTurn={lastTurn}
            onStart={startSession}
            onTurn={sendTurn}
            onAccept={accept}
            onReject={reject} />
        </div>

        <div className="panel right">
          <OfferHistory offers={offers} audit={audit} lastTurn={lastTurn} />
        </div>
      </div>
    </>
  );
}
