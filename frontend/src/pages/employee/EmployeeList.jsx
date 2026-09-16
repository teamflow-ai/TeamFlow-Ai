import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Mail, Phone } from 'lucide-react';
import toast from 'react-hot-toast';
import PageHeader from '../../components/common/PageHeader';
import SearchBox from '../../components/common/SearchBox';
import Select from '../../components/common/Select';
import DataTable from '../../components/common/DataTable';
import Avatar from '../../components/common/Avatar';
import StatusBadge from '../../components/common/StatusBadge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import EmployeeFormModal from './EmployeeFormModal';
import { useAsyncList } from '../../hooks/useAsyncList';
import { useDebounce } from '../../hooks/useDebounce';
import { useDisclosure } from '../../hooks/useDisclosure';
import { useAuth } from '../../hooks/useAuth';
import { employeeService } from '../../services/employee.service';
import { departmentService } from '../../services/department.service';
import { PERMISSIONS } from '../../constants/enums';

export default function EmployeeList() {
  const navigate = useNavigate();
  const { hasPermission } = useAuth();
  const [search, setSearch] = useState('');
  const [department, setDepartment] = useState('ALL');
  const debouncedSearch = useDebounce(search);
  const formModal = useDisclosure(false);
  const confirmModal = useDisclosure(false);
  const [editingEmployee, setEditingEmployee] = useState(null);
  const [pendingDelete, setPendingDelete] = useState(null);
  const [departments, setDepartments] = useState([]);

  useEffect(() => {
    departmentService.list({ size: 100 }).then((res) => setDepartments(res.content)).catch(() => {});
  }, []);

  const { content, page, setPage, totalPages, totalElements, loading, reload } = useAsyncList(employeeService, {
    size: 8,
    search: debouncedSearch,
    filters: { departmentId: department === 'ALL' ? undefined : department },
  });

  const openCreate = () => { setEditingEmployee(null); formModal.open(); };
  const openEdit = (emp) => { setEditingEmployee(emp); formModal.open(); };

  const confirmDelete = async () => {
    await employeeService.remove(pendingDelete.id);
    toast.success('Employee removed');
    confirmModal.close();
    reload();
  };

  const columns = [
    {
      key: 'name', header: 'Employee',
      render: (r) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <Avatar name={r.fullName} />
          <div>
            <div style={{ fontWeight: 600 }}>{r.fullName}</div>
            <div className="text-muted-c" style={{ fontSize: '0.75rem' }}>{r.employeeCode} · {r.designation}</div>
          </div>
        </div>
      ),
    },
    {
      key: 'contact', header: 'Contact',
      render: (r) => (
        <div style={{ fontSize: '0.8125rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}><Mail size={13} className="text-faint-c" /> {r.workEmail}</div>
          {r.phone && <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 2 }} className="text-muted-c"><Phone size={13} /> {r.phone}</div>}
        </div>
      ),
    },
    { key: 'department', header: 'Department', render: (r) => r.departmentName || '—' },
    { key: 'team', header: 'Team', render: (r) => r.teamName || '—' },
    { key: 'status', header: 'Status', render: (r) => <StatusBadge status={r.active ? 'DONE' : 'CANCELLED'} label={r.active ? 'Active' : 'Inactive'} /> },
    {
      key: 'actions', header: '', width: 90,
      render: (r) => (
        <div style={{ display: 'flex', gap: 8 }} onClick={(e) => e.stopPropagation()}>
          {hasPermission(PERMISSIONS.UPDATE_EMPLOYEE) && (
            <button className="btn btn-ghost btn-sm" onClick={() => openEdit(r)}>Edit</button>
          )}
          {hasPermission(PERMISSIONS.DELETE_EMPLOYEE) && (
            <button className="btn btn-danger-ghost btn-sm" onClick={() => { setPendingDelete(r); confirmModal.open(); }}>Remove</button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Employees"
        description="The HR record for everyone at TeamFlow AI — department, team, skills and capacity."
        breadcrumb={[{ label: 'Workforce' }, { label: 'Employees' }]}
        actions={hasPermission(PERMISSIONS.CREATE_EMPLOYEE) && (
          <button className="btn btn-primary" onClick={openCreate}><Plus size={16} /> Add employee</button>
        )}
      />

      <div style={{ display: 'flex', gap: 12, marginBottom: 18, flexWrap: 'wrap' }}>
        <SearchBox value={search} onChange={setSearch} placeholder="Search by name, email, designation…" />
        <Select
          value={department}
          onChange={setDepartment}
          placeholder="All departments"
          options={departments.map((d) => ({ value: d.id, label: d.name }))}
          style={{ minWidth: 180 }}
        />
      </div>

      <DataTable
        columns={columns}
        rows={content}
        loading={loading}
        onRowClick={(r) => navigate(`/employees/${r.id}`)}
        emptyTitle="No employees found"
        emptyDescription="Try a different search, or add your first employee."
        pagination={{ page, totalPages, totalElements, size: 8, onChange: setPage }}
      />

      <EmployeeFormModal isOpen={formModal.isOpen} onClose={formModal.close} onSaved={reload} employee={editingEmployee} />
      <ConfirmDialog
        isOpen={confirmModal.isOpen}
        onClose={confirmModal.close}
        onConfirm={confirmDelete}
        danger
        title="Remove employee?"
        confirmLabel="Remove"
        description={`This will remove ${pendingDelete?.fullName} from the workforce directory. This can't be undone.`}
      />
    </div>
  );
}
