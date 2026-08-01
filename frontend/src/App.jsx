import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AppShell from './components/AppShell';

import { PageLoader } from './components/Spinner';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import UploadResume from './pages/UploadResume';
import Analysis from './pages/Analysis';
import Matching from './pages/Matching';
import Interview from './pages/Interview';
import SkillGap from './pages/SkillGap';
import Careers from './pages/Careers';
import Profile from './pages/Profile';
import NotFound from './pages/NotFound';

function Shell({ children }) {
  return (
    <ProtectedRoute>
      <AppShell>{children}</AppShell>
    </ProtectedRoute>
  );
}

function RootRedirect() {
  const { user, loading } = useAuth();
  if (loading) {
    return <PageLoader />;
  }
  return <Navigate to={user ? '/dashboard' : '/login'} replace />;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<RootRedirect />} />
      <Route path="/login" element={<Login />} />

      <Route path="/dashboard" element={<Shell><Dashboard /></Shell>} />
      <Route path="/upload" element={<Shell><UploadResume /></Shell>} />
      <Route path="/analysis/:resumeId" element={<Shell><Analysis /></Shell>} />
      <Route path="/interview" element={<Shell><Interview /></Shell>} />
      <Route path="/skill-gap" element={<Shell><SkillGap /></Shell>} />
      <Route path="/careers" element={<Shell><Careers /></Shell>} />

      <Route path="/matching" element={<Shell><Matching /></Shell>} />
      <Route path="/profile" element={<Shell><Profile /></Shell>} />

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}
