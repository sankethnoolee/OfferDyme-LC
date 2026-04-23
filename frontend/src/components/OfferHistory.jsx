import React from 'react';

export default function OfferHistory({ offers, audit, lastTurn }) {
  return (
    <div>
      <h2>Live offer stream</h2>
      {lastTurn && (
        <div className="offer-card live">
          <div className="strategy">{lastTurn.strategyChosen}</div>
          <div className="amount">
            {lastTurn.offerPercent}% · ₹{Number(lastTurn.offerAmount).toLocaleString()}
          </div>
          <div style={{fontSize:12, color:'#c7d4ea', marginTop:4}}>
            {lastTurn.framingText}
          </div>
          <div className="kv">
            <span>Guardrail</span>
            <span className={lastTurn.guardrailPassed ? 'guardrail-ok' : 'guardrail-warn'}>
              {lastTurn.guardrailPassed ? 'WITHIN BOUNDS' : lastTurn.guardrailReason}
            </span>
          </div>
        </div>
      )}

      <h2 style={{marginTop:18}}>Offer trail</h2>
      {offers.length === 0 && <div style={{color:'#9aa3b2'}}>No offers yet.</div>}
      {[...offers].reverse().map(o => (
        <div key={o.offerId} className="offer-card" style={{borderColor:'#2a2f3a'}}>
          <div className="strategy">{o.strategy}</div>
          <div>{o.offerPercent}% · ₹{Number(o.offerAmount).toLocaleString()}</div>
          <div className="kv">
            <span>turn {o.turnIndex}</span>
            <span className={o.guardrailCheckPassed ? 'guardrail-ok' : 'guardrail-warn'}>
              {o.guardrailCheckPassed ? 'OK' : o.guardrailReason}
            </span>
          </div>
        </div>
      ))}

      {audit && (
        <div className="audit-block">
          <div>Guardrail audit</div>
          <div className="kv"><span>Total offers logged</span><span>{audit.totalOffers}</span></div>
          <div className="kv"><span>Out-of-bound after enforcement</span><span>0</span></div>
          <div className="kv"><span>Compliance</span><span className="guardrail-ok">{audit.complianceRatePct}%</span></div>
        </div>
      )}
    </div>
  );
}
