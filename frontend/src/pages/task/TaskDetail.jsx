import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Edit2, Sparkles, CheckCircle2, Clock, Calendar, Flag, Send, Paperclip } from 'lucide-react';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { formatDistanceToNow } from 'date-fns';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import Avatar from '../../components/common/Avatar';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import TaskFormModal from './TaskFormModal';
import AssignmentWorkspace from './AssignmentWorkspace';
import { useDisclosure } from '../../hooks/useDisclosure';
import { useAuth } from '../../hooks/useAuth';
import { taskService } from '../../services/task.service';
import { sprintService } from '../../services/sprint.service';
import { aiService } from '../../services/ai.service';
import { TASK_STATUS, PERMISSIONS, humanize } from '../../constants/enums';

export default function TaskDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { hasPermission } = useAuth();
  const [task, setTask] = useState(null);
  const [loading, setLoading] = useState(true);
  const [risk, setRisk] = useState(null);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [attachments, setAttachments] = useState([]);
  const [attachmentUrl, setAttachmentUrl] = useState('');
  const [attachmentName, setAttachmentName] = useState('');
  const [dependencies, setDependencies] = useState([]);
  const [availableTasks, setAvailableTasks] = useState([]);
  const [sprints, setSprints] = useState([]);
  const [selectedDependencyId, setSelectedDependencyId] = useState('');
  const editModal = useDisclosure(false);

  const load = async () => {
    setLoading(true);
    try {
      const t = await taskService.get(id);
      setTask(t);
      taskService.listComments(id).then((r) => setComments(r.content)).catch(() => {});
      taskService.listAttachments(id).then(setAttachments).catch(() => {});
      taskService.getDependencies(id).then(setDependencies).catch(() => {});
      taskService.list({ projectId: t.projectId, size: 100 }).then((r) => setAvailableTasks(r.content.filter(x => x.id !== id))).catch(() => {});
      sprintService.list({ projectId: t.projectId, size: 100 }).then((r) => setSprints(r.content)).catch(() => {});
      aiService.getTaskRisk(id).then(setRisk).catch(() => setRisk(null));
    } catch {
      navigate('/tasks');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [id]);

  if (loading || !task) return <LoadingSpinner size="lg" label="Loading task…" />;

  const changeStatus = async (status) => {
    try {
      await taskService.updateStatus(task.id, status);
      toast.success(`Task moved to ${humanize(status)}`);
      load();
    } catch (err) { toast.error(err.message || 'Could not update status'); }
  };

  const submitComment = async () => {
    if (!newComment.trim()) return;
    try {
      await taskService.addComment(task.id, newComment.trim());
      setNewComment('');
      const r = await taskService.listComments(id);
      setComments(r.content);
    } catch (err) { toast.error(err.message || 'Could not add comment'); }
  };

  const submitAttachment = async () => {
    if (!attachmentName.trim() || !attachmentUrl.trim()) return;
    try {
      await taskService.addAttachment(task.id, { fileName: attachmentName.trim(), fileUrl: attachmentUrl.trim() });
      setAttachmentName('');
      setAttachmentUrl('');
      const list = await taskService.listAttachments(id);
      setAttachments(list);
      toast.success('Attachment recorded');
    } catch (err) { toast.error(err.message || 'Could not record attachment'); }
  };

  const addDependency = async () => {
    if (!selectedDependencyId) return;
    try {
      await taskService.addDependency(task.id, selectedDependencyId);
      setSelectedDependencyId('');
      const deps = await taskService.getDependencies(id);
      setDependencies(deps);
      toast.success('Dependency added');
    } catch (err) {
      toast.error(err.response?.data?.message || err.message || 'Could not add dependency');
    }
  };

  const removeDependency = async (dependsOnId) => {
    try {
      await taskService.removeDependency(task.id, dependsOnId);
      const deps = await taskService.getDependencies(id);
      setDependencies(deps);
      toast.success('Dependency removed');
    } catch (err) {
      toast.error(err.message || 'Could not remove dependency');
    }
  };

  const changeSprint = async (sprintId) => {
    try {
      await taskService.assignSprint(task.id, sprintId || null);
      toast.success('Sprint updated');
      load();
    } catch (err) {
      toast.error(err.message || 'Could not update sprint');
    }
  };

  return (
    <div>
      <PageHeader
        breadcrumb={[{ label: 'Delivery' }, { label: 'Tasks', to: '/tasks' }, { label: task.title }]}
        title={task.title}
        actions={hasPermission(PERMISSIONS.UPDATE_TASK) && <button className="btn btn-secondary" onClick={editModal.open}><Edit2 size={15} /> Edit</button>}
      />
      <button className="btn btn-ghost btn-sm" onClick={() => navigate('/tasks')} style={{ marginBottom: 16 }}>
        <ArrowLeft size={14} /> Back to tasks
      </button>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px', gap: 20 }} className="dash-grid-main">
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <div className="card">
            <div className="card-body">
              <div className="markdown-body" style={{ fontSize: '0.9rem', lineHeight: 1.7 }}>
                {task.description ? (
                  <ReactMarkdown remarkPlugins={[remarkGfm]}>{task.description}</ReactMarkdown>
                ) : (
                  <p className="text-muted-c">No description provided.</p>
                )}
              </div>
              {task.requiredSkills?.length > 0 && (
                <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginTop: 14 }}>
                  {task.requiredSkills.map((s) => <span key={s} className="badge badge-primary">{s}</span>)}
                </div>
              )}
              <hr className="divider" />
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 16 }}>
                <div><div className="text-faint-c" style={{ fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: 4 }}><Flag size={13} /> Priority</div><StatusBadge status={task.priority} /></div>
                <div><div className="text-faint-c" style={{ fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: 4 }}><Calendar size={13} /> Due date</div><div style={{ fontWeight: 600, fontSize: '0.875rem', marginTop: 4 }}>{task.dueDate || '—'}</div></div>
                <div><div className="text-faint-c" style={{ fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: 4 }}><Clock size={13} /> Estimated</div><div style={{ fontWeight: 600, fontSize: '0.875rem', marginTop: 4 }}>{task.estimatedHours ?? '—'}h</div></div>
                <div><div className="text-faint-c" style={{ fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: 4 }}><CheckCircle2 size={13} /> Logged</div><div style={{ fontWeight: 600, fontSize: '0.875rem', marginTop: 4 }}>{task.actualHours ?? 0}h</div></div>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Status</h3></div>
            <div className="card-body">
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
                <span className="text-faint-c" style={{ fontSize: '0.8125rem' }}>Current status:</span>
                <StatusBadge status={task.status} />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
                {TASK_STATUS.filter(s => s !== task.status).map((s) => {
                  const isBlocked = (s === 'DONE' || s === 'IN_REVIEW') && dependencies.some(d => d.dependsOnTaskStatus !== 'DONE');
                  return (
                    <button
                      key={s}
                      title={isBlocked ? "Cannot transition: Dependent tasks are not DONE" : ""}
                      className="btn btn-secondary btn-sm"
                      style={{ fontSize: '0.75rem', padding: '6px' }}
                      onClick={() => changeStatus(s)}
                      disabled={!hasPermission(PERMISSIONS.UPDATE_TASK) || isBlocked}
                    >
                      {humanize(s)}
                    </button>
                  );
                })}
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Comments</h3></div>
            <div className="card-body">
              <div style={{ display: 'flex', flexDirection: 'column', gap: 16, marginBottom: 18, maxHeight: 400, overflowY: 'auto', paddingRight: 8 }}>
                {comments.length === 0 && <p className="text-muted-c" style={{ fontSize: '0.875rem' }}>No comments yet.</p>}
                {comments.map((c) => (
                  <div key={c.id} style={{ display: 'flex', gap: 10 }}>
                    <Avatar name={c.authorName} size="sm" />
                    <div style={{ flex: 1, backgroundColor: 'rgba(0,0,0,0.02)', padding: '10px 14px', borderRadius: '0 8px 8px 8px', border: '1px solid var(--color-border)' }}>
                      <div style={{ fontSize: '0.8125rem', display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                        <strong>{c.authorName}</strong> 
                        <span className="text-faint-c">
                          {c.createdAt ? formatDistanceToNow(new Date(c.createdAt), { addSuffix: true }) : ''}
                        </span>
                      </div>
                      <div style={{ fontSize: '0.875rem' }} className="markdown-body comment-markdown">
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>{c.comment}</ReactMarkdown>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
              {hasPermission(PERMISSIONS.UPDATE_TASK) && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8, backgroundColor: 'rgba(0,0,0,0.02)', padding: 12, borderRadius: 8, border: '1px solid var(--color-border)' }}>
                  <textarea 
                    className="form-control" 
                    placeholder="Write a comment... (Markdown is supported)" 
                    rows={2}
                    value={newComment} 
                    onChange={(e) => setNewComment(e.target.value)} 
                    onKeyDown={(e) => (e.ctrlKey || e.metaKey) && e.key === 'Enter' && submitComment()} 
                  />
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span className="text-faint-c" style={{ fontSize: '0.75rem' }}>Pro tip: Ctrl+Enter to send</span>
                    <button className="btn btn-primary btn-sm" onClick={submitComment} disabled={!newComment.trim()}>
                      <Send size={14} style={{ marginRight: 6 }} /> Comment
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>

          <div className="card">
            <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Attachments</h3></div>
            <div className="card-body">
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 14 }}>
                {attachments.length === 0 && <p className="text-muted-c" style={{ fontSize: '0.875rem' }}>No attachments recorded.</p>}
                {attachments.map((a) => (
                  <a key={a.id} href={a.fileUrl} target="_blank" rel="noreferrer" style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: '0.875rem' }}>
                    <Paperclip size={14} /> {a.fileName}
                  </a>
                ))}
              </div>
              {hasPermission(PERMISSIONS.UPDATE_TASK) && (
                <>
                  <div className="form-row">
                    <input className="form-control" placeholder="File name" value={attachmentName} onChange={(e) => setAttachmentName(e.target.value)} />
                    <input className="form-control" placeholder="File URL" value={attachmentUrl} onChange={(e) => setAttachmentUrl(e.target.value)} />
                  </div>
                  <button className="btn btn-secondary btn-sm" style={{ marginTop: 8 }} onClick={submitAttachment}>Record attachment</button>
                </>
              )}
            </div>
          </div>

          <div className="card">
            <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Dependencies</h3></div>
            <div className="card-body">
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 14 }}>
                {dependencies.length === 0 && <p className="text-muted-c" style={{ fontSize: '0.875rem' }}>No dependencies.</p>}
                {dependencies.map((d) => (
                  <div key={d.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.875rem', padding: '8px', border: '1px solid var(--color-border)', borderRadius: '6px' }}>
                    <div>
                      <div><span style={{ fontWeight: 600 }}>Blocked by:</span> {d.dependsOnTaskTitle}</div>
                      <div style={{ marginTop: 4 }}><StatusBadge status={d.dependsOnTaskStatus} /></div>
                    </div>
                    {hasPermission(PERMISSIONS.UPDATE_TASK) && (
                      <button className="btn btn-ghost btn-sm text-danger" onClick={() => removeDependency(d.dependsOnTaskId)}>Remove</button>
                    )}
                  </div>
                ))}
              </div>
              {hasPermission(PERMISSIONS.UPDATE_TASK) && availableTasks.length > 0 && (
                <div style={{ display: 'flex', gap: 8 }}>
                  <select 
                    className="form-control" 
                    value={selectedDependencyId} 
                    onChange={(e) => setSelectedDependencyId(e.target.value)}
                  >
                    <option value="">Select a task that blocks this one...</option>
                    {availableTasks.map(t => (
                      <option key={t.id} value={t.id}>{t.title} ({humanize(t.status)})</option>
                    ))}
                  </select>
                  <button className="btn btn-secondary" onClick={addDependency}>Add</button>
                </div>
              )}
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          {risk && (
            <motion.div 
              className="card" 
              initial={{ opacity: 0, y: 8 }} 
              animate={{ opacity: 1, y: 0 }}
              style={{
                borderLeft: `4px solid ${risk.riskLevel === 'HIGH' ? 'var(--color-danger, #ef4444)' : risk.riskLevel === 'MEDIUM' ? 'var(--color-warning, #f59e0b)' : 'var(--color-success, #10b981)'}`
              }}
            >
              <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3 style={{ fontSize: '1rem', display: 'flex', alignItems: 'center', gap: 8 }}>
                  <Sparkles size={16} color="var(--color-accent, #6366f1)" /> AI Risk Assessment
                </h3>
                <span className={`badge badge-${risk.riskLevel === 'HIGH' ? 'danger' : risk.riskLevel === 'MEDIUM' ? 'warning' : 'success'}`}>
                  {risk.riskLevel} RISK
                </span>
              </div>
              <div className="card-body">
                <p style={{ fontSize: '0.875rem', color: 'var(--color-text, #333)', lineHeight: 1.5, margin: 0 }}>
                  {risk.explanation}
                </p>
                <div style={{ marginTop: 10, fontSize: '0.6875rem', color: 'var(--color-text-muted, #888)', display: 'flex', justifyContent: 'space-between' }}>
                  <span>Engine: {risk.provider}</span>
                  <span>Evaluated in real-time</span>
                </div>
              </div>
            </motion.div>
          )}

          <div className="card" style={{ marginBottom: 20 }}>
            <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Sprint Context</h3></div>
            <div className="card-body">
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                {hasPermission(PERMISSIONS.UPDATE_TASK) ? (
                  <select 
                    className="form-select" 
                    value={task.sprintId || ''} 
                    onChange={(e) => changeSprint(e.target.value)}
                  >
                    <option value="">Backlog (No Sprint)</option>
                    {sprints.map(s => (
                      <option key={s.id} value={s.id}>{s.name} ({humanize(s.status)})</option>
                    ))}
                  </select>
                ) : (
                  <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>
                    {task.sprintId ? sprints.find(s => s.id === task.sprintId)?.name || 'Unknown' : 'Backlog'}
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-header"><h3 style={{ fontSize: '1rem' }}>Assignment Workspace</h3></div>
            <div className="card-body">
              {task.assigneeName && (
                <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: '20px' }}>
                  <Avatar name={task.assigneeName} />
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{task.assigneeName}</div>
                    <div className="text-faint-c" style={{ fontSize: '0.75rem' }}>via {humanize(task.assignmentMode || 'MANUAL')} assignment</div>
                  </div>
                </div>
              )}

              {hasPermission(PERMISSIONS.ASSIGN_TASK) && (
                <>
                  {task.assigneeName && <hr className="divider" />}
                  <AssignmentWorkspace 
                    taskId={task.id} 
                    currentAssigneeId={task.assigneeId} 
                    onAssigned={() => load()} 
                  />
                </>
              )}
            </div>
          </div>
        </div>
      </div>

      <TaskFormModal isOpen={editModal.isOpen} onClose={editModal.close} onSaved={load} task={task} />
    </div>
  );
}
