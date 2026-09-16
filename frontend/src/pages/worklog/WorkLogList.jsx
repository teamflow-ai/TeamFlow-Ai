import { useState, useMemo, useEffect } from 'react';
import { Plus, Clock, Users, FolderKanban, Calendar, Trash2, Search, Filter } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import WorkLogFormModal from './WorkLogFormModal';
import { useDisclosure } from '../../hooks/useDisclosure';
import { useAuth } from '../../hooks/useAuth';
import { worklogService } from '../../services/worklog.service';
import { projectService } from '../../services/project.service';
import toast from 'react-hot-toast';

export default function WorkLogList() {
  const { user } = useAuth();
  const formModal = useDisclosure(false);
  const [activeTab, setActiveTab] = useState('all'); // 'my' | 'all'
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

  // Load project filters
  useEffect(() => {
    projectService.list({ size: 100 })
      .then((res) => setProjects(res.content || []))
      .catch(() => {});
  }, []);

  // Fetch work logs depending on tab & filter
  const fetchLogs = async () => {
    setLoading(true);
    try {
      let data;
      if (activeTab === 'my') {
        data = await worklogService.mine({ page, size: 15 });
      } else {
        data = await worklogService.list({
          projectId: selectedProjectId || undefined,
          page,
          size: 15,
        });
      }
      setLogs(data?.content || []);
      setTotalPages(data?.totalPages || 1);
      setTotalElements(data?.totalElements || 0);
    } catch (err) {
      console.error('Failed to fetch work logs', err);
      toast.error('Could not load work logs');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, [activeTab, page, selectedProjectId]);

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this work log?')) return;
    try {
      await worklogService.remove(id);
      toast.success('Work log removed');
      fetchLogs();
    } catch (err) {
      toast.error(err.message || 'Could not delete work log');
    }
  };

  // Filter logs by search query client-side if needed
  const filteredLogs = useMemo(() => {
    if (!searchQuery.trim()) return logs;
    const q = searchQuery.toLowerCase();
    return logs.filter(
      (l) =>
        (l.employeeName && l.employeeName.toLowerCase().includes(q)) ||
        (l.projectName && l.projectName.toLowerCase().includes(q)) ||
        (l.taskTitle && l.taskTitle.toLowerCase().includes(q)) ||
        (l.notes && l.notes.toLowerCase().includes(q))
    );
  }, [logs, searchQuery]);

  // Aggregate summary metrics
  const totalHours = useMemo(() => {
    return logs.reduce((sum, item) => sum + (Number(item.hours) || 0), 0);
  }, [logs]);

  const uniqueContributors = useMemo(() => {
    const set = new Set(logs.map((l) => l.employeeId).filter(Boolean));
    return set.size;
  }, [logs]);

  const columns = [
    {
      key: 'logDate',
      header: 'Date',
      render: (r) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 500 }}>
          <Calendar size={14} className="text-muted-c" />
          <span>{r.logDate}</span>
        </div>
      ),
    },
    {
      key: 'employeeName',
      header: 'Team Member',
      render: (r) => {
        const name = r.employeeName || (r.employeeId === user?.employeeId ? user?.fullName || 'Me' : 'Team Member');
        const initials = name
          .split(' ')
          .map((n) => n[0])
          .join('')
          .slice(0, 2)
          .toUpperCase();
        return (
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div
              style={{
                width: 28,
                height: 28,
                borderRadius: '50%',
                backgroundColor: 'rgba(99, 102, 241, 0.15)',
                color: '#6366f1',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: 11,
                fontWeight: 600,
              }}
            >
              {initials}
            </div>
            <div>
              <div style={{ fontWeight: 600, fontSize: 13 }}>{name}</div>
              {r.employeeId === user?.employeeId && (
                <span style={{ fontSize: 10, color: 'var(--color-primary, #6366f1)' }}>(You)</span>
              )}
            </div>
          </div>
        );
      },
    },
    {
      key: 'projectName',
      header: 'Project',
      render: (r) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <FolderKanban size={14} className="text-muted-c" />
          <span style={{ fontWeight: 500 }}>{r.projectName || '—'}</span>
        </div>
      ),
    },
    {
      key: 'taskTitle',
      header: 'Task / Activity',
      render: (r) => (
        <span className={r.taskTitle ? '' : 'text-muted-c'}>
          {r.taskTitle || 'General Project Work'}
        </span>
      ),
    },
    {
      key: 'hours',
      header: 'Hours Logged',
      render: (r) => (
        <span
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '4px',
            padding: '4px 10px',
            borderRadius: '12px',
            fontSize: '12px',
            fontWeight: 700,
            background: 'rgba(16, 185, 129, 0.1)',
            color: '#10b981',
          }}
        >
          <Clock size={12} />
          {Number(r.hours).toFixed(1)}h
        </span>
      ),
    },
    {
      key: 'notes',
      header: 'Notes',
      render: (r) => (
        <span className="text-muted-c" style={{ fontSize: 13 }}>
          {r.notes || '—'}
        </span>
      ),
    },
    {
      key: 'actions',
      header: '',
      render: (r) => {
        const isOwner = r.employeeId === user?.employeeId || user?.role === 'ADMIN';
        if (!isOwner) return null;
        return (
          <button
            className="btn btn-ghost btn-sm"
            onClick={() => handleDelete(r.id)}
            title="Delete work log"
            style={{ color: '#ef4444', padding: '4px 8px' }}
          >
            <Trash2 size={15} />
          </button>
        );
      },
    },
  ];

  return (
    <div>
      <PageHeader
        title="Work Logs & Time Tracking"
        description="Monitor daily logged effort, task timesheets, and resource utilization across all active projects."
        breadcrumb={[{ label: 'Delivery' }, { label: 'Work logs' }]}
        actions={
          <button className="btn btn-primary" onClick={formModal.open}>
            <Plus size={16} /> Log work
          </button>
        }
      />

      {/* KPI Metric Cards */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '16px',
          marginBottom: '20px',
        }}
      >
        <div
          style={{
            background: 'var(--color-surface)',
            border: '1px solid var(--color-border)',
            borderRadius: '12px',
            padding: '16px',
            display: 'flex',
            alignItems: 'center',
            gap: '14px',
          }}
        >
          <div
            style={{
              width: 44,
              height: 44,
              borderRadius: '10px',
              backgroundColor: 'rgba(99, 102, 241, 0.15)',
              color: '#6366f1',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Clock size={22} />
          </div>
          <div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted, #94a3b8)', fontWeight: 500 }}>
              Total Hours Logged
            </div>
            <div style={{ fontSize: '20px', fontWeight: 700, marginTop: '2px' }}>
              {totalHours.toFixed(1)} hrs
            </div>
          </div>
        </div>

        <div
          style={{
            background: 'var(--color-surface)',
            border: '1px solid var(--color-border)',
            borderRadius: '12px',
            padding: '16px',
            display: 'flex',
            alignItems: 'center',
            gap: '14px',
          }}
        >
          <div
            style={{
              width: 44,
              height: 44,
              borderRadius: '10px',
              backgroundColor: 'rgba(16, 185, 129, 0.15)',
              color: '#10b981',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Users size={22} />
          </div>
          <div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted, #94a3b8)', fontWeight: 500 }}>
              Active Contributors
            </div>
            <div style={{ fontSize: '20px', fontWeight: 700, marginTop: '2px' }}>
              {uniqueContributors} {uniqueContributors === 1 ? 'Member' : 'Members'}
            </div>
          </div>
        </div>

        <div
          style={{
            background: 'var(--color-surface)',
            border: '1px solid var(--color-border)',
            borderRadius: '12px',
            padding: '16px',
            display: 'flex',
            alignItems: 'center',
            gap: '14px',
          }}
        >
          <div
            style={{
              width: 44,
              height: 44,
              borderRadius: '10px',
              backgroundColor: 'rgba(245, 158, 11, 0.15)',
              color: '#f59e0b',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <FolderKanban size={22} />
          </div>
          <div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted, #94a3b8)', fontWeight: 500 }}>
              Timesheet Entries
            </div>
            <div style={{ fontSize: '20px', fontWeight: 700, marginTop: '2px' }}>
              {totalElements} Logged
            </div>
          </div>
        </div>
      </div>

      {/* Tabs and Filter Header */}
      <div
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '12px',
          marginBottom: '16px',
        }}
      >
        <div style={{ display: 'flex', gap: '8px', background: 'var(--color-surface)', padding: '4px', borderRadius: '10px', border: '1px solid var(--color-border)' }}>
          <button
            onClick={() => { setActiveTab('all'); setPage(0); }}
            style={{
              padding: '6px 14px',
              borderRadius: '8px',
              fontSize: '13px',
              fontWeight: 600,
              border: 'none',
              cursor: 'pointer',
              background: activeTab === 'all' ? 'var(--color-primary)' : 'transparent',
              color: activeTab === 'all' ? '#ffffff' : 'var(--color-text-muted)',
              transition: 'all 0.15s ease',
            }}
          >
            All Team Logs ({totalElements})
          </button>
          <button
            onClick={() => { setActiveTab('my'); setPage(0); }}
            style={{
              padding: '6px 14px',
              borderRadius: '8px',
              fontSize: '13px',
              fontWeight: 600,
              border: 'none',
              cursor: 'pointer',
              background: activeTab === 'my' ? 'var(--color-primary)' : 'transparent',
              color: activeTab === 'my' ? '#ffffff' : 'var(--color-text-muted)',
              transition: 'all 0.15s ease',
            }}
          >
            My Work Logs
          </button>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flex: '1', maxWidth: '480px', justifyContent: 'flex-end' }}>
          {activeTab === 'all' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', minWidth: '180px' }}>
              <Filter size={15} className="text-muted-c" />
              <select
                className="form-select form-select-sm"
                value={selectedProjectId}
                onChange={(e) => { setSelectedProjectId(e.target.value); setPage(0); }}
                style={{ fontSize: '13px', padding: '6px 10px' }}
              >
                <option value="">All Projects</option>
                {projects.map((p) => (
                  <option key={p.id} value={p.id}>{p.name}</option>
                ))}
              </select>
            </div>
          )}

          <div style={{ position: 'relative', width: '200px' }}>
            <Search size={14} style={{ position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8' }} />
            <input
              type="text"
              placeholder="Search logs..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="form-control form-control-sm"
              style={{ paddingLeft: '30px', fontSize: '13px' }}
            />
          </div>
        </div>
      </div>

      <DataTable
        columns={columns}
        rows={filteredLogs}
        loading={loading}
        pagination={{
          page,
          totalPages,
          totalElements,
          size: 15,
          onChange: setPage,
        }}
        emptyTitle="No work logs found"
        emptyDescription="There are no work logs recorded for this view. Click 'Log work' to record your time."
      />

      <WorkLogFormModal
        isOpen={formModal.isOpen}
        onClose={formModal.close}
        onSaved={fetchLogs}
      />
    </div>
  );
}
