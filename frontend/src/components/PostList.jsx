import { useCallback, useEffect, useState } from 'react';
import PostCard from './PostCard';

/**
 * Generic paginated post list. `fetchPage(page)` must return a PageResponse
 * ({ items, page, hasNext, ... }) and should be a stable function (useCallback).
 */
export default function PostList({ fetchPage, reloadKey = 0, emptyText = 'No posts yet.', onPostsChanged }) {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async (p, append) => {
    setLoading(true);
    setError('');
    try {
      const res = await fetchPage(p);
      setItems((prev) => (append ? [...prev, ...res.items] : res.items));
      setPage(res.page);
      setHasNext(res.hasNext);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [fetchPage]);

  useEffect(() => { load(0, false); }, [load, reloadKey]);

  const updatePost = (updated) => setItems((prev) => prev.map((p) => (p.id === updated.id ? updated : p)));
  const removePost = (id) => {
    setItems((prev) => prev.filter((p) => p.id !== id));
    onPostsChanged?.();
  };

  return (
    <div>
      {error && <div className="error">{error}</div>}
      {items.map((p) => (
        <PostCard key={p.id} post={p} onChange={updatePost} onDelete={removePost} />
      ))}
      {!loading && items.length === 0 && !error && <p className="muted center">{emptyText}</p>}
      {loading && <p className="muted center">Loading…</p>}
      {hasNext && !loading && (
        <div className="center">
          <button onClick={() => load(page + 1, true)}>Load more</button>
        </div>
      )}
    </div>
  );
}
