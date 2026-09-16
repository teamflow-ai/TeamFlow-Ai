export default function LoadingSpinner({ size = 'md', label }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10, justifyContent: 'center', padding: label ? '48px 0' : 0 }}>
      <span className={`spinner ${size === 'lg' ? 'spinner-lg' : ''}`} />
      {label && <span className="text-muted-c" style={{ fontSize: '0.875rem' }}>{label}</span>}
    </div>
  );
}
