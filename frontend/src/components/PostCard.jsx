import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../AuthContext';

const fmt = (iso) => new Date(iso).toLocaleString();

export default function PostCard({ post, onChange, onDelete }) {
  const { user } = useAuth();
  const navigate = useNavigate();
  const isAuthor = user && user.id === post.authorId;

  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(post.content);
  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState(null);
  const [commentDraft, setCommentDraft] = useState('');
  const [error, setError] = useState('');

  const requireLogin = () => {
    if (user) return true;
    navigate('/login');
    return false;
  };

  const run = async (fn) => {
    setError('');
    try { await fn(); } catch (e) { setError(e.message); }
  };

  const toggleLike = () => requireLogin() && run(async () => {
    const updated = post.likedByMe ? await api.unlike(post.id) : await api.like(post.id);
    onChange?.(updated);
  });

  const saveEdit = () => run(async () => {
    const updated = await api.updatePost(post.id, draft);
    onChange?.(updated);
    setEditing(false);
  });

  const remove = () => {
    if (!window.confirm('Delete this post?')) return;
    run(async () => {
      await api.deletePost(post.id);
      onDelete?.(post.id);
    });
  };

  const loadComments = () => run(async () => setComments(await api.comments(post.id)));

  const toggleComments = () => {
    const next = !showComments;
    setShowComments(next);
    if (next && comments === null) loadComments();
  };

  const addComment = (e) => {
    e.preventDefault();
    if (!commentDraft.trim() || !requireLogin()) return;
    run(async () => {
      const c = await api.addComment(post.id, commentDraft);
      setComments((prev) => [...(prev ?? []), c]);
      setCommentDraft('');
      onChange?.({ ...post, commentCount: post.commentCount + 1 });
    });
  };

  const removeComment = (id) => run(async () => {
    await api.deleteComment(post.id, id);
    setComments((prev) => prev.filter((c) => c.id !== id));
    onChange?.({ ...post, commentCount: Math.max(0, post.commentCount - 1) });
  });

  return (
    <article className="card">
      <div className="post-header">
        <div>
          <Link className="post-author" to={`/u/${post.authorUsername}`}>{post.authorDisplayName}</Link>
          <span className="muted"> @{post.authorUsername}</span>
        </div>
        <span className="muted">
          {fmt(post.createdAt)}{post.updatedAt !== post.createdAt ? ' (edited)' : ''}
        </span>
      </div>

      {editing ? (
        <>
          <textarea value={draft} maxLength={280} onChange={(e) => setDraft(e.target.value)} />
          <div className="actions">
            <button className="primary small" onClick={saveEdit} disabled={!draft.trim()}>Save</button>
            <button className="small" onClick={() => { setEditing(false); setDraft(post.content); }}>Cancel</button>
          </div>
        </>
      ) : (
        <p className="post-content">{post.content}</p>
      )}

      {error && <div className="error">{error}</div>}

      <div className="actions">
        <button className={`small ${post.likedByMe ? 'liked' : ''}`} onClick={toggleLike}>
          {post.likedByMe ? '♥' : '♡'} {post.likeCount}
        </button>
        <button className="small" onClick={toggleComments}>💬 {post.commentCount}</button>
        {isAuthor && !editing && (
          <>
            <button className="small" onClick={() => setEditing(true)}>Edit</button>
            <button className="small danger" onClick={remove}>Delete</button>
          </>
        )}
      </div>

      {showComments && (
        <div className="comments">
          {comments === null && <p className="muted">Loading…</p>}
          {comments?.length === 0 && <p className="muted">No comments yet.</p>}
          {comments?.map((c) => (
            <div key={c.id} className="comment row between">
              <span>
                <Link className="who" to={`/u/${c.authorUsername}`}>{c.authorDisplayName}</Link>{' '}
                <span className="muted">@{c.authorUsername}</span>: {c.content}
              </span>
              {user && (user.id === c.authorId || isAuthor) && (
                <button className="link danger small" onClick={() => removeComment(c.id)}>✕</button>
              )}
            </div>
          ))}
          <form className="row" onSubmit={addComment} style={{ marginTop: 8 }}>
            <input
              placeholder={user ? 'Write a comment…' : 'Log in to comment'}
              value={commentDraft}
              maxLength={280}
              onChange={(e) => setCommentDraft(e.target.value)}
            />
            <button className="small primary" disabled={!commentDraft.trim()}>Reply</button>
          </form>
        </div>
      )}
    </article>
  );
}
