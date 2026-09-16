import { useState } from 'react';
import { Plus, Building2, Users, Wallet } from 'lucide-react';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';
import PageHeader from '../../../components/common/PageHeader';
import SearchBox from '../../../components/common/SearchBox';
import EmptyState from '../../../components/common/EmptyState';
import LoadingSpinner from '../../../components/common/LoadingSpinner';
import ConfirmDialog from '../../../components/common/ConfirmDialog';
import DepartmentFormModal from './DepartmentFormModal';
import { useAsyncList } from '../../../hooks/useAsyncList';
import { useDebounce } from '../../../hooks/useDebounce';
import { useDisclosure } from '../../../hooks/useDisclosure';
import { useAuth } from '../../../hooks/useAuth';
import { departmentService } from '../../../services/department.service';
import { PERMISSIONS } from '../../../constants/enums';

const inr = (n) => `₹${(n || 0).toLocaleString('en-IN')}`;

export default function DepartmentList() {
  const { hasPermission } = useAuth();
  const [search, setSearch] = useState('');
  const debouncedSearch = useDebounce(search);
  const formModal = useDisclosure(false);
  const confirmModal = useDisclosure(false);
  const [editing, setEditing] = useState(null);
  const [pendingDelete, setPendingDelete] = useState(null);

  const { content, loading, reload } = useAsyncList(departmentService, { size: 50, search: debouncedSearch });

  const openCreate = () => { setEditing(null); formModal.open(); };
  const openEdit = (d) => { setEditing(d); formModal.open(); };

  const confirmDelete = async () => {
    await departmentService.remove(pendingDelete.id);
    toast.success('Department deleted');
    confirmModal.close();
    reload();
  };

  return (
    <div>
      <PageHeader
        title="Departments"
        description="Organizational units, their budgets and headcount."
        breadcrumb={[{ label: 'Workforce' }, { label: 'Departments' }]}
        actions={hasPermission(PERMISSIONS.MANAGE_DEPARTMENTS) && (
          <button className="btn btn-primary" onClick={openCreate}><Plus size={16} /> New department</button>
        )}
      />

      <div style={{ marginBottom: 18 }}>
        <SearchBox value={search} onChange={setSearch} placeholder="Search departments…" />
      </div>

      {loading && <LoadingSpinner size="lg" label="Loading departments…" />}
      {!loading && content.length === 0 && <EmptyState icon={Building2} title="No departments found" description="Create your first department to start organizing the workforce." />}

      {!loading && content.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: 18 }}>
          {content.map((d, i) => (
            <motion.div key={d.id} className="card card-hover" initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.04 }}>
              <div className="card-body">
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <div style={{ width: 42, height: 42, borderRadius: 12, background: 'var(--color-primary-light)', color: 'var(--color-primary-dark)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <Building2 size={20} />
                  </div>
                  <span className="badge badge-neutral">{d.code}</span>
                </div>
                <h3 style={{ fontSize: '1.05rem', marginTop: 14 }}>{d.name}</h3>
                <p className="text-muted-c" style={{ fontSize: '0.8125rem', marginTop: 6, minHeight: 36 }}>{d.description}</p>
                <hr className="divider" />
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8125rem' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: 6 }} className="text-muted-c"><Users size={14} /> {d.employeeCount} people</span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: 6 }} className="text-muted-c"><Wallet size={14} /> {inr(d.annualBudget)}</span>
                </div>
                {d.headEmployeeName && (
                  <div className="text-faint-c" style={{ fontSize: '0.75rem', marginTop: 10 }}>Head: {d.headEmployeeName}</div>
                )}
                <div style={{ display: 'flex', gap: 8, marginTop: 16 }}>
                  {hasPermission(PERMISSIONS.MANAGE_DEPARTMENTS) && (
                    <>
                      <button className="btn btn-secondary btn-sm" style={{ flex: 1 }} onClick={() => openEdit(d)}>Edit</button>
                      <button className="btn btn-danger-ghost btn-sm" onClick={() => { setPendingDelete(d); confirmModal.open(); }}>Delete</button>
                    </>
                  )}
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      )}

      <DepartmentFormModal isOpen={formModal.isOpen} onClose={formModal.close} onSaved={reload} department={editing} />
      <ConfirmDialog
        isOpen={confirmModal.isOpen}
        onClose={confirmModal.close}
        onConfirm={confirmDelete}
        danger
        title="Delete department?"
        confirmLabel="Delete"
        description={`This will permanently delete ${pendingDelete?.name}.`}
      />
    </div>
  );
}
