import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as authApi from '../api/auth';
import { setUnauthorizedHandler } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('talentiq_user');
    return stored ? JSON.parse(stored) : null;
  });
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const persist = (userObj, token) => {
    localStorage.setItem('talentiq_user', JSON.stringify(userObj));
    if (token) localStorage.setItem('talentiq_token', token);
    setUser(userObj);
  };

  const login = useCallback(async (email, password) => {
    setLoading(true);
    try {
      const data = await authApi.login({ email, password });
      const userObj = { id: data.id, name: data.name, email: data.email, role: data.role };
      persist(userObj, data.token);
      return userObj;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('talentiq_user');
    localStorage.removeItem('talentiq_token');
    setUser(null);
    navigate('/login');
  }, [navigate]);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      setUser(null);
      navigate('/login');
    });
  }, [navigate]);

  const homeRoute = () => '/dashboard';

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, homeRoute }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
