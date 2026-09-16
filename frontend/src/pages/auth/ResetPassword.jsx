import { Link } from 'react-router-dom';
import { KeyRound, ArrowLeft } from 'lucide-react';

// Self-service reset-by-email-link isn't exposed by identity-service today —
// the only password-change surfaces are /auth/change-password (while signed
// in, from Settings) and an administrator resetting an employee's account.
// This page explains that honestly instead of simulating a flow the backend
// can't actually complete.
export default function ResetPassword() {
  return (
    <div className="auth-form-card" style={{ textAlign: 'center' }}>
      <div className="state-icon" style={{ margin: '0 auto 16px' }}>
        <KeyRound size={26} />
      </div>
      <h2>Reset password</h2>
      <p className="sub">
        This link isn&apos;t able to reset your password directly. If you&apos;re signed in, change it from
        Settings → Security. Otherwise, ask your workspace administrator to reset it from the Employees directory.
      </p>
      <Link to="/login" className="btn btn-secondary btn-block">
        <ArrowLeft size={15} /> Back to sign in
      </Link>
    </div>
  );
}
