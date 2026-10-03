import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Brain, Eye, EyeOff, CheckCircle2 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { Spinner } from '../components/Spinner';
import './Auth.css';

export default function Login() {
  const { login, homeRoute, wakingUp } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const validate = () => {
    const next = {};
    if (!form.email.trim()) next.email = 'Email is required';
    else if (!/^\S+@\S+\.\S+$/.test(form.email)) next.email = 'Enter a valid email';
    if (!form.password) next.password = 'Password is required';
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    setSubmitting(true);
    try {
      const user = await login(form.email, form.password);
      toast.success(`Welcome back, ${user.name.split(' ')[0]}!`);
      const dest = location.state?.from || homeRoute();
      navigate(dest, { replace: true });
    } catch (err) {
      toast.error(err.friendlyMessage || 'Login failed. Check your credentials.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-visual">
        <div className="auth-blob" style={{ width: 420, height: 420, top: -100, right: -100, background: 'rgba(255,255,255,0.25)' }} />
        <div className="auth-blob" style={{ width: 300, height: 300, bottom: -80, left: -60, background: 'rgba(255,255,255,0.15)' }} />
        <div className="auth-visual-content">
          <div className="brand-mark">
            <Brain size={24} />
          </div>
          <h2>Welcome back to TalentIQ AI</h2>
          <p>Sign in to access your dashboard, resume insights, and career recommendations.</p>
          <ul className="auth-visual-list">
            <li><CheckCircle2 size={18} /> AI-powered ATS scoring</li>
            <li><CheckCircle2 size={18} /> Personalized job matching</li>
            <li><CheckCircle2 size={18} /> Career growth recommendations</li>
          </ul>
        </div>
      </div>
      <div className="auth-form-side">
        <div className="auth-form-card glass fade-in-up">
          <h1>Sign in</h1>
          <p className="subtitle text-secondary">Enter your credentials to continue</p>
          <form onSubmit={handleSubmit} noValidate>
            <div className="field">
              <label htmlFor="email">Email address</label>
              <input
                id="email"
                type="email"
                className={`input ${errors.email ? 'input-error' : ''}`}
                placeholder="you@company.com"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                autoComplete="email"
              />
              {errors.email && <div className="field-error">{errors.email}</div>}
            </div>
            <div className="field">
              <label htmlFor="password">Password</label>
              <div className="password-field-wrap">
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  className={`input ${errors.password ? 'input-error' : ''}`}
                  placeholder="••••••••"
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword((v) => !v)}
                  tabIndex={-1}
                >
                  {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                </button>
              </div>
              {errors.password && <div className="field-error">{errors.password}</div>}
            </div>
            <button type="submit" className="btn btn-primary btn-block btn-lg" disabled={submitting}>
              {submitting ? <Spinner size={18} /> : 'Sign in'}
            </button>
            {submitting && wakingUp && (
              <p className="subtitle text-secondary" style={{ marginTop: 12, textAlign: 'center' }}>
                Waking up the server — this can take up to a minute on first sign-in.
              </p>
            )}
          </form>
        </div>
      </div>
    </div>
  );
}
