// Thin fetch wrapper. Relative URLs hit the Spring Boot backend
// via the "proxy" entry in package.json during dev.

const jsonHeaders = { 'Content-Type': 'application/json' };

async function handle(res) {
  if (!res.ok) {
    const text = await res.text();
    // Try to surface a JSON { error } from the backend, else fall back to raw text.
    try {
      const body = JSON.parse(text);
      if (body && body.error) {
        const err = new Error(body.error);
        err.status = res.status;
        throw err;
      }
    } catch (_) {
      // not JSON — fall through
    }
    const err = new Error(`${res.status} ${res.statusText} — ${text}`);
    err.status = res.status;
    throw err;
  }
  if (res.status === 204) return null;
  return res.json();
}

function qs(params) {
  const pairs = Object.entries(params || {})
    .filter(([, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`);
  return pairs.length ? `?${pairs.join('&')}` : '';
}

export const api = {
  listAgents:       ()                 => fetch('/api/agents').then(handle),
  listLenders:      ()                 => fetch('/api/lenders').then(handle),
  listCustomers:    (lenderId)         => fetch(`/api/customers${qs({ lenderId })}`).then(handle),
  getCustomer:      (id, lenderId)     => fetch(`/api/customers/${id}${qs({ lenderId })}`).then(handle),
  getAccount:       (id)               => fetch(`/api/accounts/${id}`).then(handle),

  // Account-level chat
  listTranscripts:  (accountId)  => fetch(`/api/chat/accounts/${accountId}/transcripts`).then(handle),
  // Portfolio-level chat (customer, all accounts)
  listPortfolioTranscripts: (customerId) =>
                                fetch(`/api/chat/customers/${customerId}/portfolio-transcripts`).then(handle),

  getTranscript:    (id)         => fetch(`/api/chat/transcripts/${id}`).then(handle),
  getHistory:       (id)         => fetch(`/api/chat/transcripts/${id}/history`).then(handle),

  // Turn-based: agent types ONLY the customer utterance, Claude auto-replies.
  accountTurn:      (body)       => fetch('/api/chat/accounts/turn', {
                                      method: 'POST', headers: jsonHeaders,
                                      body: JSON.stringify(body),
                                    }).then(handle),
  customerTurn:     (body)       => fetch('/api/chat/customers/turn', {
                                      method: 'POST', headers: jsonHeaders,
                                      body: JSON.stringify(body),
                                    }).then(handle),

  // Legacy raw append + analyze (still available)
  sendMessage:      (body)       => fetch('/api/chat/messages', {
                                      method: 'POST', headers: jsonHeaders,
                                      body: JSON.stringify(body),
                                    }).then(handle),
  analyze:          (transcriptId) => fetch(`/api/chat/transcripts/${transcriptId}/analyze`, {
                                      method: 'POST', headers: jsonHeaders,
                                    }).then(handle),

  // Policy / strategy configuration (admin)
  listPolicies:        (lenderId = 1) => fetch(`/api/policy/policies?lenderId=${lenderId}`).then(handle),
  savePolicy:          (body, lenderId = 1) => {
    const method = body.id ? 'PUT' : 'POST';
    const url = body.id ? `/api/policy/policies/${body.id}?lenderId=${lenderId}` : `/api/policy/policies?lenderId=${lenderId}`;
    return fetch(url, { method, headers: jsonHeaders, body: JSON.stringify(body) }).then(handle);
  },
  deletePolicy:        (id) => fetch(`/api/policy/policies/${id}`, { method: 'DELETE' }).then(handle),

  listStrategies:      (lenderId = 1, onlyActive = false) =>
                        fetch(`/api/policy/strategies?lenderId=${lenderId}&onlyActive=${onlyActive}`).then(handle),
  saveStrategy:        (body, lenderId = 1) => {
    const method = body.id ? 'PUT' : 'POST';
    const url = body.id ? `/api/policy/strategies/${body.id}?lenderId=${lenderId}` : `/api/policy/strategies?lenderId=${lenderId}`;
    return fetch(url, { method, headers: jsonHeaders, body: JSON.stringify(body) }).then(handle);
  },
  deleteStrategy:      (id) => fetch(`/api/policy/strategies/${id}`, { method: 'DELETE' }).then(handle),

  listSettlements:     (accountId) => fetch(`/api/settlements/accounts/${accountId}`).then(handle),
  listSettlementsForCustomer: (customerId) =>
                                    fetch(`/api/settlements/customers/${customerId}`).then(handle),
  createFromClaude:    (body)      => fetch('/api/settlements/from-claude', {
                                        method: 'POST', headers: jsonHeaders,
                                        body: JSON.stringify(body),
                                      }).then(handle),
  updateSettlement:    (id, body)  => fetch(`/api/settlements/${id}/status`, {
                                        method: 'POST', headers: jsonHeaders,
                                        body: JSON.stringify(body),
                                      }).then(handle),

  // PS#8 analytics — acceptance rate vs static baseline
  acceptanceRate: () => fetch('/api/analytics/acceptance-rate').then(handle),

  // PS#8 suggest endpoint (exact contract)
  suggest: (body) => fetch('/api/settlement/suggest', {
                       method: 'POST', headers: jsonHeaders,
                       body: JSON.stringify(body),
                     }).then(handle),
};

export const formatINR = (n) => {
  if (n === null || n === undefined) return '—';
  return '₹' + Math.round(n).toLocaleString('en-IN');
};
