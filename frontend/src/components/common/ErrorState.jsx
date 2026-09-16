import { AlertTriangle } from 'lucide-react';

export default function ErrorState({ title = 'Something went wrong', description = 'Please try again in a moment.', onRetry }) {
  return (
    <div className="state-block">
      <div className="state-icon" style={{ background: 'var(--color-danger-light)', color: 'var(--color-danger)' }}>
        <AlertTriangle size={26} />
      </div>
      <div className="state-title">{title}</div>
      <div className="state-desc">{description}</div>
      {onRetry && (
        <button className="btn btn-secondary btn-sm" style={{ marginTop: 16 }} onClick={onRetry}>
          Try again
        </button>
      )}
    </div>
  );
}
