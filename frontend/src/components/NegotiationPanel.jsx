import React, { useEffect, useRef, useState } from 'react';

export default function NegotiationPanel({
  customer, account, session, transcript, lastTurn,
  onStart, onTurn, onAccept, onReject
}) {
  const [draft, setDraft] = useState('');
  const [bundle, setBundle] = useState(false);
  const scrollRef = useRef(null);

  useEffect(() => {
    if (scrollRef.current) scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
  }, [transcript]);

  if (!customer || !account) {
    return (
      <div style={{padding:24, color:'#9aa3b2'}}>
        <h2 style={{marginTop:0}}>Pick a customer and account on the left to start.</h2>
        <p>OfferDyne will open at the lender ceiling and adapt each turn based on what the borrower says.</p>
      </div>
    );
  }

  if (!session) {
    return (
      <div style={{padding:24}}>
        <h2 style={{marginTop:0}}>Ready to start negotiation</h2>
        <div style={{marginBottom:12, color:'#9aa3b2'}}>
          <div><b>Customer:</b> {customer.customerName}</div>
          <div><b>Account:</b> {account.accountNumber} · {account.productType}</div>
          <div><b>Outstanding:</b> ₹{Number(account.outstandingAmount).toLocaleString()}</div>
        </div>
        <label style={{marginRight:12}}>
          <input type="checkbox" checked={bundle}
                 onChange={e => setBundle(e.target.checked)} /> Attempt bundle across all accounts
        </label>
        <button onClick={() => onStart(bundle)}>Start session</button>
      </div>
    );
  }

  return (
    <div className="chat">
      <div className="messages" ref={scrollRef}>
        {transcript.map(t => (
          <div key={t.transcriptId} className={'msg ' + t.speaker}>
            <div>{t.utterance}</div>
            <div className="meta">
              turn {t.turnIndex} · {t.speaker}
              {t.speaker === 'BORROWER' && (
                <span className="chips">
                  <span className={'chip ' + (t.sentiment || 'NEUTRAL')}>{t.sentiment}</span>
                  <span className="chip">{t.objectionType}</span>
                  {t.sentimentScore != null && <span className="chip">score {t.sentimentScore}</span>}
                </span>
              )}
            </div>
          </div>
        ))}
      </div>
      <div className="composer">
        <input
          placeholder={session.status === 'ACTIVE'
              ? 'Type what the borrower says…'
              : 'Session closed.'}
          value={draft}
          disabled={session.status !== 'ACTIVE'}
          onChange={e => setDraft(e.target.value)}
          onKeyDown={e => {
            if (e.key === 'Enter' && draft.trim()) {
              onTurn(draft.trim()); setDraft('');
            }
          }} />
        <button
          onClick={() => { if (draft.trim()) { onTurn(draft.trim()); setDraft(''); } }}
          disabled={!draft.trim()}>Send</button>
        <button className="good"  onClick={onAccept}>Accept</button>
        <button className="warn" onClick={onReject}>Reject</button>
      </div>
    </div>
  );
}
