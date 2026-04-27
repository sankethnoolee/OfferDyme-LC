import React, { useEffect, useState } from 'react';

/**
 * Lightweight toast system — no deps.
 * Usage:
 *   import { showToast, ToastHost } from './components/Toast';
 *   showToast('Saved', { kind: 'success' });
 *
 * Mount <ToastHost /> once, near the root of the app.
 */

const listeners = new Set();
let nextId = 1;

/**
 * Publish a toast.
 *
 * @param {string} message   What to show the user.
 * @param {object} [opts]
 * @param {'info'|'success'|'warning'|'error'} [opts.kind='info']
 * @param {number} [opts.duration=3800]  ms before auto-dismiss.
 */
export function showToast(message, opts = {}) {
  if (!message) return;
  const id = nextId++;
  const toast = {
    id,
    message: String(message),
    kind: opts.kind || 'info',
    duration: typeof opts.duration === 'number' ? opts.duration : 3800,
  };
  listeners.forEach((fn) => fn({ type: 'add', toast }));
}

function dismiss(id) {
  listeners.forEach((fn) => fn({ type: 'remove', id }));
}

const ICONS = {
  info:    'ℹ',
  success: '✓',
  warning: '⚠',
  error:   '✕',
};

export function ToastHost() {
  const [items, setItems] = useState([]);

  useEffect(() => {
    const handler = (evt) => {
      if (evt.type === 'add') {
        setItems((prev) => [...prev, evt.toast]);
        if (evt.toast.duration > 0) {
          setTimeout(() => dismiss(evt.toast.id), evt.toast.duration);
        }
      } else if (evt.type === 'remove') {
        setItems((prev) => prev.filter((t) => t.id !== evt.id));
      }
    };
    listeners.add(handler);
    return () => listeners.delete(handler);
  }, []);

  return (
    <div className="lc-toast-host" aria-live="polite" aria-atomic="true">
      {items.map((t) => (
        <div
          key={t.id}
          className={`lc-toast lc-toast-${t.kind}`}
          role={t.kind === 'error' || t.kind === 'warning' ? 'alert' : 'status'}
        >
          <span className="lc-toast-icon">{ICONS[t.kind] || ICONS.info}</span>
          <span className="lc-toast-message">{t.message}</span>
          <button
            className="lc-toast-close"
            aria-label="Dismiss"
            onClick={() => dismiss(t.id)}
          >
            ×
          </button>
        </div>
      ))}
    </div>
  );
}
