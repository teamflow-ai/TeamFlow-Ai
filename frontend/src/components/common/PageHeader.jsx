import Breadcrumb from './Breadcrumb';

export default function PageHeader({ title, description, breadcrumb, actions }) {
  return (
    <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 16, marginBottom: 28, flexWrap: 'wrap' }}>
      <div>
        {breadcrumb && <Breadcrumb items={breadcrumb} />}
        <h1 style={{ fontSize: '1.5rem', marginTop: 4 }}>{title}</h1>
        {description && <p className="text-muted-c" style={{ marginTop: 6, fontSize: '0.9rem', maxWidth: 560 }}>{description}</p>}
      </div>
      {actions && <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>{actions}</div>}
    </div>
  );
}
