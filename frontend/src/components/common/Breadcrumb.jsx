import { Link } from 'react-router-dom';
import { ChevronRight } from 'lucide-react';

export default function Breadcrumb({ items = [] }) {
  return (
    <nav aria-label="Breadcrumb" style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: '0.8125rem', marginBottom: 4, flexWrap: 'wrap' }}>
      {items.map((item, i) => {
        const isLast = i === items.length - 1;
        return (
          <span key={i} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            {item.to && !isLast ? (
              <Link to={item.to} className="text-muted-c" style={{ color: 'var(--color-text-muted)' }}>
                {item.label}
              </Link>
            ) : (
              <span style={{ color: isLast ? 'var(--color-text)' : 'var(--color-text-muted)', fontWeight: isLast ? 600 : 400 }}>
                {item.label}
              </span>
            )}
            {!isLast && <ChevronRight size={13} color="var(--color-text-faint)" />}
          </span>
        );
      })}
    </nav>
  );
}
