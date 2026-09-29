import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../AuthContext';
import PostList from '../components/PostList';

export default function ExplorePage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');
  const fetchPage = useCallback((page) => api.posts(page), []);

  useEffect(() => { api.users().then(setUsers).catch((e) => setError(e.message)); }, [user]);

  const toggleFollow = async (u) => {
    if (!user) return navigate('/login');
    try {
      const updated = u.followedByMe ? await api.unfollow(u.username) : await api.follow(u.username);
      setUsers((prev) => prev.map((x) => (x.id === updated.id ? updated : x)));
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <div className="two-col">
      <section>
        <h2>Explore</h2>
        <PostList fetchPage={fetchPage} emptyText="No posts yet." />
      </section>
      <aside>
        <div className="card user-list">
          <h3>People</h3>
          {error && <div className="error">{error}</div>}
          {users.map((u) => (
            <div key={u.id} className="user-item">
              <div>
                <Link to={`/u/${u.username}`}>{u.displayName}</Link>
                <div className="muted">@{u.username} · {u.followerCount} followers</div>
              </div>
              {(!user || user.id !== u.id) && (
                <button className={`small ${u.followedByMe ? '' : 'primary'}`} onClick={() => toggleFollow(u)}>
                  {u.followedByMe ? 'Unfollow' : 'Follow'}
                </button>
              )}
            </div>
          ))}
        </div>
      </aside>
    </div>
  );
}
