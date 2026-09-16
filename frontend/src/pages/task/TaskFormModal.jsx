import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { Sparkles } from 'lucide-react';
import SimpleMdeReact from 'react-simplemde-editor';
import 'easymde/dist/easymde.min.css';
import Modal from '../../components/common/Modal';
import FormField from '../../components/common/FormField';
import { taskService } from '../../services/task.service';
import { projectService } from '../../services/project.service';
import { sprintService } from '../../services/sprint.service';
import { PRIORITY } from '../../constants/enums';

const emptyValues = { projectId: '', sprintId: '', title: '', description: '', priority: 'MEDIUM', dueDate: '', estimatedHours: '', requiredSkills: '', assigneeId: '' };

export default function TaskFormModal({ isOpen, onClose, onSaved, task, defaultProjectId }) {
  const { register, handleSubmit, reset, watch, setValue, formState: { errors, isSubmitting } } = useForm({ defaultValues: emptyValues });
  const [projects, setProjects] = useState([]);
  const [sprints, setSprints] = useState([]);
  const [projectMembers, setProjectMembers] = useState([]);

  const watchProjectId = watch('projectId');

  useEffect(() => {
    if (!isOpen) return;
    projectService.list({ size: 200 }).then((res) => setProjects(res.content)).catch(() => {});

    reset(
      task
        ? { ...task, requiredSkills: (task.requiredSkills || []).join(', '), assigneeId: task.assigneeId || '' }
        : { ...emptyValues, projectId: defaultProjectId || '' }
    );
  }, [isOpen, task, defaultProjectId, reset]);

  useEffect(() => {
    const pid = watchProjectId || task?.projectId;
    if (pid) {
      projectService.get(pid)
        .then(res => setProjectMembers(res.members || []))
        .catch(() => setProjectMembers([]));
      sprintService.list({ projectId: pid, size: 50 })
        .then(res => setSprints(res.content || []))
        .catch(() => setSprints([]));
    } else {
      setProjectMembers([]);
      setSprints([]);
    }
  }, [watchProjectId, task?.projectId]);


  const onSubmit = async (values) => {
    const shared = {
      title: values.title,
      description: values.description || null,
      priority: values.priority,
      dueDate: values.dueDate || null,
      estimatedHours: values.estimatedHours ? Number(values.estimatedHours) : null,
      requiredSkills: values.requiredSkills.split(',').map((s) => s.trim().toUpperCase()).filter(Boolean),
    };
    try {
      let savedTaskId = task?.id;
      if (task) {
        await taskService.update(task.id, shared);
        if (values.assigneeId !== (task.assigneeId || '')) {
          await taskService.assign(task.id, { assigneeId: values.assigneeId || null, mode: 'MANUAL' });
        }
        if (values.sprintId !== (task.sprintId || '')) {
          await taskService.assignSprint(task.id, values.sprintId || null);
        }
        toast.success('Task updated');
      } else {
        shared.sprintId = values.sprintId || null;
        const created = await taskService.create({ ...shared, projectId: values.projectId });
        if (values.assigneeId) {
          await taskService.assign(created.id, { assigneeId: values.assigneeId, mode: 'MANUAL' });
        }
        toast.success('Task created');
      }

      onSaved();
      onClose();
    } catch (err) { toast.error(err.message || 'Could not save task'); }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={task ? 'Edit task' : 'Create task'} size="lg"
      footer={<>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>{isSubmitting ? 'Saving…' : 'Save'}</button>
      </>}>
      <form onSubmit={handleSubmit(onSubmit)}>
        {!task && (
          <FormField label="Project" required error={errors.projectId?.message}>
            <select className="form-select" {...register('projectId', { required: 'Required' })}>
              <option value="">Select project</option>
              {projects.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
          </FormField>
        )}
        <FormField label="Title" required error={errors.title?.message}>
          <input className="form-control" {...register('title', { required: 'Required' })} />
        </FormField>
        <FormField label="Description">
          <SimpleMdeReact
            value={watch('description')}
            onChange={(val) => setValue('description', val)}
            options={{ spellChecker: false, status: false, minHeight: '150px' }}
          />
        </FormField>
        <div className="form-row">
          <FormField label="Priority">
            <select className="form-select" {...register('priority')}>
              {PRIORITY.map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
          </FormField>
          <FormField label="Sprint">
            <select className="form-select" {...register('sprintId')}>
              <option value="">Backlog (No Sprint)</option>
              {sprints.map((s) => (
                <option key={s.id} value={s.id}>{s.name} ({s.status})</option>
              ))}
            </select>
          </FormField>
          <FormField label="Assignee">
            <select className="form-select" {...register('assigneeId')}>
              <option value="">Unassigned</option>
              {projectMembers.map(m => (
                <option key={m.employeeId} value={m.employeeId}>{m.employeeName}</option>
              ))}
            </select>
          </FormField>
        </div>
        <div className="form-row">
          <FormField label="Due date">
            <input type="date" className="form-control" {...register('dueDate')} />
          </FormField>
          <FormField label="Estimated hours">
            <input type="number" step="0.5" min="0" className="form-control" {...register('estimatedHours', { min: 0 })} />
          </FormField>
        </div>
        <FormField label="Required skills" hint="Comma-separated">
          <input className="form-control" {...register('requiredSkills')} />
        </FormField>
      </form>
    </Modal>
  );
}
