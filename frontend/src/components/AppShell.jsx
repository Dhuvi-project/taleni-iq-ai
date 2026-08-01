import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  UploadCloud,
  FileSearch,
  Target,
  MessagesSquare,
  Compass,
  User,
  Search,
  Sun,
  Moon,
  LogOut,
  Sparkles,
  X,
  Send,
  Menu,
  Brain,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { sendAssistantMessage as postAssistantMessage } from '../api/assistant';

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/upload', label: 'Upload Resume', icon: UploadCloud },
  { to: '/matching', label: 'Job Matching', icon: Target },
  { to: '/interview', label: 'Interview Prep', icon: MessagesSquare },
  { to: '/skill-gap', label: 'Skill Gap', icon: FileSearch },
  { to: '/careers', label: 'Careers', icon: Compass },
  { to: '/profile', label: 'Profile', icon: User },
];

export default function AppShell({ children }) {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [assistantOpen, setAssistantOpen] = useState(false);
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const [search, setSearch] = useState('');
  const [assistantMessages, setAssistantMessages] = useState([
    { from: 'ai', text: "Hi! I'm your TalentIQ AI assistant. Ask me about resumes, ATS scores, or interview prep." },
  ]);
  const [assistantInput, setAssistantInput] = useState('');
  const [assistantLoading, setAssistantLoading] = useState(false);
  const menuRef = useRef(null);

  const navItems = NAV_ITEMS;

  useEffect(() => {
    const onClick = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setUserMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', onClick);
    return () => document.removeEventListener('mousedown', onClick);
  }, []);

  const initials = (user?.name || '?')
    .split(' ')
    .map((p) => p[0])
    .slice(0, 2)
    .join('')
    .toUpperCase();

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (search.trim()) {
      navigate(`/matching?q=${encodeURIComponent(search.trim())}`);
    }
  };

  const sendAssistantMessage = async () => {
    const text = assistantInput.trim();
    if (!text || assistantLoading) return;
    const history = assistantMessages;
    setAssistantMessages((prev) => [...prev, { from: 'user', text }]);
    setAssistantInput('');
    setAssistantLoading(true);
    try {
      const { reply } = await postAssistantMessage(
        text,
        history.map((m) => ({ role: m.from === 'ai' ? 'assistant' : 'user', text: m.text }))
      );
      setAssistantMessages((prev) => [...prev, { from: 'ai', text: reply }]);
    } catch (err) {
      setAssistantMessages((prev) => [
        ...prev,
        { from: 'ai', text: 'Sorry, I ran into an issue reaching the AI service. Please try again.' },
      ]);
    } finally {
      setAssistantLoading(false);
    }
  };

  return (
    <div className="app-shell">
      <aside className={`app-sidebar glass ${mobileNavOpen ? 'open' : ''}`}>
        <div className="app-sidebar-brand">
          <div className="brand-mark">
            <Brain size={20} />
          </div>
          <span className="brand-text">TalentIQ AI</span>
          <button className="mobile-close-btn" onClick={() => setMobileNavOpen(false)}>
            <X size={20} />
          </button>
        </div>
        <nav className="app-sidebar-nav">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
              onClick={() => setMobileNavOpen(false)}
            >
              <item.icon size={18} />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
        <div className="app-sidebar-footer">
          <button className="btn btn-ghost btn-block" onClick={logout}>
            <LogOut size={16} />
            Sign out
          </button>
        </div>
      </aside>

      {mobileNavOpen && <div className="sidebar-overlay" onClick={() => setMobileNavOpen(false)} />}

      <div className="app-main">
        <header className="app-topbar glass">
          <button className="mobile-menu-btn" onClick={() => setMobileNavOpen(true)}>
            <Menu size={22} />
          </button>
          <form className="topbar-search" onSubmit={handleSearchSubmit}>
            <Search size={17} />
            <input
              type="text"
              placeholder="Search jobs, skills, candidates..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </form>
          <div className="topbar-actions">
            <button className="btn btn-icon btn-ghost" onClick={toggleTheme} aria-label="Toggle theme">
              {theme === 'light' ? <Moon size={19} /> : <Sun size={19} />}
            </button>
            <div className="user-menu" ref={menuRef}>
              <button className="user-menu-trigger" onClick={() => setUserMenuOpen((v) => !v)}>
                <span className="avatar-circle">{initials}</span>
                <span className="user-menu-name">{user?.name}</span>
              </button>
              {userMenuOpen && (
                <div className="user-menu-dropdown glass fade-in">
                  <div className="user-menu-info">
                    <div className="user-menu-info-name">{user?.name}</div>
                    <div className="user-menu-info-email">{user?.email}</div>
                    <span className="badge badge-primary">{user?.role}</span>
                  </div>
                  <Link to="/profile" className="user-menu-item" onClick={() => setUserMenuOpen(false)}>
                    <User size={16} /> Profile
                  </Link>
                  <button className="user-menu-item danger" onClick={logout}>
                    <LogOut size={16} /> Sign out
                  </button>
                </div>
              )}
            </div>
          </div>
        </header>

        <main className="app-content">{children}</main>
      </div>

      <button
        className="ai-fab pulse-glow"
        onClick={() => setAssistantOpen((v) => !v)}
        aria-label="AI Assistant"
      >
        {assistantOpen ? <X size={22} /> : <Sparkles size={22} />}
      </button>

      {assistantOpen && (
        <div className="ai-popover glass fade-in-up">
          <div className="ai-popover-header">
            <div className="flex items-center gap-1">
              <Sparkles size={18} className="text-gradient" />
              <strong>TalentIQ Assistant</strong>
            </div>
            <button className="btn btn-icon btn-ghost" onClick={() => setAssistantOpen(false)}>
              <X size={16} />
            </button>
          </div>
          <div className="ai-popover-body">
            {assistantMessages.map((m, i) => (
              <div key={i} className={`ai-msg ${m.from}`}>
                {m.text}
              </div>
            ))}
            {assistantLoading && (
              <div className="ai-msg ai">
                <span className="ai-typing-dot" />
                <span className="ai-typing-dot" />
                <span className="ai-typing-dot" />
              </div>
            )}
          </div>
          <div className="ai-popover-input">
            <input
              type="text"
              placeholder="Ask something..."
              value={assistantInput}
              onChange={(e) => setAssistantInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && sendAssistantMessage()}
              disabled={assistantLoading}
            />
            <button className="btn btn-primary btn-icon" onClick={sendAssistantMessage} disabled={assistantLoading}>
              <Send size={16} />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
