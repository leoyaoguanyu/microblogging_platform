# Microblogging Platform

A small full-stack microblogging app (think "mini Twitter"):

- **Backend:** Java 21, Spring Boot 3.5, Spring Security (JWT), Spring Data JPA / Hibernate, PostgreSQL
- **Frontend:** React 18 + Vite + React Router

Features: register / log in, user profiles, posts (create / edit / delete), comments, likes,
follow / unfollow, and a **paginated home feed** of posts from the people you follow.

---

## Quick start

### 1. Database

**Option A – local PostgreSQL** (Homebrew, etc.)

```bash
psql -d postgres -c "CREATE ROLE microblog LOGIN PASSWORD 'microblog';"
psql -d postgres -c "CREATE DATABASE microblog OWNER microblog;"
```

**Option B – Docker**

```bash
docker compose up -d
```

**Option C – no database at all** (in-memory H2, good for a quick demo): skip this step and
start the backend with the `h2` profile (see below).

### 2. Backend (port 8080)

```bash
cd backend
mvn spring-boot:run                                   # PostgreSQL (localhost:5432/microblog)
# or
mvn spring-boot:run -Dspring-boot.run.profiles=h2     # in-memory H2, zero setup
```

Tables are created automatically by Hibernate (`ddl-auto: update`). The designed schema is
documented in [`backend/src/main/resources/db/schema.sql`](backend/src/main/resources/db/schema.sql).

On first start the app seeds demo data (4 users, posts, follows, likes, comments).

### 3. Frontend (port 5173)

```bash
cd frontend
npm install
npm run dev
```

Open <http://localhost:5173>. The Vite dev server proxies `/api/*` to the backend.

**Demo accounts:** `alice`, `bob`, `carol`, `dave` — password `password123`.

Suggested demo flow: log in as `alice` → Home shows the feed from bob & carol → post something →
Explore → follow `dave` → feed now includes dave → open a post, like it, add a comment →
log in as `bob` in another browser and try to edit alice's post (blocked with 403).

---

## REST API

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | – | Create account, returns JWT |
| POST | `/api/auth/login` | – | Log in, returns JWT |
| GET  | `/api/me` | JWT | Current user |
| GET  | `/api/feed?page=0&size=10` | JWT | **Paginated feed** of posts from followed users (+ own) |
| GET  | `/api/posts?page=0&size=10` | – | All posts, newest first |
| POST | `/api/posts` | JWT | Create post (≤ 280 chars) |
| GET  | `/api/posts/{id}` | – | Get post |
| PUT  | `/api/posts/{id}` | JWT, **author only** | Edit post |
| DELETE | `/api/posts/{id}` | JWT, **author only** | Delete post |
| POST / DELETE | `/api/posts/{id}/like` | JWT | Like / unlike |
| GET  | `/api/posts/{id}/comments` | – | List comments |
| POST | `/api/posts/{id}/comments` | JWT | Add comment |
| DELETE | `/api/posts/{id}/comments/{cid}` | JWT, comment author **or** post author | Delete comment |
| GET  | `/api/users` | – | List users |
| GET  | `/api/users/{username}` | – | Profile with follower / following / post counts |
| GET  | `/api/users/{username}/posts?page=0&size=10` | – | A user's posts (paginated) |
| POST / DELETE | `/api/users/{username}/follow` | JWT | Follow / unfollow |

Send the token as `Authorization: Bearer <jwt>`. Paginated responses look like:

```json
{ "items": [...], "page": 0, "size": 10, "totalElements": 42, "totalPages": 5, "hasNext": true }
```

Quick check with curl:

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"password123"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])')
curl -s "localhost:8080/api/feed?page=0&size=5" -H "Authorization: Bearer $TOKEN"
```

---

## How it's built

### Authentication & authorization (Spring Security)

- `config/SecurityConfig.java` – stateless JWT setup: CSRF off, no sessions, CORS for the React
  dev server. `/api/auth/**` and read-only `GET` endpoints are public; everything else requires a
  valid token (`401` otherwise).
- `security/JwtService.java` + `security/JwtAuthFilter.java` – issue HS256 tokens on login and
  turn a `Bearer` header into an authenticated `SecurityContext` on each request.
- **Resource-level authorization:** `security/PostSecurity.java` is referenced from
  `@PreAuthorize` on the controller, e.g.

  ```java
  @PutMapping("/{id}")
  @PreAuthorize("@postSecurity.isAuthor(#id, principal)")
  public PostResponse update(...)
  ```

  so only a post's author can edit or delete it (`403` for anyone else), and a comment can be
  removed only by its author or the post's author.
- Passwords are stored as BCrypt hashes.

### Data model (PostgreSQL + JPA/Hibernate)

```
users ──< posts ──< comments >── users
  │         │
  │         └──< post_likes >── users        UNIQUE (post_id, user_id)
  └──< follows >── users                     UNIQUE (follower_id, followee_id)
```

Entities live in `model/`, repositories in `repository/`. Uniqueness of likes and follows is
enforced by DB constraints; indexes on `(author_id, created_at)` and `created_at` support the
feed and timeline queries.

### Feed query

`PostRepository.findFeed` (JPQL, paginated with `Pageable`):

```sql
SELECT p FROM Post p
WHERE p.author.id = :userId
   OR p.author.id IN (SELECT f.followee.id FROM Follow f WHERE f.follower.id = :userId)
ORDER BY p.createdAt DESC
```

`@EntityGraph(attributePaths = "author")` fetches authors in the same query to avoid N+1 selects.

### Project layout

```
microblog/
├── backend/                      Spring Boot API
│   └── src/main/java/com/leoyao/microblog/
│       ├── config/               SecurityConfig, DataSeeder
│       ├── security/             JWT service/filter, UserDetails, PostSecurity (@PreAuthorize rules)
│       ├── model/                User, Post, Comment, PostLike, Follow (JPA entities)
│       ├── repository/           Spring Data JPA repositories (incl. paginated feed query)
│       ├── service/              AuthService, UserService, PostService
│       ├── controller/           Auth, User, Post, Feed REST controllers
│       ├── dto/                  Request/response records + PageResponse
│       └── exception/            NotFound / Conflict + global JSON error handler
├── frontend/                     React + Vite
│   └── src/
│       ├── api.js                fetch wrapper (adds Bearer token)
│       ├── AuthContext.jsx       login state
│       ├── pages/                Login, Feed, Explore, Profile
│       └── components/           Composer, PostCard, PostList (load-more pagination)
└── docker-compose.yml            PostgreSQL
```
