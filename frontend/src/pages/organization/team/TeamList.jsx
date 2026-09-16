import { useState } from 'react';
import { Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import PageHeader from '../../../components/common/PageHeader';
import DataTable from '../../../components/common/DataTable';
import ConfirmDialog from '../../../components/common/ConfirmDialog';
import TeamFormModal from './TeamFormModal';
import { useAsyncList } from '../../../hooks/useAsyncList';
import { useDisclosure } from '../../../hooks/useDisclosure';
import { useAuth } from '../../../hooks/useAuth';
import { teamService } from '../../../services/team.service';
import { PERMISSIONS } from '../../../constants/enums';

export default function TeamList() {
  const { hasPermission } = useAuth();
  const formModal = useDisclosure(false);
  const confirmModal = useDisclosure(false);
  const [editing, setEditing] = useState(null);
  const [pendingDelete, setPendingDelete] = useState(null);
  const { content, page, setPage, totalPages, totalElements, loading, reload } = useAsyncList(teamService, { size: 10 });

  const confirmDelete = async () => {
    await teamService.remove(pendingDelete.id);
    toast.success('Team deleted');
    confirmModal.close();
    reload();
  };

  const columns = [
    { key: 'name', header: 'Team', render: (r) => <strong>{r.name}</strong> },
    { key: 'departmentName', header: 'Department' },
    { key: 'leadEmployeeName', header: 'Lead', render: (r) => r.leadEmployeeName || '—' },
    { key: 'memberCount', header: 'Members' },
    {
      key: 'actions', header: '', width: 140,
      render: (r) => hasPermission(PERMISSIONS.MANAGE_DEPARTMENTS) && (
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-ghost btn-sm" onClick={() => { setEditing(r); formModal.open(); }}>Edit</button>
          <button className="btn btn-danger-ghost btn-sm" onClick={() => { setPendingDelete(r); confirmModal.open(); }}>Delete</button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Teams"
        description="Sub-groups within a department, each with a lead and member roster."
        breadcrumb={[{ label: 'Workforce' }, { label: 'Teams' }]}
        actions={hasPermission(PERMISSIONS.MANAGE_DEPARTMENTS) && (
          <button className="btn btn-primary" onClick={() => { setEditing(null); formModal.open(); }}><Plus size={16} /> New team</button>
        )}
      />
      <DataTable columns={columns} rows={content} loading={loading} pagination={{ page, totalPages, totalElements, size: 10, onChange: setPage }} emptyTitle="No teams yet" />
      <TeamFormModal isOpen={formModal.isOpen} onClose={formModal.close} onSaved={reload} team={editing} />
      <ConfirmDialog isOpen={confirmModal.isOpen} onClose={confirmModal.close} onConfirm={confirmDelete} danger title="Delete team?" confirmLabel="Delete" description={`Delete ${pendingDelete?.name}? This can't be undone.`} />
    </div>
  );
}
