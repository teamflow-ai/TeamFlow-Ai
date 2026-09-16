import { Link } from 'react-router-dom';
import { Compass } from 'lucide-react';

export default function NotFound() {
  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: 16, padding: 24, textAlign: 'center' }}>
      <div className="state-icon" style={{ width: 64, height: 64 }}>
        <Compass size={30} />
      </div>
      <h1 style={{ fontSize: '1.5rem' }}>Page not found</h1>
      <p className="text-muted-c">The page you&apos;re looking for doesn&apos;t exist or may have moved.</p>
      <Link to="/dashboard" className="btn btn-primary">Back to dashboard</Link>
    </div>
  );
}
