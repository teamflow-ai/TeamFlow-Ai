import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router-dom';
import { Mail, ArrowLeft, MailCheck } from 'lucide-react';
import FormField from '../../components/common/FormField';

export default function ForgotPassword() {
  const [sent, setSent] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const { register, handleSubmit, formState: { errors } } = useForm();

  const onSubmit = async () => {
    setSubmitting(true);
    // Self-service password reset isn't available yet — an administrator can
    // reset a password from Employees, or use Settings → Security once signed in.
    await new Promise((r) => setTimeout(r, 400));
    setSubmitting(false);
    setSent(true);
  };

  if (sent) {
    return (
      <div className="auth-form-card" style={{ textAlign: 'center' }}>
        <div className="state-icon" style={{ margin: '0 auto 16px' }}>
          <MailCheck size={26} />
        </div>
        <h2>Request received</h2>
        <p className="sub">Password resets are handled by your workspace administrator. Reach out to them, or your HR/IT contact, and they can reset it from the Employees directory.</p>
        <Link to="/login" className="btn btn-secondary btn-block">
          <ArrowLeft size={15} /> Back to sign in
        </Link>
      </div>
    );
  }

  return (
    <div className="auth-form-card">
      <h2>Reset your password</h2>
      <p className="sub">Enter your work email and we&apos;ll send you a reset link.</p>

      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <FormField label="Work email" required error={errors.email?.message}>
          <div className="input-with-icon">
            <Mail size={16} />
            <input type="email" className={`form-control ${errors.email ? 'is-invalid' : ''}`} placeholder="you@teamflow.ai"
              {...register('email', { required: 'Email is required' })} />
          </div>
        </FormField>
        <button className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? 'Sending…' : 'Send reset link'}
        </button>
      </form>

      <div className="auth-footer-link">
        <Link to="/login" style={{ fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: 6 }}>
          <ArrowLeft size={14} /> Back to sign in
        </Link>
      </div>
    </div>
  );
}
