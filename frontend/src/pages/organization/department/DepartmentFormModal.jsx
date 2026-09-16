import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import Modal from '../../../components/common/Modal';
import FormField from '../../../components/common/FormField';
import { departmentService } from '../../../services/department.service';
import { employeeService } from '../../../services/employee.service';

const emptyValues = { name: '', code: '', description: '', headEmployeeId: '', annualBudget: '' };

export default function DepartmentFormModal({ isOpen, onClose, onSaved, department }) {
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({ defaultValues: emptyValues });
  const [employees, setEmployees] = useState([]);

  useEffect(() => {
    if (!isOpen) return;
    employeeService.list({ size: 200, active: true }).then((res) => setEmployees(res.content)).catch(() => {});
    reset(department || emptyValues);
  }, [isOpen, department, reset]);

  const onSubmit = async (values) => {
    const payload = {
      name: values.name,
      code: values.code,
      description: values.description || null,
      headEmployeeId: values.headEmployeeId || null,
      annualBudget: values.annualBudget ? Number(values.annualBudget) : null,
    };
    try {
      if (department) { await departmentService.update(department.id, payload); toast.success('Department updated'); }
      else { await departmentService.create(payload); toast.success('Department created'); }
      onSaved();
      onClose();
    } catch (err) { toast.error(err.message || 'Could not save department'); }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={department ? 'Edit department' : 'New department'}
      footer={
        <>
          <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>
            {isSubmitting ? 'Saving…' : 'Save'}
          </button>
        </>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="form-row">
          <FormField label="Name" required error={errors.name?.message}>
            <input className="form-control" {...register('name', { required: 'Required' })} />
          </FormField>
          <FormField label="Code" required error={errors.code?.message}>
            <input className="form-control" style={{ textTransform: 'uppercase' }} {...register('code', { required: 'Required' })} />
          </FormField>
        </div>
        <FormField label="Description">
          <textarea className="form-control" rows={3} {...register('description')} />
        </FormField>
        <div className="form-row">
          <FormField label="Head of department">
            <select className="form-select" {...register('headEmployeeId')}>
              <option value="">Unassigned</option>
              {employees.map((e) => <option key={e.id} value={e.id}>{e.fullName}</option>)}
            </select>
          </FormField>
          <FormField label="Annual budget (₹)">
            <input type="number" min={0} className="form-control" {...register('annualBudget', { min: 0 })} />
          </FormField>
        </div>
      </form>
    </Modal>
  );
}
