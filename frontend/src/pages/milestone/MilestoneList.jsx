import { useEffect, useState } from 'react';
import { Plus, Flag, Send, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import PageHeader from '../../components/common/PageHeader';
import Select from '../../components/common/Select';
import StatusBadge from '../../components/common/StatusBadge';
import EmptyState from '../../components/common/EmptyState';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import MilestoneFormModal from './MilestoneFormModal';
import { useDisclosure } from '../../hooks/useDisclosure';
import { useAuth } from '../../hooks/useAuth';
import { milestoneService } from '../../services/milestone.service';
import { projectService } from '../../services/project.service';
import { PERMISSIONS } from '../../constants/enums';

export default function MilestoneList() {
  const { hasPermission } = useAuth();
  const canManage = hasPermission(PERMISSIONS.MANAGE_MILESTONES);
  const [projectId, setProjectId] = useState('');
  const [projects, setProjects] = useState([]);
  const [milestones, setMilestones] = useState([]);
  const [loading, setLoading] = useState(false);
  const formModal = useDisclosure(false);
  const confirmModal = useDisclosure(false);
  const [editing, setEditing] = useState(null);
  const [pendingDelete, setPendingDelete] = useState(null);

  useEffect(() => {
    projectService.list({ size: 100 }).then((res) => {
      setProjects(res.content);
      if (res.content.length > 0) setProjectId(res.content[0].id);
    }).catch(() => {});
  }, []);

  const load = () => {
    if (!projectId) return;
    setLoading(true);
    milestoneService.list({ projectId, size: 100 }).then((res) => setMilestones(res.content)).finally(() => setLoading(false));
  };

  useEffect(load, [projectId]);

  const submitForReview = async (m) => {
    try {
      await milestoneService.submitForReview(m.id, 'Ready for review.');
      toast.success('Submitted for review — an approval request was opened');
      load();
    } catch (err) { toast.error(err.message || 'Could not submit milestone'); }
  };

  const confirmDelete = async () => {
    try {
      await milestoneService.remove(pendingDelete.id);
      toast.success('Milestone removed');
      confirmModal.close();
      load();
    } catch (err) { toast.error(err.message || 'Could not remove milestone'); }
  };

  return (
    <div>
      <PageHeader
        title="Milestones"
        description="Project timeline checkpoints, approved through the Approval Workflow Engine."
        breadcrumb={[{ label: 'Delivery' }, { label: 'Milestones' }]}
        actions={canManage && (
          <button className="btn btn-primary" disabled={!projectId} onClick={() => { setEditing(null); formModal.open(); }}>
            <Plus size={16} /> New milestone
          </button>
        )}
      />
      <div style={{ marginBottom: 18 }}>
        <Select
          value={projectId || 'ALL'}
          onChange={(v) => setProjectId(v === 'ALL' ? '' : v)}
          placeholder="Choose a project"
          options={projects.map((p) => ({ value: p.id, label: p.name }))}
          style={{ minWidth: 220 }}
        />
      </div>

      {!projectId && <EmptyState title="Choose a project" description="Select a project above to see its milestones." />}
      {projectId && loading && <LoadingSpinner label="Loading milestones…" />}
      {projectId && !loading && milestones.length === 0 && <EmptyState icon={Flag} title="No milestones yet" description="Add the first milestone for this project." />}
      {projectId && !loading && milestones.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {milestones.map((m) => (
            <div key={m.id} className="card">
              <div className="card-body" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
                <div>
                  <div style={{ fontWeight: 600 }}>{m.title}</div>
                  <div className="text-faint-c" style={{ fontSize: '0.75rem', marginTop: 2 }}>{m.description}</div>
                  <div className="text-faint-c" style={{ fontSize: '0.75rem', marginTop: 4 }}>Due {m.dueDate || '—'}</div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <StatusBadge status={m.status} />
                  {canManage && (
                    <>
                      {(m.status === 'PLANNED' || m.status === 'IN_PROGRESS' || m.status === 'REJECTED') && (
                        <button className="btn btn-secondary btn-sm" onClick={() => submitForReview(m)}><Send size={13} /> Submit for review</button>
                      )}
                      <button className="btn btn-ghost btn-sm" onClick={() => { setEditing(m); formModal.open(); }}>Edit</button>
                      <button className="btn btn-danger-ghost btn-sm" onClick={() => { setPendingDelete(m); confirmModal.open(); }}><Trash2 size={13} /></button>
                    </>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <MilestoneFormModal isOpen={formModal.isOpen} onClose={formModal.close} onSaved={load} milestone={editing} defaultProjectId={projectId} />
      <ConfirmDialog
        isOpen={confirmModal.isOpen}
        onClose={confirmModal.close}
        onConfirm={confirmDelete}
        danger
        title="Remove milestone?"
        confirmLabel="Remove"
        description={`Remove ${pendingDelete?.title}? This can't be undone.`}
      />
    </div>
  );
}
