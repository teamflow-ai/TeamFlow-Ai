import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import Modal from '../../components/common/Modal';
import FormField from '../../components/common/FormField';
import { leaveService } from '../../services/leave.service';
import { LEAVE_TYPE } from '../../constants/enums';

const emptyValues = { leaveType: 'CASUAL', startDate: '', endDate: '', reason: '' };

export default function LeaveFormModal({ isOpen, onClose, onSaved }) {
  const { register, handleSubmit, reset, watch, formState: { errors, isSubmitting } } = useForm({ defaultValues: emptyValues });
  const startDateValue = watch('startDate');
  useEffect(() => { if (isOpen) reset(emptyValues); }, [isOpen, reset]);

  const onSubmit = async (values) => {
    try {
      await leaveService.create(values);
      toast.success('Leave request submitted');
      onSaved();
      onClose();
    } catch (err) { toast.error(err.message || 'Could not submit request'); }
  };

  const today = new Date().toISOString().slice(0, 10);

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Request leave"
      footer={<>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>{isSubmitting ? 'Submitting…' : 'Submit request'}</button>
      </>}>
      <form onSubmit={handleSubmit(onSubmit)}>
        <FormField label="Leave type">
          <select className="form-select" {...register('leaveType')}>
            {LEAVE_TYPE.map((t) => <option key={t} value={t}>{t}</option>)}
          </select>
        </FormField>
        <div className="form-row">
          <FormField label="Start date" required error={errors.startDate?.message}>
            <input type="date" min={today} className="form-control" {...register('startDate', { required: 'Required' })} />
          </FormField>
          <FormField label="End date" required error={errors.endDate?.message}>
            <input
              type="date"
              min={startDateValue || today}
              className="form-control"
              {...register('endDate', {
                required: 'Required',
                validate: (v, formValues) => !formValues.startDate || v >= formValues.startDate || 'End date must be on/after start date',
              })}
            />
          </FormField>
        </div>
        <FormField label="Reason" required error={errors.reason?.message}>
          <textarea className="form-control" rows={3} {...register('reason', { required: 'Required' })} />
        </FormField>
      </form>
    </Modal>
  );
}
