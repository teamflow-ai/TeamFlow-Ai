import { useEffect, useRef, useState } from 'react';
import { Bell, CheckCheck } from 'lucide-react';
import { AnimatePresence, motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { notificationService } from '../../services/notification.service';
import { useDisclosure } from '../../hooks/useDisclosure';

const timeAgo = (iso) => {
  const diffMs = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diffMs / 60000);
  if (mins < 60) return `${Math.max(mins, 1)}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
};

export default function NotificationDropdown() {
  const { isOpen, toggle, close } = useDisclosure(false);
  const [items, setItems] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const ref = useRef(null);
  const navigate = useNavigate();

  const load = async () => {
    try {
      const [list, count] = await Promise.all([
        notificationService.list({ size: 8 }),
        notificationService.unreadCount(),
      ]);
      setItems(list.content);
      setUnreadCount(count);
    } catch {
      // notifications are non-critical to the rest of the app
    }
  };

  useEffect(() => {
    load();
    const interval = setInterval(load, 60000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    const onClick = (e) => ref.current && !ref.current.contains(e.target) && close();
    document.addEventListener('mousedown', onClick);
    return () => document.removeEventListener('mousedown', onClick);
  }, [close]);

  const markAllRead = async (e) => {
    e.stopPropagation();
    await notificationService.markAllRead();
    load();
  };

  const openNotification = async (n) => {
    if (!n.read) await notificationService.markRead(n.id).catch(() => {});
    close();
    load();
    if (n.targetUrl) navigate(n.targetUrl);
  };

  return (
    <div style={{ position: 'relative' }} ref={ref}>
      <button className="icon-btn" onClick={toggle} aria-label="Notifications">
        <Bell size={17} />
        {unreadCount > 0 && <span className="dot" />}
      </button>
      <AnimatePresence>
        {isOpen && (
          <motion.div
            className="dropdown-panel"
            initial={{ opacity: 0, y: -6 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -6 }}
            transition={{ duration: 0.15 }}
          >
            <div className="dropdown-head">
              <strong style={{ fontSize: '0.875rem' }}>Notifications</strong>
              {unreadCount > 0 && (
                <button className="btn btn-ghost btn-sm" onClick={markAllRead} style={{ padding: '4px 8px' }}>
                  <CheckCheck size={13} /> Mark all read
                </button>
              )}
            </div>
            <div style={{ maxHeight: 340, overflowY: 'auto' }}>
              {items.length === 0 && <div className="dropdown-empty">You&apos;re all caught up.</div>}
              {items.map((n) => (
                <div key={n.id} className={`dropdown-item ${!n.read ? 'unread' : ''}`} onClick={() => openNotification(n)} style={{ cursor: 'pointer' }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: '0.8125rem', fontWeight: 600 }}>{n.title}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: 2 }}>{n.body}</div>
                    <div style={{ fontSize: '0.6875rem', color: 'var(--color-text-faint)', marginTop: 4 }}>{timeAgo(n.createdAt)}</div>
                  </div>
                </div>
              ))}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
