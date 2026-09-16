import { Outlet } from 'react-router-dom';
import { useState } from 'react';
import Sidebar from './Sidebar';
import Topbar from './Topbar';

export default function MainLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <div className="app-shell">
      <Sidebar mobileOpen={mobileOpen} onCloseMobile={() => setMobileOpen(false)} />
      <div className="main-area">
        <Topbar onOpenMobile={() => setMobileOpen(true)} />
        <main className="page-content">
          <div className="container-page">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
