// Thin fetch wrapper. Everything goes through /api via Vite proxy.
const AGENT_HEADER = 'X-Agent-Id';

function headers(agentId) {
  return {
    'Content-Type': 'application/json',
    [AGENT_HEADER]: String(agentId ?? 1)
  };
}

async function j(res) {
  if (!res.ok) throw new Error(`${res.status} ${await res.text()}`);
  const ct = res.headers.get('content-type') || '';
  return ct.includes('application/json') ? res.json() : res.text();
}

export const api = {
  listAgents:   () => fetch('/api/agents').then(j),
  listLenders:  () => fetch('/api/lenders').then(j),
  listCustomers:() => fetch('/api/customers').then(j),
  customerAccounts: (id) => fetch(`/api/customers/${id}/accounts`).then(j),

  startSession: (agentId, body) =>
    fetch('/api/negotiations/start', {
      method: 'POST', headers: headers(agentId), body: JSON.stringify(body)
    }).then(j),

  sendTurn: (agentId, sessionId, utterance) =>
    fetch(`/api/negotiations/${sessionId}/turn`, {
      method: 'POST', headers: headers(agentId),
      body: JSON.stringify({ borrowerUtterance: utterance })
    }).then(j),

  accept: (agentId, sessionId) =>
    fetch(`/api/negotiations/${sessionId}/accept`, {
      method: 'POST', headers: headers(agentId)
    }).then(j),

  reject: (agentId, sessionId) =>
    fetch(`/api/negotiations/${sessionId}/reject`, {
      method: 'POST', headers: headers(agentId)
    }).then(j),

  transcript: (sessionId) =>
    fetch(`/api/negotiations/${sessionId}/transcript`).then(j),

  offers: (sessionId) =>
    fetch(`/api/negotiations/${sessionId}/offers`).then(j),

  customerTranscripts: (id) =>
    fetch(`/api/transcripts/by-customer/${id}`).then(j),

  customerSettlements: (id) =>
    fetch(`/api/settlements/by-customer/${id}`).then(j),

  guardrailAudit: () =>
    fetch('/api/audit/guardrail').then(j)
};
