export default function GlassCard({ children, className = '', style, ...rest }) {
  return (
    <div className={`glass glass-card ${className}`} style={style} {...rest}>
      {children}
    </div>
  );
}
