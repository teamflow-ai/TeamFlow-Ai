import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { FolderKanban, ListChecks, Briefcase, CalendarClock, Users2, SearchX } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import EmptyState from '../../components/common/EmptyState';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { searchService } from '../../services/search.service';

const SECTIONS = [
  { key: 'projects', label: 'Projects', icon: FolderKanban, to: (id) => `/projects/${id}` },
  { key: 'tasks', label: 'Tasks', icon: ListChecks, to: (id) => `/tasks/${id}` },
  { key: 'clients', label: 'Clients', icon: Briefcase, to: () => '/clients' },
  { key: 'meetings', label: 'Meetings', icon: CalendarClock, to: () => '/meetings' },
  { key: 'employees', label: 'Employees', icon: Users2, to: (id) => `/employees/${id}` },
];

export default function SearchResults() {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('q') || '';
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!query.trim()) { setResults(null); return; }
    setLoading(true);
    searchService.search(query).then(setResults).finally(() => setLoading(false));
  }, [query]);

  const totalHits = results ? SECTIONS.reduce((sum, s) => sum + (results[s.key]?.length || 0), 0) : 0;

  return (
    <div>
      <PageHeader
        title="Search results"
        description={query ? `Top matches for "${query}"` : 'Search projects, tasks, clients, meetings and employees.'}
        breadcrumb={[{ label: 'Search' }]}
      />

      {loading && <LoadingSpinner label="Searching…" />}
      {!loading && !query && <EmptyState icon={SearchX} title="Start typing" description="Use the search bar at the top of the page." />}
      {!loading && query && totalHits === 0 && <EmptyState icon={SearchX} title="No matches" description={`Nothing found for "${query}".`} />}

      {!loading && results && totalHits > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          {SECTIONS.filter((s) => results[s.key]?.length > 0).map((s) => (
            <div key={s.key} className="card">
              <div className="card-header"><h3 style={{ fontSize: '1rem', display: 'flex', alignItems: 'center', gap: 8 }}><s.icon size={16} /> {s.label}</h3></div>
              <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                {results[s.key].map((item) => (
                  <Link key={item.id} to={s.to(item.id)} style={{ display: 'flex', flexDirection: 'column', padding: '10px 0', borderBottom: '1px solid var(--color-border)', color: 'inherit' }}>
                    <span style={{ fontWeight: 600, fontSize: '0.875rem' }}>{item.title}</span>
                    {item.subtitle && <span className="text-faint-c" style={{ fontSize: '0.75rem', marginTop: 2 }}>{item.subtitle}</span>}
                  </Link>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
