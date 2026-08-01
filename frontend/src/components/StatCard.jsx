import { TrendingUp, TrendingDown } from 'lucide-react';
import GlassCard from './GlassCard';

export default function StatCard({ icon: Icon, value, label, trend, className = '' }) {
  return (
    <GlassCard className={`stat-card ${className}`}>
      {Icon && (
        <div className="stat-card-icon">
          <Icon size={22} />
        </div>
      )}
      <div className="stat-card-value">{value}</div>
      <div className="stat-card-label">{label}</div>
      {trend !== undefined && trend !== null && (
        <span className={`stat-card-trend ${trend >= 0 ? 'up' : 'down'}`}>
          {trend >= 0 ? <TrendingUp size={13} /> : <TrendingDown size={13} />}
          {Math.abs(trend)}%
        </span>
      )}
    </GlassCard>
  );
}
