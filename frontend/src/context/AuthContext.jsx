import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { supabase } from '../api/supabaseClient';
import { getMe } from '../api/auth';
import { setUnauthorizedHandler } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const loadAppUser = useCallback(async () => {
    try {
      const appUser = await getMe();
      setUser(appUser);
      return appUser;
    } catch (err) {
      setUser(null);
      return null;
    }
  }, []);

  useEffect(() => {
    let mounted = true;

    supabase.auth.getSession().then(async ({ data }) => {
      if (data?.session) {
        await loadAppUser();
      }
      if (mounted) setLoading(false);
    });

    const { data: subscription } = supabase.auth.onAuthStateChange((event) => {
      if (event === 'SIGNED_OUT') {
        setUser(null);
      }
    });

    return () => {
      mounted = false;
      subscription?.subscription?.unsubscribe();
    };
  }, [loadAppUser]);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      supabase.auth.signOut();
      setUser(null);
      navigate('/login');
    });
  }, [navigate]);

  const login = useCallback(async (email, password) => {
    const { error } = await supabase.auth.signInWithPassword({ email, password });
    if (error) {
      throw { friendlyMessage: error.message };
    }
    const appUser = await loadAppUser();
    if (!appUser) {
      throw { friendlyMessage: 'Signed in, but could not load your account. Please try again.' };
    }
    return appUser;
  }, [loadAppUser]);

  const logout = useCallback(async () => {
    await supabase.auth.signOut();
    setUser(null);
    navigate('/login');
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
