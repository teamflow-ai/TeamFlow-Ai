import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import Modal from '../../components/common/Modal';
import FormField from '../../components/common/FormField';
import { clientService } from '../../services/client.service';

const emptyValues = { name: '', code: '', contactPerson: '', email: '', phone: '', country: 'India', active: true };

export default function ClientFormModal({ isOpen, onClose, onSaved, client }) {
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({ defaultValues: emptyValues });
  useEffect(() => { if (isOpen) reset(client || emptyValues); }, [isOpen, client, reset]);

  const onSubmit = async (values) => {
    try {
      if (client) {
        await clientService.update(client.id, {
          name: values.name,
          contactPerson: values.contactPerson,
          email: values.email,
          phone: values.phone,
          country: values.country,
          active: client.active,
        });
        toast.success('Client updated');
      } else {
        await clientService.create(values);
        toast.success('Client added');
      }
      onSaved();
      onClose();
    } catch (err) { toast.error(err.message || 'Could not save client'); }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={client ? 'Edit client' : 'New client'}
      footer={<>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>{isSubmitting ? 'Saving…' : 'Save'}</button>
      </>}>
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="form-row">
          <FormField label="Client name" required error={errors.name?.message}>
            <input className="form-control" {...register('name', { required: 'Required' })} />
          </FormField>
          <FormField label="Code" required={!client} error={errors.code?.message} hint={client ? 'Code cannot be changed' : undefined}>
            <input className="form-control" style={{ textTransform: 'uppercase' }} disabled={!!client} {...register('code', { required: !client && 'Required' })} />
          </FormField>
        </div>
        <div className="form-row">
          <FormField label="Contact person">
            <input className="form-control" {...register('contactPerson')} />
          </FormField>
          <FormField label="Country">
            <input className="form-control" {...register('country')} />
          </FormField>
        </div>
        <div className="form-row">
          <FormField label="Email" error={errors.email?.message}>
            <input type="email" className="form-control" {...register('email', {
                pattern: { value: /^\S+@\S+\.\S+$/, message: 'Enter a valid email' }
            })} />
          </FormField>
          <FormField label="Phone" error={errors.phone?.message}>
            <input className="form-control" {...register('phone', {
                pattern: { value: /^\+?[1-9]\d{1,14}$/, message: 'Enter a valid phone number (e.g. +14155552671)' }
            })} />
          </FormField>
        </div>
      </form>
    </Modal>
  );
}
