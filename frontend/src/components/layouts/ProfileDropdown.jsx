import { useEffect, useRef } from 'react';
import { ChevronDown, LogOut, Settings, UserRound } from 'lucide-react';
import { AnimatePresence, motion } from 'framer-motion';
import { Link, useNavigate } from 'react-router-dom';
import Avatar from '../common/Avatar';
import { useAuth } from '../../hooks/useAuth';
import { useDisclosure } from '../../hooks/useDisclosure';
import { humanize } from '../../constants/enums';

export default function ProfileDropdown() {
  const { user, logout } = useAuth();
  const { isOpen, toggle, close } = useDisclosure(false);
  const ref = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    const onClick = (e) => ref.current && !ref.current.contains(e.target) && close();
    document.addEventListener('mousedown', onClick);
    return () => document.removeEventListener('mousedown', onClick);
  }, [close]);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div style={{ position: 'relative' }} ref={ref}>
      <button className="profile-trigger" onClick={toggle}>
        <Avatar name={user?.fullName} size="sm" imageUrl={user?.profileImageUrl} />
        <div className="profile-meta">
          <div className="name">{user?.fullName}</div>
          <div className="role">{humanize(user?.role)}</div>
        </div>
        <ChevronDown size={14} color="var(--color-text-faint)" />
      </button>
      <AnimatePresence>
        {isOpen && (
          <motion.div
            className="dropdown-panel"
            style={{ width: 220 }}
            initial={{ opacity: 0, y: -6 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -6 }}
            transition={{ duration: 0.15 }}
          >
            <Link to="/profile" onClick={close} className="dropdown-item" style={{ color: 'var(--color-text)' }}>
              <UserRound size={16} /> My profile
            </Link>
            <Link to="/settings" onClick={close} className="dropdown-item" style={{ color: 'var(--color-text)' }}>
              <Settings size={16} /> Settings
            </Link>
            <div className="dropdown-item" onClick={handleLogout} style={{ color: 'var(--color-danger)' }}>
              <LogOut size={16} /> Log out
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
