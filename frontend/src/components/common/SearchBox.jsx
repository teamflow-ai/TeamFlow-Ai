import { Search } from 'lucide-react';

export default function SearchBox({ value, onChange, placeholder = 'Search…' }) {
  return (
    <div className="input-with-icon" style={{ minWidth: 240 }}>
      <Search size={16} />
      <input
        className="form-control"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
      />
    </div>
  );
}
