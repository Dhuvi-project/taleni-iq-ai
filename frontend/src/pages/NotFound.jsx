import { Link } from 'react-router-dom';
import { Compass } from 'lucide-react';

export default function NotFound() {
  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', textAlign: 'center', padding: 24 }}>
      <Compass size={56} className="text-gradient" style={{ marginBottom: 20 }} />
      <h1 style={{ fontSize: 30 }}>Page not found</h1>
      <p className="text-secondary" style={{ marginBottom: 20 }}>The page you're looking for doesn't exist.</p>
      <Link to="/" className="btn btn-primary">Back to Home</Link>
    </div>
  );
}
