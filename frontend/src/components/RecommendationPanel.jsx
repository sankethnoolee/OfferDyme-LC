import React from 'react';
import { formatINR } from '../api/client';

/* ── PS#8 helpers ───────────────────────────────────────────────── */
const OBJ_META = {
  AFFORDABILITY:       { label: 'AFFORDABILITY',       bg: '#fff3cd', border: '#ffc107', text: '#664d03', icon: '💸' },
  HARDSHIP:            { label: 'HARDSHIP',             bg: '#fee2e2', border: '#ef4444', text: '#7f1d1d', icon: '🆘' },
  PARTIAL_WILLINGNESS: { label: 'PARTIAL WILLINGNESS',  bg: '#d1fae5', border: '#10b981', text: '#064e3b', icon: '🤝' },
  AVOIDANCE:           { label: 'AVOIDANCE',            bg: '#f1f5f9', border: '#94a3b8', text: '#334155', icon: '🚫' },
  DISPUTE:             { label: 'DISPUTE',              bg: '#ede9fe', border: '#7c3aed', text: '#4c1d95', icon: '⚠️' },
  NONE:                { label: 'NO OBJECTION',         bg: '#ecfdf5', border: '#10b981', text: '#064e3b', icon: '✅' },
};
const GUARDRAIL_META = {
  WITHIN_LIMITS: { label: 'WITHIN LIMITS', bg: '#d1fae5', border: '#10b981', text: '#064e3b', icon: '✅' },
  FLOORED:       { label: 'FLOORED ↑',     bg: '#fee2e2', border: '#ef4444', text: '#7f1d1d', icon: '🔴' },
  CAPPED:        { label: 'CAPPED ↓',      bg: '#fff3cd', border: '#f59e0b', text: '#78350f', icon: '🟡' },
};

function PS8LiveBar({ objectionType, objectionConfidence, guardrailStatus }) {
  const obj = OBJ_META[objectionType] || OBJ_META['NONE'];
  const gr  = GUARDRAIL_META[guardrailStatus] || GUARDRAIL_META['WITHIN_LIMITS'];
  const conf = objectionConfidence != null ? `${(objectionConfidence * 100).toFixed(0)}%` : '—';
  return (
    <div style={{
      display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center',
      padding: '10px 12px', background: '#f8fafc',
      border: '1px solid #e2e8f0', borderRadius: 8, marginBottom: 10,
    }}>
      <span style={{ fontSize: 10, fontWeight: 700, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: 1 }}>
        Live signals
      </span>
      {/* Objection chip */}
      <span style={{
        display: 'inline-flex', alignItems: 'center', gap: 5,
        padding: '4px 10px', borderRadius: 20, fontSize: 11, fontWeight: 700,
        background: obj.bg, border: `1px solid ${obj.border}`, color: obj.text,
      }}>
        {obj.icon} {obj.label}
        {objectionConfidence != null && objectionConfidence > 0 && (
          <span style={{ fontWeight: 400, opacity: 0.85 }}>{conf}</span>
        )}
      </span>
      {/* Guardrail badge */}
      <span style={{
        display: 'inline-flex', alignItems: 'center', gap: 5,
        padding: '4px 10px', borderRadius: 20, fontSize: 11, fontWeight: 700,
        background: gr.bg, border: `1px solid ${gr.border}`, color: gr.text,
        marginLeft: 'auto',
      }}>
        {gr.icon} Guardrail: {gr.label}
      </span>
    </div>
  );
}

function ScriptLineCard({ scriptLine }) {
  if (!scriptLine) return null;
  return (
    <div style={{
      padding: '10px 14px', borderRadius: 8, marginBottom: 10,
      background: '#eff6ff', border: '2px solid #3b82f6',
    }}>
      <div style={{ fontSize: 10, fontWeight: 700, color: '#1d4ed8', textTransform: 'uppercase', letterSpacing: 1, marginBottom: 4 }}>
        🎙 Say this now
      </div>
      <div style={{ fontSize: 13, fontWeight: 600, color: '#1e3a5f', lineHeight: 1.5, fontStyle: 'italic' }}>
        "{scriptLine}"
      </div>
    </div>
  );
}

function HistorySummaryCard({ summary }) {
  if (!summary) return null;
  return (
    <div style={{
      padding: '8px 12px', borderRadius: 8, marginBottom: 10,
      background: '#faf5ff', border: '1px solid #d8b4fe',
    }}>
      <span style={{ fontSize: 10, fontWeight: 700, color: '#7c3aed', textTransform: 'uppercase', letterSpacing: 1 }}>
        📋 Customer history
      </span>
      <div style={{ fontSize: 12, color: '#4c1d95', marginTop: 4, lineHeight: 1.5 }}>{summary}</div>
    </div>
  );
}

/**
 * Visualise a floor / current / ceiling range as a horizontal bar with
 *   - a filled band between floor and ceiling
 *   - a marker for the current recommendation
 *   - a label headline for the current value
 */
function RangeSlider({ title, floor, current, ceiling, formatter, sliderClass = '' }) {
  const min = Math.min(floor ?? current ?? 0, current ?? 0, ceiling ?? current ?? 0);
  const max = Math.max(floor ?? current ?? 0, current ?? 0, ceiling ?? current ?? 0);
  const span = max - min || 1;

  const floorPct   = floor   == null ? 0   : ((floor   - min) / span) * 100;
  const ceilingPct = ceiling == null ? 100 : ((ceiling - min) / span) * 100;
  const currentPct = current == null ? 50  : ((current - min) / span) * 100;

  return (
    <div className={`range-slider ${sliderClass}`}>
      <div className="rs-title">{title}</div>
      <div className="rs-current">{formatter ? formatter(current) : current}</div>
      <div className="rs-bar">
        <div className="rs-fill" style={{ left: `${floorPct}%`, right: `${100 - ceilingPct}%` }} />
        <div className="rs-marker" style={{ left: `${currentPct}%` }} title="current recommendation" />
      </div>
      <div className="rs-labels">
        <span className="floor">
          <strong>floor:</strong> {formatter ? formatter(floor) : floor}
        </span>
        <span className="ceil">
          <strong>ceiling:</strong> {formatter ? formatter(ceiling) : ceiling}
        </span>
      </div>
    </div>
  );
}

function SentimentSlider({ sentiment, score }) {
  // Score range: -1 .. 1
  const pct = ((Math.max(-1, Math.min(1, score ?? 0)) + 1) / 2) * 100;
  return (
    <div className="range-slider sentiment-slider">
      <div className="rs-title">Customer sentiment</div>
      <div className="rs-current">
        {sentiment || 'NEUTRAL'}
        <span className="muted" style={{ fontSize: 12, marginLeft: 8, fontWeight: 400 }}>
          score {Number(score ?? 0).toFixed(2)}
        </span>
      </div>
      <div className="rs-bar">
        <div className="rs-marker" style={{ left: `${pct}%` }} title="current sentiment" />
      </div>
      <div className="rs-labels">
        <span className="floor"><strong>negative</strong> −1</span>
        <span className="ceil"><strong>positive</strong> +1</span>
      </div>
    </div>
  );
}

const pct = (v) => (v == null ? '—' : `${Number(v).toFixed(1)}%`);

function SkeletonBlock({ h = 18, mt = 0, w = '100%' }) {
  return <div className="skeleton" style={{ height: h, marginTop: mt, width: w, borderRadius: 6 }} />;
}

function RecommendationSkeleton() {
  return (
    <div style={{ padding: '0 2px' }}>
      {/* live signal bar */}
      <SkeletonBlock h={44} mt={0} />
      {/* script line */}
      <SkeletonBlock h={56} mt={10} />
      {/* history */}
      <SkeletonBlock h={40} mt={10} />
      {/* strategy card */}
      <div style={{ background: 'var(--lc-surface)', border: '1px solid var(--lc-border)', borderRadius: 8, padding: 14, marginTop: 10 }}>
        <SkeletonBlock h={14} w="40%" />
        <SkeletonBlock h={14} mt={8} w="70%" />
        <SkeletonBlock h={12} mt={6} w="90%" />
      </div>
      {/* suggested reply */}
      <div style={{ background: 'var(--lc-surface)', border: '1px solid var(--lc-border)', borderRadius: 8, padding: 14, marginTop: 10 }}>
        <SkeletonBlock h={12} w="35%" />
        <SkeletonBlock h={14} mt={8} />
        <SkeletonBlock h={14} mt={6} w="85%" />
        <SkeletonBlock h={12} mt={6} w="60%" />
      </div>
      {/* sliders */}
      <SkeletonBlock h={62} mt={10} />
      <SkeletonBlock h={62} mt={10} />
      <SkeletonBlock h={62} mt={10} />
    </div>
  );
}

export default function RecommendationPanel({ decision, onSaveSettlement, scopeLabel, loading }) {
  if (loading && !decision) {
    return (
      <div>
        <div className="rec-panel-header">
          <h3>AI recommendations</h3>
          <div className="sub">Analysing account…</div>
        </div>
        <RecommendationSkeleton />
      </div>
    );
  }

  if (!decision) {
    return (
      <div>
        <div className="rec-panel-header">
          <h3>AI recommendations</h3>
          <div className="sub">Submit a customer utterance to see live guidance.</div>
        </div>
        <div className="empty-state" style={{ padding: '40px 10px' }}>
          <div className="icon">🧠</div>
          <div>Nothing yet — start the conversation on the left and the assistant will advise.</div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="rec-panel-header">
        <h3>AI recommendations</h3>
        <div className="sub" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          {loading
            ? <><span className="skeleton" style={{ display: 'inline-block', width: 12, height: 12, borderRadius: '50%' }} /> Analysing…</>
            : (scopeLabel || 'Live guidance for this conversation.')
          }
        </div>
        {decision.policyLabel && (
          <div className="policy-chip" title={`Policy #${decision.policyId}`}>
            <span className="dot" /> Policy: {decision.policyLabel}
          </div>
        )}
      </div>

      {/* ── PS#8 Live Signal Bar — objection type + guardrail status ── */}
      <PS8LiveBar
        objectionType={decision.objectionType}
        objectionConfidence={decision.objectionConfidence}
        guardrailStatus={decision.guardrailStatus}
      />

      {/* ── PS#8 Script Line — one-liner for agent to say verbatim ── */}
      <ScriptLineCard scriptLine={decision.scriptLine} />

      {/* ── PS#8 Customer History Summary ── */}
      <HistorySummaryCard summary={decision.customerHistorySummary} />

      {/* --- Strategy card --- */}
      {decision.selectedStrategyCode && (
        <div className="rec-card strategy-card">
          <div className="rc-title">
            Strategy
            <span className="strategy-pill">{decision.selectedStrategyCode}</span>
            {decision.bundle && decision.bundleAccounts?.length > 0 && (
              <span
                style={{
                  marginLeft: 8, padding: '2px 8px', borderRadius: 12,
                  background: '#fbbf24', color: '#78350f',
                  fontSize: 10, fontWeight: 700, letterSpacing: 0.5,
                }}
              >
                BUNDLE · {decision.bundleAccounts.length} accts
              </span>
            )}
          </div>
          {decision.selectedStrategyName && (
            <div className="rc-text" style={{ fontWeight: 600 }}>
              {decision.selectedStrategyName}
            </div>
          )}
          {decision.strategyRationale && (
            <div className="rc-reasoning" style={{ marginTop: 6 }}>
              {decision.strategyRationale}
            </div>
          )}
          {decision.bundleNote && (
            <div
              style={{
                marginTop: 8, padding: '6px 10px', borderRadius: 6,
                background: '#fff3cd', border: '1px solid #ffc107',
                color: '#664d03', fontSize: 12,
              }}
            >
              ⚠ {decision.bundleNote}
            </div>
          )}
        </div>
      )}

      {/* --- Single-account scope note (non-BUNDLE in portfolio) --- */}
      {!decision.bundle && decision.primaryAccountNumber && (
        <div
          className="rec-card"
          style={{ background: '#f0f9ff', borderColor: '#bae6fd' }}
        >
          <div className="rc-title">
            Offer applies to one account
            <span
              style={{
                marginLeft: 'auto',
                fontSize: 11,
                fontWeight: 700,
                color: 'var(--lc-primary-dark)',
              }}
            >
              {decision.primaryAccountNumber}
            </span>
          </div>
          <div className="rc-reasoning" style={{ marginTop: 4 }}>
            Floor, ceiling and offer below are for <strong>this single account</strong>
            — not the full portfolio. Switch to BUNDLE to cover all accounts at once.
          </div>
        </div>
      )}

      {/* --- Bundle preview (shown BEFORE save) --- */}
      {decision.bundle && decision.bundleAccounts?.length > 0 && (
        <div className="rec-card" style={{ background: '#fff7ed', borderColor: '#fed7aa' }}>
          <div className="rc-title">
            Bundle covers {decision.bundleAccounts.length} accounts
            <span
              style={{
                marginLeft: 'auto', fontSize: 11, fontWeight: 700,
                color: 'var(--lc-accent-dark)',
              }}
            >
              Total {formatINR(decision.bundleOutstanding)}
            </span>
          </div>
          <div className="rc-reasoning" style={{ marginTop: 4, marginBottom: 8 }}>
            Offer + floor/ceiling below apply to the <strong>aggregated</strong> outstanding.
            The save button will propose one settlement linked to all accounts listed here.
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
            {decision.bundleAccounts.map(a => (
              <span
                key={a.id}
                style={{
                  display: 'inline-flex', alignItems: 'center', gap: 6,
                  padding: '4px 8px', borderRadius: 6,
                  background: '#fff', border: '1px solid #fed7aa', fontSize: 11,
                }}
                title={a.productType}
              >
                <strong>{a.accountNumber}</strong>
                <span style={{ color: '#92400e' }}>
                  {a.productType?.replace(/_/g, ' ')}
                </span>
                <span style={{ fontWeight: 600 }}>{formatINR(a.outstandingAmount)}</span>
              </span>
            ))}
          </div>
        </div>
      )}

      {/* --- Suggested reply card --- */}
      <div className="rec-card">
        <div className="rc-title">
          Suggested reply
          <span className={`rc-risk risk-pill ${decision.riskLevel}`}>
            Risk: {decision.riskLevel}
          </span>
        </div>
        <div className="rc-text">{decision.proposedReply}</div>
        {decision.reasoning && (
          <div className="rc-reasoning"><strong>Why:</strong> {decision.reasoning}</div>
        )}
      </div>

      {/* --- Sentiment slider --- */}
      <SentimentSlider sentiment={decision.sentiment} score={decision.sentimentScore} />

      {/* --- Discount slider --- */}
      <RangeSlider
        title="Discount (%)"
        floor={decision.discountFloor}
        current={decision.recommendedDiscountPercent}
        ceiling={decision.discountCeiling}
        formatter={pct}
      />

      {/* --- Offer amount slider + recovery % --- */}
      <RangeSlider
        title={`Offer amount (INR)${decision.suggestedOfferPercent != null ? `  ·  ${Number(decision.suggestedOfferPercent).toFixed(1)}% of outstanding` : ''}`}
        floor={decision.offerFloor}
        current={decision.recommendedOfferAmount}
        ceiling={decision.offerCeiling}
        formatter={formatINR}
      />

      {/* --- Key-value block --- */}
      <div className="kv-grid">
        <div>
          <div className="k">Payment plan</div>
          <div className="v">
            {decision.recommendedPaymentPlan?.replace(/_/g, ' ')}
            {decision.recommendedInstallments > 1 && ` (${decision.recommendedInstallments}×)`}
          </div>
        </div>
        <div>
          <div className="k">Settlement now?</div>
          <div className="v">{decision.shouldOfferSettlement ? 'Yes' : 'Hold'}</div>
        </div>
        {decision.summary && (
          <div style={{ gridColumn: '1 / -1' }}>
            <div className="k">Summary</div>
            <div style={{ fontSize: 12, color: 'var(--lc-text)' }}>{decision.summary}</div>
          </div>
        )}
      </div>

      {decision.shouldOfferSettlement && onSaveSettlement && (
        <div className="rc-actions" style={{ marginTop: 14 }}>
          <button className="btn btn-accent btn-sm" onClick={onSaveSettlement}>
            Save settlement proposal
          </button>
        </div>
      )}
    </div>
  );
}
