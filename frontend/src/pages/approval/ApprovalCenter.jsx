import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Check, X, RotateCcw, ClipboardCheck, Sparkles, Filter, AlertCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import PageHeader from '../../components/common/PageHeader';
import Select from '../../components/common/Select';
import StatusBadge from '../../components/common/StatusBadge';
import EmptyState from '../../components/common/EmptyState';
import Modal from '../../components/common/Modal';
import FormField from '../../components/common/FormField';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { useAsyncList } from '../../hooks/useAsyncList';
import { useDisclosure } from '../../hooks/useDisclosure';
import { useAuth } from '../../hooks/useAuth';
import { approvalService } from '../../services/approval.service';
import { APPROVAL_TYPE, APPROVAL_STATUS, humanize, PERMISSIONS } from '../../constants/enums';

const STATUS_TABS = [
  { label: 'All Requests', value: 'ALL' },
  { label: 'Pending', value: 'PENDING' },
  { label: 'Approved', value: 'APPROVED' },
  { label: 'Returned', value: 'RETURNED_FOR_CHANGES' },
  { label: 'Rejected', value: 'REJECTED' },
];

export default function ApprovalCenter() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { hasPermission } = useAuth();
  const [approvalType, setApprovalType] = useState('ALL');
  const [status, setStatus] = useState('ALL');
  const decideModal = useDisclosure(false);
  const [target, setTarget] = useState(null);
  const [decision, setDecision] = useState('APPROVED');
  const [remarks, setRemarks] = useState('');
  const [saving, setSaving] = useState(false);
  const [targetedItem, setTargetedItem] = useState(null);

  useEffect(() => {
    if (id) {
      approvalService.get(id)
        .then(res => {
          setTargetedItem(res);
          // Only auto-open if it's still pending and the user can actually decide it
          if (res.status === 'PENDING' && canDecide(res.approvalType)) {
            openDecide(res, 'APPROVED');
          }
        })
        .catch(() => {
          toast.error("Could not load the requested approval");
          navigate('/approvals', { replace: true });
        });
    }
  }, [id, navigate]);

  const { content, page, setPage, totalPages, totalElements, loading, reload } = useAsyncList(
    approvalService,
    {
      size: 10,
      filters: {
        approvalType: approvalType === 'ALL' ? undefined : approvalType,
        status: status === 'ALL' ? undefined : status,
      },
    }
  );

  const openDecide = (approval, initialDecision) => {
    setTarget(approval);
    setDecision(initialDecision);
    setRemarks('');
    decideModal.open();
  };

  const canDecide = (type) => {
    switch (type) {
      case 'MILESTONE': return hasPermission(PERMISSIONS.APPROVE_MILESTONE);
      case 'TASK_COMPLETION': return hasPermission(PERMISSIONS.APPROVE_TASK_COMPLETION);
      case 'PROJECT_CLOSURE': return hasPermission(PERMISSIONS.APPROVE_PROJECT_CLOSURE);
      case 'CLIENT_APPROVAL': return hasPermission(PERMISSIONS.MANAGE_CLIENT_APPROVAL);
      default: return false;
    }
  };

  const submitDecision = async () => {
    if (!target) return;
    setSaving(true);
    try {
      await approvalService.decide(target.id, { decision, remarks: remarks ? remarks.trim() : null });
      toast.success(`Approval request ${humanize(decision).toLowerCase()} successfully`);
      decideModal.close();
      reload();
    } catch (err) {
      toast.error(err.message || 'Could not record approval decision');
    } finally {
      setSaving(false);
    }
  };

  const getDecisionButtonClass = (dec) => {
    if (dec === 'APPROVED') return 'btn-primary';
    if (dec === 'REJECTED') return 'btn-danger';
    return 'btn-secondary';
  };

  return (
    <div>
      <PageHeader
        title="Approval Center"
        description="Review and sign off on project milestones, task completions, and project closure requests."
        breadcrumb={[{ label: 'Delivery' }, { label: 'Approvals' }]}
      />

      {/* Filter Bar */}
      <div style={{ display: 'flex', gap: 12, marginBottom: 18, flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          {STATUS_TABS.map((tab) => (
            <button
              key={tab.value}
              className={`btn btn-sm ${status === tab.value ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setStatus(tab.value)}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          <Select
            value={approvalType}
            onChange={setApprovalType}
            placeholder="All approval types"
            options={APPROVAL_TYPE}
            style={{ minWidth: 200 }}
          />
          {(status !== 'ALL' || approvalType !== 'ALL') && (
            <button
              className="btn btn-ghost btn-sm"
              onClick={() => {
                setStatus('ALL');
                setApprovalType('ALL');
              }}
            >
              Reset filters
            </button>
          )}
        </div>
      </div>

      {loading && <LoadingSpinner label="Loading approval requests…" />}

      {!loading && content.length === 0 && (
        <EmptyState
          icon={ClipboardCheck}
          title="No approval requests found"
          description="There are currently no requests matching the selected filters."
        />
      )}

      {!loading && (content.length > 0 || targetedItem) && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          {/* Ensure the targeted item is always visible at the top, without duplicating it if it's already on page 1 */}
          {(targetedItem && !content.some(a => a.id === targetedItem.id) ? [targetedItem, ...content] : content).map((a) => (
            <div key={a.id} className="card" style={{ transition: 'all 0.2s ease' }}>
              <div
                className="card-body"
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'flex-start',
                  flexWrap: 'wrap',
                  gap: 16,
                }}
              >
                <div style={{ flex: 1, minWidth: 280 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 6 }}>
                    <span className="badge badge-primary" style={{ fontWeight: 600 }}>
                      {humanize(a.approvalType)}
                    </span>
                    <StatusBadge status={a.status} />
                  </div>

                  {a.remarks && (
                    <div
                      style={{
                        fontSize: '0.875rem',
                        color: 'var(--color-text)',
                        marginTop: 8,
                        background: 'var(--color-bg)',
                        padding: '10px 14px',
                        borderRadius: 8,
                        border: '1px solid var(--color-border)',
                        lineHeight: 1.5,
                      }}
                    >
                      <strong style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--color-text-muted)', display: 'block', marginBottom: 4 }}>
                        Submission Remarks
                      </strong>
                      {a.remarks}
                    </div>
                  )}

                  <div
                    style={{
                      display: 'flex',
                      gap: 16,
                      flexWrap: 'wrap',
                      marginTop: 10,
                      fontSize: '0.75rem',
                      color: 'var(--color-text-faint)',
                    }}
                  >
                    <span>
                      <strong>Requested:</strong> {a.requestedDate ? new Date(a.requestedDate).toLocaleString('en-IN') : '—'}
                    </span>
                    {a.approvedDate && (
                      <span style={{ color: 'var(--color-success)', fontWeight: 500 }}>
                        <strong>Approved:</strong> {new Date(a.approvedDate).toLocaleString('en-IN')}
                      </span>
                    )}
                    {a.rejectedDate && (
                      <span style={{ color: 'var(--color-danger)', fontWeight: 500 }}>
                        <strong>Rejected:</strong> {new Date(a.rejectedDate).toLocaleString('en-IN')}
                      </span>
                    )}
                  </div>
                </div>

                {/* Actions */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, alignSelf: 'center' }}>
                  {a.status === 'PENDING' ? (
                    canDecide(a.approvalType) ? (
                      <>
                        <button
                          className="btn btn-primary btn-sm"
                          style={{ display: 'flex', alignItems: 'center', gap: 6 }}
                          onClick={() => openDecide(a, 'APPROVED')}
                        >
                          <Check size={14} /> Approve
                        </button>
                        <button
                          className="btn btn-secondary btn-sm"
                          style={{ display: 'flex', alignItems: 'center', gap: 6 }}
                          onClick={() => openDecide(a, 'RETURNED_FOR_CHANGES')}
                        >
                          <RotateCcw size={14} /> Return
                        </button>
                        <button
                          className="btn btn-danger-ghost btn-sm"
                          style={{ display: 'flex', alignItems: 'center', gap: 6 }}
                          onClick={() => openDecide(a, 'REJECTED')}
                        >
                          <X size={14} /> Reject
                        </button>
                      </>
                    ) : (
                      <span className="text-faint-c" style={{ fontSize: '0.8125rem' }}>
                        Waiting for approver
                      </span>
                    )
                  ) : (
                    <span className="text-faint-c" style={{ fontSize: '0.8125rem' }}>
                      Decision finalized
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))}

          {/* Pagination */}
          <div className="pagination-bar" style={{ marginTop: 8 }}>
            <span className="text-muted-c" style={{ fontSize: '0.8125rem' }}>
              {totalElements} total request{totalElements === 1 ? '' : 's'}
            </span>
            <div style={{ display: 'flex', gap: 8 }}>
              <button
                className="btn btn-secondary btn-sm"
                disabled={page <= 0}
                onClick={() => setPage(page - 1)}
              >
                Prev
              </button>
              <button
                className="btn btn-secondary btn-sm"
                disabled={page >= totalPages - 1}
                onClick={() => setPage(page + 1)}
              >
                Next
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Decision Confirmation Modal */}
      <Modal
        isOpen={decideModal.isOpen}
        onClose={decideModal.close}
        title={`${humanize(decision)} Approval Request`}
        footer={
          <>
            <button className="btn btn-secondary" onClick={decideModal.close} disabled={saving}>
              Cancel
            </button>
            <button
              className={`btn ${getDecisionButtonClass(decision)}`}
              onClick={submitDecision}
              disabled={saving}
            >
              {saving ? 'Recording decision…' : `Confirm ${humanize(decision)}`}
            </button>
          </>
        }
      >
        <div style={{ marginBottom: 16 }}>
          <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginBottom: 12 }}>
            Are you sure you want to mark this <strong>{target ? humanize(target.approvalType) : ''}</strong> request as <strong>{humanize(decision)}</strong>?
          </p>
          <FormField label="Decision remarks" hint="Optional notes or feedback for the requester">
            <textarea
              className="form-control"
              rows={3}
              placeholder="e.g. All test suites verified. Ready for deployment."
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
            />
          </FormField>
        </div>
      </Modal>
    </div>
  );
}
