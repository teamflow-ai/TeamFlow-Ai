import { Outlet } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Sparkles, ShieldCheck, Gauge, Users } from 'lucide-react';
import { APP_NAME } from '../../constants/app';

const highlights = [
  { icon: Gauge, text: 'Deterministic, explainable workload scoring for every assignment' },
  { icon: Users, text: 'One workforce graph — employees, teams, departments in sync' },
  { icon: ShieldCheck, text: 'Role & permission based access on every request' },
];

export default function AuthLayout() {
  return (
    <div className="auth-shell">
      <div className="auth-visual">
        <div className="auth-visual-inner">
          <div className="sidebar-logo" style={{ width: 44, height: 44, borderRadius: 14 }}>
            <Sparkles size={22} />
          </div>
          <h1 className="auth-visual-title">{APP_NAME}</h1>
          <p className="auth-visual-sub">
            Enterprise project &amp; workforce management with an intelligent
            workload engine that tells you exactly who should take the next task, and why.
          </p>
          <ul className="auth-highlight-list">
            {highlights.map((h, i) => (
              <motion.li
                key={h.text}
                initial={{ opacity: 0, x: -8 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.1 * i, duration: 0.3 }}
              >
                <h.icon size={16} />
                <span>{h.text}</span>
              </motion.li>
            ))}
          </ul>
        </div>
      </div>
      <div className="auth-form-side">
        <Outlet />
      </div>
    </div>
  );
}
