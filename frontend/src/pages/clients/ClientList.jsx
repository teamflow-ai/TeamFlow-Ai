import { useState } from 'react';
import { Plus, Mail, Phone } from 'lucide-react';
import toast from 'react-hot-toast';
import PageHeader from '../../components/common/PageHeader';
import SearchBox from '../../components/common/SearchBox';
import DataTable from '../../components/common/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import ClientFormModal from './ClientFormModal';
import { useAsyncList } from '../../hooks/useAsyncList';
import { useDebounce } from '../../hooks/useDebounce';
import { useDisclosure } from '../../hooks/useDisclosure';
import { useAuth } from '../../hooks/useAuth';
import { clientService } from '../../services/client.service';
import { PERMISSIONS } from '../../constants/enums';

export default function ClientList() {
  const { hasPermission } = useAuth();
  const [search, setSearch] = useState('');
  const debouncedSearch = useDebounce(search);
  const formModal = useDisclosure(false);
  const confirmModal = useDisclosure(false);
  const [editing, setEditing] = useState(null);
  const [pendingDelete, setPendingDelete] = useState(null);
  const { content, page, setPage, totalPages, totalElements, loading, reload } = useAsyncList(clientService, { size: 10, search: debouncedSearch });

  const confirmDelete = async () => {
    await clientService.remove(pendingDelete.id);
    toast.success('Client deleted');
    confirmModal.close();
    reload();
  };

  const columns = [
    { key: 'name', header: 'Client', render: (r) => (<div><div style={{ fontWeight: 600 }}>{r.name}</div><div className="text-faint-c" style={{ fontSize: '0.75rem' }}>{r.code}</div></div>) },
    { key: 'contactPerson', header: 'Contact person', render: (r) => r.contactPerson || '—' },
    { key: 'reach', header: 'Reach', render: (r) => (
      <div style={{ fontSize: '0.8125rem' }}>
        {r.email && <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}><Mail size={13} className="text-faint-c" /> {r.email}</div>}
        {r.phone && <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 2 }} className="text-muted-c"><Phone size={13} /> {r.phone}</div>}
      </div>
    ) },
    { key: 'country', header: 'Country' },
    { key: 'status', header: 'Status', render: (r) => <StatusBadge status={r.active ? 'DONE' : 'CANCELLED'} label={r.active ? 'Active' : 'Inactive'} /> },
    { key: 'actions', header: '', width: 140, render: (r) => hasPermission(PERMISSIONS.UPDATE_PROJECT) && (
      <div style={{ display: 'flex', gap: 8 }}>
        <button className="btn btn-ghost btn-sm" onClick={() => { setEditing(r); formModal.open(); }}>Edit</button>
        <button className="btn btn-danger-ghost btn-sm" onClick={() => { setPendingDelete(r); confirmModal.open(); }}>Delete</button>
      </div>
    ) },
  ];

  return (
    <div>
      <PageHeader
        title="Clients"
        description="External clients whose projects TeamFlow AI delivers."
        breadcrumb={[{ label: 'Delivery' }, { label: 'Clients' }]}
        actions={hasPermission(PERMISSIONS.CREATE_PROJECT) && (
          <button className="btn btn-primary" onClick={() => { setEditing(null); formModal.open(); }}><Plus size={16} /> New client</button>
        )}
      />
      <div style={{ marginBottom: 18 }}><SearchBox value={search} onChange={setSearch} placeholder="Search clients…" /></div>
      <DataTable columns={columns} rows={content} loading={loading} pagination={{ page, totalPages, totalElements, size: 10, onChange: setPage }} emptyTitle="No clients found" />
      <ClientFormModal isOpen={formModal.isOpen} onClose={formModal.close} onSaved={reload} client={editing} />
      <ConfirmDialog isOpen={confirmModal.isOpen} onClose={confirmModal.close} onConfirm={confirmDelete} danger title="Delete client?" confirmLabel="Delete" description={`Delete ${pendingDelete?.name}?`} />
    </div>
  );
}
