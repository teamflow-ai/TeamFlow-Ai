import { Link } from 'react-router-dom';
import { ShieldAlert } from 'lucide-react';

export default function Forbidden() {
  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: 16, padding: 24, textAlign: 'center' }}>
      <div className="state-icon" style={{ width: 64, height: 64, background: 'var(--color-danger-light)', color: 'var(--color-danger)' }}>
        <ShieldAlert size={30} />
      </div>
      <h1 style={{ fontSize: '1.5rem' }}>You don&apos;t have access to this page</h1>
      <p className="text-muted-c" style={{ maxWidth: 420 }}>Your role doesn&apos;t include the permission this page requires. If you think that&apos;s wrong, ask an administrator to review your role.</p>
      <Link to="/dashboard" className="btn btn-primary">Back to dashboard</Link>
    </div>
  );
}
