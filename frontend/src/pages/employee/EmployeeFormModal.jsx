import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import Modal from '../../components/common/Modal';
import FormField from '../../components/common/FormField';
import { employeeService } from '../../services/employee.service';
import { departmentService } from '../../services/department.service';
import { teamService } from '../../services/team.service';

const emptyValues = {
  firstName: '', lastName: '', workEmail: '', phone: '', designation: '', roleName: 'DEVELOPER',
  dateOfJoining: '', dateOfBirth: '',
  departmentId: '', teamId: '', secondaryTeamIds: [], managerId: '', weeklyCapacityHours: 40,
  yearsOfExperience: 0, skills: '',
};

export default function EmployeeFormModal({ isOpen, onClose, onSaved, employee }) {
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({ defaultValues: emptyValues });
  const [departments, setDepartments] = useState([]);
  const [teams, setTeams] = useState([]);
  const [managers, setManagers] = useState([]);

  useEffect(() => {
    if (!isOpen) return;
    departmentService.list({ size: 200 }).then((res) => setDepartments(res.content)).catch(() => {});
    teamService.list({ size: 200 }).then((res) => setTeams(res.content)).catch(() => {});
    employeeService.list({ size: 200, active: true }).then((res) => setManagers(res.content)).catch(() => {});
    reset(
      employee
        ? { ...employee, skills: (employee.skills || []).join(', '), secondaryTeamIds: (employee.allTeams || []).filter(t => !t.primary).map(t => t.teamId) }
        : emptyValues
    );
  }, [isOpen, employee, reset]);

  const onSubmit = async (values) => {
    const payload = {
      firstName: values.firstName,
      lastName: values.lastName,
      workEmail: values.workEmail,
      phone: values.phone || null,
      designation: values.designation || null,
      roleName: values.roleName || null,
      dateOfJoining: values.dateOfJoining || null,
      dateOfBirth: values.dateOfBirth || null,
      departmentId: values.departmentId || null,
      teamId: values.teamId || null,
      secondaryTeamIds: values.secondaryTeamIds || [],
      managerId: values.managerId || null,
      weeklyCapacityHours: values.weeklyCapacityHours ? Number(values.weeklyCapacityHours) : null,
      yearsOfExperience: values.yearsOfExperience !== '' ? Number(values.yearsOfExperience) : null,
      skills: values.skills.split(',').map((s) => s.trim().toUpperCase()).filter(Boolean),
    };
    try {
      if (employee) {
        await employeeService.update(employee.id, payload);
        toast.success('Employee updated');
      } else {
        await employeeService.create(payload);
        toast.success('Employee added');
      }
      onSaved();
      onClose();
    } catch (err) {
      toast.error(err.message || 'Could not save employee');
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={employee ? 'Edit employee' : 'Add employee'}
      size="lg"
      footer={
        <>
          <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>
            {isSubmitting ? 'Saving…' : employee ? 'Save changes' : 'Add employee'}
          </button>
        </>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="form-row">
          <FormField label="First name" required error={errors.firstName?.message}>
            <input className="form-control" {...register('firstName', { required: 'Required' })} />
          </FormField>
          <FormField label="Last name" required error={errors.lastName?.message}>
            <input className="form-control" {...register('lastName', { required: 'Required' })} />
          </FormField>
        </div>
        <div className="form-row">
          <FormField label="Work email" required error={errors.workEmail?.message}>
            <input
              type="email"
              className="form-control"
              {...register('workEmail', {
                required: 'Required',
                pattern: { value: /^\S+@\S+\.\S+$/, message: 'Enter a valid email' },
              })}
            />
          </FormField>
          <FormField label="Phone" error={errors.phone?.message}>
            <input className="form-control" {...register('phone', {
                pattern: { value: /^\+?[1-9]\d{1,14}$/, message: 'Enter a valid phone number (e.g. +14155552671)' }
            })} />
          </FormField>
        </div>
        <div className="form-row">
          <FormField label="Designation" required error={errors.designation?.message}>
            <input className="form-control" {...register('designation', { required: 'Required' })} />
          </FormField>
          <FormField label="System Role" hint="Affects permissions">
            <select className="form-select" {...register('roleName')}>
              <option value="DEVELOPER">Developer (Default)</option>
              <option value="PROJECT_MANAGER">Project Manager</option>
              <option value="TEAM_LEAD">Team Lead</option>
              <option value="QA">QA</option>
              <option value="HR">HR</option>
            </select>
          </FormField>
        </div>
        <div className="form-row">
          <FormField label="Weekly capacity (hrs)">
            <input type="number" min={1} max={80} className="form-control" {...register('weeklyCapacityHours', { min: 1, max: 80 })} />
          </FormField>
          <FormField label="Date of joining">
            <input type="date" className="form-control" {...register('dateOfJoining')} />
          </FormField>
          <FormField label="Date of birth">
            <input type="date" max={new Date().toISOString().split('T')[0]} className="form-control" {...register('dateOfBirth')} />
          </FormField>
        </div>
        <div className="form-row">
          <FormField label="Department">
            <select className="form-select" {...register('departmentId')}>
              <option value="">Select department</option>
              {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </FormField>
          <FormField label="Primary Team">
            <select className="form-select" {...register('teamId')}>
              <option value="">Select primary team</option>
              {teams.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
            </select>
          </FormField>
          <FormField label="Secondary Teams" hint="Hold Ctrl/Cmd to select multiple">
            <select multiple className="form-select" {...register('secondaryTeamIds')} size={3}>
              {teams.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
            </select>
          </FormField>
        </div>
        <div className="form-row">
          <FormField label="Reporting manager">
            <select className="form-select" {...register('managerId')}>
              <option value="">None</option>
              {managers.filter((e) => e.id !== employee?.id).map((e) => <option key={e.id} value={e.id}>{e.fullName}</option>)}
            </select>
          </FormField>
          <FormField label="Years of experience">
            <input type="number" min={0} className="form-control" {...register('yearsOfExperience', { min: 0 })} />
          </FormField>
        </div>
        <FormField label="Skills" hint="Comma-separated, e.g. JAVA, REACT, TESTING">
          <input className="form-control" {...register('skills')} />
        </FormField>
      </form>
    </Modal>
  );
}
