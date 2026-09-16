import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import Modal from '../../components/common/Modal';
import FormField from '../../components/common/FormField';
import { milestoneService } from '../../services/milestone.service';

const emptyValues = { title: '', description: '', dueDate: '' };

export default function MilestoneFormModal({ isOpen, onClose, onSaved, milestone, defaultProjectId }) {
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({ defaultValues: emptyValues });

  useEffect(() => {
    if (isOpen) reset(milestone ? { title: milestone.title, description: milestone.description, dueDate: milestone.dueDate || '' } : emptyValues);
  }, [isOpen, milestone, reset]);

  const onSubmit = async (values) => {
    const payload = { title: values.title, description: values.description || null, dueDate: values.dueDate || null };
    try {
      if (milestone) {
        await milestoneService.update(milestone.id, payload);
        toast.success('Milestone updated');
      } else {
        await milestoneService.create({ ...payload, projectId: defaultProjectId });
        toast.success('Milestone created');
      }
      onSaved();
      onClose();
    } catch (err) { toast.error(err.message || 'Could not save milestone'); }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={milestone ? 'Edit milestone' : 'New milestone'}
      footer={<>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>{isSubmitting ? 'Saving…' : 'Save'}</button>
      </>}>
      <form onSubmit={handleSubmit(onSubmit)}>
        <FormField label="Title" required error={errors.title?.message}>
          <input className="form-control" {...register('title', { required: 'Required' })} />
        </FormField>
        <FormField label="Description">
          <textarea className="form-control" rows={3} {...register('description')} />
        </FormField>
        <FormField label="Due date">
          <input type="date" className="form-control" {...register('dueDate')} />
        </FormField>
      </form>
    </Modal>
  );
}
