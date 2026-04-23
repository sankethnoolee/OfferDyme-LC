import React from 'react';

const statusClass = (s) => s === 'SETTLED' ? 'settled'
                         : s === 'IN_NEGOTIATION' ? 'negotiation' : 'delinquent';

export default function AccountPanel({ accounts, selected, onSelect }) {
  return (
    <div style={{marginTop:16}}>
      <h2>Accounts ({accounts.length})</h2>
      {accounts.length === 0 && <div style={{color:'#9aa3b2'}}>No accounts for this customer.</div>}
      {accounts.map(a => (
        <div
          key={a.accountId}
          className={'card' + (selected?.accountId === a.accountId ? ' selected' : '')}
          onClick={() => onSelect(a)}>
          <div style={{fontWeight:600}}>{a.accountNumber}</div>
          <div style={{fontSize:12, color:'#9aa3b2'}}>
            {a.productType} · DPD {a.dpd}
          </div>
          <div style={{fontSize:14}}>Outstanding: ₹{Number(a.outstandingAmount).toLocaleString()}</div>
          <span className={'badge ' + statusClass(a.accountStatus)}>{a.accountStatus}</span>
        </div>
      ))}
    </div>
  );
}
