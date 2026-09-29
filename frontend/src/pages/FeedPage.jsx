import { useCallback, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import Composer from '../components/Composer';
import PostList from '../components/PostList';

export default function FeedPage() {
  const [reloadKey, setReloadKey] = useState(0);
  const fetchPage = useCallback((page) => api.feed(page), []);

  return (
    <>
      <h2>Home</h2>
      <Composer onCreated={() => setReloadKey((k) => k + 1)} />
      <PostList
        fetchPage={fetchPage}
        reloadKey={reloadKey}
        emptyText="Your feed is empty. Follow people on the Explore page."
      />
      <p className="muted center">
        Showing posts from people you follow. <Link to="/explore">Find more people →</Link>
      </p>
    </>
  );
}
