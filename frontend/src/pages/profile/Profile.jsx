import { Mail, Briefcase, ShieldCheck, IdCard } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import Avatar from '../../components/common/Avatar';
import { useAuth } from '../../hooks/useAuth';
import { humanize } from '../../constants/enums';

export default function Profile() {
  const { user } = useAuth();

  return (
    <div>
      <PageHeader title="My profile" description="Your personal details as seen by the rest of the organization." breadcrumb={[{ label: 'Workspace' }, { label: 'Profile' }]} />

      <div style={{ display: 'grid', gridTemplateColumns: '300px 1fr', gap: 20 }} className="dash-grid-main">
        <div className="card">
          <div className="card-body" style={{ textAlign: 'center' }}>
            <Avatar name={user?.fullName} size="lg" imageUrl={user?.profileImageUrl} />
            <h3 style={{ marginTop: 14, fontSize: '1.1rem' }}>{user?.fullName}</h3>
            <p className="text-muted-c" style={{ fontSize: '0.8125rem' }}>{user?.designation}</p>
            <hr className="divider" />
            <div style={{ textAlign: 'left', display: 'flex', flexDirection: 'column', gap: 12, fontSize: '0.8125rem' }}>
              <div style={{ display: 'flex', gap: 10 }}><Mail size={15} className="text-faint-c" /> {user?.email}</div>
              {user?.designation && <div style={{ display: 'flex', gap: 10 }}><Briefcase size={15} className="text-faint-c" /> {user.designation}</div>}
              <div style={{ display: 'flex', gap: 10 }}><ShieldCheck size={15} className="text-faint-c" /> {humanize(user?.role)}</div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Account details</h3></div>
          <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div className="form-row">
              <div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>First name</div><div style={{ fontWeight: 600 }}>{user?.firstName}</div></div>
              <div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>Last name</div><div style={{ fontWeight: 600 }}>{user?.lastName}</div></div>
            </div>
            <div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>Email</div><div style={{ fontWeight: 600 }}>{user?.email}</div></div>
            <div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>Role</div><div style={{ fontWeight: 600 }}>{humanize(user?.role)}</div></div>
            {user?.employeeId && (
              <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                <IdCard size={15} className="text-faint-c" />
                <span className="text-muted-c" style={{ fontSize: '0.8125rem' }}>
                  Your HR record can be updated from the Employees directory by an administrator.
                </span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
