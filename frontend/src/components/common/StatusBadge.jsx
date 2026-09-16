import { STATUS_TONE, humanize } from '../../constants/enums';

export default function StatusBadge({ status, label }) {
  const tone = STATUS_TONE[status] || 'neutral';
  return <span className={`badge badge-${tone}`}>{label || humanize(status)}</span>;
}
