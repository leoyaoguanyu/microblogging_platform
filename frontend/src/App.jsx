import { BrowserRouter, Link, Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './AuthContext';
import LoginPage from './pages/LoginPage';
import FeedPage from './pages/FeedPage';
import ExplorePage from './pages/ExplorePage';
import ProfilePage from './pages/ProfilePage';

function Nav() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  return (
    <header className="nav">
      <Link to="/" className="brand">Microblog</Link>
      <nav>
        <Link to="/">Home</Link>
        <Link to="/explore">Explore</Link>
        {user ? (
          <>
            <Link to={`/u/${user.username}`}>@{user.username}</Link>
            <button className="link" onClick={() => { logout(); navigate('/login'); }}>Log out</button>
          </>
        ) : (
          <Link to="/login">Log in</Link>
        )}
      </nav>
    </header>
  );
}

function RequireAuth({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <p className="muted">Loading…</p>;
  return user ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Nav />
        <main className="container">
          <Routes>
            <Route path="/" element={<RequireAuth><FeedPage /></RequireAuth>} />
            <Route path="/explore" element={<ExplorePage />} />
            <Route path="/u/:username" element={<ProfilePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </BrowserRouter>
    </AuthProvider>
  );
}
