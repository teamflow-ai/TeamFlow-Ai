import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import Modal from '../../components/common/Modal';
import FormField from '../../components/common/FormField';
import { projectService } from '../../services/project.service';
import { clientService } from '../../services/client.service';
import { employeeService } from '../../services/employee.service';
import { PRIORITY } from '../../constants/enums';

const emptyValues = { name: '', code: '', description: '', clientId: '', managerId: '', priority: 'MEDIUM', startDate: '', endDate: '', budget: '' };

export default function ProjectFormModal({ isOpen, onClose, onSaved, project }) {
  const { register, handleSubmit, reset, watch, formState: { errors, isSubmitting } } = useForm({ defaultValues: emptyValues });
  const startDateValue = watch('startDate');
  const [clients, setClients] = useState([]);
  const [employees, setEmployees] = useState([]);

  useEffect(() => {
    if (!isOpen) return;
    clientService.list({ size: 200 }).then((res) => setClients(res.content)).catch(() => {});
    employeeService.list({ size: 200, active: true }).then((res) => setEmployees(res.content)).catch(() => {});
    reset(project || emptyValues);
  }, [isOpen, project, reset]);

  const onSubmit = async (values) => {
    const shared = {
      name: values.name,
      description: values.description || null,
      clientId: values.clientId || null,
      managerId: values.managerId,
      priority: values.priority,
      startDate: values.startDate || null,
      endDate: values.endDate || null,
      budget: values.budget ? Number(values.budget) : null,
    };
    try {
      if (project) {
        await projectService.update(project.id, shared);
        toast.success('Project updated');
      } else {
        await projectService.create({ ...shared, code: values.code });
        toast.success('Project created');
      }
      onSaved();
      onClose();
    } catch (err) { toast.error(err.message || 'Could not save project'); }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={project ? 'Edit project' : 'New project'} size="lg"
      footer={<>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>{isSubmitting ? 'Saving…' : 'Save'}</button>
      </>}>
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="form-row">
          <FormField label="Project name" required error={errors.name?.message}>
            <input className="form-control" {...register('name', { required: 'Required' })} />
          </FormField>
          <FormField label="Code" required={!project} error={errors.code?.message} hint={project ? 'Code cannot be changed after creation' : undefined}>
            <input className="form-control" style={{ textTransform: 'uppercase' }} disabled={!!project} {...register('code', { required: !project && 'Required' })} />
          </FormField>
        </div>
        <FormField label="Description">
          <textarea className="form-control" rows={3} {...register('description')} />
        </FormField>
        <div className="form-row">
          <FormField label="Client">
            <select className="form-select" {...register('clientId')}>
              <option value="">Internal project</option>
              {clients.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </FormField>
          <FormField label="Project manager" required error={errors.managerId?.message}>
            <select className="form-select" {...register('managerId', { required: 'Required' })}>
              <option value="">Select manager</option>
              {employees.map((e) => <option key={e.id} value={e.id}>{e.fullName}</option>)}
            </select>
          </FormField>
        </div>
        <div className="form-row">
          <FormField label="Priority">
            <select className="form-select" {...register('priority')}>
              {PRIORITY.map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
          </FormField>
          <FormField label="Budget (₹)">
            <input type="number" min={0} className="form-control" {...register('budget', { min: 0 })} />
          </FormField>
        </div>
        <div className="form-row">
          <FormField label="Start date">
            <input type="date" className="form-control" {...register('startDate')} />
          </FormField>
          <FormField label="End date" error={errors.endDate?.message}>
            <input
              type="date"
              min={startDateValue || undefined}
              className="form-control"
              {...register('endDate', {
                validate: (v, formValues) =>
                  !v || !formValues.startDate || v >= formValues.startDate || 'End date must be after start date',
              })}
            />
          </FormField>
        </div>
      </form>
    </Modal>
  );
}
