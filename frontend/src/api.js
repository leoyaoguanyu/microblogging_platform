const TOKEN_KEY = 'microblog_token';

export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (token) =>
  token ? localStorage.setItem(TOKEN_KEY, token) : localStorage.removeItem(TOKEN_KEY);

async function request(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`/api${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    const err = new Error(data?.message || `Request failed (${res.status})`);
    err.status = res.status;
    throw err;
  }
  return data;
}

export const api = {
  // auth
  register: (body) => request('/auth/register', { method: 'POST', body }),
  login: (body) => request('/auth/login', { method: 'POST', body }),
  me: () => request('/me'),

  // posts & feed
  feed: (page = 0, size = 10) => request(`/feed?page=${page}&size=${size}`),
  posts: (page = 0, size = 10) => request(`/posts?page=${page}&size=${size}`),
  userPosts: (username, page = 0, size = 10) =>
    request(`/users/${encodeURIComponent(username)}/posts?page=${page}&size=${size}`),
  createPost: (content) => request('/posts', { method: 'POST', body: { content } }),
  updatePost: (id, content) => request(`/posts/${id}`, { method: 'PUT', body: { content } }),
  deletePost: (id) => request(`/posts/${id}`, { method: 'DELETE' }),

  // likes
  like: (id) => request(`/posts/${id}/like`, { method: 'POST' }),
  unlike: (id) => request(`/posts/${id}/like`, { method: 'DELETE' }),

  // comments
  comments: (id) => request(`/posts/${id}/comments`),
  addComment: (id, content) => request(`/posts/${id}/comments`, { method: 'POST', body: { content } }),
  deleteComment: (postId, commentId) =>
    request(`/posts/${postId}/comments/${commentId}`, { method: 'DELETE' }),

  // users & follows
  users: () => request('/users'),
  user: (username) => request(`/users/${encodeURIComponent(username)}`),
  follow: (username) => request(`/users/${encodeURIComponent(username)}/follow`, { method: 'POST' }),
  unfollow: (username) => request(`/users/${encodeURIComponent(username)}/follow`, { method: 'DELETE' }),
};
