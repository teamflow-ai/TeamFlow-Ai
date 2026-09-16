import { motion } from 'framer-motion';

const toneMap = {
  primary: { bg: 'var(--color-primary-light)', fg: 'var(--color-primary-dark)' },
  accent: { bg: 'var(--color-accent-light)', fg: '#047857' },
  warning: { bg: 'var(--color-warning-light)', fg: '#b45309' },
  danger: { bg: 'var(--color-danger-light)', fg: '#b91c1c' },
  info: { bg: 'var(--color-info-light)', fg: '#0369a1' },
};

export default function StatCard({ icon: Icon, label, value, tone = 'primary', trend, delay = 0 }) {
  const colors = toneMap[tone] || toneMap.primary;
  return (
    <motion.div
      className="card"
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3, delay }}
    >
      <div className="card-body" style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
        <div>
          <div className="text-muted-c" style={{ fontSize: '0.8125rem', fontWeight: 600, marginBottom: 8 }}>{label}</div>
          <div style={{ fontFamily: 'var(--font-display)', fontSize: '1.75rem', fontWeight: 700 }}>{value}</div>
          {trend && <div style={{ fontSize: '0.75rem', color: 'var(--color-text-faint)', marginTop: 6 }}>{trend}</div>}
        </div>
        <div style={{ width: 44, height: 44, borderRadius: 12, background: colors.bg, color: colors.fg, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
          <Icon size={20} />
        </div>
      </div>
    </motion.div>
  );
}
