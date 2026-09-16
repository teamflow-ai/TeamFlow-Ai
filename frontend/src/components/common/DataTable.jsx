import { TableSkeleton } from './Skeleton';
import EmptyState from './EmptyState';
import Pagination from './Pagination';

/**
 * Generic list table used by every module: pass columns + data, get sorting-
 * free but consistent header/row/empty/loading/pagination behavior for free.
 *
 * columns: [{ key, header, render?(row) }]
 */
export default function DataTable({
  columns,
  rows,
  loading,
  emptyTitle = 'No records found',
  emptyDescription = 'Try adjusting your search or filters.',
  onRowClick,
  pagination,
}) {
  const showEmpty = !loading && rows.length === 0;

  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            {columns.map((c) => (
              <th key={c.key} style={c.width ? { width: c.width } : undefined}>
                {c.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading && <TableSkeleton rows={6} cols={columns.length} />}
          {!loading &&
            rows.map((row) => (
              <tr
                key={row.id}
                onClick={() => onRowClick && onRowClick(row)}
                style={onRowClick ? { cursor: 'pointer' } : undefined}
              >
                {columns.map((c) => (
                  <td key={c.key}>{c.render ? c.render(row) : row[c.key]}</td>
                ))}
              </tr>
            ))}
        </tbody>
      </table>
      {showEmpty && <EmptyState title={emptyTitle} description={emptyDescription} />}
      {pagination && rows.length > 0 && <Pagination {...pagination} />}
    </div>
  );
}
