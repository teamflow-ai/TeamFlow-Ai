import { ChevronLeft, ChevronRight } from 'lucide-react';

export default function Pagination({ page, totalPages, totalElements, size, onChange }) {
  const from = totalElements === 0 ? 0 : page * size + 1;
  const to = Math.min((page + 1) * size, totalElements);

  return (
    <div className="pagination-bar">
      <span className="text-muted-c" style={{ fontSize: '0.8125rem' }}>
        Showing <strong style={{ color: 'var(--color-text)' }}>{from}–{to}</strong> of {totalElements}
      </span>
      <div style={{ display: 'flex', gap: 8 }}>
        <button className="btn btn-secondary btn-sm" disabled={page <= 0} onClick={() => onChange(page - 1)}>
          <ChevronLeft size={15} /> Prev
        </button>
        <button className="btn btn-secondary btn-sm" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>
          Next <ChevronRight size={15} />
        </button>
      </div>
    </div>
  );
}
