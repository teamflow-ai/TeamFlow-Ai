import { useEffect, useState } from 'react';
import { Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import PageHeader from '../../components/common/PageHeader';
import SearchBox from '../../components/common/SearchBox';
import Select from '../../components/common/Select';
import DataTable from '../../components/common/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import EmptyState from '../../components/common/EmptyState';
import BugFormModal from './BugFormModal';
import { useAsyncList } from '../../hooks/useAsyncList';
import { useDebounce } from '../../hooks/useDebounce';
import { useDisclosure } from '../../hooks/useDisclosure';
import { useAuth } from '../../hooks/useAuth';
import { bugService } from '../../services/bug.service';
import { projectService } from '../../services/project.service';
import { BUG_STATUS, PERMISSIONS } from '../../constants/enums';

export default function BugList() {
  const { hasPermission } = useAuth();
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('ALL');
  const [projectId, setProjectId] = useState('');
  const [projects, setProjects] = useState([]);
  const debouncedSearch = useDebounce(search);
  const formModal = useDisclosure(false);
  const [editing, setEditing] = useState(null);

  useEffect(() => {
    projectService.list({ size: 100 }).then((res) => {
      setProjects(res.content);
      if (res.content.length > 0) setProjectId(res.content[0].id);
    }).catch(() => {});
  }, []);

  const { content, page, setPage, totalPages, totalElements, loading, reload } = useAsyncList(bugService, {
    size: 8,
    search: debouncedSearch,
    filters: { projectId: projectId || undefined, status: status === 'ALL' ? undefined : status },
  });

  const resolve = async (bug) => {
    try {
      await bugService.updateStatus(bug.id, { status: 'RESOLVED', resolution: 'Fixed and verified.' });
      toast.success('Bug marked resolved');
      reload();
    } catch (err) {
      toast.error(err.message || 'Could not update bug');
    }
  };

  const columns = [
    { key: 'title', header: 'Bug', render: (r) => (<div><div style={{ fontWeight: 600 }}>{r.title}</div></div>) },
    { key: 'severity', header: 'Severity', render: (r) => <StatusBadge status={r.severity} /> },
    { key: 'status', header: 'Status', render: (r) => <StatusBadge status={r.status} /> },
    { key: 'assigneeName', header: 'Assignee', render: (r) => r.assigneeName || 'Unassigned' },
    { key: 'actions', header: '', width: 160, render: (r) => hasPermission(PERMISSIONS.UPDATE_TASK) && (
      <div style={{ display: 'flex', gap: 8 }} onClick={(e) => e.stopPropagation()}>
        <button className="btn btn-ghost btn-sm" onClick={() => { setEditing(r); formModal.open(); }}>Edit</button>
        {r.status !== 'RESOLVED' && r.status !== 'CLOSED' && <button className="btn btn-secondary btn-sm" onClick={() => resolve(r)}>Resolve</button>}
      </div>
    ) },
  ];

  return (
    <div>
      <PageHeader
        title="Bugs"
        description="Defects reported against a project, optionally linked to the task they block."
        breadcrumb={[{ label: 'Delivery' }, { label: 'Bugs' }]}
        actions={<button className="btn btn-primary" disabled={!projectId} onClick={() => { setEditing(null); formModal.open(); }}><Plus size={16} /> Report bug</button>}
      />
      <div style={{ display: 'flex', gap: 16, marginBottom: 24, flexWrap: 'wrap', alignItems: 'center' }}>
        <Select
          value={projectId || 'ALL'}
          onChange={(v) => setProjectId(v === 'ALL' ? '' : v)}
          placeholder="Choose a project"
          options={projects.map((p) => ({ value: p.id, label: p.name }))}
          style={{ minWidth: 220, borderRadius: '20px' }}
        />
        
        <div style={{ display: 'flex', overflowX: 'auto', gap: 4, padding: '4px', backgroundColor: 'rgba(0,0,0,0.03)', border: '1px solid var(--color-border)', borderRadius: '30px' }}>
          <button className={`btn btn-sm ${status === 'ALL' ? 'btn-primary' : 'btn-ghost'}`} style={{ borderRadius: '20px', padding: '4px 12px' }} onClick={() => setStatus('ALL')}>All Statuses</button>
          {BUG_STATUS.map(s => (
            <button key={s} className={`btn btn-sm ${status === s ? 'btn-primary' : 'btn-ghost'}`} style={{ borderRadius: '20px', padding: '4px 12px', whiteSpace: 'nowrap' }} onClick={() => setStatus(s)}>
              {s.replace('_', ' ')}
            </button>
          ))}
        </div>

        <SearchBox value={search} onChange={setSearch} placeholder="Search bugs…" />
      </div>
      {!projectId ? (
        <EmptyState title="Choose a project" description="Select a project above to see its reported bugs." />
      ) : (
        <DataTable columns={columns} rows={content} loading={loading} pagination={{ page, totalPages, totalElements, size: 8, onChange: setPage }} emptyTitle="No bugs found" />
      )}
      <BugFormModal isOpen={formModal.isOpen} onClose={formModal.close} onSaved={reload} bug={editing} defaultProjectId={projectId} />
    </div>
  );
}
