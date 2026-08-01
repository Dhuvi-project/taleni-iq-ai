export default function SkillBar({ label, value = 0, max = 100, rightLabel }) {
  const pct = Math.max(0, Math.min(100, (value / max) * 100));
  return (
    <div className="skill-bar">
      <div className="skill-bar-head">
        <span>{label}</span>
        <span className="text-muted">{rightLabel !== undefined ? rightLabel : `${Math.round(pct)}%`}</span>
      </div>
      <div className="skill-bar-track">
        <div className="skill-bar-fill" style={{ width: `${pct}%` }} />
      </div>
    </div>
  );
}
