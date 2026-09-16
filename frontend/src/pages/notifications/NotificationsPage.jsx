import { useEffect, useState } from 'react';
import { CheckCheck, Bell, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import PageHeader from '../../components/common/PageHeader';
import EmptyState from '../../components/common/EmptyState';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { notificationService } from '../../services/notification.service';

const timeAgo = (iso) => {
  const diffMs = Date.now() - new Date(iso).getTime();
  const hrs = Math.floor(diffMs / 3600000);
  if (hrs < 1) return 'just now';
  if (hrs < 24) return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
};

export default function NotificationsPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      const res = await notificationService.list({ size: 50 });
      setItems(res.content);
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => { load(); }, []);

  const markAllRead = async () => { await notificationService.markAllRead(); load(); };
  const remove = async (id) => { await notificationService.remove(id); load(); };
  const markRead = async (id) => { await notificationService.markRead(id); load(); };

  if (loading) return <LoadingSpinner size="lg" label="Loading notifications…" />;

  return (
    <div>
      <PageHeader
        title="Notifications"
        description="Everything TeamFlow AI wants you to know about."
        breadcrumb={[{ label: 'Workspace' }, { label: 'Notifications' }]}
        actions={items.some((n) => !n.read) && <button className="btn btn-secondary" onClick={markAllRead}><CheckCheck size={15} /> Mark all read</button>}
      />
      {items.length === 0 ? (
        <EmptyState icon={Bell} title="You're all caught up" description="New notifications will show up here." />
      ) : (
        <div className="card">
          {items.map((n, i) => (
            <div
              key={n.id}
              style={{ display: 'flex', gap: 14, padding: '16px 20px', borderBottom: i === items.length - 1 ? 'none' : '1px solid var(--color-border)', background: n.read ? 'transparent' : 'var(--color-primary-light)' }}
            >
              <div style={{ width: 36, height: 36, borderRadius: 10, background: 'var(--color-surface)', border: '1px solid var(--color-border)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                <Bell size={16} color="var(--color-primary)" />
              </div>
              <div style={{ flex: 1 }}>
                <Link to={n.targetUrl || '#'} onClick={() => markRead(n.id)} style={{ color: 'inherit' }}>
                  <div style={{ fontWeight: 600, fontSize: '0.875rem' }}>{n.title}</div>
                  <div style={{ fontSize: '0.8125rem', color: 'var(--color-text-muted)', marginTop: 2 }}>{n.body}</div>
                </Link>
                <div style={{ fontSize: '0.6875rem', color: 'var(--color-text-faint)', marginTop: 6 }}>{timeAgo(n.createdAt)} · {n.category}</div>
              </div>
              <button className="btn btn-icon btn-ghost" onClick={() => remove(n.id)} aria-label="Delete notification">
                <Trash2 size={15} />
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
