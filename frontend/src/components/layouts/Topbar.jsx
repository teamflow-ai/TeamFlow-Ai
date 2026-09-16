import { Menu, Search } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import NotificationDropdown from './NotificationDropdown';
import ProfileDropdown from './ProfileDropdown';

export default function Topbar({ onOpenMobile }) {
  const [q, setQ] = useState('');
  const navigate = useNavigate();

  const onSearch = (e) => {
    e.preventDefault();
    if (q.trim()) navigate(`/search?q=${encodeURIComponent(q.trim())}`);
  };

  return (
    <header className="topbar">
      <button className="icon-btn topbar-menu-btn" onClick={onOpenMobile} aria-label="Open menu">
        <Menu size={18} />
      </button>

      <form className="topbar-search input-with-icon" onSubmit={onSearch}>
        <Search size={16} />
        <input
          className="form-control"
          placeholder="Search projects, tasks, clients, meetings, people…"
          value={q}
          onChange={(e) => setQ(e.target.value)}
        />
      </form>

      <div className="topbar-actions">
        <NotificationDropdown />
        <ProfileDropdown />
      </div>
    </header>
  );
}
