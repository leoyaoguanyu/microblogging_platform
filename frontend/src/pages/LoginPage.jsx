import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../AuthContext';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ username: '', password: '', displayName: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      const auth = mode === 'login'
        ? await api.login({ username: form.username, password: form.password })
        : await api.register(form);
      login(auth);
      navigate('/');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="card" style={{ maxWidth: 420, margin: '40px auto' }}>
      <h2>{mode === 'login' ? 'Log in' : 'Create an account'}</h2>
      <form onSubmit={submit}>
        <input placeholder="Username" value={form.username} onChange={set('username')} autoFocus />
        {mode === 'register' && (
          <input placeholder="Display name" value={form.displayName} onChange={set('displayName')} />
        )}
        <input type="password" placeholder="Password" value={form.password} onChange={set('password')} />
        {error && <div className="error">{error}</div>}
        <button className="primary" disabled={busy} style={{ width: '100%' }}>
          {mode === 'login' ? 'Log in' : 'Sign up'}
        </button>
      </form>
      <p className="muted" style={{ marginTop: 12 }}>
        {mode === 'login' ? (
          <>No account? <button className="link" onClick={() => setMode('register')}>Sign up</button></>
        ) : (
          <>Have an account? <button className="link" onClick={() => setMode('login')}>Log in</button></>
        )}
      </p>
      <p className="muted">Demo accounts: alice / bob / carol / dave — password <code>password123</code></p>
    </div>
  );
}
