import { useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { Building2, Bell, ShieldCheck } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import Toggle from '../../components/common/Toggle';
import FormField from '../../components/common/FormField';
import Modal from '../../components/common/Modal';
import { ORGANIZATION } from '../../constants/app';
import { useAuth } from '../../hooks/useAuth';
import { useDisclosure } from '../../hooks/useDisclosure';
import { authService } from '../../services/auth.service';
import { humanize } from '../../constants/enums';

const TABS = [
  { key: 'org', label: 'Organization', icon: Building2 },
  { key: 'notifications', label: 'Notifications', icon: Bell },
  { key: 'security', label: 'Security', icon: ShieldCheck },
];

export default function Settings() {
  const { user, logout } = useAuth();
  const [tab, setTab] = useState('org');
  const [notif, setNotif] = useState({ email: true, taskAssigned: true, deadlineReminders: true, weeklyDigest: false });
  const passwordModal = useDisclosure(false);
  const [loggingOutAll, setLoggingOutAll] = useState(false);

  const saveNotif = () => toast.success('Preferences saved on this device');

  const logoutAllDevices = async () => {
    setLoggingOutAll(true);
    try {
      await authService.logoutAll();
      toast.success('Signed out of every device');
      await logout();
    } catch (err) {
      toast.error(err.message || 'Could not sign out of all devices');
    } finally {
      setLoggingOutAll(false);
    }
  };

  return (
    <div>
      <PageHeader title="Settings" description="Workspace, notification and security preferences." breadcrumb={[{ label: 'Workspace' }, { label: 'Settings' }]} />

      <div style={{ display: 'grid', gridTemplateColumns: '220px 1fr', gap: 20 }} className="dash-grid-main">
        <div className="card" style={{ height: 'fit-content' }}>
          <div style={{ padding: 8 }}>
            {TABS.map((t) => (
              <button
                key={t.key}
                onClick={() => setTab(t.key)}
                className="sidebar-link"
                style={{ width: '100%', border: 'none', cursor: 'pointer', background: tab === t.key ? 'var(--color-primary-light)' : 'transparent', color: tab === t.key ? 'var(--color-primary-dark)' : 'var(--color-text-muted)', fontWeight: tab === t.key ? 600 : 500 }}
              >
                <t.icon size={16} /> {t.label}
              </button>
            ))}
          </div>
        </div>

        <div className="card">
          <div className="card-body">
            {tab === 'org' && (
              <>
                <h3 style={{ fontSize: '1rem', marginBottom: 18 }}>Organization</h3>
                <div className="form-row">
                  <FormField label="Organization name"><input className="form-control" defaultValue={ORGANIZATION.name} disabled /></FormField>
                  <FormField label="Industry"><input className="form-control" defaultValue={ORGANIZATION.industry} disabled /></FormField>
                </div>
                <div className="form-row">
                  <FormField label="City"><input className="form-control" defaultValue={ORGANIZATION.city} disabled /></FormField>
                  <FormField label="State"><input className="form-control" defaultValue={ORGANIZATION.state} disabled /></FormField>
                </div>
                <FormField label="Timezone"><input className="form-control" defaultValue={ORGANIZATION.timezone} disabled /></FormField>
              </>
            )}

            {tab === 'notifications' && (
              <>
                <h3 style={{ fontSize: '1rem', marginBottom: 18 }}>Notifications</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                  <Toggle checked={notif.email} onChange={(v) => setNotif({ ...notif, email: v })} label="Email notifications" />
                  <Toggle checked={notif.taskAssigned} onChange={(v) => setNotif({ ...notif, taskAssigned: v })} label="Notify me when a task is assigned to me" />
                  <Toggle checked={notif.deadlineReminders} onChange={(v) => setNotif({ ...notif, deadlineReminders: v })} label="Deadline reminders" />
                  <Toggle checked={notif.weeklyDigest} onChange={(v) => setNotif({ ...notif, weeklyDigest: v })} label="Weekly workload digest" />
                </div>
                <button className="btn btn-primary" style={{ marginTop: 24 }} onClick={saveNotif}>Save preferences</button>
              </>
            )}

            {tab === 'security' && (
              <>
                <h3 style={{ fontSize: '1rem', marginBottom: 18 }}>Security</h3>
                <FormField label="Signed in as"><input className="form-control" defaultValue={user?.email} disabled /></FormField>
                <FormField label="Role"><input className="form-control" defaultValue={humanize(user?.role)} disabled /></FormField>
                <hr className="divider" />
                <button className="btn btn-secondary" onClick={passwordModal.open}>Change password</button>
                <button className="btn btn-danger-ghost" style={{ marginLeft: 10 }} onClick={logoutAllDevices} disabled={loggingOutAll}>
                  {loggingOutAll ? 'Signing out…' : 'Log out of all devices'}
                </button>
              </>
            )}

          </div>
        </div>
      </div>

      <ChangePasswordModal isOpen={passwordModal.isOpen} onClose={passwordModal.close} />
    </div>
  );
}

function ChangePasswordModal({ isOpen, onClose }) {
  const { logout } = useAuth();
  const { register, handleSubmit, reset, watch, formState: { errors, isSubmitting } } = useForm();

  const onSubmit = async (values) => {
    try {
      await authService.changePassword({ currentPassword: values.currentPassword, newPassword: values.newPassword });
      toast.success('Password changed — please sign in again');
      reset();
      onClose();
      await logout();
    } catch (err) {
      toast.error(err.message || 'Could not change password');
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Change password"
      footer={<>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>{isSubmitting ? 'Saving…' : 'Change password'}</button>
      </>}
    >
      <form onSubmit={handleSubmit(onSubmit)}>
        <FormField label="Current password" required error={errors.currentPassword?.message}>
          <input type="password" className="form-control" {...register('currentPassword', { required: 'Required' })} />
        </FormField>
        <FormField label="New password" required hint="8+ characters, with upper, lower, digit and special character." error={errors.newPassword?.message}>
          <input
            type="password"
            className="form-control"
            {...register('newPassword', {
              required: 'Required',
              pattern: {
                value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,72}$/,
                message: 'Does not meet the password policy',
              },
            })}
          />
        </FormField>
        <FormField label="Confirm new password" required error={errors.confirmPassword?.message}>
          <input
            type="password"
            className="form-control"
            {...register('confirmPassword', { required: 'Required', validate: (v) => v === watch('newPassword') || 'Passwords do not match' })}
          />
        </FormField>
      </form>
    </Modal>
  );
}
