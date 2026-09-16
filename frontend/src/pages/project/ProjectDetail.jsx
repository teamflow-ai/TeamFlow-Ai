import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Edit2, Calendar, IndianRupee, Trello, HeartPulse, UserPlus, Flag } from 'lucide-react';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import Avatar from '../../components/common/Avatar';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import Modal from '../../components/common/Modal';
import FormField from '../../components/common/FormField';
import ProjectFormModal from './ProjectFormModal';
import { useDisclosure } from '../../hooks/useDisclosure';
import { useAuth } from '../../hooks/useAuth';
import { projectService } from '../../services/project.service';
import { taskService } from '../../services/task.service';
import { sprintService } from '../../services/sprint.service';
import { bugService } from '../../services/bug.service';
import { meetingService } from '../../services/meeting.service';
import { milestoneService } from '../../services/milestone.service';
import { employeeService } from '../../services/employee.service';
import { aiService } from '../../services/ai.service';
import { PERMISSIONS } from '../../constants/enums';

const TABS = ['Overview', 'Tasks', 'Sprints', 'Bugs', 'Meetings', 'Milestones', 'Members', 'Health'];

export default function ProjectDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { hasPermission } = useAuth();
  const [project, setProject] = useState(null);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('Overview');
  const editModal = useDisclosure(false);
  const memberModal = useDisclosure(false);

  const [tasks, setTasks] = useState([]);
  const [sprints, setSprints] = useState([]);
  const [bugs, setBugs] = useState([]);
  const [meetings, setMeetings] = useState([]);
  const [milestones, setMilestones] = useState([]);
  const [health, setHealth] = useState(null);
  const [healthLoading, setHealthLoading] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      setProject(await projectService.get(id));
    } catch {
      navigate('/projects');
      return;
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [id]);

  useEffect(() => {
    if (!project) return;
    taskService.list({ projectId: id, size: 50 }).then((r) => setTasks(r.content)).catch(() => {});
    sprintService.list({ projectId: id, size: 50 }).then((r) => setSprints(r.content)).catch(() => {});
    bugService.list({ projectId: id, size: 50 }).then((r) => setBugs(r.content)).catch(() => {});
    meetingService.list({ projectId: id, size: 50 }).then((r) => setMeetings(r.content)).catch(() => {});
    milestoneService.list({ projectId: id, size: 50 }).then((r) => setMilestones(r.content)).catch(() => {});
  }, [project, id]);

  useEffect(() => {
    if (tab !== 'Health' || !project || !hasPermission(PERMISSIONS.VIEW_ANALYTICS)) return;
    setHealthLoading(true);
    projectService.health(id).then(setHealth).catch(() => setHealth(null)).finally(() => setHealthLoading(false));
  }, [tab, project, id]);

  if (loading || !project) return <LoadingSpinner size="lg" label="Loading project…" />;

  return (
    <div>
      <PageHeader
        breadcrumb={[{ label: 'Delivery' }, { label: 'Projects', to: '/projects' }, { label: project.name }]}
        title={project.name}
        description={project.description}
        actions={
          <>
            <Link to={`/projects/${id}/kanban`} className="btn btn-secondary"><Trello size={15} /> Kanban board</Link>
            {hasPermission(PERMISSIONS.UPDATE_PROJECT) && (
              <button className="btn btn-secondary" onClick={editModal.open}><Edit2 size={15} /> Edit</button>
            )}
          </>
        }
      />
      <button className="btn btn-ghost btn-sm" onClick={() => navigate('/projects')} style={{ marginBottom: 16 }}>
        <ArrowLeft size={14} /> Back to projects
      </button>

      <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', marginBottom: 20 }}>
        <div className="card" style={{ flex: '1 1 160px' }}><div className="card-body"><StatusBadge status={project.status} /><div style={{ marginTop: 8, fontSize: '0.75rem' }} className="text-muted-c">Status</div></div></div>
        <div className="card" style={{ flex: '1 1 160px' }}><div className="card-body"><div style={{ fontWeight: 700, fontSize: '1.25rem' }}>{project.progressPercent}%</div><div style={{ fontSize: '0.75rem' }} className="text-muted-c">Progress</div></div></div>
        <div className="card" style={{ flex: '1 1 160px' }}><div className="card-body"><div style={{ fontWeight: 700, fontSize: '1.25rem', display: 'flex', alignItems: 'center', gap: 4 }}><IndianRupee size={16} />{project.budget ? (project.budget / 100000).toFixed(1) : '0'}L</div><div style={{ fontSize: '0.75rem' }} className="text-muted-c">Budget</div></div></div>
        <div className="card" style={{ flex: '1 1 160px' }}><div className="card-body"><div style={{ fontWeight: 700, fontSize: '1.25rem', display: 'flex', alignItems: 'center', gap: 6 }}><Calendar size={16} />{project.endDate || '—'}</div><div style={{ fontSize: '0.75rem' }} className="text-muted-c">Target end</div></div></div>
      </div>

      <div style={{ display: 'flex', gap: 24, borderBottom: '1px solid var(--color-border)', marginBottom: 24, overflowX: 'auto', paddingLeft: 8 }}>
        {TABS.map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            style={{
              position: 'relative',
              background: 'none',
              border: 'none',
              padding: '10px 4px',
              fontSize: '0.875rem',
              fontWeight: 600,
              cursor: 'pointer',
              color: tab === t ? 'var(--color-primary)' : 'var(--color-text-muted)',
              whiteSpace: 'nowrap',
            }}
          >
            {t}
            {tab === t && (
              <motion.div
                layoutId="activeProjectTab"
                style={{
                  position: 'absolute',
                  bottom: -1,
                  left: 0,
                  right: 0,
                  height: 2,
                  backgroundColor: 'var(--color-primary)',
                }}
              />
            )}
          </button>
        ))}
      </div>

      {tab === 'Overview' && (
        <div className="card"><div className="card-body">
          <p style={{ fontSize: '0.9rem', lineHeight: 1.7 }}>{project.description || 'No description provided.'}</p>
          <hr className="divider" />
          <div className="form-row">
            <div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>Client</div><div style={{ fontWeight: 600 }}>{project.clientName || 'Internal'}</div></div>
            <div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>Manager</div><div style={{ fontWeight: 600 }}>{project.managerName}</div></div>
          </div>
          <div className="form-row">
            <div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>Start date</div><div style={{ fontWeight: 600 }}>{project.startDate || '—'}</div></div>
            <div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>Priority</div><StatusBadge status={project.priority} /></div>
          </div>
          {hasPermission(PERMISSIONS.REQUEST_PROJECT_CLOSURE) && project.status === 'ACTIVE' && (
            <>
              <hr className="divider" />
              <button
                className="btn btn-secondary btn-sm"
                onClick={async () => {
                  try {
                    await projectService.requestClosure(id, 'Requesting closure — all deliverables complete.');
                    toast.success('Closure requested — awaiting approval');
                  } catch (err) { toast.error(err.message || 'Could not request closure'); }
                }}
              >
                <Flag size={14} /> Request project closure
              </button>
            </>
          )}
        </div></div>
      )}

      {tab === 'Tasks' && (
        <div className="card">
          <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Tasks in this project</h3><Link to={`/tasks?projectId=${id}`} className="btn btn-secondary btn-sm">Manage tasks</Link></div>
          <div className="card-body">
            {tasks.length === 0 && <p className="text-muted-c">No tasks yet.</p>}
            {tasks.map((t) => (
              <Link key={t.id} to={`/tasks/${t.id}`} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderBottom: '1px solid var(--color-border)', color: 'inherit' }}>
                <div><div style={{ fontWeight: 600, fontSize: '0.875rem' }}>{t.title}</div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>{t.assigneeName || 'Unassigned'}</div></div>
                <StatusBadge status={t.status} />
              </Link>
            ))}
          </div>
        </div>
      )}

      {tab === 'Sprints' && (
        <div className="card"><div className="card-body">
          {sprints.length === 0 && <p className="text-muted-c">No sprints yet.</p>}
          {sprints.map((s) => (
            <div key={s.id} style={{ padding: '12px 0', borderBottom: '1px solid var(--color-border)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <strong style={{ fontSize: '0.9rem' }}>{s.name}</strong>
                <StatusBadge status={s.status} />
              </div>
              <div className="text-muted-c" style={{ fontSize: '0.8125rem', marginTop: 4 }}>{s.goal}</div>
              <div className="text-faint-c" style={{ fontSize: '0.75rem', marginTop: 4 }}>{s.startDate || '—'} → {s.endDate || '—'}</div>
            </div>
          ))}
        </div></div>
      )}

      {tab === 'Bugs' && (
        <div className="card"><div className="card-body">
          {bugs.length === 0 && <p className="text-muted-c">No bugs reported.</p>}
          {bugs.map((b) => (
            <div key={b.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderBottom: '1px solid var(--color-border)' }}>
              <div><div style={{ fontWeight: 600, fontSize: '0.875rem' }}>{b.title}</div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>{b.assigneeName || 'Unassigned'}</div></div>
              <div style={{ display: 'flex', gap: 8 }}><StatusBadge status={b.severity} /><StatusBadge status={b.status} /></div>
            </div>
          ))}
        </div></div>
      )}

      {tab === 'Meetings' && (
        <div className="card"><div className="card-body">
          {meetings.length === 0 && <p className="text-muted-c">No meetings scheduled.</p>}
          {meetings.map((m) => (
            <div key={m.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderBottom: '1px solid var(--color-border)' }}>
              <div><div style={{ fontWeight: 600, fontSize: '0.875rem' }}>{m.title}</div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>{new Date(m.scheduledAt).toLocaleString('en-IN')}</div></div>
              <StatusBadge status={m.status} />
            </div>
          ))}
        </div></div>
      )}

      {tab === 'Milestones' && (
        <div className="card"><div className="card-body">
          {milestones.length === 0 && <p className="text-muted-c">No milestones yet.</p>}
          {milestones.map((m) => (
            <div key={m.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderBottom: '1px solid var(--color-border)' }}>
              <div><div style={{ fontWeight: 600, fontSize: '0.875rem' }}>{m.title}</div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>Due {m.dueDate || '—'}</div></div>
              <StatusBadge status={m.status} />
            </div>
          ))}
          <div style={{ marginTop: 12 }}>
            <Link to="/milestones" className="btn btn-secondary btn-sm">Manage milestones</Link>
          </div>
        </div></div>
      )}

      {tab === 'Members' && (
        <div className="card">
          <div className="card-header">
            <h3 style={{ fontSize: '1rem' }}>Project members</h3>
            {hasPermission(PERMISSIONS.UPDATE_PROJECT) && (
              <button className="btn btn-secondary btn-sm" onClick={memberModal.open}><UserPlus size={14} /> Add member</button>
            )}
          </div>
          <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {(project.members || []).length === 0 && <p className="text-muted-c">No members added yet.</p>}
            {(project.members || []).map((m) => (
              <div key={m.employeeId} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <Avatar name={m.employeeName} size="sm" />
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '0.875rem' }}>{m.employeeName}</div>
                    <div className="text-faint-c" style={{ fontSize: '0.75rem' }}>
                      {m.roleOnProject || 'Member'} • {m.allocatedHours || 40}h/week allocated
                    </div>
                  </div>
                </div>
                {hasPermission(PERMISSIONS.UPDATE_PROJECT) && (
                  <button
                    className="btn btn-danger-ghost btn-sm"
                    onClick={async () => {
                      try {
                        await projectService.removeMember(id, m.employeeId);
                        toast.success('Member removed');
                        load();
                      } catch (err) { toast.error(err.message || 'Could not remove member'); }
                    }}
                  >
                    Remove
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {tab === 'Health' && (
        <div className="card"><div className="card-body">
          {!hasPermission(PERMISSIONS.VIEW_ANALYTICS) && <p className="text-muted-c">You don&apos;t have permission to view project health.</p>}
          {hasPermission(PERMISSIONS.VIEW_ANALYTICS) && healthLoading && <LoadingSpinner label="Scoring project health…" />}
          {hasPermission(PERMISSIONS.VIEW_ANALYTICS) && !healthLoading && health && (
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 16 }}>
                <HeartPulse size={28} color="var(--color-primary)" />
                <div>
                  <div style={{ fontSize: '1.75rem', fontWeight: 700 }}>{Math.round(health.healthScore)}/100</div>
                  <span className={`badge badge-${health.category === 'HEALTHY' ? 'success' : health.category === 'AT_RISK' ? 'warning' : 'danger'}`}>{health.category}</span>
                </div>
              </div>
              <p style={{ fontSize: '0.9rem', lineHeight: 1.7 }}>{health.summary}</p>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16, marginTop: 16 }}>
                <div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>Tasks completed</div><div style={{ fontWeight: 700 }}>{health.completedTasks}/{health.totalTasks}</div></div>
                <div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>Delayed tasks</div><div style={{ fontWeight: 700 }}>{health.delayedTasks}</div></div>
                <div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>Blocked tasks</div><div style={{ fontWeight: 700 }}>{health.blockedTasks}</div></div>
                <div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>Open bugs</div><div style={{ fontWeight: 700 }}>{health.openBugs}</div></div>
                <div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>On leave today</div><div style={{ fontWeight: 700 }}>{health.employeesOnLeave}</div></div>
              </div>
            </div>
          )}
          {hasPermission(PERMISSIONS.VIEW_ANALYTICS) && !healthLoading && !health && <p className="text-muted-c">Health score unavailable right now.</p>}
        </div></div>
      )}

      <ProjectFormModal isOpen={editModal.isOpen} onClose={editModal.close} onSaved={load} project={project} />
      <AddMemberModal isOpen={memberModal.isOpen} onClose={memberModal.close} onSaved={load} projectId={id} existingIds={(project.members || []).map((m) => m.employeeId)} />
    </div>
  );
}

function AddMemberModal({ isOpen, onClose, onSaved, projectId, existingIds }) {
  const [employees, setEmployees] = useState([]);
  const [employeeId, setEmployeeId] = useState('');
  const [roleOnProject, setRoleOnProject] = useState('');
  const [allocatedHours, setAllocatedHours] = useState(40);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (isOpen) employeeService.list({ size: 200, active: true }).then((r) => setEmployees(r.content)).catch(() => {});
  }, [isOpen]);

  const submit = async () => {
    if (!employeeId) return;
    setSaving(true);
    try {
      await projectService.addMember(projectId, { 
        employeeId, 
        roleOnProject: roleOnProject || null,
        allocatedHours: Number(allocatedHours) || 40 
      });
      toast.success('Member added');
      onSaved();
      onClose();
      setEmployeeId('');
      setRoleOnProject('');
      setAllocatedHours(40);
    } catch (err) {
      toast.error(err.message || 'Could not add member');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Add project member"
      footer={<>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={submit} disabled={saving || !employeeId}>{saving ? 'Adding…' : 'Add member'}</button>
      </>}
    >
      <FormField label="Employee" required>
        <select className="form-select" value={employeeId} onChange={(e) => setEmployeeId(e.target.value)}>
          <option value="">Select employee</option>
          {employees.filter((e) => !existingIds.includes(e.id)).map((e) => <option key={e.id} value={e.id}>{e.fullName}</option>)}
        </select>
      </FormField>
      <FormField label="Role on project" hint="e.g. Frontend Developer, QA Lead">
        <input className="form-control" value={roleOnProject} onChange={(e) => setRoleOnProject(e.target.value)} />
      </FormField>
      <FormField label="Allocated Capacity (hours/week)" hint="How many hours this employee is expected to dedicate to this project">
        <input type="number" min={1} max={80} className="form-control" value={allocatedHours} onChange={(e) => setAllocatedHours(e.target.value)} />
      </FormField>
    </Modal>
  );
}
