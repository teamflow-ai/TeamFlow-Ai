import { NavLink } from 'react-router-dom';
import { Sparkles, X } from 'lucide-react';
import { NAV_SECTIONS } from './navConfig';
import { useAuth } from '../../hooks/useAuth';
import { APP_NAME } from '../../constants/app';

export default function Sidebar({ mobileOpen, onCloseMobile }) {
  const { user, hasPermission, hasAnyPermission } = useAuth();

  const isVisible = (item) => {
    // If the item specifically excludes roles, or we need to hardcode CLIENT restrictions
    if (user?.role === 'CLIENT' && !['Dashboard', 'Projects', 'Reports'].includes(item.label)) return false;
    
    if (item.anyPermission) return hasAnyPermission(item.anyPermission);
    return hasPermission(item.permission);
  };

  return (
    <>
      <aside className={`sidebar ${mobileOpen ? 'sidebar-mobile-open' : ''}`}>
        <div className="sidebar-brand">
          <div className="sidebar-logo">
            <Sparkles size={18} />
          </div>
          <span className="sidebar-brand-text">{APP_NAME}</span>
          <button className="btn btn-icon btn-ghost sidebar-close-btn" onClick={onCloseMobile} aria-label="Close menu">
            <X size={18} />
          </button>
        </div>

        <nav className="sidebar-nav">
          {NAV_SECTIONS.map((section) => {
            const visibleItems = section.items.filter(isVisible);
            if (visibleItems.length === 0) return null;
            return (
              <div className="sidebar-section" key={section.label}>
                <div className="sidebar-section-label">{section.label}</div>
                {visibleItems.map((item) => (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    onClick={onCloseMobile}
                    className={({ isActive }) => `sidebar-link ${isActive ? 'sidebar-link-active' : ''}`}
                  >
                    <item.icon size={17} strokeWidth={2.1} />
                    <span>{item.label}</span>
                  </NavLink>
                ))}
              </div>
            );
          })}
        </nav>

        <div className="sidebar-footer-note">
          <div className="sidebar-ai-chip">
            <Sparkles size={13} />
            Smart Assignment active
          </div>
        </div>
      </aside>
      {mobileOpen && <div className="sidebar-backdrop" onClick={onCloseMobile} />}
    </>
  );
}
