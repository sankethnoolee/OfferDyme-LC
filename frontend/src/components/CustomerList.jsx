import React from 'react';

export default function CustomerList({ customers, selected, onSelect }) {
  return (
    <div>
      <h2>Customers</h2>
      {customers.map(c => (
        <div
          key={c.customerId}
          className={'card' + (selected?.customerId === c.customerId ? ' selected' : '')}
          onClick={() => onSelect(c)}>
          <div style={{fontWeight:600}}>{c.customerName}</div>
          <div style={{fontSize:12, color:'#9aa3b2'}}>
            {c.customerCode} · {c.employmentStatus} · {c.incomeBand}
          </div>
          <div className="chips">
            {c.riskSegment && <span className="chip">{c.riskSegment}</span>}
            {c.creditScore != null && <span className="chip">Score {c.creditScore}</span>}
          </div>
        </div>
      ))}
    </div>
  );
}
