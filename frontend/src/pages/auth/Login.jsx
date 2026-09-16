import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Mail, Lock, Eye, EyeOff, LogIn } from 'lucide-react';
import toast from 'react-hot-toast';
import FormField from '../../components/common/FormField';
import { useAuth } from '../../hooks/useAuth';
import { LOGIN_ROLE_OPTIONS } from '../../constants/app';

// Role-based redirect targets, keyed by the *backend-confirmed* role on the
// authenticated user — never by the cosmetic selector below.
const ROLE_HOME = {
  ADMIN: '/dashboard',
  SUPER_ADMIN: '/dashboard',
  PROJECT_MANAGER: '/dashboard',
  EMPLOYEE: '/dashboard',
  DEVELOPER: '/dashboard',
  QA: '/dashboard',
  HR: '/dashboard',
};

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [loginAs, setLoginAs] = useState('EMPLOYEE');
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ defaultValues: { email: '', password: '' } });

  const onSubmit = async (values) => {
    setSubmitting(true);
    try {
      // `loginAs` only shapes which dashboard tab we land on first if the
      // backend role doesn't map cleanly — it is never sent to the server and
      // never used to decide what the person can see or do. Authorization is
      // always the role and permissions the backend returns in `user`.
      const user = await login(values.email, values.password);
      toast.success(`Welcome back, ${user.fullName.split(' ')[0]}`);
      const destination = location.state?.from || ROLE_HOME[user.role] || '/dashboard';
      navigate(destination, { replace: true });
    } catch (err) {
      toast.error(err.message || 'Login failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-form-card">
      <h2>Sign in to your workspace</h2>
      <p className="sub">Welcome back — enter your credentials to continue.</p>

      <div className="role-select-group" role="radiogroup" aria-label="I am signing in as">
        {LOGIN_ROLE_OPTIONS.map((opt) => (
          <button
            key={opt.value}
            type="button"
            role="radio"
            aria-checked={loginAs === opt.value}
            className={`role-select-pill ${loginAs === opt.value ? 'active' : ''}`}
            onClick={() => setLoginAs(opt.value)}
          >
            {opt.label}
          </button>
        ))}
      </div>

      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <FormField label="Work email" required error={errors.email?.message}>
          <div className="input-with-icon">
            <Mail size={16} />
            <input
              type="email"
              className={`form-control ${errors.email ? 'is-invalid' : ''}`}
              placeholder="you@teamflow.ai"
              {...register('email', { required: 'Email is required' })}
            />
          </div>
        </FormField>

        <FormField label="Password" required error={errors.password?.message}>
          <div className="input-with-icon" style={{ position: 'relative' }}>
            <Lock size={16} />
            <input
              type={showPassword ? 'text' : 'password'}
              className={`form-control ${errors.password ? 'is-invalid' : ''}`}
              placeholder="Enter your password"
              style={{ paddingRight: 40 }}
              {...register('password', { required: 'Password is required' })}
            />
            <button
              type="button"
              onClick={() => setShowPassword((s) => !s)}
              style={{ position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', color: 'var(--color-text-faint)', cursor: 'pointer' }}
              aria-label="Toggle password visibility"
            >
              {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
            </button>
          </div>
        </FormField>

        <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 20 }}>
          <Link to="/forgot-password" style={{ fontSize: '0.8125rem', fontWeight: 600 }}>Forgot password?</Link>
        </div>

        <button className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? 'Signing in…' : (<><LogIn size={16} /> Sign in</>)}
        </button>
      </form>

      <div className="auth-footer-link">
        New to TeamFlow AI? <Link to="/register" style={{ fontWeight: 600 }}>Create an account</Link>
      </div>
    </div>
  );
}
