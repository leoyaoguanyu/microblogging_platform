import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../AuthContext';
import PostList from '../components/PostList';

export default function ProfilePage() {
  const { username } = useParams();
  const { user: me } = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    api.user(username).then(setProfile).catch((e) => setError(e.message));
  }, [username]);

  useEffect(() => { setProfile(null); setError(''); load(); }, [load, me]);

  const fetchPage = useCallback((page) => api.userPosts(username, page), [username]);

  const toggleFollow = async () => {
    if (!me) return navigate('/login');
    try {
      setProfile(profile.followedByMe ? await api.unfollow(username) : await api.follow(username));
    } catch (e) {
      setError(e.message);
    }
  };

  if (error) return <div className="error">{error}</div>;
  if (!profile) return <p className="muted">Loading…</p>;

  const isMe = me && me.id === profile.id;

  return (
    <>
      <div className="card">
        <div className="row between">
          <div>
            <h2 style={{ marginBottom: 0 }}>{profile.displayName}</h2>
            <div className="muted">@{profile.username}</div>
          </div>
          {!isMe && (
            <button className={profile.followedByMe ? '' : 'primary'} onClick={toggleFollow}>
              {profile.followedByMe ? 'Unfollow' : 'Follow'}
            </button>
          )}
        </div>
        {profile.bio && <p>{profile.bio}</p>}
        <div className="stats muted">
          <span><b>{profile.postCount}</b>posts</span>
          <span><b>{profile.followerCount}</b>followers</span>
          <span><b>{profile.followingCount}</b>following</span>
        </div>
      </div>
      <PostList fetchPage={fetchPage} emptyText="No posts yet." onPostsChanged={load} />
    </>
  );
}
