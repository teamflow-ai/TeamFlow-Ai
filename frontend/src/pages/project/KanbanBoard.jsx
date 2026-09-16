import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import PageHeader from '../../components/common/PageHeader';
import Avatar from '../../components/common/Avatar';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import TaskFormModal from '../task/TaskFormModal';
import { useDisclosure } from '../../hooks/useDisclosure';
import { useAuth } from '../../hooks/useAuth';
import { taskService } from '../../services/task.service';
import { projectService } from '../../services/project.service';
import { TASK_STATUS, PERMISSIONS, humanize, canTaskTransition } from '../../constants/enums';

export default function KanbanBoard() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { hasPermission } = useAuth();
  const [project, setProject] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dragTaskId, setDragTaskId] = useState(null);
  const [dragOverStatus, setDragOverStatus] = useState(null);
  const formModal = useDisclosure(false);

  const load = async () => {
    setLoading(true);
    try {
      const [p, t] = await Promise.all([
        projectService.get(id),
        taskService.list({ projectId: id, size: 200 }),
      ]);
      setProject(p);
      setTasks(t.content);
    } catch {
      navigate('/projects');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [id]);

  const canEdit = hasPermission(PERMISSIONS.UPDATE_TASK);

  const onDrop = async (status) => {
    setDragOverStatus(null);
    if (!dragTaskId || !canEdit) return;
    const task = tasks.find((t) => t.id === dragTaskId);
    setDragTaskId(null);
    if (!task || task.status === status) return;
    
    if (!canTaskTransition(task.status, status)) {
      toast.error(`Cannot move a task from ${humanize(task.status)} to ${humanize(status)}`);
      return;
    }
    
    setTasks((prev) => prev.map((t) => (t.id === task.id ? { ...t, status } : t)));
    try {
      await taskService.updateStatus(task.id, status);
    } catch (err) {
      toast.error(err.message || 'Could not move task');
      load();
    }
  };

  const getDragStateProps = (targetStatus) => {
    if (!dragTaskId) return { droppable: false, valid: false };
    const task = tasks.find((t) => t.id === dragTaskId);
    if (!task) return { droppable: false, valid: false };
    return { 
      droppable: true, 
      valid: canTaskTransition(task.status, targetStatus) 
    };
  };

  if (loading || !project) return <LoadingSpinner size="lg" label="Loading board…" />;

  return (
    <div>
      <PageHeader
        breadcrumb={[{ label: 'Delivery' }, { label: 'Projects', to: '/projects' }, { label: project.name, to: `/projects/${id}` }, { label: 'Kanban' }]}
        title={`${project.name} — Kanban`}
        description="Drag a card to change its status. Every move updates the backend immediately."
        actions={canEdit && <button className="btn btn-primary" onClick={formModal.open}><Plus size={16} /> New task</button>}
      />
      <Link to={`/projects/${id}`} className="btn btn-ghost btn-sm" style={{ marginBottom: 16 }}>
        <ArrowLeft size={14} /> Back to project
      </Link>

      <div className="kanban-board">
        {TASK_STATUS.map((status) => {
          const columnTasks = tasks.filter((t) => t.status === status);
          const dragState = getDragStateProps(status);
          
          let columnClass = 'kanban-column';
          if (dragOverStatus === status) {
            columnClass += dragState.valid ? ' kanban-column-over' : ' kanban-column-invalid';
          } else if (dragState.droppable && dragState.valid) {
            columnClass += ' kanban-column-valid-target';
          }

          return (
            <div
              key={status}
              className={columnClass}
              onDragOver={(e) => { 
                e.preventDefault(); 
                if (dragState.droppable && dragState.valid) {
                  setDragOverStatus(status);
                }
              }}
              onDragLeave={() => setDragOverStatus((s) => (s === status ? null : s))}
              onDrop={(e) => { 
                e.preventDefault(); 
                if (dragState.droppable && dragState.valid) {
                  onDrop(status); 
                } else {
                  setDragOverStatus(null);
                  if (dragState.droppable) {
                    toast.error(`Invalid transition to ${humanize(status)}`);
                  }
                }
              }}
            >
              <div className="kanban-column-head">
                <span>{humanize(status)}</span>
                <span className="badge badge-neutral">{columnTasks.length}</span>
              </div>
              <div className="kanban-column-body">
                {columnTasks.map((t) => (
                  <div
                    key={t.id}
                    className="kanban-card"
                    draggable={canEdit}
                    onDragStart={() => setDragTaskId(t.id)}
                    onClick={() => navigate(`/tasks/${t.id}`)}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 8 }}>
                      <span style={{ fontSize: '0.8125rem', fontWeight: 600, lineHeight: 1.4 }}>{t.title}</span>
                      <StatusBadge status={t.priority} />
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 12 }}>
                      <span className="text-faint-c" style={{ fontSize: '0.6875rem' }}>{t.dueDate || 'No due date'}</span>
                      {t.assigneeName ? <Avatar name={t.assigneeName} size="sm" /> : <span className="badge badge-neutral" style={{ fontSize: '0.625rem' }}>Unassigned</span>}
                    </div>
                  </div>
                ))}
                {columnTasks.length === 0 && <div className="kanban-empty">No tasks</div>}
              </div>
            </div>
          );
        })}
      </div>

      <TaskFormModal isOpen={formModal.isOpen} onClose={formModal.close} onSaved={load} task={null} defaultProjectId={id} />
    </div>
  );
}
