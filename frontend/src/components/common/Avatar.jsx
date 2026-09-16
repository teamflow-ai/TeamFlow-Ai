const colors = ['#3b82f6', '#ef4444', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#06b6d4', '#f97316'];

const getColor = (name = '') => {
  if (!name) return '#94a3b8';
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return colors[Math.abs(hash) % colors.length];
};

const initialsOf = (name = '') =>
  name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0])
    .join('')
    .toUpperCase();

export default function Avatar({ name, size = 'md', imageUrl }) {
  const sizeClass = size === 'sm' ? 'avatar-sm' : size === 'lg' ? 'avatar-lg' : '';
  if (imageUrl) {
    return (
      <img
        src={imageUrl}
        alt={name}
        className={`avatar ${sizeClass}`}
        style={{ objectFit: 'cover' }}
      />
    );
  }
  
  const bgColor = getColor(name);
  
  return (
    <div 
      className={`avatar ${sizeClass}`} 
      style={{ backgroundColor: bgColor, color: '#fff', border: 'none' }}
    >
      {initialsOf(name)}
    </div>
  );
}
