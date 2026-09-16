import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import Modal from '../../components/common/Modal';
import FormField from '../../components/common/FormField';
import { worklogService } from '../../services/worklog.service';
import { projectService } from '../../services/project.service';
import { taskService } from '../../services/task.service';
import { useAuth } from '../../hooks/useAuth';

const emptyValues = { projectId: '', taskId: '', logDate: new Date().toISOString().slice(0, 10), hours: '', notes: '' };

export default function WorkLogFormModal({ isOpen, onClose, onSaved }) {
  const { user } = useAuth();
  const { register, handleSubmit, reset, watch, formState: { errors, isSubmitting } } = useForm({ defaultValues: emptyValues });
  const [projects, setProjects] = useState([]);
  const [tasks, setTasks] = useState([]);
  const projectId = watch('projectId');

  useEffect(() => {
    if (!isOpen) return;
    projectService.list({ size: 100 }).then((res) => setProjects(res.content)).catch(() => {});
    reset(emptyValues);
  }, [isOpen, reset]);

  useEffect(() => {
    if (!projectId) { setTasks([]); return; }
    taskService.list({ projectId, size: 100 }).then((res) => setTasks(res.content || [])).catch(() => {});
  }, [projectId]);

  const onSubmit = async (values) => {
    try {
      await worklogService.create({
        projectId: values.projectId,
        taskId: values.taskId || null,
        logDate: values.logDate,
        hours: Number(values.hours),
        notes: values.notes || null,
      });
      toast.success('Work log added');
      onSaved();
      onClose();
    } catch (err) { toast.error(err.message || 'Could not save log'); }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Log work"
      footer={<>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>{isSubmitting ? 'Saving…' : 'Save'}</button>
      </>}>
      <form onSubmit={handleSubmit(onSubmit)}>
        <FormField label="Project" required error={errors.projectId?.message}>
          <select className="form-select" {...register('projectId', { required: 'Required' })}>
            <option value="">Select project</option>
            {projects.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
        </FormField>
        <FormField label="Task" hint="Optional — leave blank to log general project time">
          <select className="form-select" {...register('taskId')} disabled={!projectId}>
            <option value="">No specific task</option>
            {tasks.map((t) => <option key={t.id} value={t.id}>{t.title}</option>)}
          </select>
        </FormField>
        <div className="form-row">
          <FormField label="Date" required error={errors.logDate?.message}>
            <input type="date" max={new Date().toISOString().slice(0, 10)} className="form-control" {...register('logDate', { required: 'Required' })} />
          </FormField>
          <FormField label="Hours" required error={errors.hours?.message}>
            <input type="number" step="0.5" min="0.01" max="24" className="form-control" {...register('hours', { required: 'Required', min: 0.01, max: 24 })} />
          </FormField>
        </div>
        <FormField label="Notes">
          <textarea className="form-control" rows={3} {...register('notes')} />
        </FormField>
      </form>
    </Modal>
  );
}
