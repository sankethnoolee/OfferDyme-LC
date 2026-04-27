import React, { useEffect, useRef, useState } from 'react';

/**
 * A themed replacement for window.prompt / window.confirm.
 *
 * Controlled by a parent component:
 *   const [modal, setModal] = useState(null);
 *   setModal({ title, label, placeholder, confirmLabel, kind, onSubmit });
 *   <PromptModal {...modal} onClose={() => setModal(null)} />
 *
 * Props:
 *   open:          whether to render (defaults true when rendered conditionally)
 *   title:         heading text
 *   label:         description above the input
 *   placeholder:   input placeholder
 *   initialValue:  prefill
 *   confirmLabel:  button text (default "Submit")
 *   cancelLabel:   button text (default "Cancel")
 *   kind:          'primary' | 'success' | 'warning' | 'danger'  — colors the primary button
 *   multiline:     render a textarea instead of input
 *   required:      prevent submit when empty (default false)
 *   onSubmit(value): called with the value on confirm
 *   onClose():       called on cancel or after submit
 */
export default function PromptModal({
  title,
  label,
  placeholder,
  initialValue = '',
  confirmLabel = 'Submit',
  cancelLabel = 'Cancel',
  kind = 'primary',
  multiline = true,
  required = false,
  onSubmit,
  onClose,
}) {
  const [value, setValue] = useState(initialValue);
  const inputRef = useRef(null);

  useEffect(() => {
    // Focus the input on mount.
    const t = setTimeout(() => inputRef.current?.focus(), 30);
    return () => clearTimeout(t);
  }, []);

  useEffect(() => {
    const onKey = (e) => {
      if (e.key === 'Escape') {
        e.preventDefault();
        onClose && onClose();
      }
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [onClose]);

  const handleSubmit = (e) => {
    e?.preventDefault?.();
    const trimmed = (value ?? '').trim();
    if (required && !trimmed) return;
    onSubmit && onSubmit(value ?? '');
    onClose && onClose();
  };

  const handleBackdrop = (e) => {
    if (e.target === e.currentTarget) onClose && onClose();
  };

  return (
    <div className="lc-modal-backdrop" onMouseDown={handleBackdrop} role="dialog" aria-modal="true">
      <form className="lc-modal" onSubmit={handleSubmit}>
        {title && <div className="lc-modal-title">{title}</div>}
        {label && <div className="lc-modal-label">{label}</div>}

        {multiline ? (
          <textarea
            ref={inputRef}
            className="lc-modal-input"
            rows={3}
            placeholder={placeholder}
            value={value}
            onChange={(e) => setValue(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) handleSubmit(e);
            }}
          />
        ) : (
          <input
            ref={inputRef}
            className="lc-modal-input"
            type="text"
            placeholder={placeholder}
            value={value}
            onChange={(e) => setValue(e.target.value)}
          />
        )}

        <div className="lc-modal-actions">
          <button type="button" className="btn btn-sm btn-outline" onClick={onClose}>
            {cancelLabel}
          </button>
          <button
            type="submit"
            className={`btn btn-sm lc-modal-confirm lc-modal-confirm-${kind}`}
            disabled={required && !(value ?? '').trim()}
          >
            {confirmLabel}
          </button>
        </div>
      </form>
    </div>
  );
}
