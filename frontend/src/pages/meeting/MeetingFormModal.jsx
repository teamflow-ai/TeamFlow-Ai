import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import Modal from '../../components/common/Modal';
import FormField from '../../components/common/FormField';
import { meetingService } from '../../services/meeting.service';

const emptyValues = { title: '', agenda: '', scheduledAt: '', durationMinutes: 30 };

export default function MeetingFormModal({ isOpen, onClose, onSaved, meeting, defaultProjectId }) {
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({ defaultValues: emptyValues });
  useEffect(() => {
    if (isOpen) reset(meeting ? { title: meeting.title, agenda: meeting.agenda, scheduledAt: meeting.scheduledAt?.slice(0, 16), durationMinutes: meeting.durationMinutes } : emptyValues);
  }, [isOpen, meeting, reset]);

  const onSubmit = async (values) => {
    const payload = {
      title: values.title,
      agenda: values.agenda,
      scheduledAt: new Date(values.scheduledAt).toISOString(),
      durationMinutes: Number(values.durationMinutes),
    };
    try {
      if (meeting) {
        await meetingService.update(meeting.id, payload);
        toast.success('Meeting updated');
      } else {
        await meetingService.create({ ...payload, projectId: defaultProjectId, participantIds: [] });
        toast.success('Meeting scheduled');
      }
      onSaved();
      onClose();
    } catch (err) { toast.error(err.message || 'Could not save meeting'); }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={meeting ? 'Edit meeting' : 'Schedule meeting'}
      footer={<>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>{isSubmitting ? 'Saving…' : 'Save'}</button>
      </>}>
      <form onSubmit={handleSubmit(onSubmit)}>
        <FormField label="Title" required error={errors.title?.message}>
          <input className="form-control" {...register('title', { required: 'Required' })} />
        </FormField>
        <FormField label="Agenda">
          <textarea className="form-control" rows={2} {...register('agenda')} />
        </FormField>
        <div className="form-row">
          <FormField label="Date & time" required error={errors.scheduledAt?.message}>
            <input type="datetime-local" className="form-control" {...register('scheduledAt', { required: 'Required' })} />
          </FormField>
          <FormField label="Duration (min)">
            <input type="number" min={5} max={480} className="form-control" {...register('durationMinutes', { min: 5, max: 480 })} />
          </FormField>
        </div>
      </form>
    </Modal>
  );
}
