import { useState } from 'react';
import { api } from '../api';

const MAX = 280;

export default function Composer({ onCreated }) {
  const [content, setContent] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    if (!content.trim()) return;
    setBusy(true);
    setError('');
    try {
      const post = await api.createPost(content);
      setContent('');
      onCreated?.(post);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <form className="card" onSubmit={submit}>
      <textarea
        placeholder="What's happening?"
        value={content}
        maxLength={MAX}
        onChange={(e) => setContent(e.target.value)}
      />
      {error && <div className="error">{error}</div>}
      <div className="row between">
        <span className="muted">{content.length}/{MAX}</span>
        <button className="primary" disabled={busy || !content.trim()}>Post</button>
      </div>
    </form>
  );
}
