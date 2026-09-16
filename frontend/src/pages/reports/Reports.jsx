import { useEffect, useState } from 'react';
import { AlertTriangle, Sparkles, Download } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import toast from 'react-hot-toast';
import PageHeader from '../../components/common/PageHeader';
import Avatar from '../../components/common/Avatar';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { dashboardService } from '../../services/dashboard.service';
import { aiService } from '../../services/ai.service';
import { projectService } from '../../services/project.service';
import { sprintService } from '../../services/sprint.service';
import { employeeService } from '../../services/employee.service';
import { reportService } from '../../services/report.service';
import { humanize } from '../../constants/enums';

export default function Reports() {
  const [workload, setWorkload] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [projects, setProjects] = useState([]);
  const [employees, setEmployees] = useState([]);

  const [reportType, setReportType] = useState('project');
  const [selectedProjectId, setSelectedProjectId] = useState('');
  const [selectedSprintId, setSelectedSprintId] = useState('');
  const [sprints, setSprints] = useState([]);
  const [selectedEmployeeId, setSelectedEmployeeId] = useState('');
  const [fromDate, setFromDate] = useState(() => new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 10));
  const [toDate, setToDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    Promise.all([dashboardService.workload(), aiService.alerts()])
      .then(([w, a]) => { setWorkload(w); setAlerts(a); })
      .finally(() => setLoading(false));
    projectService.list({ size: 200 }).then((r) => setProjects(r.content)).catch(() => {});
    employeeService.list({ size: 200, active: true }).then((r) => setEmployees(r.content)).catch(() => {});
  }, []);

  useEffect(() => {
    if (!selectedProjectId) { setSprints([]); return; }
    sprintService.list({ projectId: selectedProjectId, size: 100 }).then((r) => setSprints(r.content)).catch(() => {});
  }, [selectedProjectId]);

  const runExport = async (format) => {
    setExporting(true);
    try {
      if (reportType === 'project') {
        if (!selectedProjectId) return toast.error('Choose a project');
        await reportService.exportProjectReport(selectedProjectId, format);
      } else if (reportType === 'sprint') {
        if (!selectedSprintId) return toast.error('Choose a sprint');
        await reportService.exportSprintReport(selectedSprintId, format);
      } else if (reportType === 'worklog') {
        if (!selectedProjectId) return toast.error('Choose a project');
        await reportService.exportWorklogReport(selectedProjectId, fromDate, toDate, format);
      } else if (reportType === 'productivity') {
        if (!selectedEmployeeId) return toast.error('Choose an employee');
        await reportService.exportEmployeeProductivityReport(selectedEmployeeId, fromDate, toDate, format);
      }
      toast.success('Report downloaded');
    } catch (err) {
      toast.error(err.message || 'Could not generate report');
    } finally {
      setExporting(false);
    }
  };

  if (loading) return <LoadingSpinner size="lg" label="Crunching the numbers…" />;

  const workloadChartData = workload.map((w) => ({ name: w.employeeName.split(' ')[0], score: w.workloadScore }));

  return (
    <div>
      <PageHeader
        title="Reports & Analytics"
        description="Deterministic workload scoring, live alerts and exportable delivery reports."
        breadcrumb={[{ label: 'Insights' }, { label: 'Reports & Analytics' }]}
      />

      <div className="card" style={{ marginBottom: 20 }}>
        <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Generate a report</h3></div>
        <div className="card-body">
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', alignItems: 'flex-end' }}>
            <div>
              <label className="form-label">Report type</label>
              <select className="form-select" value={reportType} onChange={(e) => setReportType(e.target.value)}>
                <option value="project">Project</option>
                <option value="sprint">Sprint</option>
                <option value="worklog">Worklog</option>
                <option value="productivity">Employee productivity</option>
              </select>
            </div>
            {(reportType === 'project' || reportType === 'worklog') && (
              <div>
                <label className="form-label">Project</label>
                <select className="form-select" value={selectedProjectId} onChange={(e) => setSelectedProjectId(e.target.value)}>
                  <option value="">Select project</option>
                  {projects.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
              </div>
            )}
            {reportType === 'sprint' && (
              <>
                <div>
                  <label className="form-label">Project</label>
                  <select className="form-select" value={selectedProjectId} onChange={(e) => setSelectedProjectId(e.target.value)}>
                    <option value="">Select project</option>
                    {projects.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="form-label">Sprint</label>
                  <select className="form-select" value={selectedSprintId} onChange={(e) => setSelectedSprintId(e.target.value)} disabled={!selectedProjectId}>
                    <option value="">Select sprint</option>
                    {sprints.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
                  </select>
                </div>
              </>
            )}
            {reportType === 'productivity' && (
              <div>
                <label className="form-label">Employee</label>
                <select className="form-select" value={selectedEmployeeId} onChange={(e) => setSelectedEmployeeId(e.target.value)}>
                  <option value="">Select employee</option>
                  {employees.map((e) => <option key={e.id} value={e.id}>{e.fullName}</option>)}
                </select>
              </div>
            )}
            {(reportType === 'worklog' || reportType === 'productivity') && (
              <>
                <div><label className="form-label">From</label><input type="date" className="form-control" value={fromDate} onChange={(e) => setFromDate(e.target.value)} /></div>
                <div><label className="form-label">To</label><input type="date" className="form-control" value={toDate} onChange={(e) => setToDate(e.target.value)} /></div>
              </>
            )}
            <button className="btn btn-secondary" disabled={exporting} onClick={() => runExport('csv')}><Download size={14} /> CSV</button>
            <button className="btn btn-secondary" disabled={exporting} onClick={() => runExport('xlsx')}><Download size={14} /> Excel</button>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }} className="dash-grid-main">
        <div className="card">
          <div className="card-header"><h3 style={{ fontSize: '1rem', display: 'flex', alignItems: 'center', gap: 8 }}><Sparkles size={16} color="var(--color-accent)" /> Workload score by employee</h3></div>
          <div className="card-body" style={{ height: 280 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={workloadChartData}>
                <CartesianGrid vertical={false} stroke="var(--color-border)" />
                <XAxis dataKey="name" fontSize={12} stroke="#94a3b8" axisLine={false} tickLine={false} />
                <YAxis fontSize={12} stroke="#94a3b8" axisLine={false} tickLine={false} width={28} />
                <Tooltip contentStyle={{ borderRadius: 10, border: '1px solid var(--color-border)', fontSize: 12 }} />
                <Bar dataKey="score" fill="var(--color-primary)" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="card">
          <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Manager alerts</h3></div>
          <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 12, maxHeight: 280, overflowY: 'auto' }}>
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

      <div className="card" style={{ marginTop: 20 }}>
        <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Workload board detail</h3></div>
        <div className="table-wrap" style={{ border: 'none', borderRadius: 0 }}>
          <table className="data-table">
            <thead><tr><th>Employee</th><th>Score</th><th>Band</th><th>Active</th><th>Overdue</th><th>Open bugs</th></tr></thead>
            <tbody>
              {workload.map((w) => (
                <tr key={w.employeeId}>
                  <td><div style={{ display: 'flex', alignItems: 'center', gap: 10 }}><Avatar name={w.employeeName} size="sm" />{w.employeeName}</div></td>
                  <td>{w.workloadScore}</td>
                  <td><span className={`badge badge-${w.band === 'HIGH' || w.band === 'CRITICAL' ? 'danger' : w.band === 'MODERATE' ? 'warning' : 'success'}`}>{humanize(w.band)}</span></td>
                  <td>{w.activeTaskCount}</td>
                  <td className={w.overdueTaskCount > 0 ? 'text-danger' : ''}>{w.overdueTaskCount}</td>
                  <td>{w.openBugCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
