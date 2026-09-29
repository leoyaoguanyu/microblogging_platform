package com.leoyao.microblog.service;

import com.leoyao.microblog.dto.PageResponse;
import com.leoyao.microblog.dto.PostDtos.CommentRequest;
import com.leoyao.microblog.dto.PostDtos.CommentResponse;
import com.leoyao.microblog.dto.PostDtos.PostRequest;
import com.leoyao.microblog.dto.PostDtos.PostResponse;
import com.leoyao.microblog.exception.NotFoundException;
import com.leoyao.microblog.model.Comment;
import com.leoyao.microblog.model.Post;
import com.leoyao.microblog.model.PostLike;
import com.leoyao.microblog.model.User;
import com.leoyao.microblog.repository.CommentRepository;
import com.leoyao.microblog.repository.PostLikeRepository;
import com.leoyao.microblog.repository.PostRepository;
import com.leoyao.microblog.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PostRepository posts;
    private final CommentRepository comments;
    private final PostLikeRepository likes;
    private final UserRepository users;

    public PostService(PostRepository posts, CommentRepository comments,
                       PostLikeRepository likes, UserRepository users) {
        this.posts = posts;
        this.comments = comments;
        this.likes = likes;
        this.users = users;
    }

    // ---------- reads ----------

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> listAll(int page, int size, Long viewerId) {
        return PageResponse.of(posts.findAllByOrderByCreatedAtDesc(pageable(page, size)), p -> toResponse(p, viewerId));
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> listByUser(String username, int page, int size, Long viewerId) {
        if (!users.existsByUsername(username)) throw new NotFoundException("User not found: " + username);
        return PageResponse.of(posts.findByAuthorUsernameOrderByCreatedAtDesc(username, pageable(page, size)),
                p -> toResponse(p, viewerId));
    }

    /** Home feed: posts from followed users (plus the user's own), newest first, paginated. */
    @Transactional(readOnly = true)
    public PageResponse<PostResponse> feed(Long meId, int page, int size) {
        return PageResponse.of(posts.findFeed(meId, pageable(page, size)), p -> toResponse(p, meId));
    }

    @Transactional(readOnly = true)
    public PostResponse get(Long id, Long viewerId) {
        return toResponse(findPost(id), viewerId);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(Long postId) {
        findPost(postId);
        return comments.findByPostIdOrderByCreatedAtAsc(postId).stream().map(PostService::toResponse).toList();
    }

    // ---------- writes ----------

    @Transactional
    public PostResponse create(Long meId, PostRequest req) {
        User author = users.findById(meId).orElseThrow(() -> new NotFoundException("User not found"));
        Post post = posts.save(new Post(author, req.content().trim()));
        return toResponse(post, meId);
    }

    /** Ownership is enforced by @PreAuthorize on the controller (see PostSecurity). */
    @Transactional
    public PostResponse update(Long id, PostRequest req, Long meId) {
        Post post = findPost(id);
        post.setContent(req.content().trim());
        return toResponse(post, meId);
    }

    /** Ownership is enforced by @PreAuthorize on the controller (see PostSecurity). */
    @Transactional
    public void delete(Long id) {
        Post post = findPost(id);
        likes.deleteByPostId(id);
        comments.deleteByPostId(id);
        posts.delete(post);
    }

    @Transactional
    public PostResponse like(Long postId, Long meId) {
        Post post = findPost(postId);
        if (!likes.existsByPostIdAndUserId(postId, meId)) {
            likes.save(new PostLike(post, users.getReferenceById(meId)));
        }
        return toResponse(post, meId);
    }

    @Transactional
    public PostResponse unlike(Long postId, Long meId) {
        Post post = findPost(postId);
        likes.findByPostIdAndUserId(postId, meId).ifPresent(likes::delete);
        return toResponse(post, meId);
    }

    @Transactional
    public CommentResponse addComment(Long postId, Long meId, CommentRequest req) {
        Post post = findPost(postId);
        User author = users.findById(meId).orElseThrow(() -> new NotFoundException("User not found"));
        return toResponse(comments.save(new Comment(post, author, req.content().trim())));
    }

    /** Permission is enforced by @PreAuthorize on the controller (see PostSecurity). */
    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        Comment comment = comments.findById(commentId)
                .filter(c -> c.getPost().getId().equals(postId))
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        comments.delete(comment);
    }

    // ---------- helpers ----------

    private Post findPost(Long id) {
        return posts.findWithAuthorById(id).orElseThrow(() -> new NotFoundException("Post not found"));
    }

    private static Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
    }

    private PostResponse toResponse(Post p, Long viewerId) {
        User a = p.getAuthor();
        return new PostResponse(
                p.getId(), p.getContent(),
                a.getId(), a.getUsername(), a.getDisplayName(),
                p.getCreatedAt(), p.getUpdatedAt(),
                likes.countByPostId(p.getId()),
                comments.countByPostId(p.getId()),
                viewerId != null && likes.existsByPostIdAndUserId(p.getId(), viewerId));
    }

    private static CommentResponse toResponse(Comment c) {
        User a = c.getAuthor();
        return new CommentResponse(c.getId(), c.getContent(), a.getId(), a.getUsername(), a.getDisplayName(), c.getCreatedAt());
    }
}
