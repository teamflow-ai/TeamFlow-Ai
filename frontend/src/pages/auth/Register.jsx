import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { User, Mail, Lock, UserPlus } from 'lucide-react';
import toast from 'react-hot-toast';
import FormField from '../../components/common/FormField';
import { useAuth } from '../../hooks/useAuth';

const PASSWORD_RULE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

export default function Register() {
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm();

  const onSubmit = async (values) => {
    setSubmitting(true);
    try {
      const user = await registerUser(values);
      toast.success(`Account created — welcome, ${user.firstName}`);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      toast.error(err.message || 'Registration failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-form-card">
      <h2>Create your account</h2>
      <p className="sub">Set up access to the TeamFlow AI workspace.</p>

      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="form-row">
          <FormField label="First name" required error={errors.firstName?.message}>
            <div className="input-with-icon">
              <User size={16} />
              <input className={`form-control ${errors.firstName ? 'is-invalid' : ''}`} placeholder="First name"
                {...register('firstName', { required: 'Required', maxLength: 50 })} />
            </div>
          </FormField>
          <FormField label="Last name" required error={errors.lastName?.message}>
            <input className={`form-control ${errors.lastName ? 'is-invalid' : ''}`} placeholder="Last name"
              {...register('lastName', { required: 'Required', maxLength: 50 })} />
          </FormField>
        </div>

        <FormField label="Work email" required error={errors.email?.message}>
          <div className="input-with-icon">
            <Mail size={16} />
            <input type="email" className={`form-control ${errors.email ? 'is-invalid' : ''}`} placeholder="you@teamflow.ai"
              {...register('email', { required: 'Email is required' })} />
          </div>
        </FormField>

        <FormField
          label="Password"
          required
          hint="8+ characters, with upper, lower, digit and special character."
          error={errors.password?.message}
        >
          <div className="input-with-icon">
            <Lock size={16} />
            <input type="password" className={`form-control ${errors.password ? 'is-invalid' : ''}`} placeholder="Create a strong password"
              {...register('password', { required: 'Password is required', pattern: { value: PASSWORD_RULE, message: 'Does not meet the password policy' } })} />
          </div>
        </FormField>

        <FormField label="Confirm password" required error={errors.confirmPassword?.message}>
          <div className="input-with-icon">
            <Lock size={16} />
            <input type="password" className={`form-control ${errors.confirmPassword ? 'is-invalid' : ''}`} placeholder="Re-enter password"
              {...register('confirmPassword', {
                required: 'Please confirm your password',
                validate: (v) => v === watch('password') || 'Passwords do not match',
              })} />
          </div>
        </FormField>

        <button className="btn btn-primary btn-block" disabled={submitting} style={{ marginTop: 8 }}>
          {submitting ? 'Creating account…' : (<><UserPlus size={16} /> Create account</>)}
        </button>
      </form>

      <div className="auth-footer-link">
        Already have an account? <Link to="/login" style={{ fontWeight: 600 }}>Sign in</Link>
      </div>
    </div>
  );
}
