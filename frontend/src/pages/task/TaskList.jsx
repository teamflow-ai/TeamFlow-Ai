import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Plus } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import Select from '../../components/common/Select';
import DataTable from '../../components/common/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import Avatar from '../../components/common/Avatar';
import TaskFormModal from './TaskFormModal';
import { useAsyncList } from '../../hooks/useAsyncList';
import { useDisclosure } from '../../hooks/useDisclosure';
import { useAuth } from '../../hooks/useAuth';
import { taskService } from '../../services/task.service';
import { TASK_STATUS, PRIORITY, PERMISSIONS, humanize } from '../../constants/enums';

export default function TaskList() {
  const navigate = useNavigate();
  const { hasPermission, user } = useAuth();
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState('ALL');
  const [priority, setPriority] = useState('ALL');
  const [mineOnly, setMineOnly] = useState(false);
  const formModal = useDisclosure(false);

  const { content, page, setPage, totalPages, totalElements, loading, reload } = useAsyncList(taskService, {
    size: 8,
    filters: {
      status: status === 'ALL' ? undefined : status,
      priority: priority === 'ALL' ? undefined : priority,
      projectId: searchParams.get('projectId') || undefined,
      assigneeId: mineOnly ? user?.employeeId : undefined,
    },
  });

  const columns = [
    { key: 'title', header: 'Task', render: (r) => (<div><div style={{ fontWeight: 600 }}>{r.title}</div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>{r.projectName}</div></div>) },
    { key: 'assignee', header: 'Assignee', render: (r) => r.assigneeName ? (
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}><Avatar name={r.assigneeName} size="sm" /><span style={{ fontSize: '0.8125rem' }}>{r.assigneeName}</span></div>
      ) : <span className="badge badge-neutral">Unassigned</span> },
    { key: 'priority', header: 'Priority', render: (r) => <StatusBadge status={r.priority} /> },
    { key: 'status', header: 'Status', render: (r) => <StatusBadge status={r.status} /> },
    { key: 'dueDate', header: 'Due date', render: (r) => (
        <span style={{ color: r.overdue ? 'var(--color-danger)' : 'inherit', fontWeight: r.overdue ? 600 : 400 }}>{r.dueDate}{r.overdue && ' (overdue)'}</span>
      ) },
  ];

  return (
    <div>
      <PageHeader
        title="Tasks"
        description="Work items across every project, with AI-assisted smart assignment."
        breadcrumb={[{ label: 'Delivery' }, { label: 'Tasks' }]}
        actions={hasPermission(PERMISSIONS.UPDATE_TASK) && (
          <button className="btn btn-primary" onClick={formModal.open}><Plus size={16} /> New task</button>
        )}
      />

      <div style={{ display: 'flex', gap: 16, marginBottom: 24, flexWrap: 'wrap', alignItems: 'center' }}>
        <div style={{ display: 'flex', overflowX: 'auto', gap: 4, padding: '4px', backgroundColor: 'rgba(0,0,0,0.03)', border: '1px solid var(--color-border)', borderRadius: '30px' }}>
          <button className={`btn btn-sm ${status === 'ALL' ? 'btn-primary' : 'btn-ghost'}`} style={{ borderRadius: '20px', padding: '4px 12px' }} onClick={() => setStatus('ALL')}>All Statuses</button>
          {TASK_STATUS.map(s => (
            <button key={s} className={`btn btn-sm ${status === s ? 'btn-primary' : 'btn-ghost'}`} style={{ borderRadius: '20px', padding: '4px 12px', whiteSpace: 'nowrap' }} onClick={() => setStatus(s)}>
              {humanize(s)}
            </button>
          ))}
        </div>

        <Select value={priority} onChange={setPriority} placeholder="All priorities" options={PRIORITY} style={{ minWidth: 140, borderRadius: '20px' }} />
        
        {user?.employeeId && (
          <button className={`btn btn-sm ${mineOnly ? 'btn-primary' : 'btn-secondary'}`} style={{ borderRadius: '20px', padding: '6px 16px' }} onClick={() => setMineOnly((v) => !v)}>
            {mineOnly ? 'Assigned to me' : 'Any assignee'}
          </button>
        )}
      </div>

      <DataTable
        columns={columns}
        rows={content}
        loading={loading}
        onRowClick={(r) => navigate(`/tasks/${r.id}`)}
        emptyTitle="No tasks found"
        emptyDescription="Try a different filter, or create a new task."
        pagination={{ page, totalPages, totalElements, size: 8, onChange: setPage }}
      />

      <TaskFormModal isOpen={formModal.isOpen} onClose={formModal.close} onSaved={reload} task={null} />
    </div>
  );
}
