import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import {
  FolderKanban, ListChecks, CheckCircle2, Bug, CalendarClock,
  AlertTriangle, Sparkles, ArrowUpRight, Newspaper, Briefcase,
} from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';
import { Link } from 'react-router-dom';
import StatCard from '../../components/common/StatCard';
import Avatar from '../../components/common/Avatar';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { dashboardService } from '../../services/dashboard.service';
import { aiService } from '../../services/ai.service';
import { projectService } from '../../services/project.service';
import { taskService } from '../../services/task.service';
import { useAuth } from '../../hooks/useAuth';
import { humanize, PERMISSIONS } from '../../constants/enums';

const PIE_COLORS = { PLANNING: '#0ea5e9', ACTIVE: '#4f46e5', ON_HOLD: '#f59e0b', COMPLETED: '#22c55e', CANCELLED: '#ef4444' };

export default function Dashboard() {
  const { user, hasPermission } = useAuth();
  const canViewAnalytics = hasPermission(PERMISSIONS.VIEW_ANALYTICS);
  const [summary, setSummary] = useState(null);
  const [workload, setWorkload] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [brief, setBrief] = useState(null);
  const [projects, setProjects] = useState([]);
  const [clientProjects, setClientProjects] = useState({});
  const [statusSplit, setStatusSplit] = useState([]);
  const [myTasks, setMyTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [s, w, a, b, projRes, taskRes] = await Promise.all([
          canViewAnalytics ? dashboardService.summary().catch(() => null) : Promise.resolve(null),
          canViewAnalytics ? dashboardService.workload().catch(() => []) : Promise.resolve([]),
          canViewAnalytics ? aiService.alerts().catch(() => []) : Promise.resolve([]),
          canViewAnalytics ? aiService.dailyBrief().catch(() => null) : Promise.resolve(null),
          projectService.list({ size: 100 }).catch(() => ({ content: [] })),
          user?.employeeId
            ? taskService.list({ assigneeId: user.employeeId, size: 5 })
                .then((res) => (res?.content?.length ? res : taskService.list({ size: 5 })))
                .catch(() => taskService.list({ size: 5 }).catch(() => ({ content: [] })))
            : taskService.list({ size: 5 }).catch(() => ({ content: [] })),
        ]);

        setSummary(s);
        setWorkload(Array.isArray(w) ? w : []);
        setAlerts(Array.isArray(a) ? a : []);
        setBrief(b);

        const projectList = projRes?.content || [];
        setProjects(projectList.slice(0, 6));
        setMyTasks(taskRes?.content || []);
        
        const groupedByClient = {};
        projectList.forEach((p) => {
          const client = p.clientName || 'Internal Initiatives';
          if (!groupedByClient[client]) groupedByClient[client] = [];
          groupedByClient[client].push(p);
        });
        setClientProjects(groupedByClient);

        const counts = {};
        projectList.forEach((p) => { counts[p.status] = (counts[p.status] || 0) + 1; });
        setStatusSplit(Object.entries(counts).map(([name, value]) => ({ name, value })));
      } catch (err) {
        console.error('Error loading dashboard data:', err);
      } finally {
        setLoading(false);
      }
    })();
  }, [canViewAnalytics, user]);

  if (loading) return <LoadingSpinner size="lg" label="Loading your dashboard…" />;

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: 16, marginBottom: 28 }}>
        <div>
          <h1 style={{ fontSize: '1.5rem' }}>Welcome back, {user?.fullName?.split(' ')[0]}</h1>
          <p className="text-muted-c" style={{ marginTop: 6, fontSize: '0.9rem' }}>
            Here&apos;s what&apos;s happening across TeamFlow AI today.
          </p>
        </div>
        <Link to="/tasks" className="btn btn-primary">
          <ListChecks size={16} /> View all tasks
        </Link>
      </div>

      {summary && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 18, marginBottom: 24 }}>
          <StatCard icon={FolderKanban} label="Active projects" value={summary.activeProjects} tone="primary" delay={0} />
          <StatCard icon={ListChecks} label="Pending tasks" value={summary.pendingTasks} tone="info" delay={0.05} />
          <StatCard icon={CheckCircle2} label="Completed tasks" value={summary.completedTasks} tone="accent" delay={0.1} />
          <StatCard icon={Bug} label="Open bugs" value={summary.openBugs} tone="danger" delay={0.15} />
          <StatCard icon={CalendarClock} label="Today's meetings" value={summary.todaysMeetings} tone="warning" delay={0.2} />
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1.6fr 1fr', gap: 20, marginBottom: 20 }} className="dash-grid-main">
        <div className="card">
          <div className="card-header">
            <h3 style={{ fontSize: '1rem' }}>Recent projects</h3>
            <Link to="/projects" style={{ fontSize: '0.8125rem', fontWeight: 600 }}>View all</Link>
          </div>
          <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {projects.length === 0 && <p className="text-muted-c" style={{ fontSize: '0.875rem' }}>No projects yet.</p>}
            {projects.map((p) => (
              <Link key={p.id} to={`/projects/${p.id}`} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: 'inherit' }}>
                <div>
                  <div style={{ fontSize: '0.875rem', fontWeight: 600 }}>{p.name}</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--color-text-faint)' }}>{p.clientName || 'Internal'} · {p.managerName}</div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                  <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>{p.progressPercent}%</span>
                  <StatusBadge status={p.status} />
                </div>
              </Link>
            ))}
            <div style={{ borderTop: '1px solid var(--color-border)', paddingTop: 14, marginTop: 4 }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 10 }}>
                <div style={{ fontSize: '0.8125rem', fontWeight: 600 }}>{user?.role === 'SUPER_ADMIN' ? 'Recent active tasks' : 'My tasks'}</div>
                <Link to="/tasks" style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--color-primary)' }}>View all tasks</Link>
              </div>
              {myTasks.length === 0 && <p className="text-muted-c" style={{ fontSize: '0.8125rem' }}>No active tasks at the moment.</p>}
              {myTasks.map((t) => (
                <Link key={t.id} to={`/tasks/${t.id}`} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '7px 0', color: 'inherit' }}>
                  <span style={{ fontSize: '0.8125rem' }}>{t.title}</span>
                  <StatusBadge status={t.status} />
                </Link>
              ))}
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Project status</h3></div>
          <div className="card-body" style={{ height: 280, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
            {statusSplit.length === 0 ? (
              <p className="text-muted-c" style={{ fontSize: '0.875rem' }}>No projects yet.</p>
            ) : (
              <>
                <ResponsiveContainer width="100%" height={180}>
                  <PieChart>
                    <Pie data={statusSplit} dataKey="value" nameKey="name" innerRadius={50} outerRadius={78} paddingAngle={3}>
                      {statusSplit.map((entry) => (
                        <Cell key={entry.name} fill={PIE_COLORS[entry.name] || '#94a3b8'} />
                      ))}
                    </Pie>
                    <Tooltip contentStyle={{ borderRadius: 10, border: '1px solid var(--color-border)', fontSize: 12 }} />
                  </PieChart>
                </ResponsiveContainer>
                <div style={{ display: 'flex', gap: 14, flexWrap: 'wrap', justifyContent: 'center', marginTop: 8 }}>
                  {statusSplit.map((s) => (
                    <span key={s.name} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                      <span style={{ width: 8, height: 8, borderRadius: '50%', background: PIE_COLORS[s.name] || '#94a3b8' }} /> {humanize(s.name)}
                    </span>
                  ))}
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      {canViewAnalytics && (
        <>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, marginBottom: 20 }} className="dash-grid-secondary">
            <div className="card">
              <div className="card-header">
                <h3 style={{ fontSize: '1rem', display: 'flex', alignItems: 'center', gap: 8 }}>
                  <Sparkles size={16} color="var(--color-accent)" /> Live workload board
                </h3>
                <Link to="/reports" style={{ fontSize: '0.8125rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: 4 }}>
                  Full report <ArrowUpRight size={13} />
                </Link>
              </div>
              <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                {workload.length === 0 && <p className="text-muted-c" style={{ fontSize: '0.875rem' }}>No active employees to score.</p>}
                {workload.map((w) => (
                  <div key={w.employeeId}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 6 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                        <Avatar name={w.employeeName} size="sm" />
                        <div>
                          <div style={{ fontSize: '0.8125rem', fontWeight: 600 }}>{w.employeeName}</div>
                          <div style={{ fontSize: '0.6875rem', color: 'var(--color-text-faint)' }}>
                            {w.activeTaskCount} active · {w.remainingEstimatedHours}h remaining
                          </div>
                        </div>
                      </div>
                      <span className={`badge badge-${w.band === 'HIGH' || w.band === 'CRITICAL' ? 'danger' : w.band === 'MODERATE' ? 'warning' : 'success'}`}>
                        {humanize(w.band)}
                      </span>
                    </div>
                    <div style={{ height: 6, borderRadius: 999, background: 'var(--color-bg)', overflow: 'hidden' }}>
                      <motion.div
                        initial={{ width: 0 }}
                        animate={{ width: `${Math.min(w.workloadScore, 100)}%` }}
                        transition={{ duration: 0.6, ease: 'easeOut' }}
                        style={{
                          height: '100%',
                          borderRadius: 999,
                          background: w.workloadScore > 70 ? 'var(--color-danger)' : w.workloadScore > 40 ? 'var(--color-warning)' : 'var(--color-success)',
                        }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="card">
              <div className="card-header">
                <h3 style={{ fontSize: '1rem' }}>Alerts</h3>
                <span className="badge badge-danger">{alerts.length} active</span>
              </div>
              <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                {alerts.length === 0 && <p className="text-muted-c" style={{ fontSize: '0.875rem' }}>No active alerts.</p>}
                {alerts.map((a, i) => (
                  <div key={i} style={{ display: 'flex', gap: 12, padding: 12, borderRadius: 10, background: a.severity === 'CRITICAL' ? 'var(--color-danger-light)' : 'var(--color-warning-light)' }}>
                    <AlertTriangle size={16} color={a.severity === 'CRITICAL' ? 'var(--color-danger)' : 'var(--color-warning)'} style={{ flexShrink: 0, marginTop: 2 }} />
                    <div>
                      <div style={{ fontSize: '0.8125rem', fontWeight: 600 }}>{humanize(a.type)}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: 2 }}>{a.message}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {brief && (
            <div className="card">
              <div className="card-header"><h3 style={{ fontSize: '1rem', display: 'flex', alignItems: 'center', gap: 8 }}><Newspaper size={16} color="var(--color-primary)" /> Today&apos;s manager brief</h3></div>
              <div className="card-body">
                <p style={{ fontSize: '0.9rem', lineHeight: 1.7, marginBottom: 14 }}>{brief.summary}</p>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 16 }}>
                  {[
                    ['Today\'s deadlines', brief.todaysDeadlines],
                    ['Blocked tasks', brief.blockedTasks],
                    ['Overloaded employees', brief.overloadedEmployees],
                    ['On leave today', brief.employeesOnLeaveToday],
                    ['Upcoming meetings', brief.upcomingMeetings],
                    ['At-risk projects', brief.atRiskProjects],
                    ['Suggested actions', brief.suggestedActions],
                  ].filter(([, items]) => items && items.length > 0).map(([label, items]) => (
                    <div key={label}>
                      <div className="text-faint-c" style={{ fontSize: '0.75rem', fontWeight: 600, marginBottom: 6 }}>{label}</div>
                      <ul style={{ paddingLeft: 16, fontSize: '0.8125rem', lineHeight: 1.8 }}>
                        {items.map((it, i) => <li key={i}>{it}</li>)}
                      </ul>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          <div className="card" style={{ marginTop: 20 }}>
            <div className="card-header">
              <h3 style={{ fontSize: '1rem', display: 'flex', alignItems: 'center', gap: 8 }}>
                <Briefcase size={16} color="var(--color-primary)" /> Client & Project Portfolio
              </h3>
              <Link to="/clients" style={{ fontSize: '0.8125rem', fontWeight: 600 }}>Manage clients</Link>
            </div>
            <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
              {Object.keys(clientProjects).length === 0 && (
                <p className="text-muted-c" style={{ fontSize: '0.875rem' }}>No client projects recorded yet.</p>
              )}
              {Object.entries(clientProjects).map(([clientName, projs]) => (
                <div key={clientName} style={{ border: '1px solid var(--color-border)', borderRadius: 10, padding: 14 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                    <div style={{ fontWeight: 600, fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: 6 }}>
                      <Briefcase size={14} className="text-faint-c" /> {clientName}
                    </div>
                    <span className="badge badge-neutral" style={{ fontSize: '0.6875rem' }}>
                      {projs.length} {projs.length === 1 ? 'project' : 'projects'}
                    </span>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    {projs.map((p) => (
                      <Link
                        key={p.id}
                        to={`/projects/${p.id}`}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                          padding: '8px 10px',
                          borderRadius: 8,
                          background: 'var(--color-bg)',
                          color: 'inherit',
                          fontSize: '0.8125rem',
                          textDecoration: 'none',
                        }}
                      >
                        <div>
                          <span style={{ fontWeight: 600 }}>{p.name}</span>
                          <span className="text-faint-c" style={{ marginLeft: 8, fontSize: '0.75rem' }}>Mgr: {p.managerName || 'Unassigned'}</span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                          <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>{p.progressPercent}%</span>
                          <StatusBadge status={p.status} />
                        </div>
                      </Link>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
