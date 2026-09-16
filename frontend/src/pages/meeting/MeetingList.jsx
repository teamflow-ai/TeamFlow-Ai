import { useEffect, useState } from 'react';
import { Plus, CalendarClock } from 'lucide-react';
import { motion } from 'framer-motion';
import PageHeader from '../../components/common/PageHeader';
import Select from '../../components/common/Select';
import StatusBadge from '../../components/common/StatusBadge';
import EmptyState from '../../components/common/EmptyState';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import MeetingFormModal from './MeetingFormModal';
import { useAsyncList } from '../../hooks/useAsyncList';
import { useDisclosure } from '../../hooks/useDisclosure';
import { meetingService } from '../../services/meeting.service';
import { projectService } from '../../services/project.service';

export default function MeetingList() {
  const formModal = useDisclosure(false);
  const [editing, setEditing] = useState(null);
  const [projectId, setProjectId] = useState('');
  const [projects, setProjects] = useState([]);

  useEffect(() => {
    projectService.list({ size: 100 }).then((res) => {
      setProjects(res.content);
      if (res.content.length > 0) setProjectId(res.content[0].id);
    }).catch(() => {});
  }, []);

  const { content, loading, reload } = useAsyncList(meetingService, {
    size: 50,
    filters: { projectId: projectId || undefined },
  });

  return (
    <div>
      <PageHeader
        title="Meetings"
        description="Scheduled meetings for a project."
        breadcrumb={[{ label: 'Delivery' }, { label: 'Meetings' }]}
        actions={<button className="btn btn-primary" disabled={!projectId} onClick={() => { setEditing(null); formModal.open(); }}><Plus size={16} /> Schedule meeting</button>}
      />
      <div style={{ marginBottom: 18 }}>
        <Select
          value={projectId || 'ALL'}
          onChange={(v) => setProjectId(v === 'ALL' ? '' : v)}
          placeholder="Choose a project"
          options={projects.map((p) => ({ value: p.id, label: p.name }))}
          style={{ minWidth: 220 }}
        />
      </div>
      {!projectId && <EmptyState title="Choose a project" description="Select a project above to see its meetings." />}
      {projectId && loading && <LoadingSpinner size="lg" label="Loading meetings…" />}
      {projectId && !loading && content.length === 0 && <EmptyState icon={CalendarClock} title="No meetings scheduled" description="Schedule the first meeting for this project." />}
      {projectId && !loading && content.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {content.map((m, i) => (
            <motion.div key={m.id} className="card" initial={{ opacity: 0, y: 6 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.03 }}>
              <div className="card-body" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
                <div style={{ display: 'flex', gap: 16, alignItems: 'center' }}>
                  <div style={{ textAlign: 'center', minWidth: 56 }}>
                    <div style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '1.1rem', color: 'var(--color-primary)' }}>
                      {new Date(m.scheduledAt).getDate()}
                    </div>
                    <div className="text-faint-c" style={{ fontSize: '0.6875rem', textTransform: 'uppercase' }}>
                      {new Date(m.scheduledAt).toLocaleString('en-IN', { month: 'short' })}
                    </div>
                  </div>
                  <div>
                    <div style={{ fontWeight: 600 }}>{m.title}</div>
                    <div className="text-faint-c" style={{ fontSize: '0.75rem', marginTop: 2 }}>
                      {new Date(m.scheduledAt).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })} · {m.durationMinutes} min
                    </div>
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                  <StatusBadge status={m.status} />
                  <button className="btn btn-ghost btn-sm" onClick={() => { setEditing(m); formModal.open(); }}>Edit</button>
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      )}
      <MeetingFormModal isOpen={formModal.isOpen} onClose={formModal.close} onSaved={reload} meeting={editing} defaultProjectId={projectId} />
    </div>
  );
}
