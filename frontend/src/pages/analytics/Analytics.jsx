import { useEffect, useState } from 'react';
import { Users, FolderKanban, ListChecks, Bug, TrendingUp } from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, BarChart, Bar, XAxis, YAxis, CartesianGrid } from 'recharts';
import PageHeader from '../../components/common/PageHeader';
import StatCard from '../../components/common/StatCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { dashboardService } from '../../services/dashboard.service';
import { projectService } from '../../services/project.service';
import { humanize } from '../../constants/enums';

const PIE_COLORS = { LOW: '#22c55e', MODERATE: '#f59e0b', HIGH: '#ef4444', CRITICAL: '#b91c1c' };

export default function Analytics() {
  const [summary, setSummary] = useState(null);
  const [workload, setWorkload] = useState([]);
  const [statusSplit, setStatusSplit] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([dashboardService.summary(), dashboardService.workload(), projectService.list({ size: 200 })])
      .then(([s, w, projRes]) => {
        setSummary(s);
        setWorkload(w);
        const counts = {};
        projRes.content.forEach((p) => { counts[p.status] = (counts[p.status] || 0) + 1; });
        setStatusSplit(Object.entries(counts).map(([name, value]) => ({ name, value })));
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner size="lg" label="Loading analytics…" />;

  const riskCounts = {};
  workload.forEach((w) => { riskCounts[w.band] = (riskCounts[w.band] || 0) + 1; });
  const riskSplit = Object.entries(riskCounts).map(([name, value]) => ({ name, value }));

  return (
    <div>
      <PageHeader title="Analytics" description="Company-wide delivery and workload metrics, computed live from current data." breadcrumb={[{ label: 'Insights' }, { label: 'Analytics' }]} />

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 18, marginBottom: 24 }}>
        <StatCard icon={Users} label="Total employees" value={summary.totalEmployees} tone="primary" />
        <StatCard icon={FolderKanban} label="Active projects" value={summary.activeProjects} tone="info" />
        <StatCard icon={ListChecks} label="Pending tasks" value={summary.pendingTasks} tone="warning" />
        <StatCard icon={Bug} label="Open bugs" value={summary.openBugs} tone="danger" />
        <StatCard icon={TrendingUp} label="Overloaded employees" value={summary.overloadedEmployees} tone="accent" />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }} className="dash-grid-main">
        <div className="card">
          <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Projects by status</h3></div>
          <div className="card-body" style={{ height: 280 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={statusSplit}>
                <CartesianGrid vertical={false} stroke="var(--color-border)" />
                <XAxis dataKey="name" tickFormatter={humanize} fontSize={11} stroke="#94a3b8" axisLine={false} tickLine={false} />
                <YAxis fontSize={12} stroke="#94a3b8" axisLine={false} tickLine={false} width={28} />
                <Tooltip contentStyle={{ borderRadius: 10, border: '1px solid var(--color-border)', fontSize: 12 }} labelFormatter={humanize} />
                <Bar dataKey="value" fill="var(--color-primary)" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="card">
          <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Workforce burnout-risk distribution</h3></div>
          <div className="card-body" style={{ height: 280, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
            {riskSplit.length === 0 ? (
              <p className="text-muted-c" style={{ fontSize: '0.875rem' }}>No workload data available.</p>
            ) : (
              <>
                <ResponsiveContainer width="100%" height={180}>
                  <PieChart>
                    <Pie data={riskSplit} dataKey="value" nameKey="name" innerRadius={50} outerRadius={78} paddingAngle={3}>
                      {riskSplit.map((entry) => <Cell key={entry.name} fill={PIE_COLORS[entry.name] || '#94a3b8'} />)}
                    </Pie>
                    <Tooltip contentStyle={{ borderRadius: 10, border: '1px solid var(--color-border)', fontSize: 12 }} />
                  </PieChart>
                </ResponsiveContainer>
                <div style={{ display: 'flex', gap: 14, flexWrap: 'wrap', justifyContent: 'center', marginTop: 8 }}>
                  {riskSplit.map((s) => (
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
    </div>
  );
}
