import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Mail, Phone, Calendar, Briefcase, Users2, Edit2, ArrowLeft } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import Avatar from '../../components/common/Avatar';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmployeeFormModal from './EmployeeFormModal';
import { useDisclosure } from '../../hooks/useDisclosure';
import { useAuth } from '../../hooks/useAuth';
import { employeeService } from '../../services/employee.service';
import { taskService } from '../../services/task.service';
import { worklogService } from '../../services/worklog.service';

export default function EmployeeDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [employee, setEmployee] = useState(null);
  const [assignedTasks, setAssignedTasks] = useState([]);
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const editModal = useDisclosure(false);

  const load = async () => {
    setLoading(true);
    try {
      const emp = await employeeService.get(id);
      setEmployee(emp);
      taskService.list({ assigneeId: id, size: 6 }).then((r) => setAssignedTasks(r.content)).catch(() => {});
      if (user?.employeeId === id) {
        worklogService.mine({ size: 6 }).then((r) => setLogs(r.content)).catch(() => {});
      }
    } catch {
      navigate('/employees');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [id]);

  if (loading || !employee) return <LoadingSpinner size="lg" label="Loading employee…" />;

  return (
    <div>
      <PageHeader
        breadcrumb={[{ label: 'Workforce' }, { label: 'Employees', to: '/employees' }, { label: employee.fullName }]}
        title={employee.fullName}
        description={employee.designation}
        actions={<button className="btn btn-secondary" onClick={editModal.open}><Edit2 size={15} /> Edit</button>}
      />

      <button className="btn btn-ghost btn-sm" onClick={() => navigate('/employees')} style={{ marginBottom: 16 }}>
        <ArrowLeft size={14} /> Back to employees
      </button>

      <div style={{ display: 'grid', gridTemplateColumns: '340px 1fr', gap: 20 }} className="dash-grid-main">
        <div className="card">
          <div className="card-body" style={{ textAlign: 'center' }}>
            <Avatar name={employee.fullName} size="lg" />
            <h3 style={{ marginTop: 14, fontSize: '1.1rem' }}>{employee.fullName}</h3>
            <p className="text-muted-c" style={{ fontSize: '0.8125rem', marginTop: 4 }}>{employee.employeeCode}</p>
            <div style={{ marginTop: 10 }}>
              <StatusBadge status={employee.active ? 'DONE' : 'CANCELLED'} label={employee.active ? 'Active' : 'Inactive'} />
            </div>
            <hr className="divider" />
            <div style={{ textAlign: 'left', display: 'flex', flexDirection: 'column', gap: 12, fontSize: '0.8125rem' }}>
              <div style={{ display: 'flex', gap: 10 }}><Mail size={15} className="text-faint-c" /> {employee.workEmail}</div>
              {employee.phone && <div style={{ display: 'flex', gap: 10 }}><Phone size={15} className="text-faint-c" /> {employee.phone}</div>}
              <div style={{ display: 'flex', gap: 10 }}><Briefcase size={15} className="text-faint-c" /> {employee.departmentName || '—'} · {employee.teamName || '—'}</div>
              <div style={{ display: 'flex', gap: 10 }}><Users2 size={15} className="text-faint-c" /> Reports to {employee.managerName || '—'}</div>
              {employee.dateOfJoining && <div style={{ display: 'flex', gap: 10 }}><Calendar size={15} className="text-faint-c" /> Joined {employee.dateOfJoining}</div>}
            </div>
            <hr className="divider" />
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8125rem' }}>
              <div><div style={{ fontWeight: 700, fontSize: '1.1rem' }}>{employee.weeklyCapacityHours ?? '—'}h</div><div className="text-faint-c">Weekly capacity</div></div>
              <div><div style={{ fontWeight: 700, fontSize: '1.1rem' }}>{employee.yearsOfExperience ?? '—'}y</div><div className="text-faint-c">Experience</div></div>
              <div><div style={{ fontWeight: 700, fontSize: '1.1rem' }}>{employee.annualLeaveBalance ?? '—'}</div><div className="text-faint-c">Leave balance</div></div>
            </div>
            {employee.skills?.length > 0 && (
              <>
                <hr className="divider" />
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, justifyContent: 'center' }}>
                  {employee.skills.map((s) => <span key={s} className="badge badge-primary">{s}</span>)}
                </div>
              </>
            )}
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <div className="card">
            <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Assigned tasks</h3></div>
            <div className="card-body">
              {assignedTasks.length === 0 && <p className="text-muted-c" style={{ fontSize: '0.875rem' }}>No tasks currently assigned.</p>}
              {assignedTasks.map((t) => (
                <Link key={t.id} to={`/tasks/${t.id}`} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderBottom: '1px solid var(--color-border)', color: 'inherit' }}>
                  <div>
                    <div style={{ fontSize: '0.875rem', fontWeight: 600 }}>{t.title}</div>
                    <div className="text-faint-c" style={{ fontSize: '0.75rem' }}>Due {t.dueDate || '—'}</div>
                  </div>
                  <StatusBadge status={t.status} />
                </Link>
              ))}
            </div>
          </div>

          {user?.employeeId === employee.id && (
            <div className="card">
              <div className="card-header"><h3 style={{ fontSize: '1rem' }}>My recent work logs</h3></div>
              <div className="card-body">
                {logs.length === 0 && <p className="text-muted-c" style={{ fontSize: '0.875rem' }}>No work logged yet.</p>}
                {logs.map((w) => (
                  <div key={w.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderBottom: '1px solid var(--color-border)' }}>
                    <div>
                      <div style={{ fontSize: '0.875rem' }}>{w.notes || 'Work logged'}</div>
                      <div className="text-faint-c" style={{ fontSize: '0.75rem' }}>{w.logDate}</div>
                    </div>
                    <span style={{ fontWeight: 600, fontSize: '0.875rem' }}>{w.hours}h</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      <EmployeeFormModal isOpen={editModal.isOpen} onClose={editModal.close} onSaved={load} employee={employee} />
    </div>
  );
}
