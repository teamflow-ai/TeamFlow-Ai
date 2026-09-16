import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Calendar, IndianRupee, FolderKanban } from 'lucide-react';
import { motion } from 'framer-motion';
import PageHeader from '../../components/common/PageHeader';
import SearchBox from '../../components/common/SearchBox';
import Select from '../../components/common/Select';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import ProjectFormModal from './ProjectFormModal';
import { useAsyncList } from '../../hooks/useAsyncList';
import { useDebounce } from '../../hooks/useDebounce';
import { useDisclosure } from '../../hooks/useDisclosure';
import { useAuth } from '../../hooks/useAuth';
import { projectService } from '../../services/project.service';
import { PROJECT_STATUS, PERMISSIONS } from '../../constants/enums';

const inr = (n) => `₹${((n || 0) / 100000).toFixed(1)}L`;

export default function ProjectList() {
  const navigate = useNavigate();
  const { hasPermission } = useAuth();
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('ALL');
  const debouncedSearch = useDebounce(search);
  const formModal = useDisclosure(false);

  const { content, loading, reload } = useAsyncList(projectService, {
    size: 50,
    search: debouncedSearch,
    filters: { status: status === 'ALL' ? undefined : status },
  });

  return (
    <div>
      <PageHeader
        title="Projects"
        description="Every engagement TeamFlow AI is delivering, for clients and internally."
        breadcrumb={[{ label: 'Delivery' }, { label: 'Projects' }]}
        actions={hasPermission(PERMISSIONS.CREATE_PROJECT) && (
          <button className="btn btn-primary" onClick={formModal.open}><Plus size={16} /> New project</button>
        )}
      />

      <div style={{ display: 'flex', gap: 12, marginBottom: 18, flexWrap: 'wrap' }}>
        <SearchBox value={search} onChange={setSearch} placeholder="Search projects…" />
        <Select value={status} onChange={setStatus} placeholder="All statuses" options={PROJECT_STATUS} style={{ minWidth: 170 }} />
      </div>

      {loading && <LoadingSpinner size="lg" label="Loading projects…" />}
      {!loading && content.length === 0 && <EmptyState icon={FolderKanban} title="No projects found" description="Start a new project to see it here." />}

      {!loading && content.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 18 }}>
          {content.map((p, i) => (
            <motion.div
              key={p.id}
              className="card card-hover"
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.04 }}
              onClick={() => navigate(`/projects/${p.id}`)}
              style={{ cursor: 'pointer' }}
            >
              <div className="card-body">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div className="text-faint-c" style={{ fontSize: '0.75rem', fontWeight: 600 }}>{p.code}</div>
                    <h3 style={{ fontSize: '1.05rem', marginTop: 2 }}>{p.name}</h3>
                  </div>
                  <StatusBadge status={p.status} />
                </div>
                <p className="text-muted-c" style={{ fontSize: '0.8125rem', marginTop: 10, minHeight: 36 }}>{p.description}</p>

                <div style={{ marginTop: 12 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', marginBottom: 6 }}>
                    <span className="text-muted-c">Progress</span>
                    <span style={{ fontWeight: 600 }}>{p.progressPercent}%</span>
                  </div>
                  <div style={{ height: 6, borderRadius: 999, background: 'var(--color-bg)', overflow: 'hidden' }}>
                    <div style={{ height: '100%', width: `${p.progressPercent}%`, background: 'var(--color-primary)', borderRadius: 999 }} />
                  </div>
                </div>

                <hr className="divider" />
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8125rem' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: 6 }} className="text-muted-c"><Calendar size={14} /> {p.endDate}</span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: 6 }} className="text-muted-c"><IndianRupee size={14} /> {inr(p.budget)}</span>
                </div>
                <div className="text-faint-c" style={{ fontSize: '0.75rem', marginTop: 8 }}>{p.clientName || 'Internal'} · Managed by {p.managerName}</div>
              </div>
            </motion.div>
          ))}
        </div>
      )}

      <ProjectFormModal isOpen={formModal.isOpen} onClose={formModal.close} onSaved={reload} project={null} />
    </div>
  );
}
