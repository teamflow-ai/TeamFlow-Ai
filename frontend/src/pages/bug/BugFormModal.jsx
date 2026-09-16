import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import SimpleMdeReact from 'react-simplemde-editor';
import 'easymde/dist/easymde.min.css';
import Modal from '../../components/common/Modal';
import FormField from '../../components/common/FormField';
import { bugService } from '../../services/bug.service';
import { employeeService } from '../../services/employee.service';
import { PRIORITY } from '../../constants/enums';

const emptyValues = { title: '', description: '', severity: 'MEDIUM', assigneeId: '' };

export default function BugFormModal({ isOpen, onClose, onSaved, bug, defaultProjectId }) {
  const { register, handleSubmit, reset, watch, setValue, formState: { errors, isSubmitting } } = useForm({ defaultValues: emptyValues });
  const [employees, setEmployees] = useState([]);

  useEffect(() => {
    if (!isOpen) return;
    employeeService.list({ size: 200, active: true }).then((res) => setEmployees(res.content)).catch(() => {});
    reset(bug ? { title: bug.title, description: bug.description, severity: bug.severity, assigneeId: bug.assigneeId || '' } : emptyValues);
  }, [isOpen, bug, reset]);

  const onSubmit = async (values) => {
    try {
      if (bug) {
        await bugService.update(bug.id, { title: values.title, description: values.description, severity: values.severity });
        toast.success('Bug updated');
      } else {
        await bugService.create({
          projectId: defaultProjectId,
          title: values.title,
          description: values.description,
          severity: values.severity,
          assigneeId: values.assigneeId || null,
        });
        toast.success('Bug reported');
      }
      onSaved();
      onClose();
    } catch (err) {
      toast.error(err.message || 'Could not save bug');
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={bug ? 'Edit bug' : 'Report a bug'}
      footer={<>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>{isSubmitting ? 'Saving…' : 'Save'}</button>
      </>}>
      <form onSubmit={handleSubmit(onSubmit)}>
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
          <FormField label="Severity">
            <select className="form-select" {...register('severity')}>
              {PRIORITY.map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
          </FormField>
          {!bug && (
            <FormField label="Assignee">
              <select className="form-select" {...register('assigneeId')}>
                <option value="">Unassigned</option>
                {employees.map((e) => <option key={e.id} value={e.id}>{e.fullName}</option>)}
              </select>
            </FormField>
          )}
        </div>
      </form>
    </Modal>
  );
}
