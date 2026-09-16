export default function Select({ value, onChange, options, placeholder = 'All', style }) {
  return (
    <select className="form-select" value={value} onChange={(e) => onChange(e.target.value)} style={style}>
      <option value="ALL">{placeholder}</option>
      {options.map((opt) => (
        <option key={opt.value ?? opt} value={opt.value ?? opt}>
          {opt.label ?? opt}
        </option>
      ))}
    </select>
  );
}
