import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import Modal from '../../../components/common/Modal';
import FormField from '../../../components/common/FormField';
import { teamService } from '../../../services/team.service';
import { departmentService } from '../../../services/department.service';
import { employeeService } from '../../../services/employee.service';

const emptyValues = { name: '', description: '', departmentId: '', leadEmployeeId: '' };

export default function TeamFormModal({ isOpen, onClose, onSaved, team }) {
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({ defaultValues: emptyValues });
  const [departments, setDepartments] = useState([]);
  const [employees, setEmployees] = useState([]);

  useEffect(() => {
    if (!isOpen) return;
    departmentService.list({ size: 200 }).then((res) => setDepartments(res.content)).catch(() => {});
    employeeService.list({ size: 200, active: true }).then((res) => setEmployees(res.content)).catch(() => {});
    reset(team || emptyValues);
  }, [isOpen, team, reset]);

  const onSubmit = async (values) => {
    const payload = {
      name: values.name,
      description: values.description || null,
      departmentId: values.departmentId,
      leadEmployeeId: values.leadEmployeeId || null,
    };
    try {
      if (team) { await teamService.update(team.id, payload); toast.success('Team updated'); }
      else { await teamService.create(payload); toast.success('Team created'); }
      onSaved();
      onClose();
    } catch (err) { toast.error(err.message || 'Could not save team'); }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={team ? 'Edit team' : 'New team'}
      footer={<>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>{isSubmitting ? 'Saving…' : 'Save'}</button>
      </>}
    >
      <form onSubmit={handleSubmit(onSubmit)}>
        <FormField label="Team name" required error={errors.name?.message}>
          <input className="form-control" {...register('name', { required: 'Required' })} />
        </FormField>
        <FormField label="Description">
          <textarea className="form-control" rows={2} {...register('description')} />
        </FormField>
        <div className="form-row">
          <FormField label="Department" required error={errors.departmentId?.message}>
            <select className="form-select" {...register('departmentId', { required: 'Required' })}>
              <option value="">Select department</option>
              {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </FormField>
          <FormField label="Team lead">
            <select className="form-select" {...register('leadEmployeeId')}>
              <option value="">Unassigned</option>
              {employees.map((e) => <option key={e.id} value={e.id}>{e.fullName}</option>)}
            </select>
          </FormField>
        </div>
      </form>
    </Modal>
  );
}
