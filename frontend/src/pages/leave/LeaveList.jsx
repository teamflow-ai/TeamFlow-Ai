import { useState } from 'react';
import { Plus, Check, X } from 'lucide-react';
import toast from 'react-hot-toast';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import LeaveFormModal from './LeaveFormModal';
import { useAsyncList } from '../../hooks/useAsyncList';
import { useDisclosure } from '../../hooks/useDisclosure';
import { useAuth } from '../../hooks/useAuth';
import { leaveService } from '../../services/leave.service';
import { PERMISSIONS } from '../../constants/enums';

const mineList = { list: (params) => leaveService.myLeaves(params) };
const pendingList = { list: (params) => leaveService.pending(params) };

export default function LeaveList() {
  const { hasPermission, user } = useAuth();
  const formModal = useDisclosure(false);
  const [tab, setTab] = useState('mine');
  const canApprove = hasPermission(PERMISSIONS.APPROVE_LEAVE);

  const mine = useAsyncList(mineList, { size: 10 });
  const pending = useAsyncList(pendingList, { size: 10 });
  const active = tab === 'mine' ? mine : pending;

  const decide = async (leave, approve) => {
    try {
      const isFirstStage = leave.status === 'PENDING';
      if (isFirstStage) await leaveService.managerDecision(leave.id, { approve, comment: approve ? 'Approved by manager.' : 'Declined by manager.' });
      else await leaveService.hrDecision(leave.id, { approve, comment: approve ? 'Approved by HR.' : 'Declined by HR.' });
      toast.success(approve ? 'Leave approved' : 'Leave rejected');
      mine.reload();
      pending.reload();
    } catch (err) { toast.error(err.message || 'Could not record decision'); }
  };

  const cancel = async (leave) => {
    try {
      await leaveService.cancel(leave.id);
      toast.success('Leave request cancelled');
      mine.reload();
    } catch (err) { toast.error(err.message || 'Could not cancel request'); }
  };

  const columns = [
    ...(tab === 'pending' ? [{ key: 'employeeName', header: 'Employee' }] : []),
    { key: 'leaveType', header: 'Type' },
    { key: 'dates', header: 'Dates', render: (r) => `${r.startDate} → ${r.endDate}` },
    { key: 'totalDays', header: 'Days' },
    { key: 'reason', header: 'Reason', render: (r) => <span className="text-muted-c">{r.reason}</span> },
    { key: 'status', header: 'Status', render: (r) => <StatusBadge status={r.status} /> },
    {
      key: 'actions', header: '', width: 160,
      render: (r) => (
        <div style={{ display: 'flex', gap: 8 }} onClick={(e) => e.stopPropagation()}>
          {tab === 'pending' && canApprove && (r.status === 'PENDING' || r.status === 'MANAGER_APPROVED') && (
            <>
              <button className="btn btn-icon btn-secondary btn-sm" onClick={() => decide(r, true)} title="Approve"><Check size={14} /></button>
              <button className="btn btn-icon btn-danger-ghost btn-sm" onClick={() => decide(r, false)} title="Reject"><X size={14} /></button>
            </>
          )}
          {tab === 'mine' && (r.status === 'PENDING' || r.status === 'MANAGER_APPROVED') && (
            <button className="btn btn-danger-ghost btn-sm" onClick={() => cancel(r)}>Cancel</button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Leave requests"
        description="Two-stage approval: manager, then HR."
        breadcrumb={[{ label: 'Workforce' }, { label: 'Leave requests' }]}
        actions={<button className="btn btn-primary" onClick={formModal.open}><Plus size={16} /> Request leave</button>}
      />
      {canApprove && (
        <div style={{ display: 'flex', gap: 8, marginBottom: 18 }}>
          <button className={`btn btn-sm ${tab === 'mine' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab('mine')}>My requests</button>
          <button className={`btn btn-sm ${tab === 'pending' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab('pending')}>Pending approval</button>
        </div>
      )}
      <DataTable
        columns={columns}
        rows={active.content}
        loading={active.loading}
        pagination={{ page: active.page, totalPages: active.totalPages, totalElements: active.totalElements, size: 10, onChange: active.setPage }}
        emptyTitle={tab === 'mine' ? 'No leave requests yet' : 'Nothing pending approval'}
      />
      <LeaveFormModal isOpen={formModal.isOpen} onClose={formModal.close} onSaved={mine.reload} />
    </div>
  );
}
